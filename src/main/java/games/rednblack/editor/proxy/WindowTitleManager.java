package games.rednblack.editor.proxy;

import com.badlogic.gdx.Gdx;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.Proxy;

public class WindowTitleManager extends Proxy {
    private static final String TAG = WindowTitleManager.class.getCanonicalName();
    public static final String NAME = TAG;

    private String currentWindowTitle = "";

    public WindowTitleManager() {
        super(NAME, null);
    }

    public void setWindowTitle(String title) {
        currentWindowTitle = title;
        Gdx.graphics.setTitle(currentWindowTitle);
        facade.sendNotification(MsgAPI.WINDOW_TITLE_CHANGED, currentWindowTitle);
    }

    public void appendSaveHintTitle(boolean isModified) {
        String title;
        if (!isModified) {
            title = currentWindowTitle;
        } else {
            title = "● " + currentWindowTitle;
        }
        Gdx.graphics.setTitle(title);
        facade.sendNotification(MsgAPI.WINDOW_TITLE_CHANGED, title);
    }
}