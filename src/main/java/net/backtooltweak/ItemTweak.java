package net.backtooltweak;

/**
 * Config de um item.
 * item1 = item nas costas. item2 = item na cintura; se for null, a cintura usa a mesma pose do item1.
 */
public class ItemTweak {
    public Pose item1 = new Pose();
    public Pose item2 = null;

    public ItemTweak() {}

    public ItemTweak(Pose item1) {
        this.item1 = item1;
    }

    public Pose pose(boolean belt) {
        return (belt && item2 != null) ? item2 : item1;
    }
}
