package games.rednblack.editor.plugin.mcp.tools.entity;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import games.rednblack.editor.plugin.mcp.tools.McpContext;
import games.rednblack.editor.plugin.mcp.tools.McpToolResult;
import games.rednblack.editor.plugin.mcp.tools.RemoteOps;
import games.rednblack.editor.plugin.mcp.tools.Tool;
import games.rednblack.h2d.common.remote.RemoteCreateEntityRequest;
import games.rednblack.h2d.common.remote.RemoteCreateEntityResult;

import java.io.IOException;

/**
 * Create a new entity in the current scene. Routes through the editor's ItemFactory (the same
 * path as dragging an asset) via the RemoteOps bridge, so the creation + the ACTION_CREATE_ITEM
 * command run exactly as the UI does it (undoable). Supports all entity types.
 */
public class CreateEntityTool implements Tool {
    private final McpContext ctx;

    public CreateEntityTool(McpContext ctx) {
        this.ctx = ctx;
    }

    @Override public String name() { return "create_entity"; }
    @Override public String description() {
        return "Create a new entity in the current scene at (x, y) in world units. type: "
                + "image | spriteAnimation | spineAnimation | libraryItem | 9patch | tinyvg | particle | talos "
                + "(name-based, pass the asset name from list_assets), primitive (width/height rect), composite "
                + "(empty), label (optional fontFamily/fontSize), light (lightType POINT/CONE). "
                + "Returns the new entity's uniqueId. The creation is undoable (Ctrl+Z).";
    }

    @Override
    public void writeInputSchema(JsonWriter w) throws IOException {
        w.set("type", "object");
        w.object("properties");
        w.object("type");
        w.set("type", "string");
        w.array("enum");
        for (String t : new String[]{"image", "spriteAnimation", "spineAnimation", "libraryItem",
                "9patch", "tinyvg", "particle", "talos", "primitive", "composite", "label", "light"}) {
            w.value(t);
        }
        w.pop();
        w.pop();
        w.object("name");
        w.set("type", "string");
        w.set("description", "Asset name (for name-based types; see list_assets).");
        w.pop();
        w.object("x"); w.set("type", "number"); w.pop();
        w.object("y"); w.set("type", "number"); w.pop();
        w.object("width"); w.set("type", "number"); w.set("description", "primitive rect width (default 100)."); w.pop();
        w.object("height"); w.set("type", "number"); w.set("description", "primitive rect height (default 100)."); w.pop();
        w.object("fontFamily"); w.set("type", "string"); w.set("description", "label font family (optional)."); w.pop();
        w.object("fontSize"); w.set("type", "integer"); w.set("description", "label font size (optional, default 20)."); w.pop();
        w.object("lightType"); w.set("type", "string"); w.array("enum"); w.value("POINT"); w.value("CONE"); w.pop();
        w.set("description", "light type (optional, default POINT)."); w.pop();
        w.pop();
        w.name("required");
        w.array();
        w.value("type");
        w.pop();
        w.set("additionalProperties", false);
    }

    @Override
    public McpToolResult call(JsonValue args) {
        RemoteCreateEntityRequest req = new RemoteCreateEntityRequest();
        req.type = args.getString("type", "");
        req.name = args.getString("name", "");
        req.x = args.getFloat("x", 0f);
        req.y = args.getFloat("y", 0f);
        if (args.has("width")) req.width = args.getFloat("width", 100f);
        if (args.has("height")) req.height = args.getFloat("height", 100f);
        if (args.has("fontFamily")) req.fontFamily = args.getString("fontFamily");
        if (args.has("fontSize")) req.fontSize = args.getInt("fontSize", 20);
        if (args.has("lightType")) req.lightType = args.getString("lightType");

        RemoteCreateEntityResult r = new RemoteOps(ctx).createEntity(req, 5000);
        if (!r.ok) return McpToolResult.error(r.error != null ? r.error : "create failed");
        return McpToolResult.text(r.uniqueId != null ? r.uniqueId : "ok");
    }
}