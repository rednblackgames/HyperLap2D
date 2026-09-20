package games.rednblack.editor.view.ui.properties.panels;

import com.badlogic.gdx.utils.ObjectMap;
import games.rednblack.editor.controller.commands.component.UpdateWidgetDataCommand;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.NodeComponent;
import games.rednblack.editor.renderer.components.widget.WidgetComponent;
import games.rednblack.editor.renderer.components.widget.WidgetPartComponent;
import games.rednblack.editor.renderer.data.WidgetVO;
import games.rednblack.editor.renderer.widget.WidgetType;
import games.rednblack.editor.renderer.widget.WidgetTypes;
import games.rednblack.editor.view.ui.properties.UIItemPropertiesMediator;
import games.rednblack.h2d.common.MsgAPI;

public class UIWidgetPropertiesMediator extends UIItemPropertiesMediator<UIWidgetProperties> {

    private static final String TAG = UIWidgetPropertiesMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    private final ObjectMap<String, String> assignedParts = new ObjectMap<>();

    public UIWidgetPropertiesMediator() {
        super(NAME, new UIWidgetProperties());

        viewComponent.setCurrentStateProvider(() -> {
            if (!validReference()) return null;
            WidgetComponent widget = entityData.get(observableReference, WidgetComponent.class);
            return widget == null ? null : widget.getState();
        });
    }

    @Override
    protected void translateObservableDataToView(int entity) {
        WidgetComponent widget = entityData.get(entity, WidgetComponent.class);
        if (widget == null) return;

        assignedParts.clear();
        collectAssignedParts(entity);

        viewComponent.setWidget(WidgetTypes.get(widget.widgetType), widget.widgetType, widget.states,
                widget.properties, assignedParts);
    }

    private void collectAssignedParts(int parent) {
        NodeComponent node = entityData.get(parent, NodeComponent.class);
        if (node == null) return;

        for (int child : node.children) {
            WidgetPartComponent part = entityData.get(child, WidgetPartComponent.class);
            if (part != null && part.role != null && !part.role.isEmpty() && !assignedParts.containsKey(part.role)) {
                MainItemComponent mainItem = entityData.get(child, MainItemComponent.class);
                boolean named = mainItem.itemIdentifier != null && !mainItem.itemIdentifier.isEmpty();
                assignedParts.put(part.role, named ? mainItem.itemIdentifier : mainItem.uniqueId);
            }
            // the parts of a nested widget are its own business
            if (entityData.get(child, WidgetComponent.class) == null) collectAssignedParts(child);
        }
    }

    @Override
    protected void translateViewToItemData() {
        WidgetComponent widget = entityData.get(observableReference, WidgetComponent.class);
        if (widget == null) return;

        WidgetVO oldVo = new WidgetVO();
        oldVo.loadFromComponent(widget);

        WidgetVO newVo = new WidgetVO(oldVo);
        WidgetType type = WidgetTypes.get(widget.widgetType);
        ObjectMap<String, String> entered = viewComponent.getSettings();
        if (type != null) {
            for (WidgetType.Property property : type.properties) {
                String value = entered.get(property.key);
                // an invalid entry is dropped, the field shows the stored value again on the next update
                if (value == null || !isValid(property, value)) continue;
                newVo.properties.put(property.key, value);
            }
        }

        if (!oldVo.equals(newVo)) {
            facade.sendNotification(MsgAPI.ACTION_UPDATE_WIDGET_DATA, UpdateWidgetDataCommand.payload(observableReference, newVo));
        }
    }

    private boolean isValid(WidgetType.Property property, String value) {
        try {
            switch (property.kind) {
                case INT:
                    Integer.parseInt(value.trim());
                    return true;
                case FLOAT:
                    Float.parseFloat(value.trim());
                    return true;
                default:
                    return true;
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
