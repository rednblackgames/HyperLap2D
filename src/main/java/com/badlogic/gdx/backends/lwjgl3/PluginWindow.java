package com.badlogic.gdx.backends.lwjgl3;

import games.rednblack.h2d.common.plugins.H2DWindow;

/**
 * A window a plugin opened, and the one place that decides it is gone.
 * <p>
 * It lives in the backend's package because asking a window to render, which is how a plugin window stays
 * off the loop between frames, is not public API.
 */
public class PluginWindow implements H2DWindow {

    private final Lwjgl3Window window;
    private final Runnable onClose;
    private boolean open = true;

    public PluginWindow(Lwjgl3Window window, Runnable onClose) {
        this.window = window;
        this.onClose = onClose;

        window.setWindowListener(new Lwjgl3WindowAdapter() {
            @Override
            public boolean closeRequested() {
                closed();
                return true;
            }
        });
    }

    @Override
    public void close() {
        if (!open) return;

        //closeWindow only asks the loop to drop it, and never reaches the close callback
        closed();
        window.closeWindow();
    }

    @Override
    public void focus() {
        if (open) window.focusWindow();
    }

    @Override
    public void requestRender() {
        if (open) window.requestRendering();
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    private void closed() {
        if (!open) return;

        open = false;
        if (onClose != null) onClose.run();
    }
}
