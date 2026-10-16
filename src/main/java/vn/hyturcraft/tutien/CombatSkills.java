package vn.hyturcraft.tutien;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.RaycastContext;
import org.joml.Vector3f;
import java.util.*;

/** H: sword projectile (Luyen Khi–Kim Dan); double-action myriad sword array (Nguyen Anh+). */
public final class CombatSkills {
    private CombatSkills() {}
    private static final Map<UUID, Integer> COOLDOWN = new HashMap<>();
    private static final Map<UUID, Integer> CHARGING = new HashMap<>();

    public static void activate(ServerPlayerEntity player) {
        if (player.isSpectator()) return;
        int stage = ((CultivationHolder)player).hytur$getStage();
        int realm = Realms.realm(stage);
        Integer charging = CHARGING.get(player.getUuid());
        if (realm >= 3 && charging != null) {
            CHARGING.remove(player.getUuid());
            fireMyriad(player, stage);
            COOLDOWN.put(player.getUuid(), player.age + 160);
            return;
        }
        if (COOLDOWN.getOrDefault(player.getUuid(), 0) > player.age) {
            player.sendMessage(Text.literal("Kiếm thuật đang hồi chiêu."), true); return;
        }
        if (realm < 3) {
            fireSword(player, stage);
            COOLDOWN.put(player.getUuid(), player.age + 80);
        } else {
            CHARGING.put(player.getUuid(), player.age + 200);
            player.sendMessage(Text.literal("VẠN KIẾM QUY TÔNG: đang tụ kiếm! Nhấn H lần nữa để phóng." )
                .formatted(Formatting.AQUA), true);
            player.getServerWorld().playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BEACON_POWER_SELECT,
                SoundCategory.PLAYERS, 0.9f, 1.35f);
        }
    }
    public static void tick(ServerPlayerEntity player) {
        Integer until = CHARGING.get(player.getUuid());
        if (until == null) return;
        if (until < player.age) {
            CHARGING.remove(player.getUuid()); return;
        }
        int stage = ((CultivationHolder)player).hytur$getStage();
        if (player.age % 3 == 0) {
            ServerWorld world = player.getServerWorld();
            int swords = Math.min(24, 6 + Realms.realm(stage) * 2 + Realms.layer(stage) / 2);
            DustParticleEffect particle = tint(stage);
            for (int i = 0; i < swords; i++) {
                double angle = player.age * 0.09 + Math.PI * 2 * i / swords;
                double radius = 1.4 + 0.015 * stage;
                double height = 1.05 + 0.55 * Math.sin(angle * 2);
                world.spawnParticles(particle, player.getX() + Math.cos(angle) * radius,
                    player.getY() + height, player.getZ() + Math.sin(angle) * radius,
                    1, 0, 0, 0, 0);
                if (i % 3 == 0) world.spawnParticles(ParticleTypes.END_ROD,
                    player.getX() + Math.cos(angle) * radius,
                    player.getY() + height, player.getZ() + Math.sin(angle) * radius,
                    1, 0, 0, 0, 0);
            }
        }
    }
    private static DustParticleEffect tint(int stage) {
        float[] col = Realms.COLORS[Realms.realm(stage)];
        return new DustParticleEffect(new Vector3f(col[0], col[1], col[2]), 1.5f);
    }
    private static void fireSword(ServerPlayerEntity player, int stage) {
        int realm = Realms.realm(stage);
        double length = 18 + realm * 5;
        double damage = 6 + stage * 0.27;
        traceAttack(player, 1, length, damage, false, stage);
        player.getServerWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
            SoundCategory.PLAYERS, 1f, 0.9f + realm * 0.12f);
    }
    private static void fireMyriad(ServerPlayerEntity player, int stage) {
        int realm = Realms.realm(stage);
        int swords = Math.min(40, 8 + realm * 3 + Realms.layer(stage));
        double damage = 9 + stage * .32 + swords * 0.5;
        // One hit per target; sword count and arc are visual, avoids instant bosses one-shotting.
        traceAttack(player, swords, 28 + realm * 3, damage, true, stage);
        player.getServerWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDER_DRAGON_GROWL,
            SoundCategory.PLAYERS, 0.55f, 1.7f);
    }
    private static void traceAttack(ServerPlayerEntity p, int swords, double length, double damage,
                                    boolean wide, int stage) {
        ServerWorld world = p.getServerWorld();
        Vec3d eye = p.getEyePos();
        Vec3d dir = p.getRotationVec(1.0f).normalize();
        double maxDist = length;
        BlockHitResult block = world.raycast(new RaycastContext(eye, eye.add(dir.multiply(length)),
            RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, p));
        if (block.getType() == HitResult.Type.BLOCK) maxDist = eye.distanceTo(block.getPos());
        Vec3d end = eye.add(dir.multiply(maxDist));
        List<LivingEntity> candidates = world.getEntitiesByClass(LivingEntity.class,
            new Box(eye, end).expand(wide ? 5 : 1.4),
            e -> e != p && e.isAlive() && !(e instanceof PlayerEntity));
        for (LivingEntity target : candidates) {
            Vec3d d = target.getBoundingBox().getCenter().subtract(eye);
            double projection = d.dotProduct(dir);
            if (projection < 0 || projection > maxDist) continue;
            double separation = d.subtract(dir.multiply(projection)).length();
            if (separation > (wide ? 3.5 : 1.0) + target.getWidth() / 2.0) continue;
            HitResult wall = world.raycast(new RaycastContext(eye, target.getBoundingBox().getCenter(),
                RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, p));
            if (wall.getType() == HitResult.Type.BLOCK) continue;
            target.damage(p.getDamageSources().playerAttack(p), (float) damage);
        }
        DustParticleEffect dust = tint(stage);
        for (int i = 0; i <= (int)(maxDist * 2); i++) {
            Vec3d at = eye.add(dir.multiply(i * .5));
            world.spawnParticles(dust, at.x, at.y, at.z, 1,
                wide ? 0.8 : 0.12, wide ? 0.8 : 0.12, wide ? 0.8 : 0.12, 0.01);
            if (i % 3 == 0) world.spawnParticles(ParticleTypes.END_ROD, at.x, at.y, at.z,
                Math.max(1, swords / 8), wide ? .4 : 0, wide ? .4 : 0, wide ? .4 : 0, .01);
        }
    }
    public static void clear(ServerPlayerEntity p) {
        CHARGING.remove(p.getUuid()); COOLDOWN.remove(p.getUuid());
    }
}
