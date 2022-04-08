package games.rednblack.editor.view.ui.properties.panels;

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.FontManager;
import games.rednblack.editor.proxy.ResourceManager;
import games.rednblack.editor.view.stage.tools.TextTool;
import games.rednblack.editor.view.ui.properties.UIAbstractPropertiesMediator;

/**
 * Created by avetiszakharyan on 4/24/15.
 */
public class UITextToolPropertiesMediator extends UIAbstractPropertiesMediator<TextTool, UITextToolProperties> {

    private static final String TAG = UITextToolPropertiesMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    private FontManager fontManager;
    private ResourceManager resourceManager;

    public UITextToolPropertiesMediator() {
        super(NAME, new UITextToolProperties());
    }

    @Override
    public void onRegister() {
        facade = HyperLap2DFacade.getInstance();
        fontManager = facade.retrieveProxy(FontManager.NAME);
        resourceManager = facade.retrieveProxy(ResourceManager.NAME);
        viewComponent.setFontFamilyList(fontManager.getFontNamesFromMap());
        viewComponent.setBitmapFontList(resourceManager.getBitmapFontList());
    }

    @Override
    protected void translateObservableDataToView(TextTool item) {
        viewComponent.setFontFamily(item.getFontFamily());
        viewComponent.setFontSize(item.getFontSize());
    }

    @Override
    public void setItem(TextTool settings) {
        super.setItem(settings);
        observableReference.setFontFamily(viewComponent.getFontFamily());

    }

    @Override
    protected void translateViewToItemData() {
        observableReference.setFontFamily(viewComponent.getFontFamily());
        observableReference.setFontSize(viewComponent.getFontSize());
    }
}
