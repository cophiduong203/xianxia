package vn.hyturcraft.tutien;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TuTienMod implements ModInitializer {
    public static final String MOD_ID = "hyturtutien";
    public static final Identifier ACTION_PACKET = new Identifier(MOD_ID, "action");
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static TuTienConfig CONFIG;
    private static long gameTicks = 0;

    @Override public void onInitialize() {
        CONFIG = TuTienConfig.load();
        PillItems.register();
        SpiritSwordItems.register();
        registerKills();
        registerTicks();
        registerCommands();
        registerControls();
        LOGGER.info("Hytur Tu Tien 3.0 ready - spirit sword flying, I ultimate, auto breakthrough");
    }
    private static void registerControls() {
        ServerPlayNetworking.registerGlobalReceiver(ACTION_PACKET, (server, player, handler, buf, responseSender) -> {
            int code = buf.readUnsignedByte();
            server.execute(() -> {
                if (!player.isAlive()) return;
                switch (code) {
                    case 0 -> CombatSkills.activate(player);
                    case 2 -> TeleportManager.activate(player);
                    case 3 -> FlightManager.toggle(player);
                    case 4 -> VoidSwordUltimate.activate(player);
                }
            });
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity p = handler.getPlayer();
            FlightManager.clear(p);
            CombatSkills.clear(p);
            TeleportManager.clear(p);
            CeremonyManager.clear(p);
            VoidSwordUltimate.clear(p);
        });
    }
    private static void registerKills() {
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, killer, dead) -> {
            if (!(killer instanceof ServerPlayerEntity player)) return;
            if (dead instanceof ServerPlayerEntity) return;
            if (dead.getType().getSpawnGroup() != SpawnGroup.MONSTER) return;
            double health = Math.max(1, Math.min(2000, dead.getMaxHealth()));
            long reward = Math.max(1, Math.round((9.0 + health * .65) * CONFIG.mobXpMultiplier));
            CultivationManager.addXp(player, reward);
        });
    }
    private static void registerTicks() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            gameTicks++;
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                FlightManager.tick(player);
                SwordFlightManager.tick(player);
                VoidSwordUltimate.tick(player);
                CombatSkills.tick(player);
                CeremonyManager.tick(player);
                if (CONFIG.aurasEnabled && gameTicks % CONFIG.auraPulseTicks == 0) {
                    AuraEffects.pulse(player, (int)(gameTicks % 1_000_000L), CONFIG);
                }
                if (gameTicks % 20 == 0 && !player.isSpectator()) CultivationManager.applyPendingBreakthroughs(player);
                if (gameTicks % 80 == 0 && !player.isSpectator()) CultivationManager.refreshAttributes(player);
                if (gameTicks % 100 == 0 && !player.isSpectator()) CultivationManager.displayProgress(player);
            }
        });
    }
    private static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("canhgioi")
                .executes(ctx -> showInfo(ctx.getSource().getPlayerOrThrow()))
                .then(CommandManager.literal("xem")
                    .executes(ctx -> showInfo(ctx.getSource().getPlayerOrThrow())))
                .then(CommandManager.literal("huongdan")
                    .executes(ctx -> {
                        var player = ctx.getSource().getPlayerOrThrow();
                        player.sendMessage(Text.literal("[TU TIÊN 3.0] Hạ quái, uống đan để thu linh khí. Đủ tu vi tự đột phá."), false);
                        player.sendMessage(Text.literal("H = Phóng Kiếm/Vạn Kiếm. I = Thiên Kiếm Giáng Thế (Hợp Thể+). Z = Thuấn Di (Hóa Thần+)."), false);
                        player.sendMessage(Text.literal("Trúc Cơ+: nhấn đôi Space để đứng trên kiếm ngự không. Đại Thừa+: bay vô hạn."), false);
                        player.sendMessage(Text.literal("Đan dược rơi từ loot rương/mod và một số bảng loot quái. /canhgioi xem để xem tầng."), false);
                        return 1;
                    }))
                .then(CommandManager.literal("addxp")
                    .requires(source -> source.hasPermissionLevel(2))
                    .then(CommandManager.argument("amount", IntegerArgumentType.integer(1, 1_000_000))
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                            CultivationManager.addXp(player, amount);
                            ctx.getSource().sendFeedback(() -> Text.literal("Đã thêm " + amount + " linh khí."), false);
                            return amount;
                        })))
                .then(CommandManager.literal("settang")
                    .requires(source -> source.hasPermissionLevel(2))
                    .then(CommandManager.argument("tang", IntegerArgumentType.integer(1, 100))
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                            int stage = IntegerArgumentType.getInteger(ctx, "tang");
                            CultivationManager.setStage(player, stage);
                            ctx.getSource().sendFeedback(() -> Text.literal("Cảnh giới mới: "
                                + CultivationManager.displayStage(player)), false);
                            return stage;
                        })))
                .then(CommandManager.literal("reload")
                    .requires(source -> source.hasPermissionLevel(2))
                    .executes(ctx -> {
                        CONFIG = TuTienConfig.load();
                        for (ServerPlayerEntity p : ctx.getSource().getServer().getPlayerManager().getPlayerList())
                            CultivationManager.refreshAttributes(p);
                        ctx.getSource().sendFeedback(() -> Text.literal("Đã đọc lại config/hyturtutien.json"), false);
                        return 1;
                    }))
            );
        });
    }
    private static int showInfo(ServerPlayerEntity player) {
        CultivationHolder data = (CultivationHolder) player;
        player.sendMessage(Text.literal("────────── ☯ TU TIÊN 3.0 ☯ ──────────").formatted(Formatting.GOLD), false);
        player.sendMessage(Text.literal("Cảnh giới: " + CultivationManager.displayStage(player))
            .formatted(Realms.color(data.hytur$getStage())), false);
        String xp = Realms.maxed(data.hytur$getStage()) ? "ĐẠI VIÊN MÃN"
            : data.hytur$getCultivationXp() + " / " + CultivationManager.needed(data.hytur$getStage());
        player.sendMessage(Text.literal("Linh khí: " + xp).formatted(Formatting.AQUA), false);
        player.sendMessage(Text.literal("H = Kiếm | I = Thiên Kiếm | Z = Thuấn Di | Nhảy đôi = Ngự Kiếm | Tự đột phá"), false);
        return 1;
    }
}
