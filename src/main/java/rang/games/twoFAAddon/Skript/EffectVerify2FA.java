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

public class EffectVerify2FA extends Effect {
    private Expression<String> codeExpr;
    private Expression<String> idExpr;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        codeExpr = (Expression<String>) exprs[0];
        idExpr = (Expression<String>) exprs[1];
        return true;
    }

    @Override
    public String toString(Event e, boolean debug) {
        return "verify 2fa code " + codeExpr.toString(e, debug) + " for id " + idExpr.toString(e, debug);
    }

    @Override
    protected void execute(Event e) {
        String code = codeExpr.getSingle(e);
        String id = idExpr.getSingle(e);
        if (code != null && id != null) {
            TwoFAManager manager = TwoFAAddon.getInstance().getTwoFAManager();
            boolean isValid = manager.isValidKey(id, code);

            ConsoleCommandSender console = Bukkit.getConsoleSender();
            if (isValid) {
                console.sendMessage("§a2FA 코드가 유효합니다. ID: " + id);
            } else {
                console.sendMessage("§c2FA 코드가 유효하지 않습니다. ID: " + id);
            }
        }
    }
}