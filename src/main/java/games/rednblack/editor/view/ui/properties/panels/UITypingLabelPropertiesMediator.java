package games.rednblack.editor.view.ui.properties.panels;

import games.rednblack.editor.renderer.ecs.Component;
import games.rednblack.editor.view.ui.properties.UIRemovableComponentPropertiesMediator;
import games.rednblack.h2d.extension.typinglabel.TypingLabelComponent;
import games.rednblack.puremvc.interfaces.INotification;
import games.rednblack.puremvc.util.Interests;
import org.apache.commons.lang3.ArrayUtils;

public class UITypingLabelPropertiesMediator extends UIRemovableComponentPropertiesMediator<UITypingLabelProperties> {
    private static final String TAG = UITypingLabelPropertiesMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    public UITypingLabelPropertiesMediator() {
        super(NAME, new UITypingLabelProperties());
    }

    @Override
    protected String getCloseClickedEventName() {
        return UITypingLabelProperties.CLOSE_CLICKED;
    }

    @Override
    protected Class<? extends Component> getComponentClass() {
        return TypingLabelComponent.class;
    }

    @Override
    public void listNotificationInterests(Interests interests) {
        super.listNotificationInterests(interests);
        interests.add(UITypingLabelProperties.RESTART_BUTTON_CLICKED);
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);

        switch (notification.getName()) {
            case UITypingLabelProperties.RESTART_BUTTON_CLICKED:
                restartTypingLabel();
                break;
        }
    }

    private void restartTypingLabel() {
        TypingLabelComponent typingLabelComponent = entityData.get(observableReference, TypingLabelComponent.class);
        typingLabelComponent.typingLabel.restart();
    }

    @Override
    protected void translateObservableDataToView(int item) {

    }

    @Override
    protected void translateViewToItemData() {

    }
}
