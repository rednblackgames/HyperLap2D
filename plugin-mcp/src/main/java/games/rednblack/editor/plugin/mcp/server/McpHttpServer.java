package games.rednblack.editor.plugin.mcp.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import games.rednblack.editor.plugin.mcp.tools.ToolRegistry;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Serves MCP Streamable HTTP on the JDK built-in HttpServer at /mcp. */
public class McpHttpServer {
    private final ToolRegistry tools;
    private final McpSessionHandler handler;
    private HttpServer httpServer;
    private ExecutorService executor;
    private int port;

    public McpHttpServer(ToolRegistry tools) {
        this.tools = tools;
        this.handler = new McpSessionHandler(tools);
    }

    public synchronized void start(int port) throws IOException {
        if (httpServer != null) return;
        this.port = port;
        httpServer = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        httpServer.createContext("/mcp", this::handleMcp);
        // Daemon-thread executor so worker threads never keep the JVM alive on editor exit.
        // The HttpServer selector thread is stopped explicitly via stop() (on MsgAPI.DISPOSE / shutdown hook).
        executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "h2d-mcp-http-worker");
            t.setDaemon(true);
            return t;
        });
        httpServer.setExecutor(executor);
        httpServer.start();
        System.out.println("[MCP] server listening on http://127.0.0.1:" + port + "/mcp");
    }

    public synchronized void stop() {
        if (httpServer != null) {
            httpServer.stop(0);
            httpServer = null;
            System.out.println("[MCP] server stopped");
        }
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    public synchronized boolean isRunning() {
        return httpServer != null;
    }

    public int getPort() {
        return port;
    }

    private void handleMcp(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(405, -1);
            exchange.close();
            return;
        }
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        String response;
        try {
            response = handler.process(body);
        } catch (Throwable t) {
            response = McpSessionHandler.error(null, -32603, "Internal error: " + t.getMessage());
        }

        if (response == null) {
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(202, -1);
            exchange.close();
            return;
        }
        byte[] respBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, respBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(respBytes);
        }
        exchange.close();
    }
}