package games.rednblack.editor.plugin.mcp.tools.scene;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import games.rednblack.editor.plugin.mcp.server.McpJson;
import games.rednblack.editor.plugin.mcp.tools.McpContext;
import games.rednblack.editor.plugin.mcp.tools.McpToolResult;
import games.rednblack.editor.plugin.mcp.tools.Tool;
import games.rednblack.editor.renderer.data.SceneVO;

import java.io.IOException;
import java.util.ArrayList;

/** Lists all scenes in the current project. */
public class ListScenesTool implements Tool {
    private final McpContext ctx;

    public ListScenesTool(McpContext ctx) {
        this.ctx = ctx;
    }

    @Override public String name() { return "list_scenes"; }
    @Override public String description() {
        return "List all scenes in the current HyperLap2D project by name.";
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
        if (ctx.api().getCurrentProjectInfoVO() == null) {
            return McpToolResult.text("[]");
        }
        ArrayList<SceneVO> scenes = ctx.api().getCurrentProjectInfoVO().scenes;
        String json = McpJson.array(w -> {
            for (int i = 0; i < scenes.size(); i++) {
                w.object();
                w.set("name", scenes.get(i).sceneName);
                w.pop();
            }
        });
        return McpToolResult.text(json);
    }
}