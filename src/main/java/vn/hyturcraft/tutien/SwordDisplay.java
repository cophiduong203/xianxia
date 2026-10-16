package vn.hyturcraft.tutien;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

/** Server-owned sword display; 3D model ships inside the mod, no external resource pack. */
public final class SwordDisplay {
    private SwordDisplay() {}

    private static NbtList floats(float... values) {
        NbtList out = new NbtList();
        for (float value : values) out.add(NbtFloat.of(value));
        return out;
    }

    /** A horizontal sword when vertical=false; a falling sky-sword when true. */
    public static DisplayEntity.ItemDisplayEntity spawn(ServerWorld world, Vec3d at,
            int realm, float size, boolean vertical, float yaw) {
        DisplayEntity.ItemDisplayEntity entity = new DisplayEntity.ItemDisplayEntity(EntityType.ITEM_DISPLAY, world);
        NbtCompound tag = new NbtCompound();
        ItemStack stack = new ItemStack(vertical ? SpiritSwordItems.RIFT_SWORD
            : SpiritSwordItems.SWORDS[Math.max(0, Math.min(9, realm))]);
        tag.put("item", stack.writeNbt(new NbtCompound()));
        tag.putString("item_display", "fixed");
        tag.putString("billboard", "fixed");
        tag.putByte("Glowing", (byte) 1);
        tag.putFloat("view_range", 2.5f);
        tag.putInt("interpolation_duration", 2);
        NbtCompound transform = new NbtCompound();
        transform.put("translation", floats(0f, 0f, 0f));
        // The sword model is built along Z. Rotate it upward to make the falling sword.
        transform.put("left_rotation", vertical
            ? floats(-0.70710677f, 0f, 0f, 0.70710677f)
            : floats(0f, 0f, 0f, 1f));
        transform.put("scale", floats(size, size, size));
        transform.put("right_rotation", floats(0f, 0f, 0f, 1f));
        tag.put("transformation", transform);
        entity.readNbt(tag);
        entity.setNoGravity(true);
        entity.setPosition(at.x, at.y, at.z);
        entity.setYaw(yaw);
        world.spawnEntity(entity);
        return entity;
    }
}
