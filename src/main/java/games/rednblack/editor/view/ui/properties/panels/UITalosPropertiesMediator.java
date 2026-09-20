package games.rednblack.editor.view.ui.properties.panels;

import com.badlogic.gdx.utils.Array;
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
        for (int key : keys) {
            float[] value = new float[TalosComponent.ScopeValue.CHANNELS];
            talosComponent.getScopeValue(key, value);
            values.put(key, value);
        }

        viewComponent.setScopeSlots(keys, values);
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
