package pl.emoticat;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Emotes {
    /**
     * LISTA EMOTEK. Żeby dodać nową:
     * 1) dopisz nazwę na końcu tej tablicy (nie zmieniaj kolejności istniejących!)
     * 2) wrzuć textures/font/<nazwa>.png (32x32) do assets/emoticat/
     * 3) dopisz wpis w assets/emoticat/font/emotes.json z kolejnym znakiem \uE0xx
     */
    public static final String[] NAMES = {
        "lol", "smile", "sad", "cry", "angry", "love", "cool", "wow", "think", "sleep",
        "party", "skull", "wink", "sick", "clown", "heart", "fire", "star", "diamond",
        "creeper", "sword", "gg", "ez",
        "luzer", "kosz", "warden"
    };

    public static final Identifier FONT = Identifier.of("emoticat", "emotes");
    private static final Pattern CODE = Pattern.compile(":([a-z0-9_]+):");

    private static final Set<String> FACES = Set.of("lol", "smile", "sad", "cry", "angry", "love", "cool",
            "wow", "think", "sleep", "party", "skull", "wink", "sick", "clown");
    private static final Set<String> ANIM = Set.of("luzer", "kosz", "warden");

    public static boolean isFace(String n) { return FACES.contains(n); }
    public static boolean isAnim(String n) { return ANIM.contains(n); }
    public static boolean isSymbol(String n) { return !isFace(n) && !isAnim(n); }

    public static Identifier texture(String name) {
        return Identifier.of("emoticat", "textures/font/" + name + ".png");
    }

    public static int indexOf(String name) {
        for (int i = 0; i < NAMES.length; i++) if (NAMES[i].equals(name)) return i;
        return -1;
    }

    /** Zamienia :nazwa: na ikonkę (własna czcionka). Bez moda gracze widzą zwykły tekst. */
    public static Text convert(Text in) {
        MutableText out = Text.empty();
        boolean[] changed = {false};
        in.<Void>visit((style, str) -> {
            Matcher m = CODE.matcher(str);
            int last = 0;
            while (m.find()) {
                int idx = indexOf(m.group(1));
                if (idx < 0) continue;
                if (m.start() > last) out.append(Text.literal(str.substring(last, m.start())).setStyle(style));
                out.append(Text.literal(String.valueOf((char) (0xE000 + idx))).setStyle(style.withFont(FONT)));
                last = m.end();
                changed[0] = true;
            }
            if (last < str.length()) out.append(Text.literal(str.substring(last)).setStyle(style));
            return Optional.empty();
        }, Style.EMPTY);
        return changed[0] ? out : in;
    }
}
