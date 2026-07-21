package games.rednblack.editor.plugin.mcp.tools;

import com.badlogic.gdx.utils.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Result of a tool call: one or more content parts (text or image), plus an error flag.
 * Serialized into the JSON-RPC "result" object via {@link #write(JsonWriter)} using libGDX's
 * JsonWriter (no hand-rolled JSON).
 */
public class McpToolResult {
    public boolean isError;
    public final List<Content> content = new ArrayList<>();

    public static McpToolResult text(String text) {
        McpToolResult r = new McpToolResult();
        r.content.add(new Content("text", text, null, null));
        return r;
    }

    public static McpToolResult image(byte[] png, int width, int height) {
        McpToolResult r = new McpToolResult();
        r.content.add(new Content("image", null, Base64.getEncoder().encodeToString(png), "image/png"));
        return r;
    }

    public static McpToolResult error(String message) {
        McpToolResult r = new McpToolResult();
        r.isError = true;
        r.content.add(new Content("text", message, null, null));
        return r;
    }

    /** A result with a text part (e.g. dimensions) followed by an image part (a PNG). */
    public static McpToolResult textAndImage(String text, byte[] png) {
        McpToolResult r = new McpToolResult();
        r.content.add(new Content("text", text, null, null));
        r.content.add(new Content("image", null, Base64.getEncoder().encodeToString(png), "image/png"));
        return r;
    }

    /**
     * Write {@code {content:[...], isError:bool}} into the given writer, which must be
     * positioned inside the JSON-RPC "result" object.
     */
    public void write(JsonWriter w) throws IOException {
        w.array("content");
        for (Content c : content) {
            w.object();
            if ("text".equals(c.type)) {
                w.set("type", "text");
                w.set("text", c.text);
            } else if ("image".equals(c.type)) {
                w.set("type", "image");
                w.set("data", c.data);
                w.set("mimeType", c.mimeType);
            }
            w.pop();
        }
        w.pop();
        w.set("isError", isError);
    }

    public static class Content {
        public final String type;
        public final String text;
        public final String data;     // base64, for image
        public final String mimeType; // for image
        Content(String type, String text, String data, String mimeType) {
            this.type = type;
            this.text = text;
            this.data = data;
            this.mimeType = mimeType;
        }
    }
}