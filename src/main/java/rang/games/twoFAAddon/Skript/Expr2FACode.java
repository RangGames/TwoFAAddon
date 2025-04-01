package rang.games.twoFAAddon.Skript;


import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import rang.games.twoFAAddon.TwoFAAddon;
import rang.games.twoFAAddon.TwoFAManager;

public class Expr2FACode extends SimpleExpression<String> {
    private Expression<String> idExpr;

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        idExpr = (Expression<String>) exprs[0];
        return true;
    }

    @Override
    public String toString(Event e, boolean debug) {
        return "2fa code of " + idExpr.toString(e, debug);
    }

    @Override
    protected String[] get(Event e) {
        String id = idExpr.getSingle(e);
        if (id != null) {
            TwoFAManager manager = TwoFAAddon.getInstance().getTwoFAManager();
            String code = manager.getCurrentCode(id);
            if (code != null) {
                return new String[]{code};
            }
        }
        return new String[0];
    }
}
