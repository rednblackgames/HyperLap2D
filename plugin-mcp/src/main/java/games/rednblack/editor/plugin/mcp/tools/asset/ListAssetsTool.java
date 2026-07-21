package games.rednblack.editor.plugin.mcp.tools.asset;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import games.rednblack.editor.plugin.mcp.server.McpJson;
import games.rednblack.editor.plugin.mcp.tools.McpContext;
import games.rednblack.editor.plugin.mcp.tools.McpToolResult;
import games.rednblack.editor.plugin.mcp.tools.RemoteOps;
import games.rednblack.editor.plugin.mcp.tools.Tool;
import games.rednblack.h2d.common.remote.RemoteAssetsResult;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/** Lists loaded project assets by category (spine/sprite/particle/talos/font/tinyvg/shader). */
public class ListAssetsTool implements Tool {
    private final RemoteOps remote;

    public ListAssetsTool(McpContext ctx) {
        this.remote = new RemoteOps(ctx);
    }

    @Override public String name() { return "list_assets"; }
    @Override public String description() {
        return "List loaded project assets by category: imageRegion, ninePatchRegion, spineAnimation, "
                + "spriteAnimation, particleEffect, talosEffect, font, tinyvg, shader.";
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
        RemoteAssetsResult r = remote.listAssets(5000);
        if (!r.ok) {
            return McpToolResult.error(r.error != null ? r.error : "list assets failed");
        }
        String json = McpJson.object(w -> {
            for (Map.Entry<String, List<String>> e : r.categories.entrySet()) {
                w.name(e.getKey());
                w.array();
                List<String> names = e.getValue();
                for (String n : names) w.value(n);
                w.pop();
            }
        });
        return McpToolResult.text(json);
    }
}