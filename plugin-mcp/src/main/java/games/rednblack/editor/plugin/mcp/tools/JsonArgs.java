package games.rednblack.editor.plugin.mcp.tools;

import com.badlogic.gdx.utils.JsonValue;

import java.util.HashMap;
import java.util.Map;

/** Converts a JSON object (from MCP tool arguments) to a Map<String, Object> for RemoteEditRequest.fields. */
public final class JsonArgs {
    private JsonArgs() {}

    public static Map<String, Object> toFieldMap(JsonValue object) {
        Map<String, Object> m = new HashMap<>();
        if (object == null || !object.isObject()) return m;
        for (JsonValue child = object.child; child != null; child = child.next) {
            m.put(child.name, toValue(child));
        }
        return m;
    }

    private static Object toValue(JsonValue v) {
        if (v.isBoolean()) return v.asBoolean();
        if (v.isLong() || v.isDouble()) return v.asFloat();
        if (v.isNull()) return null;
        if (v.isArray()) {
            java.util.List<Object> list = new java.util.ArrayList<>();
            for (JsonValue item = v.child; item != null; item = item.next) list.add(toValue(item));
            return list;
        }
        return v.asString();
    }
}