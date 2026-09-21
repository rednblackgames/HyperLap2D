package games.rednblack.editor.view.ui.properties.panels;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.color.ColorPicker;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;
import games.rednblack.editor.proxy.PluginUIBridge;
import games.rednblack.h2d.common.view.ui.widget.HyperLapColorPicker;
import games.rednblack.puremvc.interfaces.INotification;
import games.rednblack.puremvc.util.Interests;
import com.badlogic.gdx.utils.ObjectMap;
import games.rednblack.editor.controller.commands.component.UpdateTalosDataCommand;
import games.rednblack.h2d.extension.talos.TalosAnchorConstraintComponent;
import games.rednblack.h2d.extension.talos.TalosComponent;
import games.rednblack.h2d.extension.talos.TalosVO;
import games.rednblack.editor.view.ui.properties.UIItemPropertiesMediator;
import games.rednblack.h2d.common.MsgAPI;

public class UITalosPropertiesMediator extends UIItemPropertiesMediator<UITalosProperties> {

    private static final String TAG = UITalosPropertiesMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    public UITalosPropertiesMediator() {
        super(NAME, new UITalosProperties());
    }

    @Override
    public void listNotificationInterests(Interests interests) {
        super.listNotificationInterests(interests);
        interests.add(UITalosProperties.SCOPE_COLOR_CLICKED);
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        if (!validReference()) return;

        if (UITalosProperties.SCOPE_COLOR_CLICKED.equals(notification.getName())) {
            pickScopeColor(notification.getBody());
        }
    }

    /**
     * The effect shows the colour live while it is being picked, straight from the slot, and only
     * picking it for good goes through a command, like the tint of an item does.
     */
    private void pickScopeColor(final int key) {
        final TalosComponent talosComponent = entityData.get(observableReference, TalosComponent.class);
        final float[] before = viewComponent.getScopeValue(key);

        ColorPicker picker = new HyperLapColorPicker(new ColorPickerAdapter() {
            @Override
            public void changed(Color color) {
                talosComponent.pushScopeValue(key, new float[]{color.r, color.g, color.b, color.a});
            }

            @Override
            public void canceled(Color oldColor) {
                talosComponent.pushScopeValue(key, before);
            }

            @Override
            public void finished(Color color) {
                talosComponent.pushScopeValue(key, before);
                viewComponent.setScopeColor(key, color);
                facade.sendNotification(viewComponent.getUpdateEventName());
            }
        });
        picker.setColor(new Color(before[0], before[1], before[2], before[3]));
        PluginUIBridge.get().getSandbox().getUIStage().addActor(picker.fadeIn());
    }

    @Override
    protected void translateObservableDataToView(int item) {
        TalosComponent talosComponent = entityData.get(item, TalosComponent.class);
        viewComponent.setMatrixTransformEnabled(talosComponent.transform);
        viewComponent.setAutoStartEnabled(talosComponent.autoStart);

        // the slots the effect reads, minus the ones an anchor constraint already drives
        TalosAnchorConstraintComponent anchor = entityData.get(item, TalosAnchorConstraintComponent.class);
        Array<Integer> keys = new Array<>();
        for (int key : talosComponent.getUsedScopeKeys()) {
            if (!isAnchored(anchor, key)) keys.add(key);
        }

        ObjectMap<Integer, float[]> values = new ObjectMap<>();
        ObjectMap<Integer, TalosComponent.ScopeKind> kinds = new ObjectMap<>();
        for (int key : keys) {
            float[] value = new float[TalosComponent.ScopeValue.CHANNELS];
            talosComponent.getScopeValue(key, value);
            values.put(key, value);
            kinds.put(key, talosComponent.getScopeKind(key));
        }

        viewComponent.setScopeSlots(keys, kinds, values);
    }

    private static boolean isAnchored(TalosAnchorConstraintComponent anchor, int key) {
        if (anchor == null) return false;
        for (TalosAnchorConstraintComponent.AnchorBinding binding : anchor.bindings) {
            if (binding.scopeKey == key) return true;
        }
        return false;
    }

    private static void setValue(TalosVO vo, int key, float[] value) {
        for (TalosVO.ScopeValueVO stored : vo.scopeValues) {
            if (stored.key == key) {
                System.arraycopy(value, 0, stored.value, 0, stored.value.length);
                return;
            }
        }
        vo.scopeValues.add(new TalosVO.ScopeValueVO(key, value));
    }

    @Override
    protected void translateViewToItemData() {
        TalosVO payloadVo = new TalosVO();
        payloadVo.transform = viewComponent.isMatrixTransformEnabled();
        payloadVo.autoStart = viewComponent.isAutoStartEnabled();

        TalosComponent talosComponent = entityData.get(observableReference, TalosComponent.class);
        // slots the panel does not show, anchored ones included, keep whatever they hold
        for (TalosComponent.ScopeValue value : talosComponent.scopeValues) {
            payloadVo.scopeValues.add(new TalosVO.ScopeValueVO(value.key, value.value));
        }
        for (ObjectMap.Entry<Integer, float[]> entered : viewComponent.getScopeValues().entries()) {
            setValue(payloadVo, entered.key, entered.value);
        }

        Object payload = UpdateTalosDataCommand.payload(observableReference, payloadVo);
        facade.sendNotification(MsgAPI.ACTION_UPDATE_TALOS_DATA, payload);
    }
}
