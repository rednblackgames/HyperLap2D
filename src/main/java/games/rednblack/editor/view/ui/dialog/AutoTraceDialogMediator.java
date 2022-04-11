package games.rednblack.editor.view.ui.dialog;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.renderer.components.shape.PolygonShapeComponent;
import games.rednblack.editor.renderer.components.TextureRegionComponent;
import games.rednblack.editor.utils.poly.PolygonUtils;
import games.rednblack.editor.utils.poly.tracer.Tracer;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.editor.view.ui.FollowersUIMediator;
import games.rednblack.editor.view.ui.followers.BasicFollower;
import games.rednblack.editor.view.ui.followers.PolygonFollower;
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
        PolygonShapeComponent polygonShapeComponent = SandboxComponentRetriever.get(entity, PolygonShapeComponent.class);

        if (polygonShapeComponent != null) {
            TextureRegionComponent textureRegionComponent = SandboxComponentRetriever.get(entity, TextureRegionComponent.class);

            if (textureRegionComponent != null && textureRegionComponent.region != null && !textureRegionComponent.regionName.equals("")) {
                Vector2[][] traceResult = Tracer.trace(textureRegionComponent.region, viewComponent.getHullTolerance(),
                        viewComponent.getAlphaTolerance(), viewComponent.isMultiPartDetection(),
                        viewComponent.isHoleDetection());

                if (traceResult != null) {
                    Vector2[] points = Stream.of(traceResult)
                            .flatMap(Stream::of)
                            .toArray(Vector2[]::new);
                    polygonShapeComponent.vertices = new Array<>(points);
                    polygonShapeComponent.polygonizedVertices = PolygonUtils.polygonize(points);

                    FollowersUIMediator followersUIMediator = HyperLap2DFacade.getInstance().retrieveMediator(FollowersUIMediator.NAME);
                    BasicFollower follower = followersUIMediator.getFollower(entity);
                    PolygonFollower polygonFollower = (PolygonFollower) follower.getSubFollower(PolygonFollower.class);
                    if (polygonFollower != null)
                        polygonFollower.update();

                    HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
                }
            } else {
                Dialogs.showErrorDialog(Sandbox.getInstance().getUIStage(), "Auto Trace can be performed only for Image type.").padBottom(20).pack();
            }
        }
    }
}
