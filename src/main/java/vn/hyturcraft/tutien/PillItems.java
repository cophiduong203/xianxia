package vn.hyturcraft.tutien;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.ItemGroup;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

/** Pills are real registry items; entries are added without replacing original mod loot. */
public final class PillItems {
    private PillItems() {}
    public static final Item GATHERING = pill("tu_linh_dan", 0);
    public static final Item RESTORE   = pill("hoi_nguyen_dan", 1);
    public static final Item GUARD     = pill("ho_the_dan", 2);
    public static final Item HEAVEN    = pill("thien_linh_dan", 3);
    private static Item pill(String name, int type) {
        return Registry.register(Registries.ITEM, new Identifier(TuTienMod.MOD_ID, name),
            new Pill(new Item.Settings().maxCount(16), type));
    }
    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(entries -> {
            entries.add(GATHERING); entries.add(RESTORE); entries.add(GUARD); entries.add(HEAVEN);
        });
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, builder, source) -> {
            String path = id.getPath();
            // Includes vanilla and modded structure chests whose paths conventionally begin with chests/.
            boolean chest = path.startsWith("chests/") || path.contains("/chests/");
            // Many mods use the normal entities/ namespace for hostile mob loot. Use a conservative chance.
            boolean entityLoot = path.startsWith("entities/") || path.contains("/entities/");
            String n = path.substring(path.lastIndexOf('/') + 1);
            boolean mob = entityLoot && (
                n.contains("zombie") || n.contains("skeleton") || n.contains("creeper")
                || n.contains("spider") || n.contains("enderman") || n.contains("witch")
                || n.contains("slime") || n.contains("guardian") || n.contains("blaze")
                || n.contains("piglin") || n.contains("hoglin") || n.contains("ghast")
                || n.contains("wither") || n.contains("shulker") || n.contains("phantom")
                || n.contains("vindicator") || n.contains("evoker") || n.contains("pillager")
                || n.contains("drowned") || n.contains("stray") || n.contains("husk")
                || n.contains("magma_cube") || n.contains("warden") || n.contains("boss")
                || n.contains("monster") || n.contains("goblin") || n.contains("knight")
                || n.contains("raider") || n.contains("undead") || n.contains("lich"));
            if (!chest && !mob) return;
            // Avoid passive-animal loot spam: optionally include only chest tables by default.
            float chance = (float) (chest ? TuTienMod.CONFIG.chestPillChance :
                TuTienMod.CONFIG.hostileMobPillChance);
            if (chance <= 0) return;
            LootPool.Builder pillPool = LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1))
                .conditionally(RandomChanceLootCondition.builder(chance))
                .with(ItemEntry.builder(GATHERING).weight(55))
                .with(ItemEntry.builder(RESTORE).weight(25))
                .with(ItemEntry.builder(GUARD).weight(16))
                .with(ItemEntry.builder(HEAVEN).weight(4));
            builder.pool(pillPool);
        });
    }

    private static final class Pill extends Item {
        private final int type;
        private Pill(Settings settings, int type) { super(settings); this.type = type; }
        @Override public int getMaxUseTime(ItemStack stack) { return 20; }
        @Override public UseAction getUseAction(ItemStack stack) { return UseAction.EAT; }
        @Override public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
            return ItemUsage.consumeHeldItem(world, user, hand);
        }
        @Override public ItemStack finishUsing(ItemStack stack, World world, LivingEntity entity) {
            if (!world.isClient && entity instanceof ServerPlayerEntity p) {
                switch (type) {
                    case 0 -> { CultivationManager.addXp(p, 800); info(p, "+800 linh khí"); }
                    case 1 -> {
                        p.heal(12.0f);
                        p.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200, 1));
                        info(p, "Hồi Nguyên Đan: hồi phục sinh lực");
                    }
                    case 2 -> {
                        p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 1200, 0));
                        info(p, "Hộ Thể Đan: kháng sát thương 60 giây");
                    }
                    case 3 -> { CultivationManager.addXp(p, 5000); info(p, "+5000 linh khí"); }
                }
                world.playSound(null, p.getBlockPos(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
                    SoundCategory.PLAYERS, 0.8f, 1.3f);
            }
            if (!(entity instanceof PlayerEntity p) || !p.getAbilities().creativeMode) stack.decrement(1);
            return stack;
        }
        private void info(ServerPlayerEntity p, String msg) {
            p.sendMessage(Text.literal("✦ " + msg), true);
        }
    }
}
