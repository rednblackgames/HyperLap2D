package games.rednblack.editor.proxy;

import com.badlogic.gdx.Gdx;
import games.rednblack.editor.HyperLap2DFacade;
import org.puremvc.java.patterns.proxy.Proxy;

public class WindowTitleManager extends Proxy {
    private static final String TAG = WindowTitleManager.class.getCanonicalName();
    public static final String NAME = TAG;

    private String currentWindowTitle = "";

    public WindowTitleManager() {
        super(NAME);
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();
    }

    public void setWindowTitle(String title) {
        currentWindowTitle = title;
        Gdx.graphics.setTitle(currentWindowTitle);
    }

    public void appendSaveHintTitle(boolean isModified) {
        if (!isModified) {
            Gdx.graphics.setTitle(currentWindowTitle);
        } else {
            Gdx.graphics.setTitle("\u25CF " + currentWindowTitle);
        }
    }
}
