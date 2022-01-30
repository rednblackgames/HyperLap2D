package games.rednblack.editor.view.ui.widget;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.editor.HyperLap2DApp;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.SettingsManager;
import org.apache.commons.lang3.SystemUtils;

public class H2DLogo extends VisTable {
    public H2DLogo() {
        SettingsManager settingsManager = HyperLap2DFacade.getInstance().retrieveProxy(SettingsManager.NAME);
        Skin skin = VisUI.getSkin();
        setBackground(skin.getDrawable("menu-bg"));
        VisImage logo = new VisImage(VisUI.getSkin().getDrawable("logo"));
        float pad = SystemUtils.IS_OS_MAC ? 73 : 7;
        pad *= settingsManager.editorConfigVO.uiScaleDensity;
        add(logo).width(logo.getWidth()).height(logo.getHeight()).padLeft(pad);
        logo.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                if (getTapCount() == 2)
                    HyperLap2DApp.getInstance().showUISplashWindow();
            }
        });
    }
}
