package rang.games.twoFAAddon;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Getter;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import rang.games.twoFAAddon.Skript.*;

public class TwoFAAddon extends JavaPlugin {

    private SkriptAddon addon;
    private static TwoFAAddon instance;
    private TwoFAManager twoFAManager;

    @Override
    public void onEnable() {
        instance = this;
        twoFAManager = new TwoFAManager(this);
        addon = Skript.registerAddon(this);

        try {
            Skript.registerEffect(EffectRegister2FA.class,
                    "register [new] 2fa [code] (with|for) [id] %string%",
                    "create [new] 2fa [code] (with|for) [id] %string%");

            Skript.registerExpression(Expr2FACode.class, String.class, ExpressionType.PROPERTY,
                    "2fa code (of|for) [id] %string%",
                    "%string%'s 2fa code");

            Skript.registerExpression(Expr2FAMap.class, ItemStack.class, ExpressionType.PROPERTY,
                    "2fa [qr] map (of|for) [id] %string%",
                    "%string%'s 2fa [qr] map");

            Skript.registerExpression(Expr2FASecret.class, String.class, ExpressionType.PROPERTY,
                    "2fa secret (of|for) [id] %string%",
                    "%string%'s 2fa secret");

            Skript.registerExpression(Expr2FAUri.class, String.class, ExpressionType.PROPERTY,
                    "2fa (uri|url) (of|for) [id] %string%",
                    "%string%'s 2fa (uri|url)");

            Skript.registerCondition(Condition2FAVerify.class,
                    "2fa code %string% is valid (with|for) [id] %string%",
                    "2fa code %string% (isn't|is not) valid (with|for) [id] %string%");

            Skript.registerEffect(EffectRemove2FA.class,
                    "remove 2fa [code] (of|for) [id] %string%",
                    "delete 2fa [code] (of|for) [id] %string%");

            getLogger().info("TwoFA Skript 애드온이 성공적으로 로드되었습니다!");

        } catch (Exception e) {
            getLogger().severe("TwoFA Skript 애드온 로드 중 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("TwoFA Skript 애드온이 비활성화되었습니다!");
    }

    public static TwoFAAddon getInstance() {
        return instance;
    }

    public TwoFAManager getTwoFAManager() {
        return twoFAManager;
    }

}
