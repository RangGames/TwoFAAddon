package rang.games.twoFAAddon;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TwoFAManager {

    private final JavaPlugin plugin;
    private final File dataFile;
    private FileConfiguration dataConfig;

    private final SecretGenerator secretGenerator;
    private final QrGenerator qrGenerator;
    private final TimeProvider timeProvider;
    private final CodeGenerator codeGenerator;
    private final CodeVerifier codeVerifier;

    private final Map<String, String> secretCache = new HashMap<>();
    private final Map<String, String> qrImageCache = new HashMap<>();

    public TwoFAManager(JavaPlugin plugin) {
        this.plugin = plugin;

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        this.dataFile = new File(plugin.getDataFolder(), "2fa_data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("2FA 데이터 파일을 생성할 수 없습니다: " + e.getMessage());
            }
        }

        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        loadAllQRDatas();
        this.secretGenerator = new DefaultSecretGenerator();
        this.qrGenerator = new ZxingPngQrGenerator();
        this.timeProvider = new SystemTimeProvider();
        this.codeGenerator = new DefaultCodeGenerator();
        this.codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
    }

    public void loadAllQRDatas() {
        if (!dataConfig.contains("keys")) return;
        Set<String> identifiers = dataConfig.getConfigurationSection("keys").getKeys(false);
        for (String identifier : identifiers) {
            String secret = dataConfig.getString("keys."+ identifier+".secret");
            if (secret != null) {
                secretCache.put(identifier, secret);
                QrData data = new QrData.Builder()
                        .label(identifier)
                        .secret(secret)
                        .issuer("RangGames")
                        .algorithm(HashingAlgorithm.SHA1)
                        .digits(6)
                        .period(30)
                        .build();
                try {
                    byte[] qrCodeBytes = qrGenerator.generate(data);
                    String qrCodeImage = Base64.getEncoder().encodeToString(qrCodeBytes);
                    qrImageCache.put(identifier, qrCodeImage);
                } catch (QrGenerationException e) {

                }
            }
        }
    }
    public TwoFactorKeyData createKey(String identifier) {
        if (secretCache.containsKey(identifier)) return null;
        String secret = secretGenerator.generate();

        dataConfig.set("keys." + identifier + ".secret", secret);
        saveData();

        secretCache.put(identifier, secret);

        QrData data = new QrData.Builder()
                .label(identifier)
                .secret(secret)
                .issuer("RangGames")
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        String qrCodeImage = null;
        try {
            byte[] qrCodeBytes = qrGenerator.generate(data);
            qrCodeImage = Base64.getEncoder().encodeToString(qrCodeBytes);

            qrImageCache.put(identifier, qrCodeImage);
        } catch (QrGenerationException e) {
        }

        String totpUri = data.getUri();
        return new TwoFactorKeyData(identifier, secret, totpUri, qrCodeImage);
    }
    public String getTotpUri(String identifier) {
        String secret = getSecret(identifier);
        if (secret == null) return null;
        QrData data = new QrData.Builder()
                .label(identifier)
                .secret(secret)
                .issuer("RangGames")
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();
        return data.getUri();
    }

    public boolean isValidKey(String identifier, String code) {
        String secret = getSecret(identifier);
        if (secret == null) {
            return false;
        }
        return codeVerifier.isValidCode(secret, code);
    }

    public String getCurrentCode(String identifier) {
        String secret = getSecret(identifier);
        if (secret == null) {
            return null;
        }

        long timestamp = timeProvider.getTime();
        try {
            return codeGenerator.generate(secret, timestamp);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean hasKey(String identifier) {
        return dataConfig.contains("keys." + identifier + ".secret");
    }

    public boolean removeKey(String identifier) {
        if (!dataConfig.contains("keys." + identifier)) {
            return false;
        }

        dataConfig.set("keys." + identifier, null);
        saveData();
        secretCache.remove(identifier);
        qrImageCache.remove(identifier);

        return true;
    }

    public String getSecret(String identifier) {
        String secret = secretCache.get(identifier);
        if (secret == null) {
            secret = dataConfig.getString("keys." + identifier + ".secret");
            if (secret != null) {
                secretCache.put(identifier, secret);
            }
        }

        return secret;
    }

    public ItemStack createQRCodeMap(String identifier) {
        String base64Image = qrImageCache.get(identifier);

        if (base64Image == null) {
            String secret = getSecret(identifier);
            if (secret == null) {
                TwoFactorKeyData keyData = createKey(identifier);
                base64Image = keyData.getQrCodeImageBase64();
            } else {
                QrData data = new QrData.Builder()
                        .label(identifier)
                        .secret(secret)
                        .issuer("MinecraftServer")
                        .algorithm(HashingAlgorithm.SHA1)
                        .digits(6)
                        .period(30)
                        .build();

                try {
                    byte[] qrCodeBytes = qrGenerator.generate(data);
                    base64Image = Base64.getEncoder().encodeToString(qrCodeBytes);
                    qrImageCache.put(identifier, base64Image);
                } catch (QrGenerationException e) {
                    plugin.getLogger().severe("QR 코드 재생성 오류: " + e.getMessage());
                    return null;
                }
            }
        }

        try {
            return createMapItemFromBase64(base64Image, identifier);
        } catch (Exception e) {
            plugin.getLogger().severe("지도 아이템 생성 오류: " + e.getMessage());
            return null;
        }
    }

    private ItemStack createMapItemFromBase64(String base64Image, String identifier) {
        try {
            byte[] imageData = Base64.getDecoder().decode(base64Image);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));

            BufferedImage resizedImage = resizeImage(image, 128, 128);
            MapView mapView = Bukkit.createMap(Bukkit.getWorlds().get(0));

            for (MapRenderer renderer : mapView.getRenderers()) {
                mapView.removeRenderer(renderer);
            }

            QRCodeMapRenderer renderer = new QRCodeMapRenderer(resizedImage);
            mapView.addRenderer(renderer);

            ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
            MapMeta mapMeta = (MapMeta) mapItem.getItemMeta();

            if (mapMeta != null) {
                mapMeta.setMapView(mapView);
                mapMeta.setDisplayName("§e2FA 코드: " + identifier);
                mapMeta.setLore(java.util.Arrays.asList(
                        "§7Google Authenticator로 스캔하세요",
                        "§7ID: " + identifier
                ));

                mapItem.setItemMeta(mapMeta);
            }

            return mapItem;

        } catch (IOException e) {
            plugin.getLogger().severe("지도 이미지 생성 중 오류 발생: " + e.getMessage());
            return new ItemStack(Material.MAP);
        }
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        return MapPalette.resizeImage(resizedImage);
    }

    private void saveData() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("2FA 데이터를 저장할 수 없습니다: " + e.getMessage());
        }
    }

    private static class QRCodeMapRenderer extends MapRenderer {
        private final BufferedImage image;
        private boolean rendered = false;

        public QRCodeMapRenderer(BufferedImage image) {
            super(true);
            this.image = image;
        }

        @Override
        public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
            if (image != null && !rendered) {
                mapCanvas.drawImage(0, 0, image);
                rendered = true;
            }
        }
    }

    public static class TwoFactorKeyData {
        private final String identifier;
        private final String secret;
        private final String qrCodeUrl;
        private final String qrCodeImageBase64;

        public TwoFactorKeyData(String identifier, String secret, String qrCodeUrl, String qrCodeImageBase64) {
            this.identifier = identifier;
            this.secret = secret;
            this.qrCodeUrl = qrCodeUrl;
            this.qrCodeImageBase64 = qrCodeImageBase64;
        }

        public String getIdentifier() {
            return identifier;
        }

        public String getSecret() {
            return secret;
        }

        public String getQrCodeUrl() {
            return qrCodeUrl;
        }

        public String getQrCodeImageBase64() {
            return qrCodeImageBase64;
        }
    }
}
