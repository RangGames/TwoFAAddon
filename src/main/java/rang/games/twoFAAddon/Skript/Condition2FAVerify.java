package rang.games.twoFAAddon.Skript;


import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import rang.games.twoFAAddon.TwoFAAddon;
import rang.games.twoFAAddon.TwoFAManager;

public class Condition2FAVerify extends Condition {

    private Expression<String> codeExpr;
    private Expression<String> idExpr;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        codeExpr = (Expression<String>) exprs[0];
        idExpr = (Expression<String>) exprs[1];
        setNegated(matchedPattern == 1);
        return true;
    }

    @Override
    public String toString(Event e, boolean debug) {
        return "2fa code " + codeExpr.toString(e, debug) + " is " + (isNegated() ? "not " : "") +
                "valid for id " + idExpr.toString(e, debug);
    }

    @Override
    public boolean check(Event e) {
        String code = codeExpr.getSingle(e);
        String id = idExpr.getSingle(e);

        if (code == null || id == null) {
            return false;
        }

        TwoFAManager manager = TwoFAAddon.getInstance().getTwoFAManager();
        boolean isValid = manager.isValidKey(id, code);
        return isNegated() ? !isValid : isValid;
    }
}