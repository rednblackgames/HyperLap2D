package games.rednblack.editor.plugin.mcp.tools.layer;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import games.rednblack.editor.plugin.mcp.server.McpJson;
import games.rednblack.editor.plugin.mcp.tools.McpContext;
import games.rednblack.editor.plugin.mcp.tools.McpToolResult;
import games.rednblack.editor.plugin.mcp.tools.RemoteOps;
import games.rednblack.editor.plugin.mcp.tools.Tool;
import games.rednblack.h2d.common.remote.RemoteLayerRequest;
import games.rednblack.h2d.common.remote.RemoteLayerResult;

import java.io.IOException;

/**
 * Layer-management tools. Layers belong to the current viewing entity (the scene root, or the
 * composite currently being edited) and are per-composite. Layer names are case-sensitive in the
 * editor (stored by hash), but these tools resolve names case-insensitively to avoid casing
 * mistakes — "default" matches "Default". Create/delete/rename/reorder and moving an entity to a
 * layer all run through the editor's undoable layer commands.
 */
public final class LayerTools {
    private LayerTools() {}

    static McpToolResult format(RemoteLayerResult r) {
        if (r.ok) return McpToolResult.text("ok");
        return McpToolResult.error(r.error != null ? r.error : "layer op failed");
    }

    public static final class ListLayersTool implements Tool {
        private final McpContext ctx;
        public ListLayersTool(McpContext ctx) { this.ctx = ctx; }
        @Override public String name() { return "list_layers"; }
        @Override public String description() {
            return "List the layers of the current viewing entity (the scene root, or the composite currently "
                    + "being edited). Returns [{ name, index, isVisible, isLocked }] in editor order (index 0 = "
                    + "back-most layer). The default layer is named \"Default\" (case-sensitive in the editor; "
                    + "resolved case-insensitively by the layer tools).";
        }
        @Override
        public void writeInputSchema(JsonWriter w) throws IOException {
            w.set("type", "object");
            w.object("properties");
            w.pop();
            w.set("additionalProperties", false);
        }
        @Override
        public McpToolResult call(JsonValue args) {
            RemoteLayerRequest req = new RemoteLayerRequest();
            req.op = RemoteLayerRequest.Op.LIST;
            RemoteLayerResult r = new RemoteOps(ctx).layerOp(req, 5000);
            if (!r.ok) return McpToolResult.error(r.error != null ? r.error : "list layers failed");
            String json = McpJson.array(w -> {
                for (RemoteLayerResult.LayerInfo l : r.layers) {
                    w.object();
                    w.set("name", l.name);
                    w.set("index", l.index);
                    w.set("isVisible", l.isVisible);
                    w.set("isLocked", l.isLocked);
                    w.pop();
                }
            });
            return McpToolResult.text(json);
        }
    }

    public static final class CreateLayerTool implements Tool {
        private final McpContext ctx;
        public CreateLayerTool(McpContext ctx) { this.ctx = ctx; }
        @Override public String name() { return "create_layer"; }
        @Override public String description() {
            return "Create a new layer on the current viewing entity. layerName is used as-is (case-sensitive); "
                    + "rejected if a same-cased or case-insensitive match already exists. Undoable (Ctrl+Z).";
        }
        @Override
        public void writeInputSchema(JsonWriter w) throws IOException {
            w.set("type", "object");
            w.object("properties");
            w.object("layerName");
            w.set("type", "string");
            w.set("description", "New layer name (case-sensitive; must be unique).");
            w.pop();
            w.pop();
            w.name("required");
            w.array();
            w.value("layerName");
            w.pop();
            w.set("additionalProperties", false);
        }
        @Override
        public McpToolResult call(JsonValue args) {
            RemoteLayerRequest req = new RemoteLayerRequest();
            req.op = RemoteLayerRequest.Op.CREATE;
            req.layerName = args.getString("layerName", "");
            return format(new RemoteOps(ctx).layerOp(req, 5000));
        }
    }

    public static final class DeleteLayerTool implements Tool {
        private final McpContext ctx;
        public DeleteLayerTool(McpContext ctx) { this.ctx = ctx; }
        @Override public String name() { return "delete_layer"; }
        @Override public String description() {
            return "Delete a layer by name (resolved case-insensitively). WARNING: this also deletes every "
                    + "entity on that layer. Undoable (Ctrl+Z).";
        }
        @Override
        public void writeInputSchema(JsonWriter w) throws IOException {
            w.set("type", "object");
            w.object("properties");
            w.object("layerName");
            w.set("type", "string");
            w.set("description", "Layer to delete (matched case-insensitively).");
            w.pop();
            w.pop();
            w.name("required");
            w.array();
            w.value("layerName");
            w.pop();
            w.set("additionalProperties", false);
        }
        @Override
        public McpToolResult call(JsonValue args) {
            RemoteLayerRequest req = new RemoteLayerRequest();
            req.op = RemoteLayerRequest.Op.DELETE;
            req.layerName = args.getString("layerName", "");
            return format(new RemoteOps(ctx).layerOp(req, 5000));
        }
    }

