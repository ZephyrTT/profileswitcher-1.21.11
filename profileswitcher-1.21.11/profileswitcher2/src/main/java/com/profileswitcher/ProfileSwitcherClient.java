package com.profileswitcher;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.ParticlesMode;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class ProfileSwitcherClient implements ClientModInitializer {

    public static boolean pvpMode = false;

    private static GraphicsMode savedGraphicsMode;
    private static CloudRenderMode savedCloudsMode;
    private static ParticlesMode savedParticlesMode;
    private static double savedFovEffect;
    private static boolean savedBobView;

    private static int hudTimer = 0;
    private static final int HUD_DURATION = 60;

    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        // Register keybind (default: G)
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.profileswitcher.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.profileswitcher.title"
        ));

        // Tick: detect key press
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            while (toggleKey.wasPressed()) {
                pvpMode = !pvpMode;
                if (pvpMode) {
                    saveVanillaSettings(client);
                    applyPvpSettings(client);
                } else {
                    restoreVanillaSettings(client);
                }
                hudTimer = HUD_DURATION;
            }

            if (hudTimer > 0) hudTimer--;
        });

        // HUD overlay
        HudLayerRegistrationCallback.EVENT.register(layeredDrawer ->
            layeredDrawer.attachLayerBefore(
                IdentifiedLayer.STATUS_EFFECTS,
                Identifier.of("profileswitcher", "mode_overlay"),
                (context, tickCounter) -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client.player == null || client.options.hudHidden) return;
                    drawModeIndicator(context, client);
                    if (hudTimer > 0) drawToggleMessage(context, client);
                }
            )
        );
    }

    private static void saveVanillaSettings(MinecraftClient client) {
        savedGraphicsMode  = client.options.getGraphicsMode().getValue();
        savedCloudsMode    = client.options.getCloudRenderMode().getValue();
        savedParticlesMode = client.options.getParticlesMode().getValue();
        savedFovEffect     = client.options.getFovEffectScale().getValue();
        savedBobView       = client.options.getBobView().getValue();
    }

    private static void applyPvpSettings(MinecraftClient client) {
        client.options.getGraphicsMode().setValue(GraphicsMode.FAST);
        client.options.getCloudRenderMode().setValue(CloudRenderMode.OFF);
        client.options.getParticlesMode().setValue(ParticlesMode.MINIMAL);
        client.options.getFovEffectScale().setValue(0.0);
        client.options.getBobView().setValue(false);
        client.options.write();
        if (client.worldRenderer != null) client.worldRenderer.reload();
    }

    private static void restoreVanillaSettings(MinecraftClient client) {
        if (savedGraphicsMode != null)
            client.options.getGraphicsMode().setValue(savedGraphicsMode);
        if (savedCloudsMode != null)
            client.options.getCloudRenderMode().setValue(savedCloudsMode);
        if (savedParticlesMode != null)
            client.options.getParticlesMode().setValue(savedParticlesMode);
        client.options.getFovEffectScale().setValue(savedFovEffect);
        client.options.getBobView().setValue(savedBobView);
        client.options.write();
        if (client.worldRenderer != null) client.worldRenderer.reload();
    }

    private static void drawModeIndicator(DrawContext context, MinecraftClient client) {
        TextRenderer tr = client.textRenderer;
        String label = pvpMode ? "§c⚔ PvP" : "§a✦ Vanilla";
        int textW = tr.getWidth(label);
        int screenW = client.getWindow().getScaledWidth();
        int x = screenW - textW - 6;
        int y = 6;
        context.fill(x - 3, y - 2, x + textW + 3, y + 10, 0x88000000);
        context.drawText(tr, Text.of(label), x, y, 0xFFFFFF, true);
    }

    private static void drawToggleMessage(DrawContext context, MinecraftClient client) {
        TextRenderer tr = client.textRenderer;
        String title   = pvpMode ? "§c⚔ PvP Mode" : "§a✦ Vanilla Mode";
        String subtext = pvpMode
                ? "§7AppleSkin, Minimap & HUD hidden"
                : "§7All mods restored";

        int screenW = client.getWindow().getScaledWidth();
        int screenH = client.getWindow().getScaledHeight();
        int titleW   = tr.getWidth(title);
        int subtextW = tr.getWidth(subtext);
        int boxW     = Math.max(titleW, subtextW) + 16;
        int boxH     = 28;
        int boxX     = (screenW - boxW) / 2;
        int boxY     = screenH / 3;

        float alpha = hudTimer < 20 ? (hudTimer / 20f) : 1f;
        int a  = (int)(alpha * 180);
        int ta = (int)(alpha * 255);

        context.fill(boxX, boxY, boxX + boxW, boxY + boxH, (a << 24));
        context.fill(boxX,            boxY,           boxX + boxW, boxY + 1,           (a << 24) | 0xFFFFFF);
        context.fill(boxX,            boxY + boxH - 1, boxX + boxW, boxY + boxH,       (a << 24) | 0xFFFFFF);
        context.fill(boxX,            boxY,           boxX + 1,    boxY + boxH,        (a << 24) | 0xFFFFFF);
        context.fill(boxX + boxW - 1, boxY,           boxX + boxW, boxY + boxH,       (a << 24) | 0xFFFFFF);

        context.drawText(tr, Text.of(title),   (screenW - titleW) / 2,   boxY + 6,  (ta << 24) | 0xFFFFFF, true);
        context.drawText(tr, Text.of(subtext), (screenW - subtextW) / 2, boxY + 17, (ta << 24) | 0xFFFFFF, false);
    }
}
