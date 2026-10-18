package games.rednblack.editor.view.ui.properties.panels;

import games.rednblack.editor.controller.commands.component.UpdateAutoLayoutItemDataCommand;
import games.rednblack.editor.renderer.components.AutoLayoutComponent;
import games.rednblack.editor.renderer.components.AutoLayoutItemComponent;
import games.rednblack.editor.renderer.data.AutoLayoutItemVO;
import games.rednblack.editor.utils.runtime.AutoLayoutEditing;
import games.rednblack.editor.view.ui.properties.UIItemPropertiesMediator;
import games.rednblack.h2d.common.MsgAPI;

public class UIAutoLayoutItemPropertiesMediator extends UIItemPropertiesMediator<UIAutoLayoutItemProperties> {

    private static final String TAG = UIAutoLayoutItemPropertiesMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    public UIAutoLayoutItemPropertiesMediator() {
        super(NAME, new UIAutoLayoutItemProperties());
        viewComponent.setPreviewRefresher(this::refreshPreview);
    }

    @Override
    protected void translateObservableDataToView(int entity) {
        int parent = AutoLayoutEditing.layoutParent(entity);
        if (parent == -1) return;
        AutoLayoutComponent layout = entityData.get(parent, AutoLayoutComponent.class);
        viewComponent.setData(layout.mode, current(entity));
    }

    @Override
    protected void translateViewToItemData() {
        if (AutoLayoutEditing.layoutParent(observableReference) == -1) return;
        AutoLayoutItemVO current = current(observableReference);
        AutoLayoutItemVO changed = viewComponent.getData(current);
        if (!current.equals(changed)) {
            facade.sendNotification(MsgAPI.ACTION_UPDATE_AUTO_LAYOUT_ITEM_DATA, UpdateAutoLayoutItemDataCommand.payload(observableReference, changed));
        }
    }

    private AutoLayoutItemVO current(int entity) {
        AutoLayoutItemVO vo = new AutoLayoutItemVO();
        AutoLayoutItemComponent component = entityData.get(entity, AutoLayoutItemComponent.class);
        if (component != null) vo.loadFromComponent(component);
        return vo;
    }

    private void refreshPreview() {
        AutoLayoutPreviewData data = viewComponent.getPreviewData();
        int parent = validReference() ? AutoLayoutEditing.layoutParent(observableReference) : -1;
        if (parent == -1) {
            data.valid = false;
            return;
        }
        data.fill(parent, observableReference);
    }
}
