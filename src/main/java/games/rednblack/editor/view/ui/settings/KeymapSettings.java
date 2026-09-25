package games.rednblack.editor.view.ui.settings;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextField;
import games.rednblack.editor.proxy.SettingsManager;
import games.rednblack.editor.utils.KeyBindingsLayout;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.view.SettingsNodeValue;
import games.rednblack.h2d.common.view.ui.PropertyGrid;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.h2d.common.vo.EditorConfigVO;
import games.rednblack.puremvc.Facade;

import java.util.Locale;

/**
 * The keyboard, in one place: which layout the editor reads its shortcuts from, and what every
 * action answers to right now.
 *
 * The list is there to be looked at - changing a binding means writing a {@code .keymap} file, which
 * is done by hand for the time being and picked up by the layout above.
 */
public class KeymapSettings extends SettingsNodeValue<EditorConfigVO> {

    private static final float NAME_WIDTH = 220f;
    private static final float CHIP_PAD_X = 6f;
    private static final float CHIP_PAD_Y = 1f;
    private static final float CHIP_GAP = 4f;
    private static final float ROW_PAD = 3f;
    private static final float GROUP_PAD_TOP = 9f;
    /** A key cap is dark, like the button the whole combination used to sit on. */
    private static final Color CHIP_COLOR = Color.valueOf("333333");

    private final VisSelectBox<String> layout;
    private final VisTextField filter;
    private final VisTable list = new VisTable();

    public KeymapSettings(Facade facade) {
        super("Keymap", facade);

        SettingsManager settingsManager = facade.retrieveProxy(SettingsManager.NAME);

        layout = StandardWidgetsFactory.createSelectBox(String.class);
        layout.setItems(settingsManager.getKeyMappingFiles());

        filter = StandardWidgetsFactory.createTextField();
        filter.setMessageText("Search an action or a key");
        filter.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                rebuildList();
            }
        });

        PropertyGrid grid = PropertyGrid.on(getContentTable()).dialogScale().sectionPad(SECTION_PAD_TOP, SECTION_PAD_BOTTOM);

        grid.section("Layout");
        grid.row("Key mapping", layout);

        grid.section("Shortcuts");
        grid.row("Search", filter);

        //the list is as long as the keyboard is: it scrolls by itself, under a search that stays put
        list.top().left();
        VisScrollPane listScrollPane = StandardWidgetsFactory.createScrollPane(list);
        listScrollPane.setFadeScrollBars(false);
        listScrollPane.setScrollingDisabled(true, false);
        grid.wideGrow(listScrollPane);

        rebuildList();
    }

    /** The bindings that match what is being searched for, under the name of their group. */
    private void rebuildList() {
        list.clearChildren();

        String needle = filter.getText() == null ? "" : filter.getText().trim().toLowerCase(Locale.ROOT);
        String shownGroup = null;
        boolean anything = false;

        for (KeyBindingsLayout.Binding binding : new Array.ArrayIterator<>(KeyBindingsLayout.getBindings())) {
            String shortcut = KeyBindingsLayout.getShortcutText(binding.action);
            if (!matches(binding, shortcut, needle)) continue;

            if (!binding.group.equals(shownGroup)) {
                shownGroup = binding.group;
                VisLabel group = new VisLabel(shownGroup, PropertyGrid.SECTION_STYLE_LARGE);
                list.add(group).left().colspan(2).padTop(anything ? GROUP_PAD_TOP : 0).row();
            }

            list.add(new VisLabel(binding.name, PropertyGrid.LABEL_STYLE_LARGE))
                    .left().width(NAME_WIDTH).padTop(ROW_PAD).padBottom(ROW_PAD);
            list.add(keys(binding.action)).right().expandX().padTop(ROW_PAD).padBottom(ROW_PAD).row();
            anything = true;
        }

        if (!anything) {
            list.add(new VisLabel("No shortcut goes by that name", PropertyGrid.LABEL_STYLE_LARGE)).left();
        }
    }

    private boolean matches(KeyBindingsLayout.Binding binding, String shortcut, String needle) {
        if (needle.isEmpty()) return true;

        return binding.name.toLowerCase(Locale.ROOT).contains(needle)
                || binding.group.toLowerCase(Locale.ROOT).contains(needle)
                || shortcut.toLowerCase(Locale.ROOT).contains(needle);
    }

    /** The combination as keys, one chip each, and the word between the ways of saying it. */
    private VisTable keys(int action) {
        VisTable keys = new VisTable();

        Array<String[]> variants = KeyBindingsLayout.getShortcutVariants(action);
        boolean first = true;
        for (int variant = 0; variant < variants.size; variant++) {
            if (variant > 0) {
                keys.add(new VisLabel("or", PropertyGrid.LABEL_STYLE_LARGE)).padLeft(CHIP_GAP);
            }
            for (String key : variants.get(variant)) {
                keys.add(chip(key)).padLeft(first ? 0 : CHIP_GAP);
                first = false;
            }
        }
        return keys;
    }

    /** One key, outlined the way a keyboard cap is drawn. */
    private VisTable chip(String key) {
        VisTable chip = new VisTable();
        chip.setBackground(new NinePatchDrawable(VisUI.getSkin().getPatch("key-chip")).tint(CHIP_COLOR));
        chip.add(new VisLabel(key, "menuitem-shortcut")).pad(CHIP_PAD_Y, CHIP_PAD_X, CHIP_PAD_Y, CHIP_PAD_X);
        return chip;
    }

    @Override
    public void translateSettingsToView() {
        layout.setSelected(getSettings().keyBindingLayout);
        rebuildList();
    }

    @Override
    public void translateViewToSettings() {
        getSettings().keyBindingLayout = layout.getSelected();
        facade.sendNotification(MsgAPI.SAVE_EDITOR_CONFIG);
    }

    @Override
    public boolean validateSettings() {
        return !getSettings().keyBindingLayout.equals(layout.getSelected());
    }

    /** The bindings are read once at startup, so a different layout only takes hold on the next one. */
    @Override
    public boolean requireRestart() {
        return !getSettings().keyBindingLayout.equals(layout.getSelected());
    }
}
