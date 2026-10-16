package vn.hyturcraft.tutien;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.joml.Vector3f;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** I: Void Rift - Heavenly Sword Descends. Entity display + particles + server damage. */
public final class VoidSwordUltimate {
    private VoidSwordUltimate() {}
    private static final Map<UUID, Integer> COOLDOWNS = new HashMap<>();
    private static final Map<UUID, Cast> CASTS = new HashMap<>();

    private static final DustParticleEffect RIFT_PINK =
        new DustParticleEffect(new Vector3f(1f, .18f, .54f), 2.2f);
    private static final DustParticleEffect GOLD =
        new DustParticleEffect(new Vector3f(1f, .76f, .21f), 1.5f);

    private static final class Cast {
        final ServerWorld world;
        final Vec3d target;
        final int stage;
        final int realm;
        final int radius;
        final float size;
        int ticks;
        DisplayEntity.ItemDisplayEntity sword;
        Cast(ServerWorld world, Vec3d target, int stage, int realm) {
            this.world = world;
            this.target = target;
            this.stage = stage;
            this.realm = realm;
            this.radius = 10 + (realm - 6) * 2;
            this.size = 3.2f + (realm - 6) * .55f;
        }
        void discard() {
            if (sword != null && !sword.isRemoved()) sword.discard();
        }
    }

