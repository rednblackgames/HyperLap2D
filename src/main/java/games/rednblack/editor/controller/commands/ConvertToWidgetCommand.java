package games.rednblack.editor.controller.commands;

import com.badlogic.gdx.math.Vector2;
import games.rednblack.editor.renderer.components.TintComponent;
import games.rednblack.editor.renderer.components.ZIndexComponent;
import games.rednblack.editor.factory.ItemFactory;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.components.ParentNodeComponent;
import games.rednblack.editor.renderer.components.widget.WidgetComponent;
import games.rednblack.editor.renderer.components.widget.WidgetPartComponent;
import games.rednblack.editor.renderer.ecs.Component;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.renderer.widget.WidgetType;
import games.rednblack.editor.renderer.widget.WidgetTypes;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.h2d.common.MsgAPI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Turns the selection into a widget of the given type and opens it for editing. A single selected
 * composite becomes the widget itself, anything else is wrapped into a new composite first.
 *
 * A type may ask for what it is made from to go into a part of its own: a scroll pane puts it all
 * into its content, which is the composite that slides behind the pane.
 */
public class ConvertToWidgetCommand extends ConvertToCompositeCommand {

    private String widgetTypeName;
    private boolean wrapped;
    /** The part the widget's contents were put into, null when the type asks for none. */
    private String contentId;
    /** The parts the editor drew along with the widget, so undo can take them away again. */
    private final List<String> defaultPartIds = new ArrayList<>();

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

        if (widgetType.wrapRole != null) contentId = EntityUtils.getEntityId(wrapContent(entity, widgetType.wrapRole));
        createDefaultParts(entity, widgetType);

        facade.sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
        facade.sendNotification(MsgAPI.ACTION_CAMERA_CHANGE_COMPOSITE, entity);
    }

    /**
     * Puts everything the widget holds into one composite playing the given role, sitting where its
     * contents already were, so nothing moves.
     *
     * @return the new part
     */
    private int wrapContent(int widget, String role) {
        HashSet<Integer> children = EntityUtils.getChildren(widget);

        int content = ItemFactory.get().createCompositeItem(new Vector2());
        HashSet<Integer> wrapper = new HashSet<>();
        wrapper.add(content);
        EntityUtils.changeParent(wrapper, widget);
        if (children != null && !children.isEmpty()) EntityUtils.changeParent(children, content);

        TransformComponent transform = SandboxComponentRetriever.get(content, TransformComponent.class);
        transform.x = 0;
        transform.y = 0;

        DimensionsComponent widgetSize = SandboxComponentRetriever.get(widget, DimensionsComponent.class);
        DimensionsComponent contentSize = SandboxComponentRetriever.get(content, DimensionsComponent.class);
        if (widgetSize != null && contentSize != null) {
            contentSize.width = widgetSize.width;
            contentSize.height = widgetSize.height;
            if (contentSize.boundBox != null) contentSize.boundBox.set(0, 0, widgetSize.width, widgetSize.height);
        }

        sandbox.getEngine().edit(content).create(WidgetPartComponent.class).role = role;
        MainItemComponent mainItem = SandboxComponentRetriever.get(content, MainItemComponent.class);
        if (mainItem != null) mainItem.itemIdentifier = role;

        sandbox.getEngine().process();
        return content;
    }

    /**
     * Draws the parts the type asks for out of an image every project has: a caret, a selection
     * band, the shapes nobody wants to make by hand. They are ordinary items from here on.
     */
    private void createDefaultParts(int widget, WidgetType widgetType) {
        defaultPartIds.clear();
        if (widgetType.defaultParts.size == 0) return;

        for (WidgetType.DefaultPart wanted : widgetType.defaultParts) {
            if (findPart(widget, wanted.role) != -1) continue;
            if (!ItemFactory.get().createSimpleImage(wanted.region, new Vector2())) continue;

            int part = ItemFactory.get().getCreatedEntity();
            HashSet<Integer> moved = new HashSet<>();
            moved.add(part);
            EntityUtils.changeParent(moved, widget);

            sandbox.getEngine().edit(part).create(WidgetPartComponent.class).role = wanted.role;

            MainItemComponent mainItem = SandboxComponentRetriever.get(part, MainItemComponent.class);
            if (mainItem != null) mainItem.itemIdentifier = wanted.role;

            DimensionsComponent dimensions = SandboxComponentRetriever.get(part, DimensionsComponent.class);
            if (dimensions != null) {
                dimensions.width = wanted.width;
                dimensions.height = wanted.height;
                if (dimensions.boundBox != null) dimensions.boundBox.set(0, 0, wanted.width, wanted.height);
            }

            if (wanted.tint != null) {
                TintComponent tint = SandboxComponentRetriever.get(part, TintComponent.class);
                if (tint != null) tint.color.set(wanted.tint);
            }

            ZIndexComponent zIndex = SandboxComponentRetriever.get(part, ZIndexComponent.class);
            if (zIndex != null) zIndex.setZIndex(wanted.zIndex);

            defaultPartIds.add(EntityUtils.getEntityId(part));
        }
        sandbox.getEngine().process();
    }

    /** Takes away the parts the editor drew, leaving anything the author made. */
    private void removeDefaultParts() {
        for (String id : defaultPartIds) {
            int part = EntityUtils.getByUniqueId(id);
            if (part == -1) continue;

            facade.sendNotification(MsgAPI.FOLLOWER_REMOVED, part);
            sandbox.getEngine().delete(part);
        }
        defaultPartIds.clear();
        sandbox.getEngine().process();
    }

    private int findPart(int widget, String role) {
        HashSet<Integer> children = EntityUtils.getChildren(widget);
        if (children == null) return -1;

        for (int child : children) {
            WidgetPartComponent part = SandboxComponentRetriever.get(child, WidgetPartComponent.class);
            if (part != null && role.equals(part.role)) return child;
        }
        return -1;
    }

    /** Takes the contents back out of the part they were put into, and drops it. */
    private void unwrapContent(int widget) {
        if (contentId == null) return;

        int content = EntityUtils.getByUniqueId(contentId);
        contentId = null;
        if (content == -1) return;

        HashSet<Integer> children = EntityUtils.getChildren(content);
        if (children != null && !children.isEmpty()) EntityUtils.changeParent(children, widget);

        facade.sendNotification(MsgAPI.FOLLOWER_REMOVED, content);
        sandbox.getEngine().delete(content);
        sandbox.getEngine().process();
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
        removeDefaultParts();
        unwrapContent(entity);

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
