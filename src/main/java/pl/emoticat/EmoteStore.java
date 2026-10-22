package pl.emoticat;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** Ulubione i ostatnio użyte emotki, zapisywane w config/emoticat.txt. */
public class EmoteStore {
    private static final Set<String> FAV = new LinkedHashSet<>();
    private static final LinkedList<String> RECENT = new LinkedList<>();
    private static boolean loaded = false;
    private static boolean share = true;

    public static boolean share() { return share; }

    public static void toggleShare() { share = !share; save(); }

    private static Path file() { return FabricLoader.getInstance().getConfigDir().resolve("emoticat.txt"); }

    public static void load() {
        if (loaded) return;
        loaded = true;
        try {
            Path f = file();
            if (!Files.exists(f)) return;
            for (String line : Files.readAllLines(f)) {
                int i = line.indexOf(':');
                if (i < 0) continue;
                String kind = line.substring(0, i), name = line.substring(i + 1).trim();
                if (kind.equals("share")) { share = name.equals("on"); continue; }
                if (Emotes.indexOf(name) < 0) continue;
                if (kind.equals("fav")) FAV.add(name);
                else if (kind.equals("recent")) RECENT.add(name);
            }
        } catch (Exception ignored) {}
    }

    private static void save() {
        try {
            List<String> lines = new ArrayList<>();
            lines.add("share:" + (share ? "on" : "off"));
            for (String n : FAV) lines.add("fav:" + n);
            for (String n : RECENT) lines.add("recent:" + n);
            Files.write(file(), lines);
        } catch (Exception ignored) {}
    }

    public static boolean isFav(String n) { return FAV.contains(n); }

    public static void toggleFav(String n) {
        if (!FAV.remove(n)) FAV.add(n);
        save();
    }

    public static void addRecent(String n) {
        RECENT.remove(n);
        RECENT.addFirst(n);
        while (RECENT.size() > 8) RECENT.removeLast();
        save();
    }

    public static List<String> recent() { return RECENT; }
}
