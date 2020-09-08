package games.rednblack.editor.view.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.editor.HyperLap2DApp;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

public class UIWindowAction extends VisTable {

    private VisImageButton maximizeButton;
    private boolean isMaximized;

    public UIWindowAction() {
        setBackground(VisUI.getSkin().getDrawable("menu-bg"));
        align(Align.top);
        VisImageButton iconifyButton = StandardWidgetsFactory.createImageButton("window-action-iconify");
        add(iconifyButton).padRight(-1);
        iconifyButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                HyperLap2DApp.getInstance().mainWindow.iconifyWindow();
            }
        });

        maximizeButton = StandardWidgetsFactory.createImageButton("window-action-maximize");
        add(maximizeButton).padRight(-1);
        maximizeButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (isMaximized) {
                    HyperLap2DApp.getInstance().mainWindow.restoreWindow();
                } else {
                    HyperLap2DApp.getInstance().mainWindow.maximizeWindow();
                }

            }
        });
        setMaximized(true);

        VisImageButton closeButton = StandardWidgetsFactory.createImageButton("window-action-close");
        add(closeButton);
        closeButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                HyperLap2DApp.getInstance().hyperlap2D.closeRequested();
            }
        });
    }

    public void setMaximized(boolean maximized) {
        isMaximized = maximized;
        if (maximized) {
            maximizeButton.setStyle(VisUI.getSkin().get("window-action-restore", VisImageButton.VisImageButtonStyle.class));
        } else {
            maximizeButton.setStyle(VisUI.getSkin().get("window-action-maximize", VisImageButton.VisImageButtonStyle.class));
        }
    }
}
