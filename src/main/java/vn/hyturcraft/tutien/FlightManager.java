package vn.hyturcraft.tutien;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Double-jump toggles spirit sword flight. Flight physics use vanilla player abilities. */
public final class FlightManager {
    private FlightManager() {}
    private static final Map<UUID, Integer> FLYING = new HashMap<>();

    public static boolean active(ServerPlayerEntity player) {
        return FLYING.containsKey(player.getUuid());
    }

    public static void toggle(ServerPlayerEntity player) {
        if (player.isCreative() || player.isSpectator()) return;
        int stage = ((CultivationHolder)player).hytur$getStage();
        int realm = Realms.realm(stage);
        if (realm < 1) {
            player.sendMessage(Text.literal("Cần Trúc Cơ trở lên để Ngự Kiếm Phi Hành."), true);
            return;
        }
        if (active(player)) { stop(player); return; }
        int seconds = switch (realm) {
            case 1 -> 15; // Trúc Cơ
            case 2 -> 20; // Kim Đan
            case 3 -> 25; // Nguyên Anh
            case 4 -> 35; // Hóa Thần
            case 5 -> 50; // Luyện Hư
            case 6 -> 80; // Hợp Thể
            default -> -1; // Đại Thừa: vô hạn
        };
        int remaining = seconds < 0 ? -1 : seconds * 20 + (Realms.layer(stage) - 1) * 10;
        FLYING.put(player.getUuid(), remaining);
        PlayerAbilities abilities = player.getAbilities();
        abilities.allowFlying = true;
        abilities.flying = true;
        player.sendAbilitiesUpdate();
        SwordFlightManager.start(player);
        player.sendMessage(Text.literal("Ngự Kiếm Phi Hành: "
            + (remaining < 0 ? "vô hạn" : (remaining / 20) + " giây"))
            .formatted(Formatting.AQUA), true);
    }

    public static void tick(ServerPlayerEntity player) {
        Integer remaining = FLYING.get(player.getUuid());
        if (remaining == null) return;
        if (!player.isAlive()) { stop(player); return; }
        if (player.isCreative() || player.isSpectator()) {
            FLYING.remove(player.getUuid()); SwordFlightManager.clear(player); return;
        }
        if (Realms.realm(((CultivationHolder)player).hytur$getStage()) < 1) {
            stop(player); return;
        }
        if (!player.getAbilities().allowFlying) {
            player.getAbilities().allowFlying = true;
            player.sendAbilitiesUpdate();
        }
        if (remaining < 0) return;
        if (remaining <= 1) {
            stop(player);
            player.sendMessage(Text.literal("Ngự kiếm hết thời gian."), true);
            return;
        }
        FLYING.put(player.getUuid(), remaining - 1);
        if (remaining % 100 == 0)
            player.sendMessage(Text.literal("Ngự kiếm còn " + (remaining / 20) + " giây"), true);
    }

    private static void stop(ServerPlayerEntity player) {
        FLYING.remove(player.getUuid());
        SwordFlightManager.clear(player);
        if (player.isCreative() || player.isSpectator()) return;
        PlayerAbilities a = player.getAbilities();
        a.allowFlying = false;
        a.flying = false;
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 160, 0, false, false));
        player.sendAbilitiesUpdate();
    }

    public static void clear(ServerPlayerEntity player) {
        FLYING.remove(player.getUuid());
        SwordFlightManager.clear(player);
    }
}
