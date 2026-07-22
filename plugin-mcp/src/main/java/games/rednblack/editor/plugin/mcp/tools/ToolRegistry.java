package games.rednblack.editor.plugin.mcp.tools;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import games.rednblack.editor.plugin.mcp.tools.asset.ListAssetsTool;
import games.rednblack.editor.plugin.mcp.tools.entity.ComponentTools;
import games.rednblack.editor.plugin.mcp.tools.entity.CreateEntityTool;
import games.rednblack.editor.plugin.mcp.tools.entity.CreateEntitiesTool;
import games.rednblack.editor.plugin.mcp.tools.entity.ListEntitiesTool;
import games.rednblack.editor.plugin.mcp.tools.layer.LayerTools;
import games.rednblack.editor.plugin.mcp.tools.scene.ListScenesTool;
import games.rednblack.editor.plugin.mcp.tools.screenshot.ScreenshotTool;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Registers all MCP tools and dispatches tools/call to them. */
public class ToolRegistry {
    private final Map<String, Tool> tools = new LinkedHashMap<>();

    public ToolRegistry(McpContext ctx) {
        register(new ListScenesTool(ctx));
        register(new ListEntitiesTool(ctx));
        register(new ListAssetsTool(ctx));
        register(new ScreenshotTool(ctx));
        register(new ComponentTools.AddComponentTool(ctx));
        register(new ComponentTools.RemoveComponentTool(ctx));
        register(new ComponentTools.UpdateTransformTool(ctx));
        register(new ComponentTools.UpdateComponentTool(ctx));
        register(new CreateEntityTool(ctx));
        register(new CreateEntitiesTool(ctx));
        register(new ComponentTools.DeleteEntityTool(ctx));
        register(new ComponentTools.DeleteEntitiesTool(ctx));
        register(new ComponentTools.SetZIndexTool(ctx));
        register(new LayerTools.ListLayersTool(ctx));
        register(new LayerTools.CreateLayerTool(ctx));
        register(new LayerTools.DeleteLayerTool(ctx));
        register(new LayerTools.RenameLayerTool(ctx));
        register(new LayerTools.SetLayerOrderTool(ctx));
        register(new LayerTools.SetEntityLayerTool(ctx));
        register(new ComponentTools.OpenSceneTool(ctx));
        register(new ComponentTools.GetSceneSettingsTool(ctx));
        register(new ComponentTools.UpdateSceneSettingsTool(ctx));
        register(new ComponentTools.SaveProjectTool(ctx));
        register(new ComponentTools.ListActionsTool(ctx));
        register(new ComponentTools.GetEditableComponentsTool(ctx));
        register(new ComponentTools.CreateShaderTool(ctx));
        register(new ComponentTools.GetAssetDimensionsTool(ctx));
        register(new ComponentTools.GetAssetPreviewTool(ctx));
    }

    public void register(Tool tool) {
        tools.put(tool.name(), tool);
    }

    /** Write the tools array (the value of "tools") for tools/list. */
    public void writeTools(JsonWriter w) throws IOException {
        w.array();
        for (Tool t : tools.values()) {
            t.writeDescriptor(w);
        }
        w.pop();
    }

    /** Dispatch a tools/call. Returns the tool result, or an error result if unknown/failed. */
    public McpToolResult call(String toolName, JsonValue args) {
        Tool tool = tools.get(toolName);
        if (tool == null) {
            return McpToolResult.error("Unknown tool: " + toolName);
        }
        try {
            return tool.call(args);
        } catch (Throwable t) {
            return McpToolResult.error("Tool '" + toolName + "' threw: "
                    + t.getClass().getSimpleName() + ": " + t.getMessage());
        }
    }
}