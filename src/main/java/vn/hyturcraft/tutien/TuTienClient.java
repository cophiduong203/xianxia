package vn.hyturcraft.tutien;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/** H sword, I ultimate, Z teleport, double-jump sword flight. No breakthrough key. */
public final class TuTienClient implements ClientModInitializer {
    private static final String CATEGORY = "category.hyturtutien.controls";
    private static KeyBinding sword, teleport, ultimate;
    private static int ticks = 0;
    private static int jumpLast = -1000;
    private static boolean wasJumpDown = false;

    @Override public void onInitializeClient() {
        sword = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.hyturtutien.sword", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, CATEGORY));
        teleport = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.hyturtutien.teleport", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Z, CATEGORY));
        ultimate = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.hyturtutien.ultimate", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_I, CATEGORY));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ticks++;
            if (client.player == null || client.world == null) {
                wasJumpDown = false; jumpLast = -1000; return;
            }
            while (sword.wasPressed()) send(0);
            while (teleport.wasPressed()) send(2);
            while (ultimate.wasPressed()) send(4);
            // Don't consume vanilla jump input; detect fresh press from held-key edge.
            boolean jumping = client.options.jumpKey.isPressed();
            if (jumping && !wasJumpDown) {
                if (ticks - jumpLast <= 8) {
                    send(3);
                    jumpLast = -1000;
                } else jumpLast = ticks;
            }
            wasJumpDown = jumping;
        });
    }
    private static void send(int action) {
        if (!ClientPlayNetworking.canSend(TuTienMod.ACTION_PACKET)) return;
        var buf = PacketByteBufs.create();
        buf.writeByte(action);
        ClientPlayNetworking.send(TuTienMod.ACTION_PACKET, buf);
    }
}
