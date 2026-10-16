package vn.hyturcraft.tutien;

/** Stored per player in vanilla player NBT; stage is zero-based (0..99). */
public interface CultivationHolder {
    int hytur$getStage();
    long hytur$getCultivationXp();
    void hytur$setStage(int stage);
    void hytur$setCultivationXp(long xp);
}
