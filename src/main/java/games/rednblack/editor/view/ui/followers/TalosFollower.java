package games.rednblack.editor.view.ui.followers;

import com.artemis.ComponentMapper;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.VisUI;
import games.rednblack.editor.renderer.components.ParentNodeComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.utils.TransformMathUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.ui.widget.actors.basic.WhitePixel;
import games.rednblack.h2d.extension.talos.TalosComponent;
import games.rednblack.talos.runtime.IEmitter;
import games.rednblack.talos.runtime.ParticleEmitterDescriptor;
import games.rednblack.talos.runtime.modules.*;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class TalosFollower extends BasicFollower {
    private final TalosComponent talosComponent;
    private Image icon;
    private ShapeDrawer shapeDrawer;
    private final static Vector2 tmp = new Vector2();
    private final ComponentMapper<TransformComponent> transformMapper = (ComponentMapper<TransformComponent>) SandboxComponentRetriever.getMapper(TransformComponent.class);
    private final ComponentMapper<ParentNodeComponent> parentMapper = (ComponentMapper<ParentNodeComponent>) SandboxComponentRetriever.getMapper(ParentNodeComponent.class);
    private boolean hidden = true;

    public TalosFollower(int entity) {
        super(entity);
        talosComponent = SandboxComponentRetriever.get(entity, TalosComponent.class);
        setTransform(false);
    }

    @Override
    public void create() {
        icon = new Image(VisUI.getSkin().getDrawable("icon-particle-over"));
        icon.setTouchable(Touchable.disabled);
        addActor(icon);
    }

    @Override
    protected void setStage(Stage stage) {
        super.setStage(stage);
        if (stage != null)
            shapeDrawer = new ShapeDrawer(stage.getBatch(), WhitePixel.sharedInstance.textureRegion){
                /* OPTIONAL: Ensuring a certain smoothness. */
                @Override
                protected int estimateSidesRequired(float radiusX, float radiusY) {
                    return 200;
                }
            };
    }

    @Override
    public void show() {
        hidden = false;
        update();
    }

    @Override
    public void hide() {
        hidden = true;
    }

    @Override
    public void update() {
        super.update();

        Sandbox sandbox = Sandbox.getInstance();
        OrthographicCamera camera = Sandbox.getInstance().getCamera();

        int pixelPerWU = sandbox.sceneControl.sceneLoader.getRm().getProjectVO().pixelToWorld;

        float scaleX = transformComponent.scaleX * (transformComponent.flipX ? -1 : 1);
        float scaleY = transformComponent.scaleY * (transformComponent.flipY ? -1 : 1);

        setWidth ( pixelPerWU * dimensionsComponent.width * scaleX / camera.zoom );
        setHeight( pixelPerWU * dimensionsComponent.height * scaleY / camera.zoom );

        setX(getX() - getWidth() / 2f);
        setY(getY() - getHeight() / 2f);

        setOrigin(Align.center);

        icon.setX((getWidth() - icon.getWidth()) / 2);
        icon.setY((getHeight() - icon.getHeight()) / 2);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        if (hidden || Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) return;

        float a = getColor().a;
        getColor().a *= parentAlpha;

        for (IEmitter emitter : talosComponent.effect.getEmitters()) {
            ParticleEmitterDescriptor emitterDescriptor = emitter.getEmitterGraph();
            for (AbstractModule module : emitterDescriptor.getModules()) {
                if (module instanceof Vector2Module vector2Module) {
                    drawPoint(vector2Module.getDefaultX(), vector2Module.getDefaultY(), Color.YELLOW);
                }
                if (module instanceof FromToModule fromToModule) {
                    drawPoint(fromToModule.defaultFrom, Color.ORANGE);
                    drawPoint(fromToModule.defaultTo, Color.ORANGE);
                }
                if (module instanceof TargetModule targetModule) {
                    drawPoint(targetModule.defaultFrom, Color.BLUE);
                    drawPoint(targetModule.defaultTo, Color.BLUE);
                }
            }
        }

        getColor().a = a;
    }

    private void drawPoint(Vector2 v, Color color) {
        drawPoint(v.x, v.y, color);
    }

    private void drawPoint(float x, float y, Color color) {
        tmp.set(x, y);
        TransformMathUtils.localToSceneCoordinates(entity, tmp, transformMapper, parentMapper);
        Sandbox.getInstance().worldToScreen(tmp);
        shapeDrawer.filledCircle(tmp.x, tmp.y, 4, color);
    }
}
