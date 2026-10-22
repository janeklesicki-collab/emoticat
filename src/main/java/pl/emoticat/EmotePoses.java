package pl.emoticat;

import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.math.MathHelper;

/**
 * Pozy modelu gracza (kąty w radianach, pivot w pikselach modelu).
 * Prawa ręka: yaw ujemny = do środka. Lewa ręka: yaw dodatni = do środka.
 * Warstwy skina (czapka, rękawy, kurtka) są podpięte pod części i same podążają.
 */
public class EmotePoses {
    public static void apply(PlayerEntityModel m, EmoteManager.Frame f) {
        String e = f.emote() == null ? "" : f.emote();
        float t = f.emoteT();
        switch (e) {
            case "luzer" -> luzer(m, t);
            case "kosz" -> kosz(m, t);
            case "lol" -> lol(m, t);
            case "smile" -> smile(m, t);
            case "sad" -> sad(m, t);
            case "cry" -> cry(m, t);
            case "angry" -> angry(m, t);
            case "love" -> love(m, t);
            case "cool" -> cool(m, t);
            case "wow" -> wow(m, t);
            case "think" -> think(m, t);
            case "sleep" -> sleep(m, t);
            case "party" -> party(m, t);
            case "skull" -> skull(m, t);
            case "wink" -> wink(m, t);
            case "sick" -> sick(m, t);
            case "clown" -> clown(m, t);
            case "heart" -> heart(m, t);
            case "fire" -> fire(m, t);
            case "star" -> star(m, t);
            case "diamond" -> diamond(m, t);
            case "creeper" -> creeper(m, t);
            case "sword" -> sword(m, t);
            case "gg" -> gg(m, t);
            case "ez" -> ez(m, t);
            default -> {}
        }
        if (f.warden() && f.wardenT() < EmoteManager.WARDEN_ANIM) wardenPull(m, f.wardenT());
    }

    private static void arms(PlayerEntityModel m, float rp, float ry, float lp, float ly) {
        m.rightArm.pitch = rp; m.rightArm.yaw = ry;
        m.leftArm.pitch = lp; m.leftArm.yaw = ly;
    }

    private static void lol(PlayerEntityModel m, float t) {
        float s = MathHelper.sin(t * 1.3f);
        m.head.pitch = -0.35f;
        m.body.pitch = 0.12f * s;
        m.body.yaw = 0.1f * s;
        arms(m, -0.9f, -0.6f, -0.9f, 0.6f);
    }

    private static void smile(PlayerEntityModel m, float t) {
        m.rightArm.pitch = -2.7f;
        m.rightArm.yaw = MathHelper.sin(t * 0.5f) * 0.5f;
        m.head.roll = 0.1f;
    }

    private static void sad(PlayerEntityModel m, float t) {
        m.head.pitch = 0.55f;
        m.body.pitch = 0.2f;
        arms(m, 0.1f, 0f, 0.1f, 0f);
    }

    private static void cry(PlayerEntityModel m, float t) {
        float s = MathHelper.sin(t * 1.5f);
        m.head.pitch = 0.3f;
        m.head.yaw = s * 0.1f;
        arms(m, -2.4f, -0.5f, -2.4f, 0.5f);
    }

    private static void angry(PlayerEntityModel m, float t) {
        float s = MathHelper.sin(t * 1.5f) * 0.4f;
        m.body.pitch = 0.2f;
        m.head.pitch = 0.1f;
        m.rightLeg.pitch = -Math.max(0f, MathHelper.sin(t * 0.8f)) * 0.8f;
        arms(m, -0.3f + s, 0f, -0.3f - s, 0f);
    }

    private static void love(PlayerEntityModel m, float t) {
        m.head.roll = MathHelper.sin(t * 0.3f) * 0.15f;
        arms(m, -3.0f, -0.5f, -3.0f, 0.5f);
    }

    private static void cool(PlayerEntityModel m, float t) {
        m.body.pitch = -0.08f;
        m.head.pitch = -0.15f;
        arms(m, -0.7f, -0.9f, -0.7f, 0.9f);
    }

    private static void wow(PlayerEntityModel m, float t) {
        float s = MathHelper.sin(t * 1.2f) * 0.15f;
        m.head.pitch = -0.2f;
        arms(m, -2.2f + s, 0.5f, -2.2f - s, -0.5f);
    }

