package games.rednblack.editor.view.ui.properties.panels;

import games.rednblack.editor.controller.commands.component.UpdateTalosDataCommand;
import games.rednblack.h2d.extension.talos.TalosComponent;
import games.rednblack.h2d.extension.talos.TalosVO;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
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
        viewComponent.setMatrixTransformEnabled(SandboxComponentRetriever.get(item, TalosComponent.class).transform);
    }

    @Override
    protected void translateViewToItemData() {
        TalosVO payloadVo = new TalosVO();
        payloadVo.transform = viewComponent.isMatrixTransformEnabled();

        Object payload = UpdateTalosDataCommand.payload(observableReference, payloadVo);
        facade.sendNotification(MsgAPI.ACTION_UPDATE_TALOS_DATA, payload);
    }
}
