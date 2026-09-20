package games.rednblack.editor.view.ui.properties.panels;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import games.rednblack.editor.controller.commands.component.UpdateWidgetPartDataCommand;
import games.rednblack.editor.proxy.WidgetEditingProxy;
import games.rednblack.editor.renderer.components.ParentNodeComponent;
import games.rednblack.editor.renderer.components.widget.WidgetComponent;
import games.rednblack.editor.renderer.components.widget.WidgetPartComponent;
import games.rednblack.editor.renderer.data.WidgetOverrideTransitionVO;
import games.rednblack.editor.renderer.data.WidgetPartVO;
import games.rednblack.editor.renderer.data.WidgetStateOverridesVO;
import games.rednblack.editor.renderer.systems.WidgetStateSystem;
import games.rednblack.editor.renderer.utils.InterpolationMap;
import games.rednblack.editor.renderer.data.WidgetOverrideSequenceVO;
import games.rednblack.editor.renderer.widget.ChoiceOverrideHandler;
import games.rednblack.editor.renderer.widget.InterpolableOverrideHandler;
import games.rednblack.editor.renderer.widget.SequencedOverrideHandler;
import games.rednblack.editor.renderer.widget.StateOverrideHandler;
import games.rednblack.editor.renderer.widget.WidgetType;
import games.rednblack.editor.renderer.widget.WidgetTypes;
import games.rednblack.editor.renderer.widget.handlers.CoreStateOverrides;
import games.rednblack.editor.view.ui.properties.UIItemPropertiesMediator;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.interfaces.INotification;
import games.rednblack.puremvc.util.Interests;

public class UIWidgetPartPropertiesMediator extends UIItemPropertiesMediator<UIWidgetPartProperties> {

    private static final String TAG = UIWidgetPartPropertiesMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    private static final ObjectMap<String, String> NO_OVERRIDES = new ObjectMap<>(0);

    private final Array<String> roles = new Array<>();
    private final UIWidgetPartProperties.PartData partData = new UIWidgetPartProperties.PartData();
    private final ObjectSet<String> interpolableKeys = new ObjectSet<>();
    private final Array<String> interpolationFunctions = new Array<>();

    public UIWidgetPartPropertiesMediator() {
        super(NAME, new UIWidgetPartProperties());
    }

    @Override
    public void listNotificationInterests(Interests interests) {
        super.listNotificationInterests(interests);
        interests.add(UIWidgetPartProperties.RESET_OVERRIDE_CLICKED,
                MsgAPI.WIDGET_STATE_CHANGED,
                MsgAPI.WIDGET_OVERRIDES_RECORDED);
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        if (!validReference()) return;

        switch (notification.getName()) {
            case UIWidgetPartProperties.RESET_OVERRIDE_CLICKED:
                resetOverride(notification.getBody());
                break;
            case MsgAPI.WIDGET_STATE_CHANGED:
            case MsgAPI.WIDGET_OVERRIDES_RECORDED:
                onItemDataUpdate();
                break;
            default:
                break;
        }
    }

    /** @return the widget the entity is a part of: its nearest widget ancestor, -1 if none */
    public static int findOwnerWidget(int entity) {
        ParentNodeComponent parentNode = games.rednblack.editor.utils.runtime.SandboxComponentRetriever.get(entity, ParentNodeComponent.class);
        if (parentNode == null || parentNode.parentEntity == -1) return -1;
        return WidgetEditingProxy.findWidget(parentNode.parentEntity);
    }

    private WidgetComponent ownerWidget() {
        int widget = findOwnerWidget(observableReference);
        return widget == -1 ? null : entityData.get(widget, WidgetComponent.class);
    }

