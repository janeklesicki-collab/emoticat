package pl.emoticat;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

/** EmotiCat Hub: zakładki, wyszukiwarka, ulubione (PPM) i ostatnio użyte. */
public class EmoteScreen extends Screen {
    private enum Tab {
        ALL("Wszystkie"), FACES("Twarze"), SYMBOLS("Symbole"), ANIM("Animacje"), FAV("Ulubione");
        final String label;
        Tab(String l) { label = l; }
    }

    private record Cell(String name, int x, int y, int size) {}

    private static final int CELL = 28, COLS = 8;
    private static Tab tab = Tab.ALL;
    private static String query = "";

    private TextFieldWidget search;
    private final List<Cell> cells = new ArrayList<>();
    private boolean hasRecent;
    private int recentY, gridBottom, gridX;
    private boolean empty;

    public EmoteScreen() {
        super(Text.literal("EmotiCat Hub"));
        EmoteStore.load();
    }

    @Override
    protected void init() {
        Tab[] tabs = Tab.values();
        int tw = 64;
        int tx = (width - tabs.length * tw) / 2;
        for (int i = 0; i < tabs.length; i++) {
            Tab t = tabs[i];
            Text label = Text.literal(t.label).formatted(t == tab ? Formatting.YELLOW : Formatting.WHITE);
            addDrawableChild(ButtonWidget.builder(label, b -> { tab = t; clearAndInit(); })
                    .dimensions(tx + i * tw, 26, tw - 2, 18).build());
        }
        search = new TextFieldWidget(textRenderer, width / 2 - 80, 50, 160, 16, Text.literal("Szukaj"));
        search.setPlaceholder(Text.literal("Szukaj emotki..."));
        search.setText(query);
        search.setChangedListener(s -> { query = s; layout(); });
        addDrawableChild(search);
        addDrawableChild(ButtonWidget.builder(shareLabel(), b -> {
            EmoteStore.toggleShare();
            b.setMessage(shareLabel());
        }).dimensions(width - 142, 6, 136, 16).build());
        layout();
    }

    private Text shareLabel() {
        return Text.literal("Pokaż innym: " + (EmoteStore.share() ? "TAK" : "NIE"));
    }

    private boolean matchesTab(String n) {
        return switch (tab) {
            case ALL -> true;
            case FACES -> Emotes.isFace(n);
            case SYMBOLS -> Emotes.isSymbol(n);
            case ANIM -> Emotes.isAnim(n);
            case FAV -> EmoteStore.isFav(n);
        };
    }

    private void layout() {
        cells.clear();
        String q = query.toLowerCase().trim();
        List<String> view = new ArrayList<>();
        for (String n : Emotes.NAMES) {
            if (!matchesTab(n)) continue;
            if (!q.isEmpty() && !n.contains(q)) continue;
            view.add(n);
        }
        int y = 76;
        gridX = (width - COLS * CELL) / 2;
        hasRecent = tab == Tab.ALL && q.isEmpty() && !EmoteStore.recent().isEmpty();
        if (hasRecent) {
            recentY = y;
            List<String> rec = EmoteStore.recent();
            for (int i = 0; i < rec.size(); i++) cells.add(new Cell(rec.get(i), gridX + 60 + i * 20, y - 2, 18));
            y += 26;
        }
        for (int i = 0; i < view.size(); i++) {
            cells.add(new Cell(view.get(i), gridX + (i % COLS) * CELL, y + (i / COLS) * CELL, CELL));
        }
        int rows = (view.size() + COLS - 1) / COLS;
        gridBottom = y + Math.max(rows, 1) * CELL;
        empty = view.isEmpty();
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);   // tło + przyciski + wyszukiwarka
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, 10, 0xFFFFFF);

        if (hasRecent) ctx.drawTextWithShadow(textRenderer, "Ostatnio:", gridX, recentY + 2, 0xAAAAAA);

        String hover = null;
        for (Cell c : cells) {
            boolean h = mx >= c.x() && mx < c.x() + c.size() && my >= c.y() && my < c.y() + c.size();
            if (h) {
                ctx.fill(c.x(), c.y(), c.x() + c.size() - 2, c.y() + c.size() - 2, 0x80FFFFFF);
                hover = c.name();
            }
            int icon = c.size() == CELL ? 22 : 16;
            int off = (c.size() - 2 - icon) / 2;
            ctx.drawTexture(RenderLayer::getGuiTextured, Emotes.texture(c.name()),
                    c.x() + off, c.y() + off, 0f, 0f, icon, icon, 32, 32, 32, 32);
            if (c.size() == CELL && EmoteStore.isFav(c.name())) {
                ctx.drawText(textRenderer, "★", c.x() + CELL - 10, c.y() + 1, 0xFFFF55, true);
            }
        }

        if (empty) {
            String msg = tab == Tab.FAV ? "Brak ulubionych. Kliknij emotkę prawym przyciskiem." : "Nic nie znaleziono.";
            ctx.drawCenteredTextWithShadow(textRenderer, msg, width / 2, gridBottom - 14, 0xAAAAAA);
        }

        int fy = gridBottom + 8;
        if (hover != null) {
            String extra = Emotes.isAnim(hover) ? "  (animacja)" : "";
            ctx.drawCenteredTextWithShadow(textRenderer, ":" + hover + ":" + extra, width / 2, fy, 0xFFFF55);
        }
        ctx.drawCenteredTextWithShadow(textRenderer,
                "Klik: użyj od razu  |  PPM: ulubione  |  Shift+klik: wstaw do czatu", width / 2, fy + 14, 0xAAAAAA);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        for (Cell c : cells) {
            if (mx >= c.x() && mx < c.x() + c.size() && my >= c.y() && my < c.y() + c.size()) {
                if (button == 1) {
                    EmoteStore.toggleFav(c.name());
                    layout();
                    return true;
                }
                if (button == 0) {
                    EmoteStore.addRecent(c.name());
                    if (hasShiftDown()) {
                        client.setScreen(new ChatScreen(":" + c.name() + ": "));
                    } else {
                        client.setScreen(null);
                        EmoteManager.useEmote(c.name(), EmoteStore.share());
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean typing = search != null && search.isFocused();
        if (!typing && EmotiCatMod.menuKey.matchesKey(keyCode, scanCode)) { close(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() { EmotiCatMod.pending = ""; super.close(); }

    @Override
    public boolean shouldPause() { return false; }
}
