package games.rednblack.editor.view.ui.properties.panels;

import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.event.ButtonToNotificationListener;
import games.rednblack.editor.view.ui.properties.UIRemovableProperties;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

public class UITypingLabelProperties extends UIRemovableProperties {

    public static final String prefix = "games.rednblack.editor.view.ui.properties.panels.UITypingLabelProperties";
    public static final String CLOSE_CLICKED = prefix + ".CLOSE_CLICKED";
    public static final String RESTART_BUTTON_CLICKED = prefix + ".RESTART_BUTTON_CLICKED";

    public UITypingLabelProperties() {
        super("Typing Label");

        TextButton restartButton = StandardWidgetsFactory.createTextButton("Restart");
        mainTable.add(restartButton);

        restartButton.addListener(new ButtonToNotificationListener(RESTART_BUTTON_CLICKED));
    }

    @Override
    public void onRemove() {
        HyperLap2DFacade.getInstance().sendNotification(CLOSE_CLICKED);
    }
}
