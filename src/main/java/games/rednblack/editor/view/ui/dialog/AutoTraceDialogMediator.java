package games.rednblack.editor.view.ui.dialog;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.renderer.components.shape.PolygonShapeComponent;
import games.rednblack.editor.renderer.components.TextureRegionComponent;
import games.rednblack.editor.renderer.utils.poly.PolygonRuntimeUtils;
import games.rednblack.editor.utils.poly.tracer.Tracer;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.editor.view.ui.FollowersUIMediator;
import games.rednblack.editor.view.ui.followers.BasicFollower;
import games.rednblack.editor.view.ui.followers.PolygonFollower;
import games.rednblack.h2d.common.H2DDialogs;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.Facade;
import games.rednblack.puremvc.Mediator;
import games.rednblack.puremvc.interfaces.INotification;
import games.rednblack.puremvc.util.Interests;

import java.util.stream.Stream;

public class AutoTraceDialogMediator extends Mediator<AutoTraceDialog> {

    private static final String TAG = AutoTraceDialogMediator.class.getCanonicalName();
    private static final String NAME = TAG;

    private int entity;

    public AutoTraceDialogMediator() {
        super(NAME, new AutoTraceDialog());
    }

    @Override
    public void listNotificationInterests(Interests interests) {
        interests.add(AutoTraceDialog.OPEN_DIALOG,
                AutoTraceDialog.AUTO_TRACE_BUTTON_CLICKED);
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
                    polygonShapeComponent.polygonizedVertices = PolygonRuntimeUtils.polygonize(points);

                    FollowersUIMediator followersUIMediator = Facade.getInstance().retrieveMediator(FollowersUIMediator.NAME);
                    BasicFollower follower = followersUIMediator.getFollower(entity);
                    PolygonFollower polygonFollower = (PolygonFollower) follower.getSubFollower(PolygonFollower.class);
                    if (polygonFollower != null)
                        polygonFollower.update();

                    Facade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
                }
            } else {
                H2DDialogs.showErrorDialog(Sandbox.getInstance().getUIStage(), "Auto Trace can be performed only for Image type.").padBottom(20).pack();
            }
        }
    }
}
