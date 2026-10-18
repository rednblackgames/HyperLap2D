package games.rednblack.editor.view.ui.properties.panels;

import games.rednblack.editor.controller.commands.component.UpdateAutoLayoutDataCommand;
import games.rednblack.editor.renderer.components.AutoLayoutComponent;
import games.rednblack.editor.renderer.data.AutoLayoutVO;
import games.rednblack.editor.renderer.ecs.Component;
import games.rednblack.editor.view.ui.properties.UIRemovableComponentPropertiesMediator;
import games.rednblack.h2d.common.MsgAPI;

public class UIAutoLayoutPropertiesMediator extends UIRemovableComponentPropertiesMediator<UIAutoLayoutProperties> {

    private static final String TAG = UIAutoLayoutPropertiesMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    /** What the caption was last made from, so it is only made again when that changes. */
    private int captionKey = Integer.MIN_VALUE;

    public UIAutoLayoutPropertiesMediator() {
        super(NAME, new UIAutoLayoutProperties());
        viewComponent.setPreviewRefresher(this::refreshPreview);
    }

    @Override
    protected String getCloseClickedEventName() {
        return UIAutoLayoutProperties.CLOSE_CLICKED;
    }

    @Override
    protected Class<? extends Component> getComponentClass() {
        return AutoLayoutComponent.class;
    }

    @Override
    protected void translateObservableDataToView(int entity) {
        AutoLayoutComponent component = entityData.get(entity, AutoLayoutComponent.class);
        if (component == null) return;
        AutoLayoutVO vo = new AutoLayoutVO();
        vo.loadFromComponent(component);
        viewComponent.setData(vo);
    }

    @Override
    protected void translateViewToItemData() {
        AutoLayoutComponent component = entityData.get(observableReference, AutoLayoutComponent.class);
        if (component == null) return;

        AutoLayoutVO current = new AutoLayoutVO();
        current.loadFromComponent(component);
        AutoLayoutVO changed = viewComponent.getData(current);
        if (!current.equals(changed)) {
            facade.sendNotification(MsgAPI.ACTION_UPDATE_AUTO_LAYOUT_DATA, UpdateAutoLayoutDataCommand.payload(observableReference, changed));
        }
    }

    private void refreshPreview() {
        AutoLayoutPreviewData data = viewComponent.getPreviewData();
        if (!validReference()) {
            data.valid = false;
            return;
        }
        data.fill(observableReference, -1);

        AutoLayoutComponent layout = data.settings;
        int key = layout.mode.ordinal() + 4 * (data.sample ? 0 : data.items.size + 1)
                + 1000 * data.ignored.size + (layout.fitWidth ? 100000 : 0) + (layout.fitHeight ? 200000 : 0);
        if (key != captionKey) {
            captionKey = key;
            viewComponent.setCaption(caption(data));
        }
    }

    private static String caption(AutoLayoutPreviewData data) {
        AutoLayoutComponent layout = data.settings;
        StringBuilder text = new StringBuilder(UIAutoLayoutProperties.MODES[layout.mode.ordinal()]);
        if (data.sample) text.append(", no children yet");
        else text.append(", ").append(data.items.size).append(data.items.size == 1 ? " child" : " children");
        if (data.ignored.size > 0) text.append(", ").append(data.ignored.size).append(" left out");
        if (layout.fitWidth && layout.fitHeight) text.append(", fits content");
        else if (layout.fitWidth) text.append(", fits width");
        else if (layout.fitHeight) text.append(", fits height");
        return text.toString();
    }
}
