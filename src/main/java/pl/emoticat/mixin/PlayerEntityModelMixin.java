package pl.emoticat.mixin;

import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.emoticat.EmoteManager;
import pl.emoticat.EmotePoses;

@Mixin(PlayerEntityModel.class)
public class PlayerEntityModelMixin {
    @Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;)V",
            at = @At("TAIL"))
    private void emoticat$pose(PlayerEntityRenderState state, CallbackInfo ci) {
        EmoteManager.Frame f = EmoteManager.FRAMES.get(state);
        if (f != null) EmotePoses.apply((PlayerEntityModel) (Object) this, f);
    }
}
