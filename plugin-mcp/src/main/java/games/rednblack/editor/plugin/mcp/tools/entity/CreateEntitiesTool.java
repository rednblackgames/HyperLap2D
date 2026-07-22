package games.rednblack.editor.plugin.mcp.tools.entity;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import games.rednblack.editor.plugin.mcp.server.McpJson;
import games.rednblack.editor.plugin.mcp.tools.McpContext;
import games.rednblack.editor.plugin.mcp.tools.McpToolResult;
import games.rednblack.editor.plugin.mcp.tools.RemoteOps;
import games.rednblack.editor.plugin.mcp.tools.Tool;
import games.rednblack.h2d.common.remote.RemoteCreateEntityRequest;
import games.rednblack.h2d.common.remote.RemoteCreateEntityResult;

import java.io.IOException;

/**
 * Create multiple entities in one call. Each entry in {@code entities} mirrors create_entity's
 * fields (type, name, x, y, width, height, fontFamily, fontSize, lightType). Entities are
 * created sequentially through the same RemoteOps/ItemFactory path (each undoable). Returns a
 * JSON array of per-entity results ({@code {ok, uniqueId, error}}) in the same order as the input.
 */
public class CreateEntitiesTool implements Tool {
    private final McpContext ctx;

    public CreateEntitiesTool(McpContext ctx) {
        this.ctx = ctx;
    }

    @Override public String name() { return "create_entities"; }
    @Override public String description() {
        return "Create multiple entities in one call (bulk create). Pass an 'entities' array; each entry has "
                + "the same fields as create_entity (type, name, x, y, width, height, fontFamily, fontSize, "
                + "lightType, parentUniqueId). Entities are created sequentially via the same validated/undoable "
                + "path as create_entity. Returns a JSON array of per-entry results {ok, uniqueId, error} in "
                + "input order. Use this when placing many tiles/objects at once instead of many create_entity "
                + "round-trips.";
    }

    @Override
    public void writeInputSchema(JsonWriter w) throws IOException {
        w.set("type", "object");
        w.object("properties");
        w.object("entities");
        w.set("type", "array");
        w.set("description", "Array of entity specs, each with create_entity fields (type required).");
        w.object("items");
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
        w.object("name"); w.set("type", "string"); w.pop();
        w.object("x"); w.set("type", "number"); w.pop();
        w.object("y"); w.set("type", "number"); w.pop();
        w.object("width"); w.set("type", "number"); w.pop();
        w.object("height"); w.set("type", "number"); w.pop();
        w.object("fontFamily"); w.set("type", "string"); w.pop();
        w.object("fontSize"); w.set("type", "integer"); w.pop();
        w.object("lightType"); w.set("type", "string"); w.array("enum"); w.value("POINT"); w.value("CONE"); w.pop(); w.pop();
        w.object("parentUniqueId"); w.set("type", "string");
        w.set("description", "Optional composite uniqueId to create inside (x, y become local to it)."); w.pop();
        w.object("layer"); w.set("type", "string");
        w.set("description", "Optional layer name to create on (resolved case-insensitively)."); w.pop();
        w.pop();
        w.pop();
        w.pop();
        w.pop();
        w.name("required");
        w.array();
        w.value("entities");
        w.pop();
        w.set("additionalProperties", false);
    }

    @Override
    public McpToolResult call(JsonValue args) {
        JsonValue entities = args.get("entities");
        if (entities == null || !entities.isArray()) {
            return McpToolResult.error("'entities' must be an array");
        }
        RemoteOps remote = new RemoteOps(ctx);
        String json = McpJson.array(w -> {
            for (JsonValue entry = entities.child(); entry != null; entry = entry.next()) {
                w.object();
                try {
                    RemoteCreateEntityRequest req = new RemoteCreateEntityRequest();
                    req.type = entry.getString("type", "");
                    req.name = entry.getString("name", "");
                    req.x = entry.getFloat("x", 0f);
                    req.y = entry.getFloat("y", 0f);
                    req.width = entry.has("width") ? entry.getFloat("width", 100f) : 100f;
                    req.height = entry.has("height") ? entry.getFloat("height", 100f) : 100f;
                    if (entry.has("fontFamily")) req.fontFamily = entry.getString("fontFamily");
                    if (entry.has("fontSize")) req.fontSize = entry.getInt("fontSize", 20);
                    if (entry.has("lightType")) req.lightType = entry.getString("lightType");
                    if (entry.has("parentUniqueId")) req.parentUniqueId = entry.getString("parentUniqueId");
                    if (entry.has("layer")) req.layer = entry.getString("layer");

                    RemoteCreateEntityResult r = remote.createEntity(req, 5000);
                    w.set("ok", r.ok);
                    if (r.ok) {
                        w.set("uniqueId", r.uniqueId != null ? r.uniqueId : "");
                    } else {
                        w.set("error", r.error != null ? r.error : "create failed");
                    }
                } catch (Throwable t) {
                    w.set("ok", false);
                    w.set("error", t.getClass().getSimpleName() + ": " + t.getMessage());
                }
                w.pop();
            }
        });
        return McpToolResult.text(json);
    }
}