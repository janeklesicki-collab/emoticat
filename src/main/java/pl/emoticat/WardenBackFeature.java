package pl.emoticat;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.model.WardenEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.entity.state.WardenEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

/** Rysuje małego Wardena na plecach gracza (z animacją chodzenia, bez animacji przy kucaniu). */
public class WardenBackFeature extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
    private static final Identifier TEX = Identifier.ofVanilla("textures/entity/warden/warden.png");
    private static final float SCALE = 0.34f;
    private final WardenEntityModel model;
    private final WardenEntityRenderState ws = new WardenEntityRenderState();

    public WardenBackFeature(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> ctx,
                             EntityRendererFactory.Context ec) {
        super(ctx);
        this.model = new WardenEntityModel(ec.getEntityModels().getModelPart(EntityModelLayers.WARDEN));
    }

    @Override
    public void render(MatrixStack m, VertexConsumerProvider vcp, int light,
                       PlayerEntityRenderState state, float a, float b) {
        EmoteManager.Frame f = EmoteManager.FRAMES.get(state);
        if (f == null || !f.warden()) return;

        float t = f.wardenT();
        float feetY, z;
        if (t < 40f) {                                   // wyłania się z ziemi przed graczem
            float p = t / 40f;
            feetY = 1.5f + (1f - p) * 1.0f;
            z = -0.55f;
        } else if (t < EmoteManager.WARDEN_ANIM) {       // łuk znad głowy na plecy
            float q = (t - 40f) / 30f;
            q = q * q * (3f - 2f * q);
            feetY = MathHelper.lerp(q, 1.5f, 0.95f) - MathHelper.sin((float) Math.PI * q) * 1.3f;
            z = MathHelper.lerp(q, -0.55f, 0.30f);
        } else {                                         // siedzi na plecach
            feetY = 0.95f;
            z = 0.30f;
        }

        // bez animacji gdy gracz kuca
        boolean still = f.sneaking();
        ws.limbFrequency = still ? 0f : f.limbPos();
        ws.limbAmplitudeMultiplier = still ? 0f : f.limbSpeed();
        ws.age = still ? 0f : f.age();
        model.setAngles(ws);

        m.push();
        if (f.sneaking() && t >= EmoteManager.WARDEN_ANIM) m.translate(0f, 0.08f, 0.1f);
        m.translate(0f, feetY - 1.5f * SCALE, z);
        m.scale(SCALE, SCALE, SCALE);
        m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f));
        model.render(m, vcp.getBuffer(RenderLayer.getEntityCutoutNoCull(TEX)), light, OverlayTexture.DEFAULT_UV);
        m.pop();
    }
}