    public static final class RenameLayerTool implements Tool {
        private final McpContext ctx;
        public RenameLayerTool(McpContext ctx) { this.ctx = ctx; }
        @Override public String name() { return "rename_layer"; }
        @Override public String description() {
            return "Rename a layer. layerName is matched case-insensitively to the existing layer; newName is "
                    + "applied as-is (case-sensitive) and must be unique. Undoable (Ctrl+Z).";
        }
        @Override
        public void writeInputSchema(JsonWriter w) throws IOException {
            w.set("type", "object");
            w.object("properties");
            w.object("layerName");
            w.set("type", "string");
            w.set("description", "Existing layer to rename (matched case-insensitively).");
            w.pop();
            w.object("newName");
            w.set("type", "string");
            w.set("description", "New layer name (case-sensitive; must be unique).");
            w.pop();
            w.pop();
            w.name("required");
            w.array();
            w.value("layerName");
            w.value("newName");
            w.pop();
            w.set("additionalProperties", false);
        }
        @Override
        public McpToolResult call(JsonValue args) {
            RemoteLayerRequest req = new RemoteLayerRequest();
            req.op = RemoteLayerRequest.Op.RENAME;
            req.layerName = args.getString("layerName", "");
            req.newName = args.getString("newName", "");
            return format(new RemoteOps(ctx).layerOp(req, 5000));
        }
    }

    public static final class SetLayerOrderTool implements Tool {
        private final McpContext ctx;
        public SetLayerOrderTool(McpContext ctx) { this.ctx = ctx; }
        @Override public String name() { return "set_layer_order"; }
        @Override public String description() {
            return "Move a layer one step up or down in the layer stack (swaps with its neighbor). Lower index "
                    + "draws behind, higher index draws in front. layerName matched case-insensitively; direction "
                    + "is 'up' or 'down'. Undoable (Ctrl+Z).";
        }
        @Override
        public void writeInputSchema(JsonWriter w) throws IOException {
            w.set("type", "object");
            w.object("properties");
            w.object("layerName");
            w.set("type", "string");
            w.set("description", "Layer to move (matched case-insensitively).");
            w.pop();
            w.object("direction");
            w.set("type", "string");
            w.array("enum"); w.value("up"); w.value("down"); w.pop();
            w.set("description", "'up' = one step toward back (lower index); 'down' = one step toward front.");
            w.pop();
            w.pop();
            w.name("required");
            w.array();
            w.value("layerName");
            w.value("direction");
            w.pop();
            w.set("additionalProperties", false);
        }
        @Override
        public McpToolResult call(JsonValue args) {
            RemoteLayerRequest req = new RemoteLayerRequest();
            req.op = RemoteLayerRequest.Op.ORDER;
            req.layerName = args.getString("layerName", "");
            req.direction = args.getString("direction", "");
            return format(new RemoteOps(ctx).layerOp(req, 5000));
        }
    }

    public static final class SetEntityLayerTool implements Tool {
        private final McpContext ctx;
        public SetEntityLayerTool(McpContext ctx) { this.ctx = ctx; }
        @Override public String name() { return "set_entity_layer"; }
        @Override public String description() {
            return "Move an entity onto a different layer. The layer belongs to the entity's parent composite "
                    + "(so the layer must exist on that parent — use list_layers while viewing the parent). "
                    + "layerName is matched case-insensitively. The entity lands at the front of the destination "
                    + "layer; use set_z_index to fine-tune its position within the layer. Undoable (Ctrl+Z).";
        }
        @Override
        public void writeInputSchema(JsonWriter w) throws IOException {
            w.set("type", "object");
            w.object("properties");
            w.object("entityId");
            w.set("type", "string");
            w.set("description", "The entity uniqueId (from list_entities).");
            w.pop();
            w.object("layerName");
            w.set("type", "string");
            w.set("description", "Destination layer name (on the entity's parent composite; matched case-insensitively).");
            w.pop();
            w.pop();
            w.name("required");
            w.array();
            w.value("entityId");
            w.value("layerName");
            w.pop();
            w.set("additionalProperties", false);
        }
        @Override
        public McpToolResult call(JsonValue args) {
            RemoteLayerRequest req = new RemoteLayerRequest();
            req.op = RemoteLayerRequest.Op.SET_ENTITY;
            req.entityId = args.getString("entityId", "");
            req.layerName = args.getString("layerName", "");
            return format(new RemoteOps(ctx).layerOp(req, 5000));
        }
    }
}