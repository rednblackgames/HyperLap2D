package games.rednblack.editor.view.ui.properties;

import com.badlogic.gdx.graphics.Color;
import com.kotcrab.vis.ui.util.InputValidator;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.kotcrab.vis.ui.widget.VisValidatableTextField;

import java.util.List;

/** Shared helpers for {@link RemoteEditablePanel} implementations (value conversion + widget validation). */
public final class RemoteEditableSupport {
    private RemoteEditableSupport() {}

    public static String numberToString(Object value) {
        if (value instanceof Number) return String.valueOf(((Number) value).floatValue());
        return value.toString();
    }

    public static int toInt(Object value) {
        if (value instanceof Number) return ((Number) value).intValue();
        return Integer.parseInt(value.toString());
    }

    /**
     * Integer-typed field value as a string. Numbers render without a trailing ".0" (so an int
     * Spinner's digit-only filter doesn't turn "16.0" into "160"); string values pass through
     * unchanged so the field's IntegerValidator can reject non-integers like "1.5".
     */
    public static String intToString(Object value) {
        if (value instanceof Number) return String.valueOf(((Number) value).intValue());
        return value.toString();
    }

    public static boolean toBool(Object value) {
        if (value instanceof Boolean) return (Boolean) value;
        return Boolean.parseBoolean(value.toString());
    }

    public static float toFloat(Object value) {
        if (value instanceof Number) return ((Number) value).floatValue();
        return Float.parseFloat(value.toString());
    }

    /** Parse a Vector3 from "x,y,z" or a [x,y,z] list. */
    public static com.badlogic.gdx.math.Vector3 toVector3(Object value) {
        if (value instanceof com.badlogic.gdx.math.Vector3) return (com.badlogic.gdx.math.Vector3) value;
        float[] v;
        if (value instanceof List) {
            List<?> l = (List<?>) value;
            v = new float[Math.min(3, l.size())];
            for (int i = 0; i < v.length; i++) {
                Object item = l.get(i);
                v[i] = item instanceof Number ? ((Number) item).floatValue() : Float.parseFloat(String.valueOf(item));
            }
        } else {
            String[] parts = value.toString().split(",");
            v = new float[parts.length];
            for (int i = 0; i < parts.length; i++) v[i] = Float.parseFloat(parts[i].trim());
        }
        return new com.badlogic.gdx.math.Vector3(v.length > 0 ? v[0] : 0, v.length > 1 ? v[1] : 0, v.length > 2 ? v[2] : 0);
    }

    public static boolean contains(VisSelectBox<String> box, String v) {
        for (String s : box.getItems()) {
            if (s.equals(v)) return true;
        }
        return false;
    }

    /** Runs the field's VisUI validators (the same ones the user-typing path uses) and records errors. */
    public static void checkValid(String key, VisTextField field, List<String> errors) {
        if (field instanceof VisValidatableTextField) {
            VisValidatableTextField v = (VisValidatableTextField) field;
            String text = v.getText();
            for (InputValidator validator : v.getValidators()) {
                if (!validator.validateInput(text)) {
                    errors.add("field '" + key + "' invalid value: '" + text + "'");
                    return;
                }
            }
        }
    }

    /**
     * Parse a color from "r,g,b,a" floats (0-1), a [r,g,b,a] list, or "#RRGGBB"/"#RRGGBBAA" hex.
     * Missing channels default to 0 (alpha to 1).
     */
    public static Color toColor(Object value) {
        if (value instanceof Color) return (Color) value;
        float[] rgba;
        if (value instanceof List) {
            List<?> l = (List<?>) value;
            rgba = new float[Math.min(4, l.size())];
            for (int i = 0; i < rgba.length; i++) {
                Object item = l.get(i);
                rgba[i] = item instanceof Number ? ((Number) item).floatValue() : Float.parseFloat(String.valueOf(item));
            }
        } else if (value instanceof String) {
            String s = ((String) value).trim();
            if (s.startsWith("#")) {
                String h = s.substring(1);
                if (h.length() == 6) h = h + "FF";
                if (h.length() != 8) throw new IllegalArgumentException("hex color must be #RRGGBB or #RRGGBBAA: " + s);
                return new Color(
                        Integer.parseInt(h.substring(0, 2), 16) / 255f,
                        Integer.parseInt(h.substring(2, 4), 16) / 255f,
                        Integer.parseInt(h.substring(4, 6), 16) / 255f,
                        Integer.parseInt(h.substring(6, 8), 16) / 255f);
            }
            String[] parts = s.split(",");
            rgba = new float[parts.length];
            for (int i = 0; i < parts.length; i++) rgba[i] = Float.parseFloat(parts[i].trim());
        } else {
            throw new IllegalArgumentException("color must be 'r,g,b,a', a [r,g,b,a] list, or '#RRGGBBAA' hex");
        }
        float r = rgba.length > 0 ? rgba[0] : 0f;
        float g = rgba.length > 1 ? rgba[1] : 0f;
        float b = rgba.length > 2 ? rgba[2] : 0f;
        float a = rgba.length > 3 ? rgba[3] : 1f;
        return new Color(r, g, b, a);
    }
}