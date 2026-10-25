package pl.emoticat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Stan emotek: każda emotka to animacja postaci gracza. Warden na plecach to przełącznik. */
public class EmoteManager {
    public record Active(String name, int startAge) {}
    public record Frame(String emote, float emoteT, boolean warden, float wardenT,
                        boolean sneaking, float limbPos, float limbSpeed, float age) {}

    public static final int WARDEN_ANIM = 70;      // ticki animacji wyciągania i zakładania

    private static Pattern CODE;
    public static final Map<UUID, Active> ACTIVE = new HashMap<>();
    public static final Map<UUID, Integer> WARDEN = new HashMap<>();
    private static final Map<UUID, Vec3d> LAST = new HashMap<>();
    private static final Map<String, ArrayDeque<Long>> ECHO = new HashMap<>();
    /** Dane dla jednej klatki renderu (klucz = obiekt stanu renderu gracza). */
    public static final Map<Object, Frame> FRAMES = new WeakHashMap<>();

    private static Pattern code() {
        if (CODE == null) CODE = Pattern.compile(":(" + String.join("|", Emotes.NAMES) + "):");
        return CODE;
    }

    public static void clear() { ACTIVE.clear(); WARDEN.clear(); LAST.clear(); ECHO.clear(); }

    private static boolean inputPressed(MinecraftClient mc) {
        var o = mc.options;
        return o.forwardKey.isPressed() || o.backKey.isPressed() || o.leftKey.isPressed()
                || o.rightKey.isPressed() || o.jumpKey.isPressed() || o.sneakKey.isPressed();
    }

    /** Co tick: ruch, skok, kucanie, obrażenia albo upływ czasu kończą emotkę. */
    public static void tick(ClientWorld world) {
        MinecraftClient mc = MinecraftClient.getInstance();
        for (AbstractClientPlayerEntity p : world.getPlayers()) {
            UUID id = p.getUuid();
            Vec3d pos = p.getPos();
            Vec3d last = LAST.put(id, pos);
            boolean moved = last != null && (
                    (pos.x - last.x) * (pos.x - last.x) + (pos.z - last.z) * (pos.z - last.z) > 0.0004
                    || Math.abs(pos.y - last.y) > 0.08);
            boolean keys = p == mc.player && inputPressed(mc);

            Active a = ACTIVE.get(id);
            if (a != null) {
                int t = p.age - a.startAge();
                boolean stop = t > Emotes.duration(a.name())
                        || (t > 3 && (moved || keys || p.isSneaking() || p.hasVehicle() || p.hurtTime > 0));
                if (stop) ACTIVE.remove(id);
                else particles(world, p, a.name(), t);
            }
            if (!p.isAlive()) { ACTIVE.remove(id); WARDEN.remove(id); continue; }
            Integer w = WARDEN.get(id);
            if (w != null && p.isSneaking() && p.age - w > 5) { WARDEN.remove(id); w = null; }   // kucnięcie = koniec Wardena
            if (w != null && (moved || keys) && p.age - w < WARDEN_ANIM) WARDEN.put(id, p.age - WARDEN_ANIM);
        }
    }

