package games.rednblack.editor.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Array;
import org.apache.commons.lang3.ArrayUtils;

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

    private static final Array<KeyMapper> mapping = new Array<>();

    public static void init() {
        mapping.add(new KeyMapper(NEW_PROJECT, true, false, false, Input.Keys.N));
        mapping.add(new KeyMapper(OPEN_PROJECT, true, false, false, Input.Keys.O));
        mapping.add(new KeyMapper(SAVE_PROJECT, true, false, false, Input.Keys.S));
        mapping.add(new KeyMapper(EXPORT_PROJECT, true, false, false, Input.Keys.E));
        mapping.add(new KeyMapper(IMPORT_TO_LIBRARY, true, false, false, Input.Keys.I));

        mapping.add(new KeyMapper(OPEN_SETTINGS, true, true, false, Input.Keys.S));
        mapping.add(new KeyMapper(EXIT_APP, true, false, false, Input.Keys.Q));

        mapping.add(new KeyMapper(SELECTION_TOOL, false, false, false, Input.Keys.V, Input.Keys.ESCAPE));
        mapping.add(new KeyMapper(TRANSFORM_TOOL, true, false, false, Input.Keys.T));
        mapping.add(new KeyMapper(PAN_TOOL, false, false, false, Input.Keys.SPACE));

        mapping.add(new KeyMapper(ZOOM_PLUS, true, false, false, Input.Keys.MINUS, Input.Keys.SLASH));
        mapping.add(new KeyMapper(ZOOM_MINUS, true, false, false, Input.Keys.PLUS, Input.Keys.RIGHT_BRACKET));

        mapping.add(new KeyMapper(Z_INDEX_UP, true, false, false, Input.Keys.UP));
        mapping.add(new KeyMapper(Z_INDEX_DOWN, true, false, false, Input.Keys.DOWN));

        mapping.add(new KeyMapper(SELECT_ALL, true, false, false, Input.Keys.A));
        mapping.add(new KeyMapper(COPY, true, false, false, Input.Keys.C));
        mapping.add(new KeyMapper(CUT, true, false, false, Input.Keys.X));
        mapping.add(new KeyMapper(PASTE, true, false, false, Input.Keys.V));

        mapping.add(new KeyMapper(UNDO, true, false, false, Input.Keys.Z));
        mapping.add(new KeyMapper(REDO, true, false, true, Input.Keys.Z));

        mapping.add(new KeyMapper(RESET_CAMERA, true, false, false, Input.Keys.NUM_0, Input.Keys.NUMPAD_0));

        mapping.add(new KeyMapper(ALIGN_TOP, true, false, false, Input.Keys.NUM_1));
        mapping.add(new KeyMapper(ALIGN_LEFT, true, false, false, Input.Keys.NUM_2));
        mapping.add(new KeyMapper(ALIGN_BOTTOM, true, false, false, Input.Keys.NUM_3));
        mapping.add(new KeyMapper(ALIGN_RIGHT, true, false, false, Input.Keys.NUM_4));

        mapping.add(new KeyMapper(DELETE, false, false, false, Input.Keys.DEL));

        //Sort mapping in order to give priority to shortcuts that has more modifiers
        mapping.sort(new KeyMapperComparator());
    }

    public static int mapAction(int keyCode) {
        for (KeyMapper keyMapper : new Array.ArrayIterator<>(mapping)) {
            if (keyMapper.keyCodes.contains(keyCode)) {
                if (keyMapper.isControl) {
                    if (!Gdx.input.isKeyPressed(Input.Keys.SYM) && !Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
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
