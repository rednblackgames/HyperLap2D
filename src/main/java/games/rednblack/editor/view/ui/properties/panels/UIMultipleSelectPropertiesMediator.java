package games.rednblack.editor.view.ui.properties.panels;

import games.rednblack.editor.view.ui.properties.UIAbstractPropertiesMediator;
import org.apache.commons.lang3.ArrayUtils;
import org.puremvc.java.interfaces.INotification;

import java.util.Set;

public class UIMultipleSelectPropertiesMediator extends UIAbstractPropertiesMediator<Set<Integer>, UIMultipleSelectProperties> {
    private static final String TAG = UIMultipleSelectPropertiesMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    public UIMultipleSelectPropertiesMediator() {
        super(NAME, new UIMultipleSelectProperties());
    }

    @Override
    public String[] listNotificationInterests() {
        String[] defaultNotifications = super.listNotificationInterests();
        String[] notificationInterests = new String[]{
        };

        return ArrayUtils.addAll(defaultNotifications, notificationInterests);
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
    }

    @Override
    protected void translateObservableDataToView(Set<Integer> selection) {
        viewComponent.setSelectionCount(selection.size());
    }

    @Override
    protected void translateViewToItemData() {

    }
}
