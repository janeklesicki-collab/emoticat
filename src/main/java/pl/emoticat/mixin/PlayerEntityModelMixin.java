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
    private static final String SET_ANGLES =
            "setAngles(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;)V";

    /** Model jest wspólny dla wszystkich graczy, więc przed każdym graczem czyścimy pozy z emotek. */
    @Inject(method = SET_ANGLES, at = @At("HEAD"))
    private void emoticat$reset(PlayerEntityRenderState state, CallbackInfo ci) {
        PlayerEntityModel m = (PlayerEntityModel) (Object) this;
        m.head.resetTransform();
        m.body.resetTransform();
        m.rightArm.resetTransform();
        m.leftArm.resetTransform();
        m.rightLeg.resetTransform();
        m.leftLeg.resetTransform();
    }

    @Inject(method = SET_ANGLES, at = @At("TAIL"))
    private void emoticat$pose(PlayerEntityRenderState state, CallbackInfo ci) {
        EmoteManager.Frame f = EmoteManager.FRAMES.get(state);
        if (f != null) EmotePoses.apply((PlayerEntityModel) (Object) this, f);
    }
}
