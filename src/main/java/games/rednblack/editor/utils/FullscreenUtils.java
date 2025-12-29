package games.rednblack.editor.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import games.rednblack.editor.HyperLap2DApp;
import org.apache.commons.lang3.SystemUtils;

public class FullscreenUtils {
    public static boolean isFullscreen() {
        if (SystemUtils.IS_OS_MAC) {
            return HyperLap2DUtils.isCocoaFullscreen(HyperLap2DApp.getInstance().mainWindow.getWindowHandle());
        } else {
            return Gdx.graphics.isFullscreen();
        }
    }

    public static void setFullscreen(boolean fullscreen) {
        if (SystemUtils.IS_OS_MAC) {
            HyperLap2DUtils.setCocoaFullscreen(fullscreen, HyperLap2DApp.getInstance().mainWindow.getWindowHandle());
        } else {
            Graphics.DisplayMode currentMode = Gdx.graphics.getDisplayMode();
            if (fullscreen)
                Gdx.graphics.setFullscreenMode(currentMode);
            else
                Gdx.graphics.setWindowedMode(currentMode.width, currentMode.height);
        }
    }
}
