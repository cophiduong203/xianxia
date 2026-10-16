package vn.hyturcraft.tutien;

import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import org.joml.Vector3f;

/** Particle-rendered spell wheels: lightweight 3D visual approximation, no custom Phoenix assets. */
public final class AuraEffects {
    private AuraEffects() {}
    static DustParticleEffect dust(int stage, float scale) {
        float[] c = Realms.COLORS[Realms.realm(stage)];
        return new DustParticleEffect(new Vector3f(c[0], c[1], c[2]), scale);
    }
    public static void pulse(ServerPlayerEntity p, int ticks, TuTienConfig cfg) {
        if (!cfg.aurasEnabled || p.isSpectator()) return;
        int stage = ((CultivationHolder)p).hytur$getStage();
        ServerWorld world = p.getServerWorld();
        int realm = Realms.realm(stage), layer = Realms.layer(stage);
        DustParticleEffect color = dust(stage, 1.15f);
        int amount = cfg.auraParticlesPerPulse;
        double phase = ticks * .10 + p.getId();
        for (int i = 0; i < amount; i++) {
            double a = phase + Math.PI * 2 * i / amount;
            double r = .60 + realm * .035;
            world.spawnParticles(color, p.getX() + Math.cos(a) * r,
                p.getY() + .3 + (i * .45 + ticks * .025) % 1.5,
                p.getZ() + Math.sin(a) * r, 1, 0, 0, 0, 0);
        }
        if (realm >= 3) backHalo(p, ticks, realm, layer, color);
    }
    private static void backHalo(ServerPlayerEntity p, int ticks, int realm, int layer,
                                 DustParticleEffect particle) {
        ServerWorld w = p.getServerWorld();
        double yaw = Math.toRadians(p.getYaw());
        // Plane is behind the player, parallel to the player's shoulders.
        double sideX = Math.cos(yaw), sideZ = Math.sin(yaw);
        double backX = Math.sin(yaw), backZ = -Math.cos(yaw);
        double ox = p.getX() + backX * .52;
        double oy = p.getY() + 1.45;
        double oz = p.getZ() + backZ * .52;
        int rings = 1 + (layer >= 4 ? 1 : 0) + (layer >= 8 ? 1 : 0) + (realm >= 7 ? 1 : 0);
        double rotation = ticks * .018;
        for (int ring = 0; ring < rings; ring++) {
            double radius = .76 + ring * .18 + realm * .05;
            int segments = Math.min(48, 14 + layer * 2 + realm * 2);
            for (int j = 0; j < segments; j++) {
                double theta = j * Math.PI * 2 / segments + rotation * (ring % 2 == 0 ? 1 : -1);
                w.spawnParticles(particle, ox + sideX * radius * Math.cos(theta),
                    oy + radius * Math.sin(theta), oz + sideZ * radius * Math.cos(theta),
                    1, 0, 0, 0, 0);
            }
        }
        // Floating runes and intersecting spokes, added gradually at each layer.
        int spokes = 3 + layer / 2 + realm / 2;
        for (int i = 0; i < spokes; i++) {
            double a = i * Math.PI * 2 / spokes - rotation;
            for (int s = 1; s <= 3; s++) {
                double r = s * (.16 + realm * .006);
                w.spawnParticles(ParticleTypes.END_ROD,
                    ox + sideX * r * Math.cos(a), oy + r * Math.sin(a),
                    oz + sideZ * r * Math.cos(a), 1, 0, 0, 0, 0);
            }
        }
    }
    static void floorCircle(ServerPlayerEntity p, int stage, double radius, int points) {
        ServerWorld w = p.getServerWorld();
        DustParticleEffect d = dust(stage, 1.6f);
        for (int i = 0; i < points; i++) {
            double a = i * Math.PI * 2 / points;
            w.spawnParticles(d, p.getX() + Math.cos(a) * radius, p.getY() + .07,
                p.getZ() + Math.sin(a) * radius, 1, 0, 0, 0, 0);
        }
        // six-point formation in the horizontal ground plane
        for (int k = 0; k < 6; k++) {
            double a = k * Math.PI / 3;
            for (int j = 1; j <= 7; j++) {
                double r = radius * j / 8;
                w.spawnParticles(d, p.getX() + Math.cos(a) * r, p.getY() + .08,
                    p.getZ() + Math.sin(a) * r, 1, 0, 0, 0, 0);
            }
        }
    }
    public static void breakthrough(ServerPlayerEntity p, int oldStage, int stage) {
        boolean newRealm = Realms.realm(oldStage) != Realms.realm(stage);
        ServerWorld w = p.getServerWorld();
        int realm = Realms.realm(stage);
        DustParticleEffect d = dust(stage, 1.8f);
        floorCircle(p, stage, newRealm ? 3 : 2, newRealm ? 64 : 32);
        for (int level = 0; level < (newRealm ? 7 : 4); level++) {
            double y = p.getY() + .2 + level * .36;
            for (int i = 0; i < (newRealm ? 38 : 20); i++) {
                double a = i * Math.PI * 2 / (newRealm ? 38 : 20);
                double r = .85 + level * .13;
                w.spawnParticles(d, p.getX() + Math.cos(a) * r, y,
                    p.getZ() + Math.sin(a) * r, 1, 0, .1, 0, .01);
            }
        }
        w.spawnParticles(ParticleTypes.END_ROD, p.getX(), p.getY() + 1.4, p.getZ(),
            newRealm ? 160 : 70, 1.2, 1.4, 1.2, .1);
        for (int bolt = 0; bolt < (1 + realm / 2); bolt++) {
            double a = bolt * 2.4 + p.age * .7;
            double x = p.getX() + Math.cos(a) * 1.7;
            double z = p.getZ() + Math.sin(a) * 1.7;
            for (int i = 0; i < 16; i++)
                w.spawnParticles(ParticleTypes.ELECTRIC_SPARK, x, p.getY() + i * .65,
                    z, 1, .08, .06, .08, 0);
        }
        // No actual lightning entities: prevents fire, damage, and griefing in modpacks.
        w.playSound(null, p.getBlockPos(), SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER,
            SoundCategory.PLAYERS, newRealm ? .9f : .45f, 1.2f);
        w.playSound(null, p.getBlockPos(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
            SoundCategory.PLAYERS, .9f, 1.1f);
    }
}
