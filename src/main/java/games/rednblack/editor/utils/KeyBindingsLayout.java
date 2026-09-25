package games.rednblack.editor.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.ObjectMap;
import games.rednblack.editor.proxy.SettingsManager;
import games.rednblack.puremvc.Facade;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class KeyBindingsLayout {
    public static final int NEW_PROJECT = 0;
    public static final int OPEN_PROJECT = 1;
    public static final int SAVE_PROJECT = 2;
    public static final int EXPORT_PROJECT = 3;
    public static final int IMPORT_TO_LIBRARY = 4;

    public static final int OPEN_SETTINGS = 5;
    public static final int EXIT_APP = 6;

    public static final int SELECTION_TOOL = 7;
    public static final int TRANSFORM_TOOL = 8;
    public static final int PAN_TOOL = 9;

    public static final int ZOOM_PLUS = 10;
    public static final int ZOOM_MINUS = 11;

    public static final int Z_INDEX_UP = 12;
    public static final int Z_INDEX_DOWN = 13;

    public static final int SELECT_ALL = 14;
    public static final int COPY = 15;
    public static final int CUT = 16;
    public static final int PASTE = 17;

    public static final int UNDO = 18;
    public static final int REDO = 19;

    public static final int RESET_CAMERA = 20;

    public static final int ALIGN_LEFT = 21;
    public static final int ALIGN_TOP = 22;
    public static final int ALIGN_RIGHT = 23;
    public static final int ALIGN_BOTTOM = 24;

    public static final int DELETE = 25;

    public static final int HIDE_GUI = 26;
    public static final int OPEN_CONSOLE = 27;

    public static final int SAVE_PROJECT_AS = 28;

    public static final int TOGGLE_FULL_SCREEN = 29;

    public static final int SHOW_MINI_MAP = 30;

    private static final ObjectMap<Integer, KeyMapper> defaultMapper = new ObjectMap<>();
    static {
        defaultMapper.put(NEW_PROJECT, new KeyMapper(NEW_PROJECT, true, false, false, Input.Keys.N));
        defaultMapper.put(OPEN_PROJECT, new KeyMapper(OPEN_PROJECT, true, false, false, Input.Keys.O));
        defaultMapper.put(SAVE_PROJECT, new KeyMapper(SAVE_PROJECT, true, false, false, Input.Keys.S));
        defaultMapper.put(SAVE_PROJECT_AS, new KeyMapper(SAVE_PROJECT_AS, true, false, true, Input.Keys.S));
        defaultMapper.put(EXPORT_PROJECT, new KeyMapper(EXPORT_PROJECT, true, false, false, Input.Keys.E));
        defaultMapper.put(IMPORT_TO_LIBRARY, new KeyMapper(IMPORT_TO_LIBRARY, true, false, false, Input.Keys.I));

        defaultMapper.put(OPEN_SETTINGS, new KeyMapper(OPEN_SETTINGS, true, true, false, Input.Keys.S));
        defaultMapper.put(EXIT_APP, new KeyMapper(EXIT_APP, true, false, false, Input.Keys.Q));

        defaultMapper.put(SELECTION_TOOL, new KeyMapper(SELECTION_TOOL, false, false, false, Input.Keys.V));
        defaultMapper.put(TRANSFORM_TOOL, new KeyMapper(TRANSFORM_TOOL, true, false, false, Input.Keys.T));
        defaultMapper.put(PAN_TOOL, new KeyMapper(PAN_TOOL, false, false, false, Input.Keys.SPACE));

        defaultMapper.put(ZOOM_PLUS, new KeyMapper(ZOOM_PLUS, true, false, false, Input.Keys.MINUS, Input.Keys.SLASH));
        defaultMapper.put(ZOOM_MINUS, new KeyMapper(ZOOM_MINUS, true, false, false, Input.Keys.PLUS, Input.Keys.RIGHT_BRACKET));

        defaultMapper.put(Z_INDEX_UP, new KeyMapper(Z_INDEX_UP, true, false, false, Input.Keys.UP));
        defaultMapper.put(Z_INDEX_DOWN, new KeyMapper(Z_INDEX_DOWN, true, false, false, Input.Keys.DOWN));

        defaultMapper.put(SELECT_ALL, new KeyMapper(SELECT_ALL, true, false, false, Input.Keys.A));
        defaultMapper.put(COPY, new KeyMapper(COPY, true, false, false, Input.Keys.C));
        defaultMapper.put(CUT, new KeyMapper(CUT, true, false, false, Input.Keys.X));
        defaultMapper.put(PASTE, new KeyMapper(PASTE, true, false, false, Input.Keys.V));

        defaultMapper.put(UNDO, new KeyMapper(UNDO, true, false, false, Input.Keys.Z));
        defaultMapper.put(REDO, new KeyMapper(REDO, true, false, true, Input.Keys.Z));

        defaultMapper.put(RESET_CAMERA, new KeyMapper(RESET_CAMERA, true, false, false, Input.Keys.NUM_0, Input.Keys.NUMPAD_0));

        defaultMapper.put(ALIGN_TOP, new KeyMapper(ALIGN_TOP, true, false, false, Input.Keys.NUMPAD_8));
        defaultMapper.put(ALIGN_LEFT, new KeyMapper(ALIGN_LEFT, true, false, false, Input.Keys.NUMPAD_4));
        defaultMapper.put(ALIGN_BOTTOM, new KeyMapper(ALIGN_BOTTOM, true, false, false, Input.Keys.NUMPAD_2));
        defaultMapper.put(ALIGN_RIGHT, new KeyMapper(ALIGN_RIGHT, true, false, false, Input.Keys.NUMPAD_6));

        defaultMapper.put(DELETE, new KeyMapper(DELETE, false, false, false, Input.Keys.DEL, Input.Keys.FORWARD_DEL));

        defaultMapper.put(HIDE_GUI, new KeyMapper(HIDE_GUI, false, false, false, Input.Keys.F12));
        defaultMapper.put(OPEN_CONSOLE, new KeyMapper(OPEN_CONSOLE, false, false, false, Input.Keys.F10));
        defaultMapper.put(TOGGLE_FULL_SCREEN, new KeyMapper(TOGGLE_FULL_SCREEN, false, false, false, Input.Keys.F11));
        defaultMapper.put(SHOW_MINI_MAP, new KeyMapper(SHOW_MINI_MAP, false, true, false, Input.Keys.M));

        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.minimal);
    }

    /** What the settings panel calls an action, and the group it is shown under. */
    public static class Binding {
        public final int action;
        public final String group;
        public final String name;

        Binding(int action, String group, String name) {
            this.action = action;
            this.group = group;
            this.name = name;
        }
    }

    /**
     * Every action worth showing, in the order the keymap panel reads them. Actions are bare
     * numbers everywhere else, so this is the one place that says what they are called.
     */
    private static final Array<Binding> bindings = new Array<>();
    static {
        bindings.add(new Binding(NEW_PROJECT, "File", "New project"));
        bindings.add(new Binding(OPEN_PROJECT, "File", "Open project"));
        bindings.add(new Binding(SAVE_PROJECT, "File", "Save project"));
        bindings.add(new Binding(SAVE_PROJECT_AS, "File", "Save project as"));
        bindings.add(new Binding(EXPORT_PROJECT, "File", "Export project"));
        bindings.add(new Binding(IMPORT_TO_LIBRARY, "File", "Import to library"));
        bindings.add(new Binding(EXIT_APP, "File", "Exit"));

        bindings.add(new Binding(UNDO, "Edit", "Undo"));
        bindings.add(new Binding(REDO, "Edit", "Redo"));
        bindings.add(new Binding(SELECT_ALL, "Edit", "Select all"));
        bindings.add(new Binding(COPY, "Edit", "Copy"));
        bindings.add(new Binding(CUT, "Edit", "Cut"));
        bindings.add(new Binding(PASTE, "Edit", "Paste"));
        bindings.add(new Binding(DELETE, "Edit", "Delete"));

        bindings.add(new Binding(SELECTION_TOOL, "Tools", "Selection tool"));
        bindings.add(new Binding(TRANSFORM_TOOL, "Tools", "Transform tool"));
        bindings.add(new Binding(PAN_TOOL, "Tools", "Pan tool"));

        bindings.add(new Binding(Z_INDEX_UP, "Arrange", "Move up one layer"));
        bindings.add(new Binding(Z_INDEX_DOWN, "Arrange", "Move down one layer"));
        bindings.add(new Binding(ALIGN_LEFT, "Arrange", "Align left"));
        bindings.add(new Binding(ALIGN_TOP, "Arrange", "Align top"));
        bindings.add(new Binding(ALIGN_RIGHT, "Arrange", "Align right"));
        bindings.add(new Binding(ALIGN_BOTTOM, "Arrange", "Align bottom"));

        bindings.add(new Binding(ZOOM_PLUS, "View", "Zoom in"));
        bindings.add(new Binding(ZOOM_MINUS, "View", "Zoom out"));
        bindings.add(new Binding(RESET_CAMERA, "View", "Reset camera"));
        bindings.add(new Binding(SHOW_MINI_MAP, "View", "Mini map"));
        bindings.add(new Binding(TOGGLE_FULL_SCREEN, "View", "Full screen"));

        bindings.add(new Binding(OPEN_SETTINGS, "Window", "Settings"));
        bindings.add(new Binding(OPEN_CONSOLE, "Window", "Console"));
        bindings.add(new Binding(HIDE_GUI, "Window", "Widget preview mode"));
    }

    /** Every action the keymap panel lists, grouped and named, in reading order. */
    public static Array<Binding> getBindings() {
        return bindings;
    }

    /**
     * The combination an action answers to, split into the keys a keyboard has: one array per way of
     * saying it, each holding the modifiers and then the key, so a panel can draw them one chip each.
     */
    public static Array<String[]> getShortcutVariants(int action) {
        Array<String[]> variants = new Array<>();
        KeyMapper keyMapper = mapperOf(action);
        if (keyMapper == null) return variants;

        for (int keyCode : keyMapper.keyCodes) {
            Array<String> keys = new Array<>();
            if (keyMapper.isControl) keys.add(getControlLabel());
            if (keyMapper.isAlt) keys.add("Alt");
            if (keyMapper.isShift) keys.add("Shift");
            keys.add(keyLabel(keyCode));
            variants.add(keys.toArray(String.class));
        }
        return variants;
    }

    /** The same combination as one line, for a menu entry or for searching through the list. */
    public static String getShortcutText(int action) {
        StringBuilder text = new StringBuilder();
        for (String[] variant : new Array.ArrayIterator<>(getShortcutVariants(action))) {
            if (text.length() > 0) text.append(" or ");
            for (int i = 0; i < variant.length; i++) {
                if (i > 0) text.append(" ");
                text.append(variant[i]);
            }
        }
        return text.toString();
    }

    /** The name of the modifier {@link #mapAction} looks for: the command key on a Mac. */
    public static String getControlLabel() {
        return SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX ? "Cmd" : "Ctrl";
    }

    /**
     * What is written on the key. libGDX spells the punctuation out and calls backspace a delete, so
     * the ones a keyboard shows as a symbol are said here and the rest are left to it.
     */
    private static String keyLabel(int keyCode) {
        switch (keyCode) {
            case Input.Keys.PLUS: return "+";
            case Input.Keys.MINUS: return "-";
            case Input.Keys.EQUALS: return "=";
            case Input.Keys.STAR: return "*";
            case Input.Keys.SLASH: return "/";
            case Input.Keys.BACKSLASH: return "\\";
            case Input.Keys.LEFT_BRACKET: return "[";
            case Input.Keys.RIGHT_BRACKET: return "]";
            case Input.Keys.SEMICOLON: return ";";
            case Input.Keys.APOSTROPHE: return "'";
            case Input.Keys.COMMA: return ",";
            case Input.Keys.PERIOD: return ".";
            case Input.Keys.GRAVE: return "`";
            //libGDX shares one code between backspace and delete, and it is backspace on a desktop
            case Input.Keys.DEL: return "Backspace";
            case Input.Keys.FORWARD_DEL: return "Delete";
            default: return Input.Keys.toString(keyCode);
        }
    }

    /** The bindings as they are now, falling back to the defaults before {@link #init()} has run. */
    private static KeyMapper mapperOf(int action) {
        for (KeyMapper keyMapper : new Array.ArrayIterator<>(mapping)) {
            if (keyMapper.action == action) return keyMapper;
        }
        return defaultMapper.get(action);
    }

    private static final Array<KeyMapper> mapping = new Array<>();

    public static void init() {
        SettingsManager settingsManager = Facade.getInstance().retrieveProxy(SettingsManager.NAME);
        if (!settingsManager.editorConfigVO.keyBindingLayout.equals("default")) {
            String mapPath = HyperLap2DUtils.getKeyMapPath() + File.separator + settingsManager.editorConfigVO.keyBindingLayout + ".keymap";
            File mapFile = new File(mapPath);
            if (mapFile.exists()) {
                Json json = new Json();
                try {
                    String mapJson = FileUtils.readFileToString(mapFile, "utf-8");
                    ObjectMap<String, KeyMapper> customBindings = json.fromJson(ObjectMap.class, mapJson);
                    for (String key : new ObjectMap.Keys<>(customBindings)) {
                        int action = Integer.parseInt(key);
                        defaultMapper.put(action, customBindings.get(key));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        for (KeyMapper keyMapper : new ObjectMap.Values<>(defaultMapper)) {
            mapping.add(keyMapper);
        }

        //Sort mapping in order to give priority to shortcuts that has more modifiers
        mapping.sort(new KeyMapperComparator());
    }

    public static int mapAction(int keyCode) {
        for (KeyMapper keyMapper : new Array.ArrayIterator<>(mapping)) {
            if (keyMapper.keyCodes.contains(keyCode)) {
                if (keyMapper.isControl) {
                    if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) {
                        if (!Gdx.input.isKeyPressed(Input.Keys.SYM))
                            continue;
                    } else if (!Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
                            && !Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
                        continue;
                    }
                }

                if (keyMapper.isShift) {
                    if (!Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && !Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
                        continue;
                    }
                }

                if (keyMapper.isAlt) {
                    if (!Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) && !Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT)) {
                        continue;
                    }
                }

                return keyMapper.action;
            }
        }
        return -1;
    }

    public static int[] getShortcutList(int action) {
        for (KeyMapper keyMapper : new Array.ArrayIterator<>(mapping)) {
            if (keyMapper.action == action) {
                List<Integer> shortcut = new ArrayList<>();
                if (keyMapper.isControl) {
                    shortcut.add(Input.Keys.CONTROL_LEFT);
                }
                if (keyMapper.isAlt) {
                    shortcut.add(Input.Keys.ALT_LEFT);
                }
                if (keyMapper.isShift) {
                    shortcut.add(Input.Keys.SHIFT_LEFT);
                }
                shortcut.add(keyMapper.keyCodes.get(0));
                return ArrayUtils.toPrimitive(shortcut.toArray(new Integer[0]));
            }
        }
        return new int[0];
    }

    private static class KeyMapper {
        boolean isControl, isAlt, isShift;
        List<Integer> keyCodes = new ArrayList<>();
        int action;

        public KeyMapper() {

        }

        /**
         * KeyMapper object to map an action to a key combination
         *
         * @param action    id of the mapped action
         * @param isControl true if needs CONTROL_LEFT, CONTROL_RIGHT or SYM pressed
         * @param isAlt     true if needs ALT_LEFT or ALT_RIGHT pressed
         * @param isShift   true if needs SHIFT_LEFT or SHIFT_RIGHT
         * @param keyCodes  use first key as main key code and other for variants
         */
        public KeyMapper(int action, boolean isControl, boolean isAlt, boolean isShift, int... keyCodes) {
            this.isControl = isControl;
            this.isShift = isShift;
            this.isAlt = isAlt;
            for (int keycode : keyCodes)
                this.keyCodes.add(keycode);
            this.action = action;
        }
    }

    private static class KeyMapperComparator implements Comparator<KeyMapper> {
        @Override
        public int compare(KeyMapper o1, KeyMapper o2) {
            int mod1 = 0, mod2 = 0;
            if (o1.isControl)
                mod1++;
            if (o2.isControl)
                mod2++;
            if (o1.isShift)
                mod1++;
            if (o2.isShift)
                mod2++;
            if (o1.isAlt)
                mod1++;
            if (o2.isAlt)
                mod2++;
            return mod2 - mod1;
        }
    }
}
