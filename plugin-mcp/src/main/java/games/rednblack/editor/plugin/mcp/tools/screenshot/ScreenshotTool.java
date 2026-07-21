package games.rednblack.editor.plugin.mcp.tools.screenshot;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import games.rednblack.editor.plugin.mcp.tools.McpContext;
import games.rednblack.editor.plugin.mcp.tools.McpToolResult;
import games.rednblack.editor.plugin.mcp.tools.RemoteOps;
import games.rednblack.editor.plugin.mcp.tools.Tool;
import games.rednblack.h2d.common.remote.RemoteScreenshotRequest;
import games.rednblack.h2d.common.remote.RemoteScreenshotResult;

import java.io.IOException;

/**
 * Capture a PNG screenshot of the current scene and return it as MCP image content.
 * Modes: whole (all entities, regardless of viewport), view (current editor camera view),
 * region (a world-space rectangle x/y/width/height). Huge scenes are capped to the GPU's
 * max texture size.
 */
public class ScreenshotTool implements Tool {
    private final RemoteOps remote;

    public ScreenshotTool(McpContext ctx) {
        this.remote = new RemoteOps(ctx);
    }

    @Override public String name() { return "screenshot"; }
    @Override public String description() {
        return "Capture a PNG screenshot of the current scene. mode: 'whole' (all entities, regardless of "
                + "viewport), 'view' (what the editor camera currently shows), or 'region' (a world-space "
                + "rectangle; pass x, y, width, height in world units). Returned as image content. "
                + "Huge scenes are capped to the GPU's max texture size.";
    }

    @Override
    public void writeInputSchema(JsonWriter w) throws IOException {
        w.set("type", "object");
        w.object("properties");
        w.object("mode");
        w.set("type", "string");
        w.array("enum");
        w.value("whole");
        w.value("view");
        w.value("region");
        w.pop();
        w.set("default", "whole");
        w.set("description", "whole = entire scene; view = current camera; region = custom world rectangle.");
        w.pop();
        w.object("x");
        w.set("type", "number");
        w.set("description", "region only: world-space x of the rectangle's corner.");
        w.pop();
        w.object("y");
        w.set("type", "number");
        w.set("description", "region only: world-space y of the rectangle's corner.");
        w.pop();
        w.object("width");
        w.set("type", "number");
        w.set("description", "region only: world-space width.");
        w.pop();
        w.object("height");
        w.set("type", "number");
        w.set("description", "region only: world-space height.");
        w.pop();
        w.pop();
        w.set("additionalProperties", false);
    }

    @Override
    public McpToolResult call(JsonValue args) {
        String modeStr = (args != null) ? args.getString("mode", "whole") : "whole";
        RemoteScreenshotRequest.Mode mode;
        switch (modeStr) {
            case "view": mode = RemoteScreenshotRequest.Mode.VIEW; break;
            case "region": mode = RemoteScreenshotRequest.Mode.REGION; break;
            default: mode = RemoteScreenshotRequest.Mode.WHOLE;
        }
        float x = (args != null) ? args.getFloat("x", 0f) : 0f;
        float y = (args != null) ? args.getFloat("y", 0f) : 0f;
        float w = (args != null) ? args.getFloat("width", 0f) : 0f;
        float h = (args != null) ? args.getFloat("height", 0f) : 0f;

        RemoteScreenshotResult r = remote.screenshot(mode, x, y, w, h, 15000);
        if (!r.ok || r.pngBytes == null) {
            return McpToolResult.error(r.error != null ? r.error : "screenshot returned no data");
        }
        return McpToolResult.image(r.pngBytes, r.width, r.height);
    }
}