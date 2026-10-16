package vn.hyturcraft.tutien;

import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Visual sword follows the player's creative-style flight each server tick. */
public final class SwordFlightManager {
    private SwordFlightManager() {}
    private static final Map<UUID, DisplayEntity.ItemDisplayEntity> SWORDS = new HashMap<>();

    public static void start(ServerPlayerEntity player) {
        clear(player);
        int stage = ((CultivationHolder)player).hytur$getStage();
        float scale = 1.05f + Realms.realm(stage) * 0.11f + (Realms.layer(stage) - 1) * .015f;
        DisplayEntity.ItemDisplayEntity blade = SwordDisplay.spawn(player.getServerWorld(),
            player.getPos().add(0, -.55, 0), Realms.realm(stage), scale, false, player.getYaw());
        SWORDS.put(player.getUuid(), blade);
    }

    public static void tick(ServerPlayerEntity player) {
        DisplayEntity.ItemDisplayEntity sword = SWORDS.get(player.getUuid());
        if (sword == null || sword.isRemoved()) {
            if (FlightManager.active(player)) start(player);
            return;
        }
        if (!FlightManager.active(player) || player.isDead()
            || sword.getWorld() != player.getWorld()) { clear(player); return; }
        Vec3d pos = player.getPos();
        sword.setPosition(pos.x, pos.y - .50 + .045 * Math.sin(player.age * .18), pos.z);
        sword.setYaw(player.getYaw());
        sword.setPitch(0f);
        if (player.age % 3 == 0) {
            ServerWorld w = player.getServerWorld();
            int stage = ((CultivationHolder)player).hytur$getStage();
            double angle = player.age * .19;
            for (int i = 0; i < 3 + Realms.realm(stage) / 2; i++) {
                double a = angle + i * Math.PI * 2 / (3 + Realms.realm(stage) / 2);
                w.spawnParticles(AuraEffects.dust(stage, 1.3f), pos.x + .55 * Math.cos(a), pos.y - .50,
                    pos.z + .55 * Math.sin(a), 1, 0, 0, 0, 0);
            }
            if (player.age % 9 == 0) w.spawnParticles(ParticleTypes.END_ROD,
                pos.x, pos.y - .6, pos.z, 3, .55, .07, .55, .012);
        }
    }

    public static void clear(ServerPlayerEntity player) {
        DisplayEntity.ItemDisplayEntity existing = SWORDS.remove(player.getUuid());
        if (existing != null && !existing.isRemoved()) existing.discard();
    }
}
