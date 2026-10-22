package pl.emoticat;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import org.lwjgl.glfw.GLFW;

public class EmotiCatMod implements ClientModInitializer {
    public static KeyBinding menuKey;
    /** EmotiCat kliknięte w menu, czekają na wstawienie do czatu. */
    public static String pending = "";

    @Override
    public void onInitializeClient() {
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.emoticat.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_N, "category.emoticat"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new EmoteScreen());
                }
            }
        });

        ClientTickEvents.END_WORLD_TICK.register(EmoteManager::tick);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> EmoteManager.clear());

        // czat: :luzer: / :kosz: / :warden: uruchamia animację u nadawcy (tylko u graczy z modem)
        ClientReceiveMessageEvents.CHAT.register((message, signed, sender, params, ts) ->
                EmoteManager.onMessage(message.getString(), sender == null ? null : sender.getId()));
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay) EmoteManager.onMessage(message.getString(), null);
        });

        // Warden na plecach
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type, renderer, helper, context) -> {
            if (renderer instanceof PlayerEntityRenderer pr) {
                @SuppressWarnings({"unchecked", "rawtypes"})
                net.minecraft.client.render.entity.feature.FeatureRenderer fr = new WardenBackFeature(pr, context);
                helper.register(fr);
            }
        });
    }
}