    private static void think(PlayerEntityModel m, float t) {
        m.head.pitch = 0.2f;
        m.head.yaw = MathHelper.sin(t * 0.15f) * 0.3f;
        arms(m, -1.9f, -0.8f, -1.0f, 0.8f);
    }

    private static void sleep(PlayerEntityModel m, float t) {
        m.body.pitch = 0.3f;
        m.head.pitch = 0.75f + MathHelper.sin(t * 0.1f) * 0.05f;
        arms(m, 0.1f, 0f, 0.1f, 0f);
    }

    private static void party(PlayerEntityModel m, float t) {
        float s = MathHelper.sin(t * 0.6f);
        m.body.yaw = MathHelper.sin(t * 0.4f) * 0.3f;
        m.head.roll = s * 0.2f;
        arms(m, -2.9f + s * 0.3f, 0f, -2.9f - s * 0.3f, 0f);
    }

    private static void skull(PlayerEntityModel m, float t) {
        m.head.pitch = 0.2f;
        m.head.yaw = MathHelper.sin(t * 1.8f) * 0.15f;
        arms(m, -1.57f, 0f, -1.57f, 0f);
    }

    private static void wink(PlayerEntityModel m, float t) {
        float recoil = (t % 30f) < 4f ? 0.35f : 0f;
        m.rightArm.pitch = -1.57f + recoil;
        m.rightArm.yaw = -0.3f;
        m.head.roll = 0.2f;
    }

    private static void sick(PlayerEntityModel m, float t) {
        m.body.pitch = 0.5f;
        m.body.yaw = MathHelper.sin(t * 1.5f) * 0.08f;
        m.head.pitch = 0.5f;
        arms(m, -0.7f, -0.7f, -0.7f, 0.7f);
    }

    private static void clown(PlayerEntityModel m, float t) {
        float s = MathHelper.sin(t * 0.8f);
        m.head.roll = s * 0.15f;
        arms(m, -1.2f + s * 0.5f, 0f, -1.2f - s * 0.5f, 0f);
    }

    private static void heart(PlayerEntityModel m, float t) {
        float pulse = MathHelper.sin(t * 0.4f) * 0.1f;
        m.head.pitch = 0.1f;
        arms(m, -1.2f, -0.9f + pulse, -1.2f, 0.9f - pulse);
    }

    private static void fire(PlayerEntityModel m, float t) {
        float s = MathHelper.sin(t * 1.2f);
        m.body.pitch = 0.1f;
        m.rightLeg.pitch = s * 0.6f;
        m.leftLeg.pitch = -s * 0.6f;
        arms(m, -2.2f + s * 0.2f, 0.6f, -2.2f - s * 0.2f, -0.6f);
    }

    private static void star(PlayerEntityModel m, float t) {
        m.head.pitch = -0.2f;
        arms(m, -2.9f, 0.8f, -2.9f, -0.8f);
    }

    private static void diamond(PlayerEntityModel m, float t) {
        m.head.pitch = -0.3f;
        m.head.yaw = MathHelper.sin(t * 0.1f) * 0.4f;
        m.body.pitch = -0.1f;
        arms(m, -2.0f, -0.6f, -2.0f, 0.6f);
    }

    private static void creeper(PlayerEntityModel m, float t) {
        float k = Math.min(1f, t / 45f);
        float s = MathHelper.sin(t * (1.0f + k * 2f)) * 0.25f * k;
        m.body.pitch = s;
        m.body.yaw = s;
        m.head.pitch = -0.2f * k;
        arms(m, 0.05f, 0f, 0.05f, 0f);
    }

    private static void sword(PlayerEntityModel m, float t) {
        float s = MathHelper.sin(t * 0.35f);
        m.rightArm.pitch = -0.5f - 2.0f * Math.abs(s);
        m.rightArm.yaw = -0.3f;
        m.leftArm.pitch = 0.3f;
        m.body.yaw = s * 0.2f;
    }

    private static void gg(PlayerEntityModel m, float t) {
        float clap = 0.1f + 0.6f * Math.abs(MathHelper.sin(t * 0.9f));
        arms(m, -1.4f, -clap, -1.4f, clap);
    }

    private static void ez(PlayerEntityModel m, float t) {
        m.head.pitch = -0.3f;
        m.body.pitch = -0.12f;
        arms(m, -2.7f, -0.7f, -2.7f, 0.7f);
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
