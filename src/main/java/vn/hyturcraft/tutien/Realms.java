package vn.hyturcraft.tutien;

import net.minecraft.util.Formatting;

public final class Realms {
    private Realms() {}

    public static final String[] NAMES = {
        "Luyện Khí", "Trúc Cơ", "Kim Đan", "Nguyên Anh", "Hóa Thần",
        "Luyện Hư", "Hợp Thể", "Đại Thừa", "Độ Kiếp", "Chân Tiên"
    };

    /** Colored body auras, increasingly supernatural as realms grow. */
    public static final float[][] COLORS = {
        {0.35f, 0.87f, 0.98f}, // Luyen Khi - cyan
        {0.30f, 0.95f, 0.50f}, // Truc Co - jade
        {1.00f, 0.81f, 0.18f}, // Kim Dan - gold
        {0.83f, 0.48f, 0.98f}, // Nguyen Anh - purple
        {0.97f, 0.30f, 0.46f}, // Hoa Than - crimson
        {0.20f, 0.67f, 1.00f}, // Luyen Hu - sapphire
        {0.98f, 0.50f, 0.16f}, // Hop The - amber
        {0.96f, 0.96f, 0.85f}, // Dai Thua - white
        {0.55f, 0.36f, 0.99f}, // Do Kiep - lightning violet
        {1.00f, 0.93f, 0.48f}  // Chan Tien - immortal gold
    };

    public static int realm(int stage) { return Math.max(0, Math.min(9, stage / 10)); }
    public static int layer(int stage) { return (Math.max(0, Math.min(99, stage)) % 10) + 1; }
    public static String name(int stage) { return NAMES[realm(stage)]; }
    public static boolean maxed(int stage) { return stage >= 99; }

    public static Formatting color(int stage) {
        return switch (realm(stage)) {
            case 0 -> Formatting.AQUA;
            case 1 -> Formatting.GREEN;
            case 2 -> Formatting.YELLOW;
            case 3 -> Formatting.LIGHT_PURPLE;
            case 4 -> Formatting.RED;
            case 5 -> Formatting.BLUE;
            case 6 -> Formatting.GOLD;
            case 7 -> Formatting.WHITE;
            case 8 -> Formatting.DARK_PURPLE;
            default -> Formatting.GOLD;
        };
    }

    /** Increasing XP required to advance by one of 99 stages. */
    public static long xpNeeded(int stage) {
        if (maxed(stage)) return 0;
        return 70L + stage * 22L + stage * (long) stage * 3L;
    }
}
