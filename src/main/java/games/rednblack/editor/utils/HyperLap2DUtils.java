package games.rednblack.editor.utils;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import games.rednblack.editor.view.ui.UIWindowTitle;
import games.rednblack.editor.view.ui.UIWindowTitleMediator;
import games.rednblack.puremvc.Facade;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.SystemUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWNativeCocoa;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.lwjgl.system.JNI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.SharedLibrary;
import org.lwjgl.system.macosx.ObjCRuntime;
import org.lwjgl.system.windows.RECT;
import org.lwjgl.system.windows.User32;
import org.lwjgl.system.windows.WINDOWPLACEMENT;
import org.lwjgl.system.windows.WindowProc;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.foreign.*;
import java.nio.DoubleBuffer;

public class HyperLap2DUtils {
    public static final FilenameFilter PNG_FILTER = new SuffixFileFilter(".png");
    public static final FilenameFilter JSON_FILTER = new SuffixFileFilter(".json");
    public static final FilenameFilter DT_FILTER = new SuffixFileFilter(".dt");
    public static final String MY_DOCUMENTS_PATH = getMyDocumentsLocation();

    public static String getKeyMapPath() {
        return getRootPath() + File.separator + "configs" + File.separator + "keymaps";
    }

    public static String getRootPath() {
        String appRootDirectory = System.getProperty("user.home");
        if (SystemUtils.IS_OS_WINDOWS) {
            appRootDirectory = System.getenv("AppData");
        } else if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) {
            appRootDirectory += "/Library/Application Support";
        }

