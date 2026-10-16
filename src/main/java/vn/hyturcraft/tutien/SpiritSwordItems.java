package vn.hyturcraft.tutien;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/** Non-obtainable visual-only item models displayed by ITEM_DISPLAY entities. */
public final class SpiritSwordItems {
    private SpiritSwordItems() {}
    public static final Item[] SWORDS = new Item[10];
    public static Item RIFT_SWORD;

    public static void register() {
        for (int i = 0; i < SWORDS.length; i++) {
            SWORDS[i] = Registry.register(Registries.ITEM,
                new Identifier(TuTienMod.MOD_ID, "spirit_sword_" + i),
                new Item(new Item.Settings().maxCount(1)));
        }
        RIFT_SWORD = Registry.register(Registries.ITEM,
            new Identifier(TuTienMod.MOD_ID, "rift_sword"),
            new Item(new Item.Settings().maxCount(1)));
    }
}
