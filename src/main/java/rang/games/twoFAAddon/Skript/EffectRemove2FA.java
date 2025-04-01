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

public class EffectRemove2FA extends Effect {
    private Expression<String> idExpr;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        idExpr = (Expression<String>) exprs[0];
        return true;
    }

    @Override
    public String toString(Event e, boolean debug) {
        return "remove 2fa for id " + idExpr.toString(e, debug);
    }

    @Override
    protected void execute(Event e) {
        String id = idExpr.getSingle(e);
        if (id != null) {
            TwoFAManager manager = TwoFAAddon.getInstance().getTwoFAManager();
            boolean removed = manager.removeKey(id);

            ConsoleCommandSender console = Bukkit.getConsoleSender();
            if (removed) {
                console.sendMessage("§a2FA 코드가 삭제되었습니다. ID: " + id);
            } else {
                console.sendMessage("§c2FA 코드 삭제 실패. ID: " + id);
            }
        }
    }
}