    private static void particles(ClientWorld w, AbstractClientPlayerEntity p, String name, int t) {
        double x = p.getX(), y = p.getY(), z = p.getZ();
        double r = Math.toRadians(p.bodyYaw);
        double fx = -Math.sin(r), fz = Math.cos(r);       // przód
        double sx = Math.cos(r), sz = Math.sin(r);        // bok
        double side = Math.random() < 0.5 ? 1 : -1;
        double rx = (Math.random() - 0.5) * 0.8, rz = (Math.random() - 0.5) * 0.8;
        switch (name) {
            case "love", "heart" -> {
                if (t % 6 == 0) w.addParticle(ParticleTypes.HEART, x + rx, y + 2.0, z + rz, 0, 0.05, 0);
            }
            case "angry" -> {
                if (t % 10 == 0) w.addParticle(ParticleTypes.ANGRY_VILLAGER, x, y + 2.1, z, 0, 0, 0);
            }
            case "cry" -> {
                if (t % 3 == 0) w.addParticle(ParticleTypes.FALLING_WATER,
                        x + fx * 0.3 + sx * 0.15 * side, y + 1.6, z + fz * 0.3 + sz * 0.15 * side, 0, 0, 0);
            }
            case "fire" -> w.addParticle(ParticleTypes.FLAME, x + rx, y + 0.3 + Math.random() * 1.5, z + rz, 0, 0.02, 0);
            case "star" -> {
                if (t % 2 == 0) w.addParticle(ParticleTypes.END_ROD, x + rx * 1.5, y + 0.5 + Math.random() * 1.8, z + rz * 1.5, 0, 0.02, 0);
            }
            case "diamond" -> {
                if (t % 2 == 0) w.addParticle(ParticleTypes.ENCHANT, x + rx, y + 1.0 + Math.random(), z + rz, 0, 0.1, 0);
            }
            case "party" -> {
                if (t % 5 == 0) w.addParticle(ParticleTypes.NOTE, x, y + 2.2, z, Math.random(), 0, 0);
            }
            case "sick" -> {
                if (t % 4 == 0) w.addParticle(ParticleTypes.HAPPY_VILLAGER, x + fx * 0.3, y + 1.5, z + fz * 0.3, 0, 0, 0);
            }
            case "sleep" -> {
                if (t % 12 == 0) w.addParticle(ParticleTypes.CLOUD, x + fx * 0.3, y + 1.9, z + fz * 0.3, 0, 0.04, 0);
            }
            case "skull" -> {
                if (t % 6 == 0) w.addParticle(ParticleTypes.SOUL, x + rx, y + 1.0, z + rz, 0, 0.05, 0);
            }
            case "sword" -> {
                if (t % 11 == 5) w.addParticle(ParticleTypes.SWEEP_ATTACK, x + fx, y + 1.2, z + fz, 0, 0, 0);
            }
            case "gg" -> {
                if (t % 9 == 4) w.addParticle(ParticleTypes.CRIT, x + fx * 0.6, y + 1.2, z + fz * 0.6, 0, 0.1, 0);
            }
            case "creeper" -> {
                if (t == 45) w.addParticle(ParticleTypes.EXPLOSION, x, y + 1.0, z, 0, 0, 0);
            }
            default -> {}
        }
    }

    /** Wiadomość z czatu zawiera :nazwa: -> odpal animację u nadawcy. */
    public static void onMessage(String text, UUID senderOrNull) {
        Matcher m = code().matcher(text);
        if (!m.find()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientWorld world = mc.world;
        if (world == null) return;
        AbstractClientPlayerEntity p;
        if (senderOrNull != null) {
            p = world.getPlayers().stream().filter(x -> x.getUuid().equals(senderOrNull)).findFirst().orElse(null);
        } else {
            p = findByName(world, text);
        }
        if (p == null) return;
        String name = m.group(1);
        if (p == mc.player && consumeEcho(name)) return;   // własne echo już odegrane lokalnie
        start(p, name);
    }

    /** Użycie emotki z Hubu: animacja od razu; opcjonalnie sygnał dla innych graczy z modem. */
    public static void useEmote(String name, boolean share) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        start(mc.player, name);
        if (share) {
            ECHO.computeIfAbsent(name, k -> new ArrayDeque<>()).addLast(System.currentTimeMillis() + 5000);
            mc.player.networkHandler.sendChatMessage(":" + name + ":");
        }
    }

    private static boolean consumeEcho(String name) {
        ArrayDeque<Long> q = ECHO.get(name);
        if (q == null) return false;
        long now = System.currentTimeMillis();
        while (!q.isEmpty() && q.peekFirst() < now) q.pollFirst();
        if (q.isEmpty()) return false;
        q.pollFirst();
        return true;
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
        if (a == null && w == null) { FRAMES.remove(state); return; }   // stan renderu jest używany ponownie - czyścimy stare dane
        float now = p.age + td;
        float wt = w == null ? 0 : now - w;
        if (w != null && wt < 0) wt = WARDEN_ANIM;
        FRAMES.put(state, new Frame(a == null ? null : a.name(), a == null ? 0 : now - a.startAge(),
                w != null, wt, p.isSneaking(),
                p.limbAnimator.getPos(td), Math.min(1f, p.limbAnimator.getSpeed(td)), now));
    }
}
