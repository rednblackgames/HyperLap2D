package games.rednblack.editor.plugin.mcp.tools;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

import java.io.IOException;

/** A single MCP tool. The registry dispatches tools/call to {@link #call}. */
public interface Tool {
    String name();
    String description();

    /** Write this tool's JSON Schema object (the value of "inputSchema") into the writer. */
    void writeInputSchema(JsonWriter w) throws IOException;

    /** Write the full tool descriptor object ({@code {name, description, inputSchema}}). */
    default void writeDescriptor(JsonWriter w) throws IOException {
        w.object();
        w.set("name", name());
        w.set("description", description());
        w.object("inputSchema");
        writeInputSchema(w);
        w.pop();
        w.pop();
    }

    /** Execute the tool. {@code args} is the "arguments" object (may be null). */
    McpToolResult call(JsonValue args);
}