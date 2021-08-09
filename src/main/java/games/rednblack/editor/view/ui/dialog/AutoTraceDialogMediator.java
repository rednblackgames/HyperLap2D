package games.rednblack.editor.view.ui.dialog;

import com.badlogic.gdx.math.Vector2;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.renderer.components.PolygonComponent;
import games.rednblack.editor.renderer.components.TextureRegionComponent;
import games.rednblack.editor.utils.poly.PolygonUtils;
import games.rednblack.editor.utils.poly.tracer.Tracer;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.h2d.common.MsgAPI;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.mediator.Mediator;

import java.util.stream.Stream;

public class AutoTraceDialogMediator extends Mediator<AutoTraceDialog> {

    private static final String TAG = AutoTraceDialogMediator.class.getCanonicalName();
    private static final String NAME = TAG;

    private int entity;

    public AutoTraceDialogMediator() {
        super(NAME, new AutoTraceDialog());
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                AutoTraceDialog.OPEN_DIALOG,
                AutoTraceDialog.AUTO_TRACE_BUTTON_CLICKED
        };
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        Sandbox sandbox = Sandbox.getInstance();
        UIStage uiStage = sandbox.getUIStage();

        switch (notification.getName()) {
            case AutoTraceDialog.OPEN_DIALOG:
                entity = notification.getBody();
                viewComponent.show(uiStage);
                break;
            case AutoTraceDialog.AUTO_TRACE_BUTTON_CLICKED:
                addAutoTraceMesh();
                break;
        }
    }

    private void addAutoTraceMesh() {
        PolygonComponent polygonComponent = SandboxComponentRetriever.get(entity, PolygonComponent.class);

        if (polygonComponent != null) {
            TextureRegionComponent textureRegionComponent = SandboxComponentRetriever.get(entity, TextureRegionComponent.class);

            if (textureRegionComponent != null && textureRegionComponent.region != null && !textureRegionComponent.regionName.equals("")) {
                polygonComponent.vertices = Tracer.trace(textureRegionComponent.region, viewComponent.getHullTolerance(),
                        viewComponent.getAlphaTolerance(), viewComponent.isMultiPartDetection(),
                        viewComponent.isHoleDetection());

                if (polygonComponent.vertices != null) {
                    Vector2[] points = Stream.of(polygonComponent.vertices)
                            .flatMap(Stream::of)
                            .toArray(Vector2[]::new);
                    polygonComponent.vertices = PolygonUtils.polygonize(points);

                    HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
                }
            } else {
                Dialogs.showErrorDialog(Sandbox.getInstance().getUIStage(), "Auto Trace can be performed only for Image type.").padBottom(20).pack();
            }
        }
    }
}
