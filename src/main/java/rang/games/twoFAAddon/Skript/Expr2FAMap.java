package rang.games.twoFAAddon.Skript;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import rang.games.twoFAAddon.TwoFAAddon;
import rang.games.twoFAAddon.TwoFAManager;

public class Expr2FAMap extends SimpleExpression<ItemStack> {
    private Expression<String> idExpr;
    private Expression<String> labelExpr;
    @Override
    public Class<? extends ItemStack> getReturnType() {
        return ItemStack.class;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

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
            return "2fa map of " + idExpr.toString(e, debug) + " with label " + labelExpr.toString(e, debug);
        } else {
            return "2fa map of " + idExpr.toString(e, debug);
        }
    }

    @Override
    protected ItemStack[] get(Event e) {
        String id = idExpr.getSingle(e);
        if (id != null) {
            TwoFAManager manager = TwoFAAddon.getInstance().getTwoFAManager();
            if (labelExpr != null) {
                String label =labelExpr.getSingle(e);
                if (label != null) {
                    ItemStack map = manager.createQRCodeMap(id, label);
                    if (map != null) {
                        return new ItemStack[] { map };
                    }
                }
            }
            ItemStack map = manager.createQRCodeMap(id);
            if (map != null) {
                return new ItemStack[]{map};
            }
        }
        return new ItemStack[0];
    }
}
