package vn.hyturcraft.tutien;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.Reader;
import java.io.Writer;

public final class TuTienConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("hyturtutien.json");
    public double xpRequiredMultiplier = 8.0;
    public double mobXpMultiplier = 1.0;
    public boolean aurasEnabled = true;
    public int auraPulseTicks = 12;
    public int auraParticlesPerPulse = 5;
    public boolean broadcastNewRealm = true;
    public double bonusMaxHpPerLayer = 0.20;
    public double bonusAttackDamagePerLayer = 0.055;
    public double bonusArmorPerLayer = 0.03;
    public double bonusMovementSpeedPerLayer = 0.00012;
    public double realmPowerBonus = 0.40;
    public double ultimateDamageMultiplier = 1.0;
    public double ultimateDamageCap = 120.0;
    public double chestPillChance = 0.10;
    public double hostileMobPillChance = 0.015;

    public static TuTienConfig load() {
        try {
            if (Files.exists(FILE)) {
                try (Reader r = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
                    JsonObject root = JsonParser.parseReader(r).getAsJsonObject();
                    TuTienConfig loaded = GSON.fromJson(root, TuTienConfig.class);
                    if (loaded != null) {
                        // Migrate configuration from 1.0 without resetting settings from 2.0.
                        if (!root.has("seclusionSeconds") && root.has("meditationEnabled")) {
                            loaded.xpRequiredMultiplier = Math.max(8.0, loaded.xpRequiredMultiplier);
                        }
                        loaded.validate();
                        // Drop obsolete seclusion/meditation fields from the old JSON file.
                        loaded.save();
                        return loaded;
                    }
                }
            }
        } catch (Exception e) { TuTienMod.LOGGER.warn("Cannot read cultivation config", e); }
        TuTienConfig defaults = new TuTienConfig(); defaults.save(); return defaults;
    }
    private void validate() {
        xpRequiredMultiplier = clamp(xpRequiredMultiplier, 0.1, 1000);
        mobXpMultiplier = clamp(mobXpMultiplier, 0, 100);
        auraPulseTicks = Math.max(4, Math.min(200, auraPulseTicks));
        auraParticlesPerPulse = Math.max(1, Math.min(20, auraParticlesPerPulse));
        bonusMaxHpPerLayer = clamp(bonusMaxHpPerLayer, 0, 4);
        bonusAttackDamagePerLayer = clamp(bonusAttackDamagePerLayer, 0, 4);
        bonusArmorPerLayer = clamp(bonusArmorPerLayer, 0, 4);
        bonusMovementSpeedPerLayer = clamp(bonusMovementSpeedPerLayer, 0, .01);
        realmPowerBonus = clamp(realmPowerBonus, 0, 8);
        ultimateDamageMultiplier = clamp(ultimateDamageMultiplier, 0, 20);
        ultimateDamageCap = clamp(ultimateDamageCap, 1, 1000);
        chestPillChance = clamp(chestPillChance, 0, 1);
        hostileMobPillChance = clamp(hostileMobPillChance, 0, 1);
    }
    private static double clamp(double v, double low, double high) {
        return Double.isFinite(v) ? Math.max(low, Math.min(high, v)) : low;
    }
    public void save() {
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer w = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8)) { GSON.toJson(this, w); }
        } catch (Exception e) { TuTienMod.LOGGER.error("Cannot write config", e); }
    }
}
