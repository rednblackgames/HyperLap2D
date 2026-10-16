package games.rednblack.editor.controller.commands;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import games.rednblack.editor.renderer.components.CompositeTransformComponent;
import games.rednblack.editor.renderer.components.LayoutComponent;
import games.rednblack.editor.renderer.components.TintComponent;
import games.rednblack.editor.renderer.components.label.LabelComponent;
import games.rednblack.editor.renderer.components.ZIndexComponent;
import games.rednblack.editor.factory.ItemFactory;
import games.rednblack.editor.proxy.FontManager;
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
import games.rednblack.editor.view.stage.tools.TextTool;
import games.rednblack.editor.view.ui.box.UIToolBoxMediator;
import games.rednblack.h2d.common.view.tools.Tool;
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
    /** The item the widget was made from, when it alone was, and what it was given to play its role. */
    private String convertedItemId;
    private boolean convertedItemGotPart, convertedItemGotLayout;
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
        if (wrapped && widgetType.convertedItemRole != null) giveRole(entity, widgetType.convertedItemRole);
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
     * When one item alone was made into the widget, it plays the given role and is stretched to the
     * widget with constraints, so it follows when the widget is resized. An item that already plays a
     * role, or already has constraints, keeps them.
     */
    private void giveRole(int widget, String role) {
        convertedItemId = null;
        HashSet<Integer> children = EntityUtils.getChildren(widget);
        if (children == null || children.size() != 1) return;

        int item = children.iterator().next();
        convertedItemId = EntityUtils.getEntityId(item);

        convertedItemGotPart = SandboxComponentRetriever.get(item, WidgetPartComponent.class) == null;
        if (convertedItemGotPart) sandbox.getEngine().edit(item).create(WidgetPartComponent.class).role = role;

        convertedItemGotLayout = SandboxComponentRetriever.get(item, LayoutComponent.class) == null;
        if (convertedItemGotLayout) {
            WidgetType.DefaultPart stretched = WidgetType.DefaultPart.composite(role, 0, 0).matchWidth(0).matchHeight(0);
            constrainToParent(item, stretched);
        }
    }

    /** Takes back what {@link #giveRole} gave, leaving the item as it was before. */
    private void takeRoleBack() {
        if (convertedItemId == null) return;
        int item = EntityUtils.getByUniqueId(convertedItemId);
        convertedItemId = null;
        if (item == -1) return;

        if (convertedItemGotPart) sandbox.getEngine().edit(item).remove(WidgetPartComponent.class);
        if (convertedItemGotLayout) sandbox.getEngine().edit(item).remove(LayoutComponent.class);
    }

    /**
     * Makes the parts the type asks for and the author did not bring: the shapes nobody wants to
     * draw by hand, and whatever a widget needs before it can show anything, like the list and the
     * row of a select box. They are ordinary items from here on.
     */
    private void createDefaultParts(int widget, WidgetType widgetType) {
        defaultPartIds.clear();
        if (widgetType.defaultParts.size == 0) return;

        DimensionsComponent widgetSize = SandboxComponentRetriever.get(widget, DimensionsComponent.class);
        float widgetWidth = widgetSize == null ? 0 : widgetSize.width;
        float widgetHeight = widgetSize == null ? 0 : widgetSize.height;

        for (WidgetType.DefaultPart wanted : widgetType.defaultParts) {
            if (wanted.role != null && findPart(widget, wanted.role) != -1) continue;

            int part = createDefaultItem(widget, wanted, widgetWidth, widgetHeight);
            if (part != -1) defaultPartIds.add(EntityUtils.getEntityId(part));
        }
        sandbox.getEngine().process();
    }

    /**
     * Makes one default item inside the given parent, then what it holds, then - once it holds it -
     * the widget it is to be.
     *
     * @return the item, -1 when it could not be made
     */
    private int createDefaultItem(int parent, WidgetType.DefaultPart wanted, float widgetWidth, float widgetHeight) {
        int item = createItemOfKind(wanted);
        if (item == -1) return -1;

        HashSet<Integer> moved = new HashSet<>();
        moved.add(item);
        EntityUtils.changeParent(moved, parent);

        if (wanted.role != null) {
            sandbox.getEngine().edit(item).create(WidgetPartComponent.class).role = wanted.role;
        }
        MainItemComponent mainItem = SandboxComponentRetriever.get(item, MainItemComponent.class);
        if (mainItem != null && wanted.role != null) mainItem.itemIdentifier = wanted.role;

        TransformComponent transform = SandboxComponentRetriever.get(item, TransformComponent.class);
        if (transform != null) {
            transform.x = wanted.x;
            transform.y = wanted.y;
        }

        float width = wanted.width(widgetWidth);
        float height = wanted.height(widgetHeight);
        DimensionsComponent dimensions = SandboxComponentRetriever.get(item, DimensionsComponent.class);
        if (dimensions != null) {
            dimensions.width = width;
            dimensions.height = height;
            if (dimensions.boundBox != null) dimensions.boundBox.set(0, 0, width, height);
        }

        if (wanted.tint != null) {
            TintComponent tint = SandboxComponentRetriever.get(item, TintComponent.class);
            if (tint != null) tint.color.set(wanted.tint);
        }

        ZIndexComponent zIndex = SandboxComponentRetriever.get(item, ZIndexComponent.class);
        if (zIndex != null) zIndex.setZIndex(wanted.zIndex);

        if (wanted.matchWidth || wanted.matchHeight || wanted.pinTop || wanted.pinRight) constrainToParent(item, wanted);

        if (wanted.kind == WidgetType.DefaultPart.Kind.LABEL) {
            LabelComponent label = SandboxComponentRetriever.get(item, LabelComponent.class);
            if (label != null) label.setText(wanted.text);
        }

        if (wanted.kind == WidgetType.DefaultPart.Kind.COMPOSITE) {
            //it is the size it was asked to be, whatever it holds
            CompositeTransformComponent composite = SandboxComponentRetriever.get(item, CompositeTransformComponent.class);
            if (composite != null) composite.automaticResize = false;

            for (WidgetType.DefaultPart child : wanted.children) {
                createDefaultItem(item, child, widgetWidth, widgetHeight);
            }
            if (wanted.widgetType != null) {
                WidgetType type = WidgetTypes.get(wanted.widgetType);
                if (type != null) {
                    type.setup(sandbox.getEngine().edit(item).create(WidgetComponent.class));
                    sandbox.getSceneControl().sceneLoader.getEntityFactory().attachWidgetBehaviour(item);
                }
            }
        }

        if (wanted.overrides.size > 0) {
            WidgetPartComponent part = sandbox.getEngine().edit(item).create(WidgetPartComponent.class);
            for (ObjectMap.Entry<String, ObjectMap<String, String>> state : wanted.overrides) {
                part.overrides.put(state.key, new ObjectMap<>(state.value));
            }
        }
        return item;
    }

    /** Constrains the item to the edges of what holds it, so it is resized, or moved, along with it. */
    private void constrainToParent(int item, WidgetType.DefaultPart wanted) {
        LayoutComponent layout = sandbox.getEngine().edit(item).create(LayoutComponent.class);
        if (wanted.matchWidth) {
            layout.left = parentEdge(LayoutComponent.ConstraintSide.LEFT, wanted.marginX);
            layout.right = parentEdge(LayoutComponent.ConstraintSide.RIGHT, wanted.marginX);
            layout.matchConstraintWidth = true;
        }
        if (wanted.matchHeight) {
            layout.bottom = parentEdge(LayoutComponent.ConstraintSide.BOTTOM, wanted.marginY);
            layout.top = parentEdge(LayoutComponent.ConstraintSide.TOP, wanted.marginY);
            layout.matchConstraintHeight = true;
        }
        if (wanted.pinTop && !wanted.matchHeight) layout.top = parentEdge(LayoutComponent.ConstraintSide.TOP, wanted.marginTop);
        if (wanted.pinRight && !wanted.matchWidth) layout.right = parentEdge(LayoutComponent.ConstraintSide.RIGHT, wanted.marginRight);
    }

    private static LayoutComponent.ConstraintData parentEdge(LayoutComponent.ConstraintSide side, float margin) {
        LayoutComponent.ConstraintData data = new LayoutComponent.ConstraintData();
        data.targetEntity = -1;
        data.targetSide = side;
        data.margin = margin;
        data.resolved = true;
        return data;
    }

    private int createItemOfKind(WidgetType.DefaultPart wanted) {
        switch (wanted.kind) {
            case IMAGE:
                return ItemFactory.get().createSimpleImage(wanted.region, new Vector2())
                        ? ItemFactory.get().getCreatedEntity() : -1;
            case COMPOSITE:
                return ItemFactory.get().createCompositeItem(new Vector2());
            case LABEL:
                TextTool settings = textSettings();
                return settings == null ? -1 : ItemFactory.get().createLabel(settings, new Vector2());
            default:
                return -1;
        }
    }

    /**
     * A label is made the way the text tool would make it now, font and size included, in a copy so
     * the tool is left as it was. The tool only gets a real font once its panel has been shown -
     * until then it holds a built-in default that may not exist here - so the font is checked the
     * way that panel does: one it does not know gives way to the first one it lists.
     *
     * @return null when there is no font at all, and so no label to make
     */
    private TextTool textSettings() {
        UIToolBoxMediator toolBox = facade.retrieveMediator(UIToolBoxMediator.NAME);
        Tool tool = toolBox == null ? null : toolBox.getToolList().get(TextTool.NAME);
        TextTool current = tool instanceof TextTool ? (TextTool) tool : new TextTool();

        FontManager fontManager = facade.retrieveProxy(FontManager.NAME);
        Array<String> fonts = fontManager.getFontNamesFromMap();
        if (fonts.size == 0) return null;

        TextTool settings = new TextTool();
        settings.setFontFamily(fonts.contains(current.getFontFamily(), false) ? current.getFontFamily() : fonts.first());
        settings.setFontSize(current.getFontSize());
        return settings;
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
        takeRoleBack();
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