    public static void activate(ServerPlayerEntity player) {
        if (player.isSpectator()) return;
        int stage = ((CultivationHolder)player).hytur$getStage();
        int realm = Realms.realm(stage);
        if (realm < 6) {
            player.sendMessage(Text.literal("Thiên Kiếm Giáng Thế mở khóa ở Hợp Thể."), true);
            return;
        }
        if (CASTS.containsKey(player.getUuid())) {
            player.sendMessage(Text.literal("Đại kiếm đang giáng xuống!"), true);
            return;
        }
        int expires = COOLDOWNS.getOrDefault(player.getUuid(), 0);
        if (expires > player.age) {
            player.sendMessage(Text.literal("Sát chiêu hồi sau " + ((expires - player.age + 19) / 20) + " giây."), true);
            return;
        }
        Vec3d eye = player.getEyePos();
        Vec3d direction = player.getRotationVec(1f).normalize();
        Vec3d end = eye.add(direction.multiply(36));
        BlockHitResult ray = player.getServerWorld().raycast(new RaycastContext(
            eye, end, RaycastContext.ShapeType.COLLIDER,
            RaycastContext.FluidHandling.NONE, player));
        Vec3d target = ray.getType() == HitResult.Type.BLOCK ? ray.getPos() : end;
        // Prefer the monster under the crosshair; never place the spell behind a wall.
        double closest = eye.distanceTo(target);
        for (LivingEntity enemy : player.getServerWorld().getEntitiesByClass(LivingEntity.class,
            new Box(eye, end).expand(2.0),
            e -> e.isAlive() && e.getType().getSpawnGroup() == SpawnGroup.MONSTER)) {
            Vec3d middle = enemy.getBoundingBox().getCenter();
            double along = middle.subtract(eye).dotProduct(direction);
            double offAxis = middle.subtract(eye.add(direction.multiply(along))).length();
            if (along > 0 && along < closest && offAxis < 1.1 + enemy.getWidth() * .5) {
                closest = along;
                target = enemy.getPos();
            }
        }
        CASTS.put(player.getUuid(), new Cast(player.getServerWorld(), target, stage, realm));
        int cooldownSeconds = switch (realm) {
            case 6 -> 85;
            case 7 -> 70;
            case 8 -> 60;
            default -> 45;
        };
        COOLDOWNS.put(player.getUuid(), player.age + cooldownSeconds * 20);
        player.sendMessage(Text.literal("XÉ RÁCH HƯ KHÔNG · THIÊN KIẾM GIÁNG THẾ!")
            .formatted(Formatting.LIGHT_PURPLE), true);
        player.getServerWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_WITHER_SPAWN,
            SoundCategory.PLAYERS, 0.75f, 1.65f);
    }

    public static void tick(ServerPlayerEntity caster) {
        Cast cast = CASTS.get(caster.getUuid());
        if (cast == null) return;
        if (!caster.isAlive() || caster.getWorld() != cast.world) {
            cast.discard(); CASTS.remove(caster.getUuid()); return;
        }
        int tick = ++cast.ticks;
        ServerWorld world = cast.world;
        Vec3d center = cast.target;
        double riftY = center.y + 23;
        int segments = 44 + 8 * (cast.realm - 6);
        if (tick < 56) {
            double opening = Math.min(1.0, tick / 16.0);
            for (int i = 0; i < segments; i++) {
                double angle = i * Math.PI * 2 / segments + tick * .025;
                double radius = (3.4 + (cast.realm - 6) * .8) * opening;
                double x = center.x + radius * Math.cos(angle);
                double z = center.z + radius * Math.sin(angle);
                // Rift: inclined pink runic oval with electric arcs and golden glyphs.
                double y = riftY + 1.9 * Math.sin(angle * 2 + tick * .035);
                if (i % 2 == 0)
                    world.spawnParticles(RIFT_PINK, x, y, z, 1, 0, 0, 0, 0);
                if (i % 6 == 0)
                    world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z,
                        1, .22, .22, .22, .02);
                if (i % 5 == 0)
                    world.spawnParticles(GOLD, x * .70 + center.x * .30,
                        y + .4, z * .70 + center.z * .30, 1, 0, 0, 0, 0);
            }
        }
        if (tick == 17) {
            cast.sword = SwordDisplay.spawn(world, center.add(0, 26, 0),
                cast.realm, cast.size, true, 0f);
            world.spawnParticles(ParticleTypes.FLASH, center.x, riftY, center.z,
                3, 1.1, .3, 1.1, .04);
        }
        if (tick >= 17 && tick < 54 && cast.sword != null) {
            double progress = (tick - 17) / 37.0;
            double y = center.y + 26 - 19 * progress * progress;
            cast.sword.setPosition(center.x, y, center.z);
            if (tick % 2 == 0) {
                world.spawnParticles(RIFT_PINK, center.x, y - 2.8, center.z,
                    15, .9, 2.0, .9, .015);
                world.spawnParticles(ParticleTypes.END_ROD, center.x, y + 3, center.z,
                    6, .45, 2.0, .45, .04);
            }
        }
        if (tick == 54) {
            if (cast.sword != null) cast.sword.setPosition(center.x, center.y + 7, center.z);
            impact(caster, cast);
        }
        if (tick >= 54 && tick <= 76) {
            double radius = cast.radius * Math.min(1.0, (tick - 53) / 19.0);
            ring(world, center, radius, tick, cast.stage);
            if (tick % 4 == 0)
                world.spawnParticles(ParticleTypes.END_ROD,
                    center.x, center.y + .2, center.z, 16, radius / 1.7, .2, radius / 1.7, .08);
        }
        if (tick >= 84) {
            cast.discard(); CASTS.remove(caster.getUuid());
        }
    }

    private static void ring(ServerWorld world, Vec3d center, double radius, int tick, int stage) {
        DustParticleEffect realmColor = AuraEffects.dust(stage, 2.4f);
        for (int i = 0; i < 96; i++) {
            double a = i * Math.PI * 2 / 96;
            for (int ring = 0; ring < 3; ring++) {
                double r = radius * (1 - ring * .23);
                world.spawnParticles(ring == 1 ? realmColor : RIFT_PINK,
                    center.x + r * Math.cos(a + tick * .015 * (ring % 2 == 0 ? 1 : -1)),
                    center.y + .16 + ring * .04,
                    center.z + r * Math.sin(a + tick * .015 * (ring % 2 == 0 ? 1 : -1)),
                    1, 0, 0, 0, 0);
            }
            if (i % 8 == 0) {
                double a2 = a + tick * .015;
                world.spawnParticles(GOLD, center.x + .58 * radius * Math.cos(a2),
                    center.y + .2, center.z + .58 * radius * Math.sin(a2), 1, 0, 0, 0, 0);
            }
        }
        // Six-point spirit formation inside the expanding concentric circles.
        for (int ray = 0; ray < 6; ray++) {
            double a = ray * Math.PI / 3 + tick * .005;
            for (int step = 2; step < 11; step++) {
                double d = radius * step / 12.0;
                world.spawnParticles(step % 3 == 0 ? GOLD : realmColor,
                    center.x + d * Math.cos(a), center.y + .22,
                    center.z + d * Math.sin(a), 1, 0, 0, 0, 0);
            }
        }
    }

    private static void impact(ServerPlayerEntity caster, Cast cast) {
        ServerWorld world = cast.world;
        Vec3d center = cast.target;
        world.playSound(null, net.minecraft.util.math.BlockPos.ofFloored(center), SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER,
            SoundCategory.PLAYERS, 1f, .7f);
        world.playSound(null, net.minecraft.util.math.BlockPos.ofFloored(center), SoundEvents.ENTITY_GENERIC_EXPLODE,
            SoundCategory.PLAYERS, 1f, .6f);
        world.spawnParticles(ParticleTypes.FLASH, center.x, center.y + 1, center.z,
            6, 1.4, 1, 1.4, .2);
        world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y + 1, center.z,
            170, cast.radius / 3., 2., cast.radius / 3., .3);
        world.spawnParticles(ParticleTypes.END_ROD, center.x, center.y + 1, center.z,
            150, cast.radius / 3., 1.2, cast.radius / 3., .11);
        double damage = Math.min(TuTienMod.CONFIG.ultimateDamageCap,
            (32 + cast.stage * .75) * TuTienMod.CONFIG.ultimateDamageMultiplier);
        for (LivingEntity target : world.getEntitiesByClass(LivingEntity.class,
            new Box(center, center).expand(cast.radius, 6, cast.radius),
            e -> e.isAlive() && e != caster && e.getType().getSpawnGroup() == SpawnGroup.MONSTER)) {
            double distance = target.getPos().distanceTo(center);
            if (distance > cast.radius + 1.2) continue;
            float dealt = (float)(damage * Math.max(.35, 1.0 - distance / (cast.radius * 1.5)));
            if (target.damage(caster.getDamageSources().playerAttack(caster), dealt)) {
                Vec3d diff = target.getPos().subtract(center);
                target.takeKnockback(1.0, -diff.x, -diff.z);
            }
        }
    }

    public static void clear(ServerPlayerEntity player) {
        Cast cast = CASTS.remove(player.getUuid());
        if (cast != null) cast.discard();
        COOLDOWNS.remove(player.getUuid());
    }
}
