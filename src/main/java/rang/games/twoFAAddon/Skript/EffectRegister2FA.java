package rang.games.twoFAAddon.Skript;


import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.Event;
import rang.games.twoFAAddon.TwoFAAddon;
import rang.games.twoFAAddon.TwoFAManager;

public class EffectRegister2FA extends Effect {
    private Expression<String> idExpr;
    private Expression<String> labelExpr;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        idExpr = (Expression<String>) exprs[0];
        if (exprs.length > 1) {
            labelExpr = (Expression<String>) exprs[1];
        }
        return true;
    }

    @Override
    public String toString(Event e, boolean debug) {
        if (labelExpr != null) {
            return "register 2fa with id " + idExpr.toString(e, debug) + " and label " + labelExpr.toString(e, debug);
        } else {
            return "register 2fa with id " + idExpr.toString(e, debug);
        }
    }

    @Override
    protected void execute(Event e) {
        String id = idExpr.getSingle(e);
        if (id != null) {
            TwoFAManager manager = TwoFAAddon.getInstance().getTwoFAManager();;
            if (labelExpr != null) {
                String label = labelExpr.getSingle(e);
                if (label != null) {
                    manager.createKey(id, label);
                } else {
                    manager.createKey(id);
                }
            }
            manager.createKey(id);
            //ConsoleCommandSender console = Bukkit.getConsoleSender();
            //console.sendMessage("§a2FA 코드가 생성되었습니다. ID: " + id);
            //console.sendMessage("§e비밀 키: " + keyData.getSecret());
            //console.sendMessage("§bQR 코드 URL: " + keyData.getQrCodeUrl());
        }
    }
}