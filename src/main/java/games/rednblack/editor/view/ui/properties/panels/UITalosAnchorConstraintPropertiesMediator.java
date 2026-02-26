package games.rednblack.editor.view.ui.properties.panels;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntSet;
import games.rednblack.editor.controller.commands.RemoveComponentFromItemCommand;
import games.rednblack.editor.controller.commands.component.UpdateTalosAnchorConstraintCommand;
import games.rednblack.editor.renderer.components.LayoutComponent;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.NodeComponent;
import games.rednblack.editor.renderer.components.ParentNodeComponent;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.ui.properties.UIItemPropertiesMediator;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.extension.talos.TalosAnchorConstraintComponent;
import games.rednblack.h2d.extension.talos.TalosAnchorConstraintVO;
import games.rednblack.h2d.extension.talos.TalosComponent;
import games.rednblack.puremvc.Facade;
import games.rednblack.puremvc.interfaces.INotification;
import games.rednblack.puremvc.util.Interests;
import games.rednblack.talos.runtime.IEmitter;
import games.rednblack.talos.runtime.ParticleEmitterDescriptor;
import games.rednblack.talos.runtime.modules.AbstractModule;
import games.rednblack.talos.runtime.modules.GlobalScopeModule;
import org.apache.commons.lang3.math.NumberUtils;

public class UITalosAnchorConstraintPropertiesMediator extends UIItemPropertiesMediator<UITalosAnchorConstraintProperties> {

    private static final String TAG = UITalosAnchorConstraintPropertiesMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    public UITalosAnchorConstraintPropertiesMediator() {
        super(NAME, new UITalosAnchorConstraintProperties());
    }

    @Override
    public void listNotificationInterests(Interests interests) {
        super.listNotificationInterests(interests);
        interests.add(UITalosAnchorConstraintProperties.CLOSE_CLICKED);
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);

