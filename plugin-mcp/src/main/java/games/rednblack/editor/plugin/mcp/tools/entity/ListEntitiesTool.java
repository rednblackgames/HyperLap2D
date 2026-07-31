package games.rednblack.editor.plugin.mcp.tools.entity;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.SnapshotArray;
import games.rednblack.editor.plugin.mcp.server.McpJson;
import games.rednblack.editor.plugin.mcp.server.RenderThread;
import games.rednblack.editor.plugin.mcp.tools.McpContext;
import games.rednblack.editor.plugin.mcp.tools.McpToolResult;
import games.rednblack.editor.plugin.mcp.tools.RemoteOps;
import games.rednblack.editor.plugin.mcp.tools.Tool;
import com.badlogic.gdx.graphics.Color;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.NodeComponent;
import games.rednblack.editor.renderer.components.TintComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.components.ZIndexComponent;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.h2d.common.remote.RemoteTypeNamesResult;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Lists entities in the current scene: uniqueId, parentId, name, typeId, type (display name),
 * depth. Type names come from the editor's authoritative entityType -> name map (covering
 * core + Spine/Talos/TinyVG), fetched once via RemoteOps and cached.
 */
public class ListEntitiesTool implements Tool {
    private final McpContext ctx;

    /** Cached editor type-name map (typeId -> display name). Lazily fetched, stable for the session. */
    private Map<Integer, String> typeNames;

    public ListEntitiesTool(McpContext ctx) {
        this.ctx = ctx;
    }

    @Override public String name() { return "list_entities"; }
    @Override public String description() {
        return "List all entities in the current scene with uniqueId, parentId, name, typeId and type "
                + "(display name, including Spine/Talos/TinyVG), depth for tree reconstruction, zIndex "
                + "+ layer (the per-layer z-index used by set_z_index; lower draws behind, higher in front), "
                + "and a transform block (x, y, width, height, scaleX, scaleY, rotation, originX, originY, "
                + "flipX/flipY when set) — coordinates are the item's own, local to the parent composite for "
                + "a child. customVars, tags and tint are included when they are not empty/white.";
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
        if (ctx.api().getSceneLoader() == null) {
            return McpToolResult.text("[]");
        }
        Map<Integer, String> names = typeNameMap();
        String json = RenderThread.run(() -> {
            int root = ctx.api().getSceneLoader().getRoot();
            if (root < 0) return "[]";
            return McpJson.array(w -> collect(root, null, 0, names, w));
        }, 5000);
        return McpToolResult.text(json);
    }

    private Map<Integer, String> typeNameMap() {
        if (typeNames == null) {
            RemoteTypeNamesResult r = new RemoteOps(ctx).typeNames(5000);
            typeNames = r.ok && r.names != null ? r.names : new HashMap<>();
        }
        return typeNames;
    }

    private void collect(int entity, String parentId, int depth, Map<Integer, String> names, JsonWriter w) throws IOException {
        MainItemComponent main = ComponentRetriever.get(entity, MainItemComponent.class, ctx.api().getEngine());
        NodeComponent node = ComponentRetriever.get(entity, NodeComponent.class, ctx.api().getEngine());
        ZIndexComponent zindex = ComponentRetriever.get(entity, ZIndexComponent.class, ctx.api().getEngine());

        int typeId = main != null ? main.entityType : -1;
        String uniqueId = (main != null && main.uniqueId != null) ? main.uniqueId : String.valueOf(entity);
        String name = main != null ? main.itemIdentifier : "";
        String type = names.get(typeId);
        if (type == null) type = "Unknown";

        w.object();
        w.set("uniqueId", uniqueId);
        w.name("parentId");
        if (parentId != null) w.value(parentId); else w.value((Object) null);
        w.set("name", name);
        w.set("typeId", typeId);
        w.set("type", type);
        w.set("depth", depth);
        w.set("zIndex", zindex != null ? zindex.getZIndex() : 0);
        w.set("layer", zindex != null && zindex.getLayerName() != null ? zindex.getLayerName() : "");

        // Where the entity is and how big, so a caller can check what it wrote instead of assuming
        // it. Coordinates are the item's own: local to the parent composite for a child, world for
        // anything sitting at the root.
        TransformComponent transform = ComponentRetriever.get(entity, TransformComponent.class, ctx.api().getEngine());
        DimensionsComponent size = ComponentRetriever.get(entity, DimensionsComponent.class, ctx.api().getEngine());
        if (transform != null) {
            w.object("transform");
            w.set("x", transform.x);
            w.set("y", transform.y);
            if (size != null) {
                w.set("width", size.width);
                w.set("height", size.height);
            }
            w.set("scaleX", transform.scaleX);
            w.set("scaleY", transform.scaleY);
            w.set("rotation", transform.rotation);
            w.set("originX", transform.originX);
            w.set("originY", transform.originY);
            if (transform.flipX) w.set("flipX", true);
            if (transform.flipY) w.set("flipY", true);
            w.pop();
        }

        // Tint only when it is not plain opaque white, which is the overwhelming majority.
        TintComponent tint = ComponentRetriever.get(entity, TintComponent.class, ctx.api().getEngine());
        if (tint != null && !Color.WHITE.equals(tint.color)) {
            w.set("tint", "#" + tint.color.toString().toUpperCase());
        }
        // Custom variables come back too, so whatever wrote them can read them again. Omitted when
        // empty, which is the common case and would otherwise bloat every listing.
        if (main != null && main.customVariables != null && main.customVariables.size > 0) {
            w.object("customVars");
            for (ObjectMap.Entry<String, String> var : main.customVariables) {
                w.set(var.key, var.value);
            }
            w.pop();
        }
        // Tags likewise: they decide which components a runtime transmutes onto the entity, so
        // being able to read them back is how you check the scene says what you meant.
        if (main != null && main.tags != null && main.tags.size > 0) {
            w.array("tags");
            for (String tag : main.tags) w.value(tag);
            w.pop();
        }
        w.pop();

        if (node != null && node.children.size > 0) {
            SnapshotArray<Integer> c = node.children;
            Integer[] arr = c.begin();
            for (int i = 0, n = c.size; i < n; i++) {
                collect(arr[i], uniqueId, depth + 1, names, w);
            }
            c.end();
        }
    }
}