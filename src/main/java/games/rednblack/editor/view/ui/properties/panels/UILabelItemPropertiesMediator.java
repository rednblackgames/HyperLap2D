package games.rednblack.editor.view.ui.properties.panels;
import games.rednblack.editor.controller.commands.component.LabelDataPayload;

import games.rednblack.editor.proxy.FontManager;
import games.rednblack.editor.proxy.ResourceManager;
import games.rednblack.editor.renderer.components.label.LabelComponent;
import games.rednblack.editor.view.ui.properties.UIItemPropertiesMediator;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.interfaces.INotification;
import games.rednblack.puremvc.util.Interests;
import org.apache.commons.lang3.ArrayUtils;

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
        fontManager = facade.retrieveProxy(FontManager.NAME);
        resourceManager = facade.retrieveProxy(ResourceManager.NAME);
        lockUpdates = true;
        viewComponent.setFontFamilyList(fontManager.getFontNamesFromMap());
        viewComponent.setBitmapFontList(resourceManager.getBitmapFontList());
        lockUpdates = false;
    }

    @Override
    public void listNotificationInterests(Interests interests) {
        super.listNotificationInterests(interests);
        interests.add(UILabelItemProperties.LABEL_TEXT_CHAR_TYPED,
                UILabelItemProperties.LABEL_TEXT_EXPAND_SAVED);
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
        LabelComponent labelComponent = entityData.get(observableReference, LabelComponent.class);
        labelComponent.setText(viewComponent.getText());
    }

    @Override
    protected void translateObservableDataToView(int item) {
        LabelComponent labelComponent = entityData.get(item, LabelComponent.class);
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

        sendNotification(MsgAPI.ACTION_UPDATE_LABEL_DATA, new LabelDataPayload(
                observableReference,
                viewComponent.getFontFamily(),
                viewComponent.getFontSize(),
                viewComponent.getAlignValue(),
                newText,
                prevText,
                viewComponent.isWrap(),
                viewComponent.isMono(),
                viewComponent.getBitmapFont().equals(UILabelItemProperties.NONE_BITMAP_FONT) ? null : viewComponent.getBitmapFont()));

        this.prevText = newText;
    }

    @Override
    protected void afterItemDataModified() {

    }
}