    @Override
    protected void translateObservableDataToView(int entity) {
        int widgetEntity = findOwnerWidget(entity);
        if (widgetEntity == -1) return;
        WidgetComponent widget = entityData.get(widgetEntity, WidgetComponent.class);

        roles.clear();
        WidgetType type = WidgetTypes.get(widget.widgetType);
        if (type != null) {
            for (WidgetType.Part part : type.parts) roles.add(part.role);
        }

        String state = widget.getState();
        WidgetPartComponent part = entityData.get(entity, WidgetPartComponent.class);
        ObjectMap<String, String> overrides = part == null ? null : part.getOverrides(state);
        if (overrides == null) overrides = NO_OVERRIDES;

        boolean visible = !"false".equals(overrides.get(CoreStateOverrides.VISIBLE));

        partData.roles = roles;
        partData.role = part == null ? "" : part.role;
        partData.state = state;
        partData.defaultState = state.equals(widget.defaultState);
        partData.visible = visible;
        partData.overrides = overrides;
        partData.functions = interpolationFunctions();
        partData.interpolableKeys = interpolableKeys();

        partData.transitions.clear();
        partData.sequences.clear();
        partData.sequencedKeys.clear();
        partData.choices.clear();

        WidgetStateSystem stateSystem = sandbox.getEngine().getSystem(WidgetStateSystem.class);
        for (String key : overrides.keys()) {
            if (part != null) {
                WidgetPartComponent.Transition transition = part.getTransition(state, key);
                if (transition != null) {
                    partData.transitions.put(key, new WidgetOverrideTransitionVO(transition.duration, transition.interpolation));
                }
                WidgetPartComponent.Sequence sequence = part.getSequence(state, key);
                if (sequence != null) {
                    partData.sequences.put(key, new WidgetOverrideSequenceVO(sequence.enter, sequence.exit));
                }
            }

            // what the property can do, and what it may be set to, is the handler's business
            StateOverrideHandler handler = stateSystem.getHandler(key);
            if (handler == null || !handler.supports(entity)) continue;
            if (handler instanceof SequencedOverrideHandler) partData.sequencedKeys.add(key);
            if (handler instanceof ChoiceOverrideHandler) {
                partData.choices.put(key, ((ChoiceOverrideHandler) handler).getChoices(entity));
            }
        }

        viewComponent.setPart(partData);
    }

    /** The properties made of numbers, which can travel to a value: asked to the state system itself. */
    private ObjectSet<String> interpolableKeys() {
        if (interpolableKeys.size == 0) {
            WidgetStateSystem stateSystem = sandbox.getEngine().getSystem(WidgetStateSystem.class);
            for (String key : stateSystem.getHandlerKeys()) {
                if (stateSystem.getHandler(key) instanceof InterpolableOverrideHandler) interpolableKeys.add(key);
            }
        }
        return interpolableKeys;
    }

    /** Same list the interpolation node of the action editor offers, sorted, the default one first. */
    private Array<String> interpolationFunctions() {
        if (interpolationFunctions.size == 0) {
            for (String name : InterpolationMap.map.keySet()) interpolationFunctions.add(name);
            interpolationFunctions.sort();
            if (interpolationFunctions.removeValue(WidgetOverrideTransitionVO.DEFAULT_INTERPOLATION, false)) {
                interpolationFunctions.insert(0, WidgetOverrideTransitionVO.DEFAULT_INTERPOLATION);
            }
        }
        return interpolationFunctions;
    }

    @Override
    protected void translateViewToItemData() {
        WidgetComponent widget = ownerWidget();
        if (widget == null) return;

        WidgetPartVO oldVo = currentData();
        WidgetPartVO newVo = new WidgetPartVO(oldVo);

        String role = viewComponent.getRole();
        if (role != null) newVo.role = role;

        // Hidden is the only thing a state can say about visibility: items are visible by default,
        // which is also why this is an explicit override and not something recorded from an edit.
        newVo.setOverride(widget.getState(), CoreStateOverrides.VISIBLE, viewComponent.isVisibleInState() ? null : "false");

        // transitions of the state being shown, as switched on and filled in the panel
        String state = widget.getState();
        ObjectMap<String, WidgetOverrideTransitionVO> entered = viewComponent.getTransitions();
        ObjectMap<String, WidgetOverrideSequenceVO> chains = viewComponent.getSequences();
        WidgetStateOverridesVO stateOverrides = newVo.overrides.get(state);
        if (stateOverrides != null) {
            for (String key : stateOverrides.values.keys().toArray()) {
                if (interpolableKeys().contains(key)) newVo.setTransition(state, key, entered.get(key));
                if (partData.sequencedKeys.contains(key)) newVo.setSequence(state, key, chains.get(key));
            }
        }

        send(oldVo, newVo);
    }

    private void resetOverride(String key) {
        WidgetComponent widget = ownerWidget();
        if (widget == null) return;

        WidgetPartVO oldVo = currentData();
        WidgetPartVO newVo = new WidgetPartVO(oldVo);
        newVo.setOverride(widget.getState(), key, null);

        send(oldVo, newVo);
    }

    private WidgetPartVO currentData() {
        WidgetPartVO vo = new WidgetPartVO();
        WidgetPartComponent part = entityData.get(observableReference, WidgetPartComponent.class);
        if (part != null) vo.loadFromComponent(part);
        return vo;
    }

    private void send(WidgetPartVO oldVo, WidgetPartVO newVo) {
        if (oldVo.equals(newVo)) return;
        facade.sendNotification(MsgAPI.ACTION_UPDATE_WIDGET_PART_DATA,
                UpdateWidgetPartDataCommand.payload(observableReference, newVo));
    }
}
