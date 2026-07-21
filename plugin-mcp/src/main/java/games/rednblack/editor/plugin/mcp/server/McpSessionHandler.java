package games.rednblack.editor.plugin.mcp.server;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import games.rednblack.editor.plugin.mcp.tools.McpToolResult;
import games.rednblack.editor.plugin.mcp.tools.ToolRegistry;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Parses MCP JSON-RPC requests over the streamable-HTTP transport and produces responses
 * using libGDX's {@link JsonWriter}. Supports initialize, notifications/initialized,
 * tools/list, tools/call. Responses are application/json; notifications (no id) get no body.
 */
public class McpSessionHandler {
    private static final String PROTOCOL_VERSION = "2025-03-26";
    private static final String SERVER_NAME = "hyperlap2d-mcp";
    private static final String SERVER_VERSION = "0.0.1";

    private final ToolRegistry tools;

    public McpSessionHandler(ToolRegistry tools) {
        this.tools = tools;
    }

    /** @return the JSON-RPC response body, or null for a notification (no response). */
    public String process(String body) {
        JsonValue req;
        try {
            req = new JsonReader().parse(body);
        } catch (Exception e) {
            return error(null, -32700, "Parse error: " + e.getMessage());
        }
        if (req == null || !req.isObject()) {
            return error(null, -32600, "Invalid request");
        }

        String method = req.getString("method", "");
        boolean isNotification = !req.has("id");
        String id = jsonId(req);

        switch (method) {
            case "initialize":
                return build(id, w -> {
                    w.object("result");
                    w.set("protocolVersion", PROTOCOL_VERSION);
                    w.object("capabilities");
                    w.object("tools");
                    w.set("listChanged", false);
                    w.pop();
                    w.pop();
                    w.object("serverInfo");
                    w.set("name", SERVER_NAME);
                    w.set("version", SERVER_VERSION);
                    w.pop();
                    w.pop();
                });
            case "notifications/initialized":
                return null;
            case "tools/list":
                return build(id, w -> {
                    w.object("result");
                    w.name("tools");
                    tools.writeTools(w);
                    w.pop();
                });
            case "tools/call": {
                if (isNotification) return null;
                JsonValue params = req.get("params");
                String toolName = params != null ? params.getString("name", "") : "";
                JsonValue toolArgs = params != null ? params.get("arguments") : null;
                McpToolResult res = tools.call(toolName, toolArgs);
                return build(id, w -> {
                    w.object("result");
                    res.write(w);
                    w.pop();
                });
            }
            default:
                return error(id, -32601, "Method not found: " + method);
        }
    }

    /** Full JSON-RPC error response (envelope included). */
    public static String error(String id, int code, String message) {
        return build(id, w -> {
            w.object("error");
            w.set("code", code);
            w.set("message", message);
            w.pop();
        });
    }

    private static String build(String id, McpJson.JsonBuild resultBuilder) {
        StringWriter sw = new StringWriter();
        try (JsonWriter w = new JsonWriter(sw)) {
            w.object();
            w.set("jsonrpc", "2.0");
            w.name("id");
            if (id != null) w.value(id); else w.value((Object) null);
            resultBuilder.build(w);
            w.pop();
        } catch (IOException e) {
            throw new RuntimeException("JSON-RPC build failed", e);
        }
        return sw.toString();
    }

    private static String jsonId(JsonValue req) {
        if (!req.has("id")) return null;
        JsonValue id = req.get("id");
        if (id.isString()) return id.asString();
        if (id.isNumber()) return String.valueOf(id.asLong());
        return null;
    }
}