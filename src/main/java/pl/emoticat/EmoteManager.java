package pl.emoticat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Stan animowanych emotek: Luzer, Kosz (głowa jako piłka), Warden na plecach. */
public class EmoteManager {
    public record Active(String name, int startAge) {}
    public record Frame(String emote, float emoteT, boolean warden, float wardenT,
                        boolean sneaking, float limbPos, float limbSpeed, float age) {}

    public static final int WARDEN_ANIM = 70;      // ticki animacji wyciągania i zakładania
    private static final int MAX_TICKS = 20 * 20;  // Luzer/Kosz trwają max 20 s

    private static final Pattern CODE = Pattern.compile(":(luzer|kosz|warden):");
    public static final Map<UUID, Active> ACTIVE = new HashMap<>();
    public static final Map<UUID, Integer> WARDEN = new HashMap<>();
    private static final Map<UUID, Vec3d> LAST = new HashMap<>();
    /** Dane dla jednej klatki renderu (klucz = obiekt stanu renderu gracza). */
    public static final Map<Object, Frame> FRAMES = new WeakHashMap<>();

    public static void clear() { ACTIVE.clear(); WARDEN.clear(); LAST.clear(); }

    /** Co tick: chodzenie / skok / kucanie kończy emotki (Warden zostaje na plecach). */
    public static void tick(ClientWorld world) {
        for (AbstractClientPlayerEntity p : world.getPlayers()) {
            UUID id = p.getUuid();
            Vec3d pos = p.getPos();
            Vec3d last = LAST.put(id, pos);
            boolean moved = last != null && (
                    (pos.x - last.x) * (pos.x - last.x) + (pos.z - last.z) * (pos.z - last.z) > 0.0009
                    || Math.abs(pos.y - last.y) > 0.08);
            Active a = ACTIVE.get(id);
            if (a != null && (moved || p.isSneaking() || p.age - a.startAge() > MAX_TICKS)) ACTIVE.remove(id);
            Integer w = WARDEN.get(id);
            if (w != null && moved && p.age - w < WARDEN_ANIM) WARDEN.put(id, p.age - WARDEN_ANIM);
        }
    }

    /** Wiadomość z czatu zawiera :luzer: / :kosz: / :warden: -> odpal animację u nadawcy. */
    public static void onMessage(String text, UUID senderOrNull) {
        Matcher m = CODE.matcher(text);
        if (!m.find()) return;
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world == null) return;
        AbstractClientPlayerEntity p = null;
        if (senderOrNull != null) {
            p = world.getPlayers().stream().filter(x -> x.getUuid().equals(senderOrNull)).findFirst().orElse(null);
        } else {
            p = findByName(world, text);
        }
        if (p == null) return;
        String code = m.group(1);
        if (p == MinecraftClient.getInstance().player) {   // własne echo już odegrane lokalnie
            Long exp = ECHO.remove(code);
            if (exp != null && exp > System.currentTimeMillis()) return;
        }
        start(p, code);
    }

    private static AbstractClientPlayerEntity findByName(ClientWorld world, String text) {
        String head = text.length() > 48 ? text.substring(0, 48) : text;
        AbstractClientPlayerEntity best = null;
        int bestIdx = Integer.MAX_VALUE;
        for (AbstractClientPlayerEntity p : world.getPlayers()) {
            String n = p.getGameProfile().getName();
            int i = head.indexOf(n);
            if (i < 0) continue;
            if (i > 0 && isWord(head.charAt(i - 1))) continue;
            int e = i + n.length();
            if (e < head.length() && isWord(head.charAt(e))) continue;
            if (i < bestIdx || (i == bestIdx && best != null
                    && n.length() > best.getGameProfile().getName().length())) {
                best = p; bestIdx = i;
            }
        }
        return best;
    }

    private static final Map<String, Long> ECHO = new HashMap<>();

    /** Użycie emotki z Hubu: animacje odpalają się od razu, ikonki lecą jako wiadomość. */
    public static void useEmote(String name, boolean share) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        if (Emotes.isAnim(name)) {
            start(mc.player, name);                       // od razu u siebie
            if (share) {                                  // sygnał dla innych graczy z modem
                ECHO.put(name, System.currentTimeMillis() + 5000);
                mc.player.networkHandler.sendChatMessage(":" + name + ":");
            }
        } else {
            mc.player.networkHandler.sendChatMessage(":" + name + ":");
        }
    }

    private static boolean isWord(char c) { return Character.isLetterOrDigit(c) || c == '_'; }

    private static void start(AbstractClientPlayerEntity p, String name) {
        UUID id = p.getUuid();
        if (name.equals("warden")) {
            if (WARDEN.remove(id) == null) WARDEN.put(id, p.age);   // drugi raz = zdejmij
        } else {
            ACTIVE.put(id, new Active(name, p.age));
        }
    }

    /** Wołane z mixinu przy przygotowaniu stanu renderu gracza. */
    public static void capture(AbstractClientPlayerEntity p, PlayerEntityRenderState state, float td) {
        UUID id = p.getUuid();
        Active a = ACTIVE.get(id);
        Integer w = WARDEN.get(id);
        if (a == null && w == null) return;
        float now = p.age + td;
        float wt = w == null ? 0 : now - w;
        if (w != null && wt < 0) wt = WARDEN_ANIM;
        FRAMES.put(state, new Frame(a == null ? null : a.name(), a == null ? 0 : now - a.startAge(),
                w != null, wt, p.isSneaking(),
                p.limbAnimator.getPos(td), Math.min(1f, p.limbAnimator.getSpeed(td)), now));
    }
}
