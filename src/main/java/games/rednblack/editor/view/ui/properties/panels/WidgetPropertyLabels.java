package games.rednblack.editor.view.ui.properties.panels;

import com.badlogic.gdx.utils.ObjectMap;

import static games.rednblack.editor.renderer.widget.WidgetTypes.*;

/**
 * How the widget panel names the settings a widget type declares. The label column is narrow, so names
 * spelled out from the key that would not fit get a shorter one here, and the scroll pane's many settings
 * are split in sections whose title carries the word the rows then leave out. A key with no entry is
 * spelled out as it is, under the plain settings section.
 */
final class WidgetPropertyLabels {

    static final String SETTINGS = "Settings";
    static final String SCROLLING = "Scrolling";
    static final String OVERSCROLL = "Overscroll";
    static final String SCROLL_BARS = "Scroll Bars";

    private static final ObjectMap<String, String> LABELS = new ObjectMap<>();
    private static final ObjectMap<String, String> SECTIONS = new ObjectMap<>();

    static {
        put(PROPERTY_SCROLL_DISABLED_X, SCROLLING, "Disabled X");
        put(PROPERTY_SCROLL_DISABLED_Y, SCROLLING, "Disabled Y");
        put(PROPERTY_FORCE_SCROLL_X, SCROLLING, "Force X");
        put(PROPERTY_FORCE_SCROLL_Y, SCROLLING, "Force Y");
        put(PROPERTY_CLAMP, SCROLLING, null);
        put(PROPERTY_FLICK_SCROLL, SCROLLING, null);
        put(PROPERTY_FLICK_TAP_SQUARE, SCROLLING, null);
        put(PROPERTY_FLING_TIME, SCROLLING, null);
        put(PROPERTY_SMOOTH_SCROLLING, SCROLLING, null);
        put(PROPERTY_MOUSE_WHEEL_X, SCROLLING, null);
        put(PROPERTY_MOUSE_WHEEL_Y, SCROLLING, null);

        put(PROPERTY_OVERSCROLL, OVERSCROLL, "Enabled");
        put(PROPERTY_OVERSCROLL_DISTANCE, OVERSCROLL, "Distance");
        put(PROPERTY_OVERSCROLL_SPEED_MIN, OVERSCROLL, "Min Speed");
        put(PROPERTY_OVERSCROLL_SPEED_MAX, OVERSCROLL, "Max Speed");

        put(PROPERTY_FADE_SCROLL_BARS, SCROLL_BARS, "Fade");
        put(PROPERTY_FADE_DELAY, SCROLL_BARS, null);
        put(PROPERTY_FADE_DURATION, SCROLL_BARS, null);
        put(PROPERTY_VARIABLE_SIZE_KNOBS, SCROLL_BARS, "Variable Knobs");
        put(PROPERTY_SCROLL_BAR_TOUCH, SCROLL_BARS, "Touchable");
        put(PROPERTY_BARS_ON_RIGHT, SCROLL_BARS, "On Right");
        put(PROPERTY_BARS_ON_BOTTOM, SCROLL_BARS, "On Bottom");

        put(PROPERTY_ANIMATE_INTERPOLATION, null, "Interpolation");
        put(PROPERTY_ANIMATE_DURATION, null, "Anim. Duration");
        put(PROPERTY_PASSWORD_CHARACTER, null, "Password Char");
        put(PROPERTY_CURSOR_BLINK_TIME, null, "Cursor Blink");
        put(PROPERTY_SELECT_ALL_ON_FOCUS, null, "Select On Focus");
        put(PROPERTY_KEEP_WITHIN_VIEWPORT, null, "Keep In View");
        put(PROPERTY_CLOSE_ON_BACKDROP_TAP, null, "Backdrop Closes");
        put(PROPERTY_CLOSE_ON_ESCAPE, null, "Escape Closes");
        put(PROPERTY_CLOSE_ON_TOUCH_OUTSIDE, null, "Outside Closes");
        put(PROPERTY_MAX_LIST_COUNT, null, "Visible Items");
    }

    private WidgetPropertyLabels() {
    }

    private static void put(String key, String section, String label) {
        if (section != null) SECTIONS.put(key, section);
        if (label != null) LABELS.put(key, label);
    }

    static String label(String key) {
        String label = LABELS.get(key);
        return label != null ? label : UIWidgetPartProperties.label(key);
    }

    static String section(String key) {
        return SECTIONS.get(key, SETTINGS);
    }
}
