package vn.hyturcraft.tutien;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Major realm breakthrough: rise up to 10 blocks, safe fall and particle dragon-like helix. */
public final class CeremonyManager {
    private CeremonyManager() {}
    private record Rite(double startY, int realm, int ticks) {}
    private static final Map<UUID, Rite> ACTIVE = new HashMap<>();
    public static void start(ServerPlayerEntity p, int realm) {
        if (p.isSpectator()) return;
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 280, 0, false, false));
        ACTIVE.put(p.getUuid(), new Rite(p.getY(), realm, 0));
    }
    public static void tick(ServerPlayerEntity p) {
        Rite rite = ACTIVE.get(p.getUuid());
        if (rite == null) return;
        if (p.isDead() || rite.ticks() >= 115) { ACTIVE.remove(p.getUuid()); return; }
        ServerWorld w = p.getServerWorld();
        int t = rite.ticks() + 1;
        if (t <= 55 && p.getY() - rite.startY() < 10.0
            && w.isSpaceEmpty(p, p.getBoundingBox().offset(0, .21, 0))) {
            p.setVelocity(p.getVelocity().x * .4, .20, p.getVelocity().z * .4);
            p.fallDistance = 0;
        } else if (t < 85) {
            p.setVelocity(p.getVelocity().x * .6, Math.max(0, p.getVelocity().y), p.getVelocity().z * .6);
        }
        if (t % 3 == 0) {
            int stage = ((CultivationHolder)p).hytur$getStage();
            AuraEffects.floorCircle(p, stage, 1.0 + rite.realm() * .21, 24 + rite.realm() * 4);
            int dragons = rite.realm() >= 8 ? 3 : rite.realm() >= 6 ? 2 : 1;
            for (int d = 0; d < dragons; d++) {
                double a = t * .13 + d * Math.PI * 2 / dragons;
                double radius = 1.5 + rite.realm() * .18;
                // Curved particle stream; decorative dragon spirit rather than a live mob.
                for (int k = 0; k < 9; k++) {
                    double angle = a - k * .15;
                    double x = p.getX() + radius * Math.cos(angle);
                    double z = p.getZ() + radius * Math.sin(angle);
                    double y = p.getY() + 1.4 + Math.sin(k * .35 + t * .06) * .6;
                    w.spawnParticles(AuraEffects.dust(stage, k == 0 ? 2.5f : 1.1f), x, y, z,
                        1, 0, 0, 0, 0);
                    if (k == 0) w.spawnParticles(ParticleTypes.END_ROD, x, y, z, 3, .15, .15, .15, .02);
                }
            }
        }
        ACTIVE.put(p.getUuid(), new Rite(rite.startY(), rite.realm(), t));
    }
    public static void clear(ServerPlayerEntity p) { ACTIVE.remove(p.getUuid()); }
}
