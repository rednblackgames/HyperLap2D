package games.rednblack.editor.plugin.mcp.tools.entity;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.SnapshotArray;
import games.rednblack.editor.plugin.mcp.server.McpJson;
import games.rednblack.editor.plugin.mcp.server.RenderThread;
import games.rednblack.editor.plugin.mcp.tools.McpContext;
import games.rednblack.editor.plugin.mcp.tools.McpToolResult;
import games.rednblack.editor.plugin.mcp.tools.RemoteOps;
import games.rednblack.editor.plugin.mcp.tools.Tool;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.NodeComponent;
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
                + "(display name, including Spine/Talos/TinyVG), depth for tree reconstruction, and zIndex "
                + "+ layer (the per-layer z-index used by set_z_index; lower draws behind, higher in front).";
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