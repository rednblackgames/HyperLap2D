package games.rednblack.editor.view.ui.properties.panels;

import games.rednblack.editor.controller.commands.RemoveComponentFromItemCommand;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.ui.properties.UIItemPropertiesMediator;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.extension.typinglabel.TypingLabelComponent;
import games.rednblack.puremvc.Facade;
import games.rednblack.puremvc.interfaces.INotification;
import games.rednblack.puremvc.util.Interests;
import org.apache.commons.lang3.ArrayUtils;

public class UITypingLabelPropertiesMediator extends UIItemPropertiesMediator<UITypingLabelProperties> {
    private static final String TAG = UITypingLabelPropertiesMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    public UITypingLabelPropertiesMediator() {
        super(NAME, new UITypingLabelProperties());
    }

    @Override
    public void listNotificationInterests(Interests interests) {
        super.listNotificationInterests(interests);
        interests.add(UITypingLabelProperties.CLOSE_CLICKED,
                UITypingLabelProperties.RESTART_BUTTON_CLICKED);
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);

        switch (notification.getName()) {
            case UITypingLabelProperties.CLOSE_CLICKED:
                Facade.getInstance().sendNotification(MsgAPI.ACTION_REMOVE_COMPONENT, RemoveComponentFromItemCommand.payload(observableReference, TypingLabelComponent.class));
                break;
            case UITypingLabelProperties.RESTART_BUTTON_CLICKED:
                restartTypingLabel();
                break;
        }
    }

    private void restartTypingLabel() {
        TypingLabelComponent typingLabelComponent = SandboxComponentRetriever.get(observableReference, TypingLabelComponent.class);
        typingLabelComponent.typingLabel.restart();
    }

    @Override
    protected void translateObservableDataToView(int item) {

    }

    @Override
    protected void translateViewToItemData() {

    }
}
