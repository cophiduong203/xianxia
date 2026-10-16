package vn.hyturcraft.tutien.mixin;

import vn.hyturcraft.tutien.CultivationHolder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerMixin implements CultivationHolder {
    @Unique private int hytur$stage = 0;
    @Unique private long hytur$xp = 0;

    @Override public int hytur$getStage() { return hytur$stage; }
    @Override public long hytur$getCultivationXp() { return hytur$xp; }
    @Override public void hytur$setStage(int stage) { hytur$stage = Math.max(0, Math.min(99, stage)); }
    @Override public void hytur$setCultivationXp(long xp) { hytur$xp = Math.max(0L, xp); }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void hytur$writeCultivation(NbtCompound nbt, CallbackInfo ci) {
        NbtCompound data = new NbtCompound();
        data.putInt("Stage", hytur$stage);
        data.putLong("Xp", hytur$xp);
        nbt.put("HyturTuTien", data);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void hytur$readCultivation(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("HyturTuTien", NbtElement.COMPOUND_TYPE)) {
            NbtCompound data = nbt.getCompound("HyturTuTien");
            hytur$setStage(data.getInt("Stage"));
            hytur$setCultivationXp(data.getLong("Xp"));
        }
    }

    // Players are reconstructed on death and when returning from The End.
    @Inject(method = "copyFrom", at = @At("TAIL"))
    private void hytur$keepCultivation(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        CultivationHolder old = (CultivationHolder) oldPlayer;
        this.hytur$stage = old.hytur$getStage();
        this.hytur$xp = old.hytur$getCultivationXp();
    }
}
