package games.rednblack.editor.plugin.mcp.server;

import com.badlogic.gdx.utils.JsonWriter;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Thin helper over libGDX's {@link JsonWriter} for building small JSON strings (tool
 * result text content, input schemas) without hand-rolling escaping. All JSON output in
 * the plugin goes through {@link JsonWriter}; this just wraps the StringWriter boilerplate.
 */
public final class McpJson {
    private McpJson() {}

    @FunctionalInterface
    public interface JsonBuild {
        void build(JsonWriter w) throws IOException;
    }

    /** Build a JSON object string. The consumer is called inside the (already-opened) root object. */
    public static String object(JsonBuild b) {
        return build(true, b);
    }

    /** Build a JSON array string. The consumer is called inside the (already-opened) root array. */
    public static String array(JsonBuild b) {
        return build(false, b);
    }

    private static String build(boolean obj, JsonBuild b) {
        StringWriter sw = new StringWriter();
        try (JsonWriter w = new JsonWriter(sw)) {
            if (obj) w.object(); else w.array();
            b.build(w);
            w.pop();
        } catch (IOException e) {
            throw new RuntimeException("JSON build failed", e);
        }
        return sw.toString();
    }
}