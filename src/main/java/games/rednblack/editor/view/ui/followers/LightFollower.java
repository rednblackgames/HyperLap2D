package games.rednblack.editor.view.ui.followers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.FloatArray;
import com.kotcrab.vis.ui.VisUI;
import games.rednblack.editor.renderer.components.light.LightObjectComponent;
import games.rednblack.editor.renderer.lights.Light;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.ui.widget.actors.basic.WhitePixel;
import space.earlygrey.shapedrawer.JoinType;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class LightFollower extends BasicFollower {

    // --- STYLES ---
    private static final Color RANGE_COLOR = new Color(1f, 1f, 1f, 0.3f);
    private static final Color RAYCAST_OUTLINE_COLOR = new Color(1f, 0.8f, 0.4f, 0.6f);
    private static final Color HIT_POINT_COLOR = new Color(1f, 0.5f, 0f, 0.8f);

    private static final Color ISO_HIGH = new Color(1f, 1f, 1f, 0.6f);
    private static final Color ISO_MED = new Color(0.2f, 0.4f, 1f, 0.5f);
    private static final Color ISO_LOW = new Color(0.8f, 0f, 0f, 0.5f);
    private static final Color HEIGHT_COLOR = new Color(1f, 0f, 1f, 0.8f);

    protected LightObjectComponent lightObjectComponent;
    private Image icon;
    private ShapeDrawer shapeDrawer;

    private final Vector2 tmp = new Vector2();
    private final FloatArray polygonVerts = new FloatArray(256);

    private boolean selected = false;

    public LightFollower(int entity) {
        super(entity);
        lightObjectComponent = SandboxComponentRetriever.get(entity, LightObjectComponent.class);
    }

    @Override
    public void create() {
        icon = new Image(VisUI.getSkin().getDrawable("tool-sphericlight"));
        icon.setTouchable(Touchable.disabled);
        addActor(icon);
    }

    @Override
    protected void setStage(Stage stage) {
        super.setStage(stage);
        if (stage != null) {
            shapeDrawer = new ShapeDrawer(stage.getBatch(), WhitePixel.sharedInstance.textureRegion){
                @Override
                protected int estimateSidesRequired(float radiusX, float radiusY) {
                    return 60;
                }
            };
        }
    }

    @Override
    public void act(float delta) {
        setVisible(!Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT));
        super.act(delta);
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

        icon.setX((getWidth() - icon.getWidth()) / 2);
        icon.setY((getHeight() - icon.getHeight()) / 2);
        icon.setOrigin(Align.center);
        setRotation(0);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        if (!isVisible() || shapeDrawer == null || lightObjectComponent.lightObject == null) return;

        Light light = lightObjectComponent.lightObject;
        Sandbox sandbox = Sandbox.getInstance();
        OrthographicCamera camera = Sandbox.getInstance().getCamera();
        int ppwu = sandbox.sceneControl.sceneLoader.getRm().getProjectVO().pixelToWorld;

        float centerX = getX() + getWidth() / 2f;
        float centerY = getY() + getHeight() / 2f;

        float radius = lightObjectComponent.distance * ppwu / camera.zoom;

        drawTheoreticalRange(lightObjectComponent, centerX, centerY, radius);

        if (!selected || !lightObjectComponent.lightObject.isActive()) return;

        drawRaycastMesh(light, centerX, centerY, sandbox);

        drawFalloffIsoLines(lightObjectComponent, centerX, centerY, sandbox);

        if (lightObjectComponent.rayHandler.isPseudo3d()) {
            drawPseudo3DGizmo(lightObjectComponent, centerX, centerY, ppwu, camera.zoom);
        }
    }

    private void drawTheoreticalRange(LightObjectComponent component, float cx, float cy, float radius) {
        shapeDrawer.setColor(RANGE_COLOR);
        float lw = 1.5f;

        if (component.type == LightObjectComponent.LightType.CONE) {
            float angle = component.directionDegree + transformComponent.rotation;
            float coneAngle = component.coneDegree * 2;

            float startAngle = (angle - coneAngle / 2f) * MathUtils.degreesToRadians;
            float sweep = coneAngle * MathUtils.degreesToRadians;

            shapeDrawer.arc(cx, cy, radius, startAngle, sweep, lw);

            float x1 = cx + MathUtils.cos(startAngle) * radius;
            float y1 = cy + MathUtils.sin(startAngle) * radius;
            float x2 = cx + MathUtils.cos(startAngle + sweep) * radius;
            float y2 = cy + MathUtils.sin(startAngle + sweep) * radius;

            shapeDrawer.line(cx, cy, x1, y1, lw);
            shapeDrawer.line(cx, cy, x2, y2, lw);

        } else {
            shapeDrawer.circle(cx, cy, radius, lw);
        }
    }

    private void drawRaycastMesh(Light light, float cx, float cy, Sandbox sandbox) {
        float[] mx = light.getMx();
        float[] my = light.getMy();
        int rays = light.getRayNum();

        if (mx == null || my == null || rays <= 0) return;

        polygonVerts.clear();

        polygonVerts.add(cx);
        polygonVerts.add(cy);

        for (int i = 0; i < rays; i++) {
            tmp.set(mx[i], my[i]);
            sandbox.worldToScreen(tmp);

            polygonVerts.add(tmp.x);
            polygonVerts.add(tmp.y);
        }

        tmp.set(mx[0], my[0]);
        sandbox.worldToScreen(tmp);
        polygonVerts.add(tmp.x);
        polygonVerts.add(tmp.y);

        shapeDrawer.setColor(RAYCAST_OUTLINE_COLOR);
        shapeDrawer.path(polygonVerts.items, 2, polygonVerts.size - 2, 1.5f, JoinType.SMOOTH, true);

        shapeDrawer.setColor(HIT_POINT_COLOR);
        for (int i = 2; i < polygonVerts.size; i += 2) {
            shapeDrawer.filledCircle(polygonVerts.get(i), polygonVerts.get(i+1), 2f);
        }
    }

    private void drawFalloffIsoLines(LightObjectComponent loc, float cx, float cy, Sandbox sandbox) {
        Vector3 f = loc.falloff; // x=Constant, y=Linear, z=Quadratic

        float ppwu = sandbox.sceneControl.sceneLoader.getRm().getProjectVO().pixelToWorld;
        float maxRadiusPixel = (loc.distance * ppwu) / sandbox.getCamera().zoom;

        drawIsoLine(0.8f, ISO_HIGH, f, cx, cy, maxRadiusPixel, loc);
        drawIsoLine(0.5f, ISO_MED, f, cx, cy, maxRadiusPixel, loc);
        drawIsoLine(0.2f, ISO_LOW, f, cx, cy, maxRadiusPixel, loc);
    }

    private void drawIsoLine(float threshold, Color color, Vector3 f, float cx, float cy, float maxRadiusPixel, LightObjectComponent loc) {
        float intensity = loc.intensity;
        if (intensity <= 0) return;

        // FinalColor = (Color * Intensity) * Attenuation
        // Attenuation = 1.0 / (C + L*s + Q*s^2)

        // Intensity * (1 / (C + L*s + Q*s^2)) = Threshold
        // Intensity / Threshold = C + L*s + Q*s^2
        // Q*s^2 + L*s + (C - Intensity/Threshold) = 0

        float targetVal = intensity / threshold;

        float a = f.z;
        float b = f.y;
        float c = f.x - targetVal;

        float s = solveQuadraticPositive(a, b, c);

        if (s > 0) {
            float drawRadius = s * maxRadiusPixel;

            shapeDrawer.setColor(color);

            if (loc.type == LightObjectComponent.LightType.CONE) {
                float angle = loc.directionDegree + transformComponent.rotation;
                float coneAngle = loc.coneDegree * 2;

                float startAngleRad = (angle - coneAngle / 2f) * MathUtils.degreesToRadians;
                float sweepRad = coneAngle * MathUtils.degreesToRadians;
                shapeDrawer.arc(cx, cy, drawRadius, startAngleRad, sweepRad, 2f);
            } else {
                shapeDrawer.circle(cx, cy, drawRadius, 2f);
            }
        }
    }

    private float solveQuadraticPositive(float a, float b, float c) {
        if (Math.abs(a) < 0.00001f) {
            if (Math.abs(b) < 0.00001f) return -1;
            return -c / b;
        }

        float delta = b*b - 4*a*c;
        if (delta < 0) return -1;

        float sqrtDelta = (float) Math.sqrt(delta);
        float x1 = (-b + sqrtDelta) / (2*a);
        float x2 = (-b - sqrtDelta) / (2*a);

        if (x1 > 0) return x1;
        if (x2 > 0) return x2;
        return -1;
    }

    private void drawPseudo3DGizmo(LightObjectComponent loc, float cx, float cy, float ppwu, float zoom) {
        float heightPixel = (loc.height * ppwu) / zoom;

        float topX = cx;
        float topY = cy + heightPixel;

        shapeDrawer.setColor(HEIGHT_COLOR);

        shapeDrawer.line(cx, cy, topX, topY, 1.5f);
        shapeDrawer.circle(cx, cy, 3f, 1f);
        shapeDrawer.filledCircle(topX, topY, 4f);
        shapeDrawer.line(topX - 4, topY, topX + 4, topY, 1f);
    }

    @Override
    public void hide() {
        // you cannot hide light folower
        selected = false;
        icon.setColor(Color.WHITE);
    }

    @Override
    public void show() {
        super.show();
        icon.setColor(Color.ORANGE);
        selected = true;
    }
}