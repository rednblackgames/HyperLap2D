package games.rednblack.editor.view.ui.followers;

import com.artemis.ComponentMapper;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
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

    // --- STYLES ---
    private static final Color POINT_COLOR = new Color(0.2f, 0.9f, 1f, 1f);
    private static final Color FROM_TO_COLOR = new Color(1f, 0.6f, 0f, 1f);
    private static final Color TARGET_COLOR = new Color(0.8f, 0.2f, 1f, 1f);

    private final TalosComponent talosComponent;
    private Image icon;
    private ShapeDrawer shapeDrawer;

    private final static Vector2 tmp = new Vector2();
    private final static Vector2 vecFrom = new Vector2();
    private final static Vector2 vecTo = new Vector2();

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
                @Override
                protected int estimateSidesRequired(float radiusX, float radiusY) {
                    return 40;
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

        float oldColor = shapeDrawer.getPackedColor();

        float alphaMult = getColor().a * parentAlpha;

        for (IEmitter emitter : talosComponent.effect.getEmitters()) {
            ParticleEmitterDescriptor emitterDescriptor = emitter.getEmitterGraph();

            for (AbstractModule module : emitterDescriptor.getModules()) {
                if (module instanceof Vector2Module vector2Module) {
                    drawReticle(vector2Module.getDefaultX(), vector2Module.getDefaultY(), POINT_COLOR, alphaMult);
                }

                if (module instanceof FromToModule fromToModule) {
                    drawArrow(fromToModule.defaultFrom, fromToModule.defaultTo, FROM_TO_COLOR, alphaMult);
                }

                if (module instanceof TargetModule targetModule) {
                    drawArrow(targetModule.defaultFrom, targetModule.defaultTo, TARGET_COLOR, alphaMult);
                }
            }
        }

        shapeDrawer.setColor(oldColor);
    }

    private void projectToScreen(float worldX, float worldY, Vector2 out) {
        out.set(worldX, worldY);
        TransformMathUtils.localToSceneCoordinates(entity, out, transformMapper, parentMapper);
        Sandbox.getInstance().worldToScreen(out);
    }

    private void drawReticle(float x, float y, Color color, float alpha) {
        projectToScreen(x, y, tmp);

        shapeDrawer.setColor(color.r, color.g, color.b, color.a * alpha);

        float cx = tmp.x;
        float cy = tmp.y;
        float radius = 6f;
        float tickLen = 5f;
        float tickOffset = 3f;

        shapeDrawer.filledCircle(cx, cy, 2f);

        shapeDrawer.circle(cx, cy, radius, 1.5f);

        shapeDrawer.line(cx - radius - tickOffset - tickLen, cy, cx - radius - tickOffset, cy, 1.5f); // Sinistra
        shapeDrawer.line(cx + radius + tickOffset, cy, cx + radius + tickOffset + tickLen, cy, 1.5f); // Destra
        shapeDrawer.line(cx, cy - radius - tickOffset - tickLen, cx, cy - radius - tickOffset, 1.5f); // Sotto
        shapeDrawer.line(cx, cy + radius + tickOffset, cx, cy + radius + tickOffset + tickLen, 1.5f); // Sopra
    }

    private void drawArrow(Vector2 from, Vector2 to, Color color, float alpha) {
        projectToScreen(from.x, from.y, vecFrom);
        projectToScreen(to.x, to.y, vecTo);

        shapeDrawer.setColor(color.r, color.g, color.b, color.a * alpha);

        float x1 = vecFrom.x, y1 = vecFrom.y;
        float x2 = vecTo.x, y2 = vecTo.y;

        if (vecFrom.epsilonEquals(vecTo, 1f)) {
            shapeDrawer.filledCircle(x1, y1, 4f);
            return;
        }

        shapeDrawer.line(x1, y1, x2, y2, 2f);

        shapeDrawer.filledCircle(x1, y1, 3f);

        drawArrowHead(x1, y1, x2, y2, 12f);
    }

    private void drawArrowHead(float x1, float y1, float x2, float y2, float size) {
        float angle = MathUtils.atan2(y2 - y1, x2 - x1);

        float wingAngle1 = angle + MathUtils.PI * 0.85f;
        float wingAngle2 = angle - MathUtils.PI * 0.85f;

        float w1x = x2 + MathUtils.cos(wingAngle1) * size;
        float w1y = y2 + MathUtils.sin(wingAngle1) * size;

        float w2x = x2 + MathUtils.cos(wingAngle2) * size;
        float w2y = y2 + MathUtils.sin(wingAngle2) * size;

        shapeDrawer.filledTriangle(x2, y2, w1x, w1y, w2x, w2y);
    }
}