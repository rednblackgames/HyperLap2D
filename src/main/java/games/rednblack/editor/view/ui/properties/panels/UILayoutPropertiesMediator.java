package games.rednblack.editor.view.ui.properties.panels;

import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.controller.commands.RemoveComponentFromItemCommand;
import games.rednblack.editor.controller.commands.component.UpdateLayoutDataCommand;
import games.rednblack.editor.renderer.components.LayoutComponent;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.NodeComponent;
import games.rednblack.editor.renderer.components.ParentNodeComponent;
import games.rednblack.editor.renderer.data.LayoutConstraintVO;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.ui.properties.UIItemPropertiesMediator;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.Facade;
import games.rednblack.puremvc.interfaces.INotification;
import games.rednblack.puremvc.util.Interests;
import org.apache.commons.lang3.math.NumberUtils;

public class UILayoutPropertiesMediator extends UIItemPropertiesMediator<UILayoutProperties> {

    private static final String TAG = UILayoutPropertiesMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    public UILayoutPropertiesMediator() {
        super(NAME, new UILayoutProperties());
    }

    @Override
    public void listNotificationInterests(Interests interests) {
        super.listNotificationInterests(interests);
        interests.add(UILayoutProperties.CLOSE_CLICKED);
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);

        switch (notification.getName()) {
            case UILayoutProperties.CLOSE_CLICKED:
                Facade.getInstance().sendNotification(MsgAPI.ACTION_REMOVE_COMPONENT, RemoveComponentFromItemCommand.payload(observableReference, LayoutComponent.class));
                break;
        }
    }

    @Override
    protected void translateObservableDataToView(int entity) {
        LayoutComponent layoutComponent = SandboxComponentRetriever.get(entity, LayoutComponent.class);
        if (layoutComponent == null) return;

        // Build sibling list
        Array<String> siblings = new Array<>();
        ParentNodeComponent parentNode = SandboxComponentRetriever.get(entity, ParentNodeComponent.class);
        if (parentNode != null && parentNode.parentEntity != -1) {
            NodeComponent nodeComponent = SandboxComponentRetriever.get(parentNode.parentEntity, NodeComponent.class);
            if (nodeComponent != null) {
                for (int child : nodeComponent.children) {
                    if (child == entity) continue;
                    MainItemComponent mic = SandboxComponentRetriever.get(child, MainItemComponent.class);
                    if (mic != null) {
                        String label = mic.itemIdentifier != null && !mic.itemIdentifier.isEmpty()
                                ? mic.itemIdentifier : mic.uniqueId;
                        siblings.add(label);
                    }
                }
            }
        }
        viewComponent.setSiblingList(siblings);

        // Left
        if (layoutComponent.left != null) {
            viewComponent.setLeftEnabled(true);
            viewComponent.setLeftTarget(resolveTargetLabel(layoutComponent.left, entity));
            viewComponent.setLeftSide(layoutComponent.left.targetSide);
            viewComponent.getLeftMarginField().setText(layoutComponent.left.margin + "");
        } else {
            viewComponent.setLeftEnabled(false);
            viewComponent.getLeftMarginField().setText("0");
        }

        // Right
        if (layoutComponent.right != null) {
            viewComponent.setRightEnabled(true);
            viewComponent.setRightTarget(resolveTargetLabel(layoutComponent.right, entity));
            viewComponent.setRightSide(layoutComponent.right.targetSide);
            viewComponent.getRightMarginField().setText(layoutComponent.right.margin + "");
        } else {
            viewComponent.setRightEnabled(false);
            viewComponent.getRightMarginField().setText("0");
        }

        // Bottom
        if (layoutComponent.bottom != null) {
            viewComponent.setBottomEnabled(true);
            viewComponent.setBottomTarget(resolveTargetLabel(layoutComponent.bottom, entity));
            viewComponent.setBottomSide(layoutComponent.bottom.targetSide);
            viewComponent.getBottomMarginField().setText(layoutComponent.bottom.margin + "");
        } else {
            viewComponent.setBottomEnabled(false);
            viewComponent.getBottomMarginField().setText("0");
        }

        // Top
        if (layoutComponent.top != null) {
            viewComponent.setTopEnabled(true);
            viewComponent.setTopTarget(resolveTargetLabel(layoutComponent.top, entity));
            viewComponent.setTopSide(layoutComponent.top.targetSide);
            viewComponent.getTopMarginField().setText(layoutComponent.top.margin + "");
        } else {
            viewComponent.setTopEnabled(false);
            viewComponent.getTopMarginField().setText("0");
        }

        // Bias
        viewComponent.getHorizontalBiasField().setText(layoutComponent.horizontalBias + "");
        viewComponent.getVerticalBiasField().setText(layoutComponent.verticalBias + "");

        // Match constraint – only for entity types that support editable dimensions
        MainItemComponent mic = SandboxComponentRetriever.get(entity, MainItemComponent.class);
        boolean supportsMatchConstraint = mic != null && (mic.entityType == EntityFactory.COMPOSITE_TYPE
                || mic.entityType == EntityFactory.LABEL_TYPE
                || mic.entityType == EntityFactory.NINE_PATCH);
        viewComponent.setMatchConstraintVisible(supportsMatchConstraint);
        viewComponent.setMatchConstraintWidth(layoutComponent.matchConstraintWidth);
        viewComponent.setMatchConstraintHeight(layoutComponent.matchConstraintHeight);
    }

    private String resolveTargetLabel(LayoutComponent.ConstraintData data, int entity) {
        if (data.targetEntity == -1) return null; // null = Parent

        MainItemComponent mic = SandboxComponentRetriever.get(data.targetEntity, MainItemComponent.class);
        if (mic == null) return null;

        return mic.itemIdentifier != null && !mic.itemIdentifier.isEmpty()
                ? mic.itemIdentifier : mic.uniqueId;
    }

    @Override
    protected void translateViewToItemData() {
        LayoutComponent layoutComponent = SandboxComponentRetriever.get(observableReference, LayoutComponent.class);
        if (layoutComponent == null) return;

        LayoutConstraintVO oldVo = new LayoutConstraintVO();
        oldVo.loadFromComponent(layoutComponent, sandbox.getEngine());

        LayoutConstraintVO newVo = new LayoutConstraintVO();

        // Left
        if (viewComponent.isLeftEnabled()) {
            newVo.left = new LayoutConstraintVO.ConstraintDataVO();
            newVo.left.targetUniqueId = resolveTargetUniqueId(viewComponent.getLeftTargetUniqueId());
            newVo.left.targetSide = viewComponent.getLeftSide();
            newVo.left.margin = NumberUtils.toFloat(viewComponent.getLeftMarginField().getText());
        }

        // Right
        if (viewComponent.isRightEnabled()) {
            newVo.right = new LayoutConstraintVO.ConstraintDataVO();
            newVo.right.targetUniqueId = resolveTargetUniqueId(viewComponent.getRightTargetUniqueId());
            newVo.right.targetSide = viewComponent.getRightSide();
            newVo.right.margin = NumberUtils.toFloat(viewComponent.getRightMarginField().getText());
        }

        // Bottom
        if (viewComponent.isBottomEnabled()) {
            newVo.bottom = new LayoutConstraintVO.ConstraintDataVO();
            newVo.bottom.targetUniqueId = resolveTargetUniqueId(viewComponent.getBottomTargetUniqueId());
            newVo.bottom.targetSide = viewComponent.getBottomSide();
            newVo.bottom.margin = NumberUtils.toFloat(viewComponent.getBottomMarginField().getText());
        }

        // Top
        if (viewComponent.isTopEnabled()) {
            newVo.top = new LayoutConstraintVO.ConstraintDataVO();
            newVo.top.targetUniqueId = resolveTargetUniqueId(viewComponent.getTopTargetUniqueId());
            newVo.top.targetSide = viewComponent.getTopSide();
            newVo.top.margin = NumberUtils.toFloat(viewComponent.getTopMarginField().getText());
        }

        // Bias
        newVo.horizontalBias = NumberUtils.toFloat(viewComponent.getHorizontalBiasField().getText(), 0.5f);
        newVo.verticalBias = NumberUtils.toFloat(viewComponent.getVerticalBiasField().getText(), 0.5f);

        // Match constraint
        newVo.matchConstraintWidth = viewComponent.isMatchConstraintWidth();
        newVo.matchConstraintHeight = viewComponent.isMatchConstraintHeight();

        if (!oldVo.equals(newVo)) {
            Object payload = UpdateLayoutDataCommand.payload(observableReference, newVo);
            facade.sendNotification(MsgAPI.ACTION_UPDATE_LAYOUT_DATA, payload);
        }
    }

    private String resolveTargetUniqueId(String label) {
        if (label == null) return null; // Parent

        // The label could be an itemIdentifier or a uniqueId - find the matching sibling
        ParentNodeComponent parentNode = SandboxComponentRetriever.get(observableReference, ParentNodeComponent.class);
        if (parentNode == null || parentNode.parentEntity == -1) return label;

        NodeComponent nodeComponent = SandboxComponentRetriever.get(parentNode.parentEntity, NodeComponent.class);
        if (nodeComponent == null) return label;

        for (int child : nodeComponent.children) {
            if (child == observableReference) continue;
            MainItemComponent mic = SandboxComponentRetriever.get(child, MainItemComponent.class);
            if (mic != null) {
                String childLabel = mic.itemIdentifier != null && !mic.itemIdentifier.isEmpty()
                        ? mic.itemIdentifier : mic.uniqueId;
                if (label.equals(childLabel)) {
                    return mic.uniqueId;
                }
            }
        }

        return label;
    }
}
