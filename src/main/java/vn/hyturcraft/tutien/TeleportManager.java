package vn.hyturcraft.tutien;

import net.minecraft.block.BlockState;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class TeleportManager {
    private TeleportManager() {}
    private static final Map<UUID, Integer> COOLDOWN = new HashMap<>();
    public static void activate(ServerPlayerEntity p) {
        if (p.isSpectator()) return;
        int realm = Realms.realm(((CultivationHolder)p).hytur$getStage());
        if (realm < 4) { p.sendMessage(Text.literal("Cần Hóa Thần để dùng thuấn di."), true); return; }
        if (COOLDOWN.getOrDefault(p.getUuid(), 0) > p.age) {
            p.sendMessage(Text.literal("Thuấn di đang hồi chiêu."), true); return;
        }
        int range = 20 + (realm - 4) * 10;
        Vec3d look = p.getRotationVec(1f).normalize();
        Vec3d origin = p.getPos();
        Vec3d target = null;
        // Step backwards from max range; require a standing surface to prevent a Void teleport.
        for (double d = range; d >= 2; d -= 0.75) {
            Vec3d pos = origin.add(look.multiply(d));
            BlockPos feet = BlockPos.ofFloored(pos);
            if (!p.getWorld().getBlockState(feet.down()).isSolidBlock(p.getWorld(), feet.down())) continue;
            if (p.getWorld().isSpaceEmpty(p, p.getBoundingBox().offset(pos.subtract(origin)))) {
                target = pos; break;
            }
        }
        if (target == null) { p.sendMessage(Text.literal("Không tìm được chỗ dịch chuyển an toàn."), true); return; }
        p.getServerWorld().spawnParticles(ParticleTypes.PORTAL,
            origin.x, origin.y + 1, origin.z, 60, 0.5, 0.85, 0.5, 0.25);
        p.requestTeleport(target.x, target.y, target.z);
        p.fallDistance = 0;
        p.getServerWorld().spawnParticles(ParticleTypes.END_ROD,
            target.x, target.y + 1, target.z, 40, 0.5, 0.8, 0.5, 0.08);
        p.getServerWorld().playSound(null, p.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT,
            SoundCategory.PLAYERS, 0.9f, 1.3f);
        COOLDOWN.put(p.getUuid(), p.age + 100);
    }
    public static void clear(ServerPlayerEntity p) { COOLDOWN.remove(p.getUuid()); }
}
