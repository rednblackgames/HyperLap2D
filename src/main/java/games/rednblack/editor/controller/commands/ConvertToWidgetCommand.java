package games.rednblack.editor.controller.commands;

import games.rednblack.editor.proxy.WidgetEditingProxy;
import games.rednblack.editor.renderer.components.ParentNodeComponent;
import games.rednblack.editor.renderer.components.widget.WidgetComponent;
import games.rednblack.editor.renderer.ecs.Component;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.renderer.widget.WidgetType;
import games.rednblack.editor.renderer.widget.WidgetTypes;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.h2d.common.MsgAPI;

import java.util.Set;

/**
 * Turns the selection into a widget of the given type and opens it for editing. A single selected
 * composite becomes the widget itself, anything else is wrapped into a new composite first.
 */
public class ConvertToWidgetCommand extends ConvertToCompositeCommand {

    private String widgetTypeName;
    private boolean wrapped;

    @Override
    public void doAction() {
        if (widgetTypeName == null) {
            Object body = getNotification().getBody();
            widgetTypeName = body instanceof String ? (String) body : WidgetTypes.BUTTON;
        }
        WidgetType widgetType = WidgetTypes.get(widgetTypeName);

        Set<Integer> selection = sandbox.getSelector().getSelectedItems();
        if (widgetType == null || selection.isEmpty()) {
            cancel();
            return;
        }

        int entity;
        int single = selection.iterator().next();
        if (selection.size() == 1 && EntityUtils.getType(single) == EntityFactory.COMPOSITE_TYPE) {
            if (SandboxComponentRetriever.get(single, WidgetComponent.class) != null) {
                cancel();
                return;
            }
            entity = single;
            entityId = EntityUtils.getEntityId(entity);
            wrapped = false;
        } else {
            super.doAction();
            entity = EntityUtils.getByUniqueId(entityId);
            wrapped = true;
        }

        widgetType.setup(sandbox.getEngine().edit(entity).create(WidgetComponent.class));
        sandbox.getSceneControl().sceneLoader.getEntityFactory().attachWidgetBehaviour(entity);

        facade.sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
        facade.sendNotification(MsgAPI.ACTION_CAMERA_CHANGE_COMPOSITE, entity);
    }

    @Override
    public void undoAction() {
        int entity = EntityUtils.getByUniqueId(entityId);
        if (entity == -1) return;

        // never pull the composite from under the view
        if (isViewedFromInside(entity)) {
            ParentNodeComponent parentNode = SandboxComponentRetriever.get(entity, ParentNodeComponent.class);
            if (parentNode != null && parentNode.parentEntity != -1) {
                facade.sendNotification(MsgAPI.ACTION_CAMERA_CHANGE_COMPOSITE, parentNode.parentEntity);
            }
        }

        WidgetType widgetType = WidgetTypes.get(widgetTypeName);
        if (widgetType != null) {
            for (Class<? extends Component> behaviour : widgetType.behaviourComponents) {
                sandbox.getEngine().edit(entity).remove(behaviour);
            }
        }
        sandbox.getEngine().edit(entity).remove(WidgetComponent.class);

        if (wrapped) {
            super.undoAction();
        } else {
            sandbox.getEngine().process();
            facade.sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
        }
    }

    private boolean isViewedFromInside(int entity) {
        int viewed = sandbox.getCurrentViewingEntity();
        while (viewed != -1) {
            if (viewed == entity) return true;
            ParentNodeComponent parentNode = SandboxComponentRetriever.get(viewed, ParentNodeComponent.class);
            viewed = parentNode == null ? -1 : parentNode.parentEntity;
        }
        return false;
    }
}