        return appRootDirectory + File.separator + ".hyperlap2d";
    }

    private static String getMyDocumentsLocation() {
        return System.getProperty("user.home") + File.separator + "Documents";
    }

    private final static Linker linker = Linker.nativeLinker();
    private final static MemorySegment msgSend = SymbolLookup.loaderLookup().find("objc_msgSend").orElse(null);

    /// Note that this flag is deprecated on macOS for some reason despite being the only way to tell.
    /// Strange really, but there is nothing besides this mask that I was able to find.
    private final static long NSFullScreenWindowMask = 1 << 14;

    public static boolean isCocoaFullscreen(long lwjglWindow) {
        // Because GLFW can't determine this kind of fullscreen on macOS we need to handle it manually.
        final var nativeWindow = MemorySegment.ofAddress(org.lwjgl.glfw.GLFWNativeCocoa.glfwGetCocoaWindow(lwjglWindow));
        final var method = MemorySegment.ofAddress(ObjCRuntime.sel_getUid("styleMask"));
        final var signature = FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.ADDRESS);

        final var handle = linker.downcallHandle(msgSend, signature);

        try {
            final var mask = (long) handle.invokeExact(nativeWindow, method);
            return (mask & NSFullScreenWindowMask) != 0;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void setCocoaFullscreen(boolean fullscreen, long lwjglWindow) {
        // macOS only has a toggle method, ensure we don't accidentally mess up our world view.
        if (fullscreen == isCocoaFullscreen(lwjglWindow)) return;

        final var nativeWindow = MemorySegment.ofAddress(GLFWNativeCocoa.glfwGetCocoaWindow(lwjglWindow));
        final var method = MemorySegment.ofAddress(ObjCRuntime.sel_getUid("toggleFullScreen:"));
        final var signature = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS);

        final var handle = linker.downcallHandle(msgSend, signature);

        try {
            handle.invokeExact(nativeWindow, method);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Fairly based on this article: http://svenandersson.se/2016/rendering-to-full-size-of-an-nswindow-using-glfw3.html
     * Still not finished (Window dragging is not working) but useful stuff here to interact with native OS
     *
     * @param lwjglWindow lwjgl pointer to window object
     * @param FullSizeContentView apply the FullSizeContentView styleMask
     */
    public static void setCocoaCustomTitleBar(long lwjglWindow, boolean FullSizeContentView) {
        System.out.println(lwjglWindow);
        long nswindow = GLFWNativeCocoa.glfwGetCocoaWindow(lwjglWindow);
        System.out.println(nswindow);
        SharedLibrary objc = ObjCRuntime.getLibrary();
        long objc_msgSend = objc.getFunctionAddress("objc_msgSend");

        boolean bool = JNI.invokePPZ(nswindow, ObjCRuntime.sel_getUid("titlebarAppearsTransparent"), objc_msgSend);
        System.out.println("titlebarAppearsTransparent = " + bool);

        JNI.invokePPV(nswindow, ObjCRuntime.sel_getUid("setTitlebarAppearsTransparent:"), true, objc_msgSend);
        JNI.invokePPV(nswindow, ObjCRuntime.sel_getUid("setMovableByWindowBackground:"), true, objc_msgSend);

        bool = JNI.invokePPZ(nswindow, ObjCRuntime.sel_getUid("titlebarAppearsTransparent"), objc_msgSend);
        System.out.println("titlebarAppearsTransparent = " + bool);

        long NSColor = ObjCRuntime.objc_getClass("NSColor");
        long darkGrayColor = JNI.invokePPP(NSColor, ObjCRuntime.sel_getUid("darkGrayColor"), objc_msgSend);
        JNI.invokePPPV(nswindow, ObjCRuntime.sel_getUid("setBackgroundColor:"), darkGrayColor, objc_msgSend);
        //0 - visible
        //1 - hidden
        JNI.invokePPV(nswindow, ObjCRuntime.sel_getUid("setTitleVisibility:"), 1, objc_msgSend);

        if (FullSizeContentView) {
            //Borderless             - 0b0000000000000000
            //Titled                 - 0b0000000000000001
            //Closable               - 0b0000000000000010
            //Miniaturizable         - 0b0000000000000100
            //Resizable              - 0b0000000000001000
            //UtilityWindow          - 0b0000000000010000
            //DocumentModalWindow    - 0b0000000001000000
            //NonActivatingPanel     - 0b0000000010000000
            //Textured               - 0b0000000100000000
            //UnifiedTitleAndToolbar - 0b0001000000000000
            //HudWindow              - 0b0010000000000000
            //FullScreen             - 0b0100000000000000
            //FullSizeContentView    - 0b1000000000010010
            JNI.invokePPV(nswindow, ObjCRuntime.sel_getUid("setStyleMask:"), 0b1001000000001111, objc_msgSend);

            int defaultMask = JNI.invokePPI(nswindow, ObjCRuntime.sel_getUid("styleMask"), objc_msgSend);
            System.out.println("styleMask = " + defaultMask);

            GLFW.glfwMaximizeWindow(lwjglWindow);
        }
    }

    public static void performCocoaWindowDrag(long lwjglWindow) {
        long nswindow = GLFWNativeCocoa.glfwGetCocoaWindow(lwjglWindow);
        SharedLibrary objc = ObjCRuntime.getLibrary();
        long objc_msgSend = objc.getFunctionAddress("objc_msgSend");

        long nsAppClass = ObjCRuntime.objc_getClass("NSApplication");
        long sharedApp = JNI.invokePPP(nsAppClass, ObjCRuntime.sel_getUid("sharedApplication"), objc_msgSend);

        long currentEvent = JNI.invokePPP(sharedApp, ObjCRuntime.sel_getUid("currentEvent"), objc_msgSend);
        // NSEventTypeLeftMouseDown = 1, NSEventTypeLeftMouseDragged = 6
        long type = JNI.invokePPJ(currentEvent, ObjCRuntime.sel_getUid("type"), objc_msgSend);
        if (type != 1 && type != 6) {
            return;
        }

        long clickCount = JNI.invokePPJ(currentEvent, ObjCRuntime.sel_getUid("clickCount"), objc_msgSend);

        if (clickCount == 2) {
            JNI.invokePPV(nswindow, ObjCRuntime.sel_getUid("zoom:"), 0, objc_msgSend);
        } else {
            JNI.invokePPPV(nswindow, ObjCRuntime.sel_getUid("performWindowDragWithEvent:"), currentEvent, objc_msgSend);
        }
    }

    public static void setCocoaWindowDragListener(Actor actor, long lwjglWindow) {
        actor.addListener(new InputListener() {
            private final long context = lwjglWindow;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                performCocoaWindowDrag(context);
                return true;
            }
        });
    }

    /**
     * Function to override Windows behavior to use undecorated style while preserving snap to edge and resize.
     * Works only on Windows OS
     * From: https://gist.github.com/SWinxy/d48469ac83834219a0044c2c5df55f56
     * @author SWinxy
     *
     * @param lwjglWindow lwjgl pointer to window object
     */
    public static void overwriteWindowProc2(long lwjglWindow) {
        if (!SystemUtils.IS_OS_WINDOWS) return;

        long hwnd = GLFWNativeWin32.glfwGetWin32Window(lwjglWindow);
        long pWindowProc = User32.GetWindowLongPtr(hwnd, User32.GWL_WNDPROC);
        System.out.println("oldptr: " + pWindowProc);

        WindowProc proc = new WindowProc() {
            private final Vector2 tmp = new Vector2();
            private final DoubleBuffer cursorX = BufferUtils.createDoubleBuffer(1);
            private final DoubleBuffer cursorY = BufferUtils.createDoubleBuffer(1);
            private RECT rect;

            private int getX() {
                return MathUtils.floor((float) cursorX.get(0));
            }
            private int getY() {
                return MathUtils.floor((float) cursorY.get(0));
            }

            @Override
            public long invoke(long hwnd, int uMsg, long wParam, long lParam) {
                long style = User32.GetWindowLongPtr(hwnd, User32.GWL_STYLE);
                if ((style & User32.WS_POPUP) != 0) {
                    return JNI.callPPPP(hwnd, uMsg, wParam, lParam, pWindowProc);
                }

                if (uMsg == User32.WM_NCHITTEST) {
                    try (MemoryStack stack = MemoryStack.stackPush()) {
                        short x = (short) (lParam & 0xFFFF);
                        short y = (short) ((lParam & 0xFFFF0000) >> 16);
                        GLFW.glfwGetCursorPos(GLFW.glfwGetCurrentContext(), cursorX, cursorY);

                        if (rect == null)
                            rect = RECT.calloc(stack);
                        User32.GetWindowRect(hwnd, rect);

                        if (y < rect.top() + 16 && x < rect.left() + 16) {
                            return User32.HTTOPLEFT;
                        }
                        if (y > rect.bottom() - 16 && x > rect.right() - 16) {
                            return User32.HTBOTTOMRIGHT;
                        }
                        if (y < rect.top() + 12 && x > rect.right() - 16) {
                            return User32.HTTOPRIGHT;
                        }
                        if (y > rect.bottom() - 16 && x < rect.left() + 16) {
                            return User32.HTBOTTOMLEFT;
                        }

                        if (y < rect.top() + 8) {
                            return User32.HTTOP;
                        }
                        if (x < rect.left() + 16) {
                            return User32.HTLEFT;
                        }
                        if (y > rect.bottom() - 16) {
                            return User32.HTBOTTOM;
                        }
                        if (x > rect.right() - 16) {
                            return User32.HTRIGHT;
                        }

                        //Test if the pointer is in Title Bar
                        UIWindowTitleMediator uiWindowTitleMediator = Facade.getInstance().retrieveMediator(UIWindowTitleMediator.NAME);
                        UIWindowTitle uiWindowTitle = uiWindowTitleMediator.getViewComponent();

                        int glfwX = getX();
                        int glfwY = getY();

                        uiWindowTitle.getStage().screenToStageCoordinates(tmp.set(glfwX, glfwY));
                        if (uiWindowTitle.getStage().hit(tmp.x, tmp.y, true) == uiWindowTitle) {
                            return User32.HTCAPTION;
                        }

                        return JNI.callPPPP(hwnd, uMsg, wParam, lParam, pWindowProc);
                    }
                }
                if (uMsg == User32.WM_NCCALCSIZE) {
                    if (wParam == 1) {
                        try (MemoryStack stack = MemoryStack.stackPush()) {
                            WINDOWPLACEMENT windowplacement = WINDOWPLACEMENT.calloc(stack);
                            User32.GetWindowPlacement(hwnd, windowplacement);
                            // ...but instead we're gonna just pretend it's just a RECT struct
                            // the NCCALCSIZE_PARAMS struct conveniently has what we need
                            // at the very start, so we can quietly say it's a RECT struct lol
                            // hacky because LWJGL doesn't include the structs to the aforementioned
                            // struct, nor some of the other structs contained
                            RECT rect = RECT.create(lParam);
                            if (windowplacement.showCmd() != User32.SW_MAXIMIZE) {
                                rect.left(rect.left() + 8);
//								rect.top(rect.top() + 0);
                                rect.right(rect.right() - 8);
                                rect.bottom(rect.bottom() - 8);
                            } else {
                                rect.left(rect.left() + 8);
                                rect.top(rect.top() + 8);
                                rect.right(rect.right() - 8);
                                rect.bottom(rect.bottom() - 8);
                            }

                            return rect.address();
                        }
                    }
                }
                return JNI.callPPPP(hwnd, uMsg, wParam, lParam, pWindowProc);
            }
        };
        System.out.println("procaddr: " + proc.address());
        System.out.println("setptr: " + User32.SetWindowLongPtr(hwnd, User32.GWL_WNDPROC, proc.address()));
        System.out.println("setwinptr: " + User32.SetWindowPos(hwnd, 0, 0, 0, 0, 0, User32.SWP_NOMOVE | User32.SWP_NOSIZE | User32.SWP_NOZORDER | User32.SWP_FRAMECHANGED));
    }
}
