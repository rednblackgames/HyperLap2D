package games.rednblack.editor.view.ui.properties.panels;

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.FontManager;
import games.rednblack.editor.proxy.ResourceManager;
import games.rednblack.editor.renderer.components.label.LabelComponent;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.ui.properties.UIItemPropertiesMediator;
import games.rednblack.h2d.common.MsgAPI;
import org.apache.commons.lang3.ArrayUtils;
import org.puremvc.java.interfaces.INotification;

public class UILabelItemPropertiesMediator extends UIItemPropertiesMediator<UILabelItemProperties> {

    private static final String TAG = UILabelItemPropertiesMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    private String prevText = null;

    private FontManager fontManager;
    private ResourceManager resourceManager;

    public UILabelItemPropertiesMediator() {
        super(NAME, new UILabelItemProperties());
    }

    @Override
    public void onRegister() {
        facade = HyperLap2DFacade.getInstance();
        fontManager = facade.retrieveProxy(FontManager.NAME);
        resourceManager = facade.retrieveProxy(ResourceManager.NAME);
        lockUpdates = true;
        viewComponent.setFontFamilyList(fontManager.getFontNamesFromMap());
        viewComponent.setBitmapFontList(resourceManager.getBitmapFontList());
        lockUpdates = false;
    }

    @Override
    public String[] listNotificationInterests() {
        String[] defaultNotifications = super.listNotificationInterests();
        String[] notificationInterests = new String[]{
                UILabelItemProperties.LABEL_TEXT_CHAR_TYPED,
                UILabelItemProperties.LABEL_TEXT_EXPAND_SAVED
        };

        return ArrayUtils.addAll(defaultNotifications, notificationInterests);
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);

        switch (notification.getName()) {
            case UILabelItemProperties.LABEL_TEXT_CHAR_TYPED:
                onTextChange();
                break;
            case UILabelItemProperties.LABEL_TEXT_EXPAND_SAVED:
                viewComponent.setText(notification.getBody());
                facade.sendNotification(viewComponent.getUpdateEventName());
                break;
        }
    }

    private void onTextChange() {
        LabelComponent labelComponent = SandboxComponentRetriever.get(observableReference, LabelComponent.class);
        labelComponent.setText(viewComponent.getText());
    }

    @Override
    protected void translateObservableDataToView(int item) {
        LabelComponent labelComponent = SandboxComponentRetriever.get(item, LabelComponent.class);
        viewComponent.setFontFamily(labelComponent.fontName);
        viewComponent.setFontSize(labelComponent.fontSize);
        viewComponent.setAlignValue(labelComponent.labelAlign);
        viewComponent.setText(labelComponent.text.toString().replace("\\n", "\n"));
        viewComponent.setWrap(labelComponent.wrap);
        viewComponent.setMono(labelComponent.mono);
        viewComponent.setBitmapFontFamily(labelComponent.bitmapFont != null ? labelComponent.bitmapFont : UILabelItemProperties.NONE_BITMAP_FONT);

        if(prevText == null) this.prevText = viewComponent.getText();
    }

    @Override
    protected void translateViewToItemData() {
        final String newText = viewComponent.getText();

        Object[] payload = new Object[9];
        payload[0] = observableReference;
        payload[1] = viewComponent.getFontFamily();
        payload[2] = viewComponent.getFontSize();
        payload[3] = viewComponent.getAlignValue();
        payload[4] = newText;
        payload[5] = prevText;
        payload[6] = viewComponent.isWrap();
        payload[7] = viewComponent.isMono();
        payload[8] = viewComponent.getBitmapFont().equals(UILabelItemProperties.NONE_BITMAP_FONT) ? null : viewComponent.getBitmapFont();
        sendNotification(MsgAPI.ACTION_UPDATE_LABEL_DATA, payload);

        this.prevText = newText;
    }

    @Override
    protected void afterItemDataModified() {

    }
}