        if (UITalosAnchorConstraintProperties.CLOSE_CLICKED.equals(notification.getName())) {
            Facade.getInstance().sendNotification(MsgAPI.ACTION_REMOVE_COMPONENT,
                    RemoveComponentFromItemCommand.payload(observableReference, TalosAnchorConstraintComponent.class));
        }
    }

    @Override
    protected void translateObservableDataToView(int entity) {
        TalosComponent talosComp = SandboxComponentRetriever.get(entity, TalosComponent.class);
        TalosAnchorConstraintComponent anchorComp = SandboxComponentRetriever.get(entity, TalosAnchorConstraintComponent.class);
        if (talosComp == null || anchorComp == null) return;

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

        // Discover GlobalScopeModule keys from the effect
        Array<Integer> scopeKeys = new Array<>();
        IntSet seenKeys = new IntSet();
        if (talosComp.effect != null) {
            for (IEmitter emitter : talosComp.effect.getEmitters()) {
                ParticleEmitterDescriptor emitterDescriptor = emitter.getEmitterGraph();
                for (AbstractModule module : emitterDescriptor.getModules()) {
                    if (module instanceof GlobalScopeModule) {
                        int key = ((GlobalScopeModule) module).getKey();
                        if (!seenKeys.contains(key)) {
                            seenKeys.add(key);
                            scopeKeys.add(key);
                        }
                    }
                }
            }
        }

        scopeKeys.sort();
        viewComponent.rebuildRows(scopeKeys);

        // Populate existing bindings
        for (UITalosAnchorConstraintProperties.BindingRow row : viewComponent.getBindingRows()) {
            TalosAnchorConstraintComponent.AnchorBinding binding = findBinding(anchorComp, row.scopeKey);
            if (binding != null) {
                row.enabled.setChecked(true);

                // Left
                if (binding.left != null) {
                    row.leftEnabled.setChecked(true);
                    row.leftTarget.setSelected(resolveTargetLabel(binding.left, entity));
                    row.leftSide.setSelected(binding.left.targetSide.name());
                    row.leftMargin.setText(binding.left.margin + "");
                } else {
                    row.leftEnabled.setChecked(false);
                }

                // Right
                if (binding.right != null) {
                    row.rightEnabled.setChecked(true);
                    row.rightTarget.setSelected(resolveTargetLabel(binding.right, entity));
                    row.rightSide.setSelected(binding.right.targetSide.name());
                    row.rightMargin.setText(binding.right.margin + "");
                } else {
                    row.rightEnabled.setChecked(false);
                }

                // Bottom
                if (binding.bottom != null) {
                    row.bottomEnabled.setChecked(true);
                    row.bottomTarget.setSelected(resolveTargetLabel(binding.bottom, entity));
                    row.bottomSide.setSelected(binding.bottom.targetSide.name());
                    row.bottomMargin.setText(binding.bottom.margin + "");
                } else {
                    row.bottomEnabled.setChecked(false);
                }

                // Top
                if (binding.top != null) {
                    row.topEnabled.setChecked(true);
                    row.topTarget.setSelected(resolveTargetLabel(binding.top, entity));
                    row.topSide.setSelected(binding.top.targetSide.name());
                    row.topMargin.setText(binding.top.margin + "");
                } else {
                    row.topEnabled.setChecked(false);
                }

                // Bias
                row.horizontalBiasField.setText(binding.horizontalBias + "");
                row.verticalBiasField.setText(binding.verticalBias + "");
            } else {
                row.enabled.setChecked(false);
                row.leftEnabled.setChecked(false);
                row.rightEnabled.setChecked(false);
                row.bottomEnabled.setChecked(false);
                row.topEnabled.setChecked(false);
            }
            row.updateFieldsEnabled();
        }
    }

    private TalosAnchorConstraintComponent.AnchorBinding findBinding(TalosAnchorConstraintComponent comp, int scopeKey) {
        for (TalosAnchorConstraintComponent.AnchorBinding b : comp.bindings) {
            if (b.scopeKey == scopeKey) return b;
        }
        return null;
    }

    private String resolveTargetLabel(TalosAnchorConstraintComponent.ConstraintData data, int entity) {
        if (data.targetEntity == -1) return null; // null triggers "Parent" selection

        MainItemComponent mic = SandboxComponentRetriever.get(data.targetEntity, MainItemComponent.class);
        if (mic == null) return null;

        return mic.itemIdentifier != null && !mic.itemIdentifier.isEmpty()
                ? mic.itemIdentifier : mic.uniqueId;
    }

    @Override
    protected void translateViewToItemData() {
        TalosAnchorConstraintComponent comp = SandboxComponentRetriever.get(observableReference, TalosAnchorConstraintComponent.class);
        if (comp == null) return;

        TalosAnchorConstraintVO oldVo = new TalosAnchorConstraintVO();
        oldVo.loadFromComponent(comp, sandbox.getEngine());

        TalosAnchorConstraintVO newVo = new TalosAnchorConstraintVO();

        for (UITalosAnchorConstraintProperties.BindingRow row : viewComponent.getBindingRows()) {
            if (!row.enabled.isChecked()) continue;

            if (newVo.bindings == null) newVo.bindings = new com.badlogic.gdx.utils.Array<>();

            TalosAnchorConstraintVO.AnchorBindingVO bvo = new TalosAnchorConstraintVO.AnchorBindingVO();
            bvo.scopeKey = row.scopeKey;

            // Left
            if (row.leftEnabled.isChecked()) {
                bvo.left = new TalosAnchorConstraintVO.ConstraintDataVO();
                bvo.left.targetUniqueId = resolveTargetUniqueId(row.getTargetUniqueId(row.leftTarget));
                bvo.left.targetSide = LayoutComponent.ConstraintSide.valueOf(row.leftSide.getSelected());
                bvo.left.margin = NumberUtils.toFloat(row.leftMargin.getText());
            }

            // Right
            if (row.rightEnabled.isChecked()) {
                bvo.right = new TalosAnchorConstraintVO.ConstraintDataVO();
                bvo.right.targetUniqueId = resolveTargetUniqueId(row.getTargetUniqueId(row.rightTarget));
                bvo.right.targetSide = LayoutComponent.ConstraintSide.valueOf(row.rightSide.getSelected());
                bvo.right.margin = NumberUtils.toFloat(row.rightMargin.getText());
            }

            // Bottom
            if (row.bottomEnabled.isChecked()) {
                bvo.bottom = new TalosAnchorConstraintVO.ConstraintDataVO();
                bvo.bottom.targetUniqueId = resolveTargetUniqueId(row.getTargetUniqueId(row.bottomTarget));
                bvo.bottom.targetSide = LayoutComponent.ConstraintSide.valueOf(row.bottomSide.getSelected());
                bvo.bottom.margin = NumberUtils.toFloat(row.bottomMargin.getText());
            }

            // Top
            if (row.topEnabled.isChecked()) {
                bvo.top = new TalosAnchorConstraintVO.ConstraintDataVO();
                bvo.top.targetUniqueId = resolveTargetUniqueId(row.getTargetUniqueId(row.topTarget));
                bvo.top.targetSide = LayoutComponent.ConstraintSide.valueOf(row.topSide.getSelected());
                bvo.top.margin = NumberUtils.toFloat(row.topMargin.getText());
            }

            // Bias
            bvo.horizontalBias = NumberUtils.toFloat(row.horizontalBiasField.getText(), 0.5f);
            bvo.verticalBias = NumberUtils.toFloat(row.verticalBiasField.getText(), 0.5f);

            newVo.bindings.add(bvo);
        }

        if (!oldVo.equals(newVo)) {
            Object payload = UpdateTalosAnchorConstraintCommand.payload(observableReference, newVo);
            facade.sendNotification(MsgAPI.ACTION_UPDATE_TALOS_ANCHOR_CONSTRAINT_DATA, payload);
        }
    }

    private String resolveTargetUniqueId(String label) {
        if (label == null) return null; // Parent

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
