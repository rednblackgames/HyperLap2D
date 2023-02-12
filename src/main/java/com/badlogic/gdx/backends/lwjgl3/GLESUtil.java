package com.badlogic.gdx.backends.lwjgl3;

import org.lwjgl.opengles.*;
import org.lwjgl.system.Callback;

import java.io.PrintStream;

import static org.lwjgl.opengl.GL11C.glEnable;
import static org.lwjgl.opengl.GL11C.glGetInteger;
import static org.lwjgl.opengl.GL30C.GL_CONTEXT_FLAGS;
import static org.lwjgl.opengl.GL43C.*;
import static org.lwjgl.opengl.GL43C.GL_DEBUG_SOURCE_OTHER;
import static org.lwjgl.system.APIUtil.apiLog;
import static org.lwjgl.system.APIUtil.apiUnknownToken;
import static org.lwjgl.system.MemoryUtil.NULL;

public class GLESUtil {

    /** Enables or disables GL debug messages for the specified severity level. Returns false if the severity level could not be
     * set (e.g. the NOTIFICATION level is not supported by the ARB and AMD extensions).
     *
     * See {@link Lwjgl3ApplicationConfiguration#enableGLDebugOutput(boolean, PrintStream)} */
    public static boolean setGLESDebugMessageControl (Lwjgl3ApplicationGLESFix.GLDebugMessageSeverity severity, boolean enabled) {
        GLESCapabilities caps = GLES.getCapabilities();
        final int GL_DONT_CARE = 0x1100; // not defined anywhere yet

        if (caps.GLES32) {
            GLES32.glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, severity.gl43, 0, enabled);
            return true;
        }

        return false;
    }

    public static Callback setupDebugMessageCallback(PrintStream stream) {
        GLESCapabilities caps = GLES.getCapabilities();

        if (caps.GLES32) {
            apiLog("[GLES] Using OpenGL ES 3.2 for error logging.");
            GLDebugMessageCallback proc = GLDebugMessageCallback.create((source, type, id, severity, length, message, userParam) -> {
                stream.println("[LWJGL] OpenGL ES debug message");
                printDetail(stream, "ID", String.format("0x%X", id));
                printDetail(stream, "Source", getDebugSource(source));
                printDetail(stream, "Type", getDebugType(type));
                printDetail(stream, "Severity", getDebugSeverity(severity));
                printDetail(stream, "Message", GLDebugMessageCallback.getMessage(length, message));
            });
            GLES32.glDebugMessageCallback(proc, NULL);
            if ((glGetInteger(GL_CONTEXT_FLAGS) & GL_CONTEXT_FLAG_DEBUG_BIT) == 0) {
                apiLog("[GLES] Warning: A non-debug context may not produce any debug output.");
                glEnable(GL_DEBUG_OUTPUT);
            }
            return proc;
        }

        apiLog("[GLES] No debug output implementation is available.");
        return null;
    }

    private static void printDetail(PrintStream stream, String type, String message) {
        stream.printf("\t%s: %s\n", type, message);
    }

    private static String getDebugSource(int source) {
        switch (source) {
            case GL_DEBUG_SOURCE_API:
                return "API";
            case GL_DEBUG_SOURCE_WINDOW_SYSTEM:
                return "WINDOW SYSTEM";
            case GL_DEBUG_SOURCE_SHADER_COMPILER:
                return "SHADER COMPILER";
            case GL_DEBUG_SOURCE_THIRD_PARTY:
                return "THIRD PARTY";
            case GL_DEBUG_SOURCE_APPLICATION:
                return "APPLICATION";
            case GL_DEBUG_SOURCE_OTHER:
                return "OTHER";
            default:
                return apiUnknownToken(source);
        }
    }

    private static String getDebugType(int type) {
        switch (type) {
            case GL_DEBUG_TYPE_ERROR:
                return "ERROR";
            case GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR:
                return "DEPRECATED BEHAVIOR";
            case GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR:
                return "UNDEFINED BEHAVIOR";
            case GL_DEBUG_TYPE_PORTABILITY:
                return "PORTABILITY";
            case GL_DEBUG_TYPE_PERFORMANCE:
                return "PERFORMANCE";
            case GL_DEBUG_TYPE_OTHER:
                return "OTHER";
            case GL_DEBUG_TYPE_MARKER:
                return "MARKER";
            default:
                return apiUnknownToken(type);
        }
    }

    private static String getDebugSeverity(int severity) {
        switch (severity) {
            case GL_DEBUG_SEVERITY_HIGH:
                return "HIGH";
            case GL_DEBUG_SEVERITY_MEDIUM:
                return "MEDIUM";
            case GL_DEBUG_SEVERITY_LOW:
                return "LOW";
            case GL_DEBUG_SEVERITY_NOTIFICATION:
                return "NOTIFICATION";
            default:
                return apiUnknownToken(severity);
        }
    }
}
