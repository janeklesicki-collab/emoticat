package pl.emoticat;

import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.math.MathHelper;

/** Pozy modelu gracza (kąty w radianach, pivot w pikselach modelu). */
public class EmotePoses {
    public static void apply(PlayerEntityModel m, EmoteManager.Frame f) {
        String e = f.emote() == null ? "" : f.emote();
        switch (e) {
            case "luzer" -> luzer(m, f.emoteT());
            case "kosz" -> kosz(m, f.emoteT());
            default -> {}
        }
        if (f.warden() && f.wardenT() < EmoteManager.WARDEN_ANIM) wardenPull(m, f.wardenT());


    }

    /** Luzer: ręka przy czole ("L"), druga celuje w ciebie, głowa się trzęsie ze śmiechu. */
    private static void luzer(PlayerEntityModel m, float t) {
        float s = MathHelper.sin(t * 0.6f);
        m.rightArm.pitch = -2.6f;
        m.rightArm.yaw = -0.45f;
        m.leftArm.pitch = -1.45f + s * 0.15f;
        m.leftArm.yaw = 0.3f;
        m.head.pitch = -0.25f + s * 0.06f;
        m.head.roll = s * 0.08f;
    }

    /** Kosz: głowa odpięta i kozłowana jak piłka przy prawej ręce. */
    private static void kosz(PlayerEntityModel m, float t) {
        float phase = (t / 9f) % 1f;
        float h = 4f * phase * (1f - phase);          // 0 = przy podłodze, 1 = przy ręce
        m.head.pivotX = -7f;
        m.head.pivotZ = -9f;
        m.head.pivotY = 24f - h * 13f;
        m.head.pitch = t * 0.5f;
        m.head.yaw = 0f;
        m.head.roll = 0f;
        m.rightArm.pitch = -0.55f - h * 0.55f;
        m.leftArm.pitch = 0.4f;
        m.body.pitch = 0.15f;
    }

    /** Warden: schyla się, ciągnie z ziemi, potem zarzuca na plecy. */
    private static void wardenPull(PlayerEntityModel m, float t) {
        if (t < 40f) {
            float k = MathHelper.sin(t * 0.5f);
            m.body.pitch = 0.55f;
            m.head.pitch = 0.45f;
            m.rightArm.pitch = -0.9f + k * 0.4f;
            m.leftArm.pitch = -0.9f + k * 0.4f;
        } else {
            float q = MathHelper.clamp((t - 40f) / 30f, 0f, 1f);
            m.body.pitch = MathHelper.lerp(q, 0.55f, 0f);
            m.head.pitch = MathHelper.lerp(q, 0.45f, 0f);
            float a = MathHelper.lerp(q, -0.9f, -3.0f);
            m.rightArm.pitch = a;
            m.leftArm.pitch = a;
        }
    }
}
