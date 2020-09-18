package games.rednblack.editor.proxy;

import com.badlogic.gdx.Gdx;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.view.ui.UIWindowTitle;
import games.rednblack.editor.view.ui.UIWindowTitleMediator;
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
        UIWindowTitleMediator uiWindowTitleMediator = facade.retrieveMediator(UIWindowTitleMediator.NAME);
        UIWindowTitle uiWindowTitle = uiWindowTitleMediator.getViewComponent();
        uiWindowTitle.setTitle(currentWindowTitle);
    }

    public void appendSaveHintTitle(boolean isModified) {
        String title;
        if (!isModified) {
            title = currentWindowTitle;
        } else {
            title = "\u25CF " + currentWindowTitle;
        }
        Gdx.graphics.setTitle(title);
        UIWindowTitleMediator uiWindowTitleMediator = facade.retrieveMediator(UIWindowTitleMediator.NAME);
        UIWindowTitle uiWindowTitle = uiWindowTitleMediator.getViewComponent();
        uiWindowTitle.setTitle(title);
    }
}
