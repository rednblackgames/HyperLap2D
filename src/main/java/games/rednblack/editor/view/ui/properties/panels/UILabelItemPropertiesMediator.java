package games.rednblack.editor.view.ui.properties.panels;

import com.badlogic.ashley.core.Entity;
import games.rednblack.h2d.common.MsgAPI;
import com.puremvc.patterns.observer.Notification;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.FontManager;
import games.rednblack.editor.renderer.components.label.LabelComponent;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.view.ui.properties.UIItemPropertiesMediator;
import org.apache.commons.lang3.ArrayUtils;

public class UILabelItemPropertiesMediator extends UIItemPropertiesMediator<Entity, UILabelItemProperties> {

    private static final String TAG = UILabelItemPropertiesMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    private String prevText = null;

    private FontManager fontManager;

    public UILabelItemPropertiesMediator() {
        super(NAME, new UILabelItemProperties());
    }

    @Override
    public String[] listNotificationInterests() {
        final String[] parentInterests = super.listNotificationInterests();
        return ArrayUtils.add(parentInterests, UILabelItemProperties.LABEL_TEXT_CHAR_TYPED);
    }

    @Override
    public void handleNotification(Notification notification) {
        super.handleNotification(notification);
        if(notification.getName().equals(UILabelItemProperties.LABEL_TEXT_CHAR_TYPED)) {
            onTextChange();
        }
    }

    private void onTextChange() {
        LabelComponent labelComponent = ComponentRetriever.get(observableReference, LabelComponent.class);
        labelComponent.setText(viewComponent.getText());
    }

    @Override
    public void onRegister() {
        facade = HyperLap2DFacade.getInstance();
        fontManager = facade.retrieveProxy(FontManager.NAME);
        lockUpdates = true;
        viewComponent.setFontFamilyList(fontManager.getFontNamesFromMap());
        lockUpdates = false;
    }

    @Override
    protected void translateObservableDataToView(Entity item) {
        LabelComponent labelComponent = ComponentRetriever.get(item, LabelComponent.class);
        viewComponent.setFontFamily(labelComponent.fontName);
        viewComponent.setFontSize(labelComponent.fontSize);
        viewComponent.setAlignValue(labelComponent.labelAlign);
        viewComponent.setText(labelComponent.text.toString().replace("\\n", "\n"));
        viewComponent.setWrap(labelComponent.wrap);

        if(prevText == null) this.prevText = viewComponent.getText();
    }

    @Override
    protected void translateViewToItemData() {
        final String newText = viewComponent.getText();

        Object[] payload = new Object[7];
        payload[0] = observableReference;
        payload[1] = viewComponent.getFontFamily();
        payload[2] = viewComponent.getFontSize();
        payload[3] = viewComponent.getAlignValue();
        payload[4] = newText;
        payload[5] = prevText;
        payload[6] = viewComponent.isWrap();
        sendNotification(MsgAPI.ACTION_UPDATE_LABEL_DATA, payload);

        this.prevText = newText;
    }

    @Override
    protected void afterItemDataModified() {

    }
}
