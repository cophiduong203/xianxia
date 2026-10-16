package vn.hyturcraft.tutien;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.UUID;

public final class CultivationManager {
    private CultivationManager() {}
    private static final UUID HEALTH_UUID = UUID.fromString("4dbcae9b-9c0d-4dd3-8543-0cdf263f3d2e");
    private static final UUID DAMAGE_UUID = UUID.fromString("a0cf1fea-4719-4b53-9b13-c430969256c8");
    private static final UUID ARMOR_UUID = UUID.fromString("e5771b0f-d8b4-43c8-a439-7cc91cdb652d");
    private static final UUID SPEED_UUID = UUID.fromString("1bbd3e5b-40a2-45c4-9e9f-0ecfa83e689d");

    public static long needed(int stage) {
        return (long) Math.ceil(Realms.xpNeeded(stage) * TuTienMod.CONFIG.xpRequiredMultiplier);
    }
    /** Grant spiritual energy and immediately advance through all affordable layers. */
    public static void addXp(ServerPlayerEntity player, long rawXp) {
        if (rawXp <= 0 || player.isSpectator()) return;
        CultivationHolder data = (CultivationHolder) player;
        if (Realms.maxed(data.hytur$getStage())) return;
        long previous = data.hytur$getCultivationXp();
        data.hytur$setCultivationXp(previous > Long.MAX_VALUE - rawXp ? Long.MAX_VALUE : previous + rawXp);
        applyPendingBreakthroughs(player);
    }

    /** Also converts XP saved by older, seclusion-based 2.0 versions. */
    public static void applyPendingBreakthroughs(ServerPlayerEntity player) {
        if (player.isSpectator() || !player.isAlive()) return;
        CultivationHolder data = (CultivationHolder) player;
        // Fixed upper bound: max 100 layers. Prevents accidental infinite loops.
        for (int i = 0; i < 100 && !Realms.maxed(data.hytur$getStage()); i++) {
            int old = data.hytur$getStage();
            long cost = needed(old);
            if (data.hytur$getCultivationXp() < cost) break;
            data.hytur$setCultivationXp(data.hytur$getCultivationXp() - cost);
            data.hytur$setStage(old + 1);
            celebrate(player, old, old + 1);
        }
    }
    public static void setStage(ServerPlayerEntity player, int oneBasedStage) {
        CultivationHolder data = (CultivationHolder) player;
        int old = data.hytur$getStage();
        data.hytur$setStage(oneBasedStage - 1);
        data.hytur$setCultivationXp(0);
        refreshAttributes(player);
        if (data.hytur$getStage() > old) celebrate(player, old, data.hytur$getStage());
    }
    private static void celebrate(ServerPlayerEntity player, int oldStage, int newStage) {
        boolean newRealm = Realms.realm(oldStage) != Realms.realm(newStage);
        AuraEffects.breakthrough(player, oldStage, newStage);
        if (newRealm && Realms.realm(newStage) >= 3) CeremonyManager.start(player, Realms.realm(newStage));
        refreshAttributes(player);
        player.heal(newRealm ? 10.0f : 3.0f);
        sendTitle(player);
        player.sendMessage(Text.literal("✦ ĐỘT PHÁ! " + displayStage(player)).formatted(Realms.color(newStage)), false);
        if (newRealm && TuTienMod.CONFIG.broadcastNewRealm) {
            player.getServer().getPlayerManager().broadcast(Text.literal("✦ " + player.getName().getString()
                + " đã đột phá đến " + displayStage(player) + "!").formatted(Formatting.GOLD), false);
        }
    }
    public static void sendTitle(ServerPlayerEntity player) {
        int stage = ((CultivationHolder) player).hytur$getStage();
        player.networkHandler.sendPacket(new TitleFadeS2CPacket(10, 45, 15));
        player.networkHandler.sendPacket(new TitleS2CPacket(
            Text.literal(Realms.name(stage)).formatted(Realms.color(stage), Formatting.BOLD)));
        player.networkHandler.sendPacket(new SubtitleS2CPacket(
            Text.literal("Tầng " + Realms.layer(stage) + "/10  •  Đột phá thành công")
                .formatted(Formatting.WHITE)));
    }
    public static String displayStage(ServerPlayerEntity player) {
        int stage = ((CultivationHolder) player).hytur$getStage();
        return Realms.name(stage) + " - Tầng " + Realms.layer(stage) + "/10";
    }
    public static void displayProgress(ServerPlayerEntity player) {
        CultivationHolder data = (CultivationHolder) player;
        int stage = data.hytur$getStage();
        String progress = Realms.maxed(stage) ? "ĐẠI VIÊN MÃN"
            : String.format("%,d/%,d linh khí", data.hytur$getCultivationXp(), needed(stage));
        player.sendMessage(Text.literal("☯ " + Realms.name(stage) + " " + Realms.layer(stage)
            + "/10   •   " + progress).formatted(Realms.color(stage)), true);
    }
    public static void refreshAttributes(ServerPlayerEntity player) {
        int stage = ((CultivationHolder) player).hytur$getStage();
        TuTienConfig cfg = TuTienMod.CONFIG;
        // Small additional milestone bonus makes crossing a realm meaningful.
        double realmBonus = Realms.realm(stage) * cfg.realmPowerBonus;
        setModifier(player, EntityAttributes.GENERIC_MAX_HEALTH, HEALTH_UUID, "Tu Tien max health",
            stage * cfg.bonusMaxHpPerLayer + realmBonus * 2);
        setModifier(player, EntityAttributes.GENERIC_ATTACK_DAMAGE, DAMAGE_UUID, "Tu Tien attack",
            stage * cfg.bonusAttackDamagePerLayer + realmBonus);
        setModifier(player, EntityAttributes.GENERIC_ARMOR, ARMOR_UUID, "Tu Tien armor",
            stage * cfg.bonusArmorPerLayer + realmBonus * 0.5);
        setModifier(player, EntityAttributes.GENERIC_MOVEMENT_SPEED, SPEED_UUID, "Tu Tien speed",
            stage * cfg.bonusMovementSpeedPerLayer);
        if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
    }
    private static void setModifier(ServerPlayerEntity player, EntityAttribute attribute, UUID uuid,
                                    String name, double amount) {
        EntityAttributeInstance instance = player.getAttributeInstance(attribute);
        if (instance == null) return;
        EntityAttributeModifier old = instance.getModifier(uuid);
        if (old != null && Math.abs(old.getValue() - amount) < 0.0000001) return;
        if (old != null) instance.removeModifier(uuid);
        if (amount > 0) instance.addTemporaryModifier(new EntityAttributeModifier(uuid, name, amount,
            EntityAttributeModifier.Operation.ADDITION));
    }
}
