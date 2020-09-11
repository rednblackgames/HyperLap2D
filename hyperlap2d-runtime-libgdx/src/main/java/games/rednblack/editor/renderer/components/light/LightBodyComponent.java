package games.rednblack.editor.renderer.components.light;

import box2dLight.ChainLight;
import box2dLight.RayHandler;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.renderer.commons.RefreshableObject;
import games.rednblack.editor.renderer.components.PolygonComponent;
import games.rednblack.editor.renderer.components.RemovableComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.components.physics.PhysicsBodyComponent;
import games.rednblack.editor.renderer.physics.PhysicsBodyLoader;
import games.rednblack.editor.renderer.utils.ComponentRetriever;

public class LightBodyComponent extends RefreshableObject implements RemovableComponent {

    public float[] color = new float[]{1f, 1f, 1f, 1f};
    public int rays = 4;
    public float distance = 30;
    public int rayDirection = 1;
    public float softnessLength = 1f;
    public boolean isStatic = false;
    public boolean isXRay = false;
    public boolean isSoft = true;
    public boolean isActive = true;

    public ChainLight lightObject;
    private RayHandler rayHandler;

    Vector2 tmp = new Vector2();

    public LightBodyComponent() {

    }

    @Override
    public void onRemove() {
        if (lightObject != null) {
            lightObject.remove();
            lightObject = null;
        }
    }

    @Override
    public void reset() {
        color[0] = 1f;
        color[1] = 1f;
        color[2] = 1f;
        color[3] = 1f;

        rays = 4;
        distance = 30;
        rayDirection = 1;
        softnessLength = 1f;
        isStatic = false;
        isXRay = false;
        isSoft = true;
        isActive = true;

        needsRefresh = false;

        lightObject = null;
    }

    public void setRayHandler(RayHandler rayHandler) {
        this.rayHandler = rayHandler;
    }

    @Override
    protected void refresh(Entity entity) {
        if (lightObject != null) {
            lightObject.remove();
            lightObject = null;
        }

        PolygonComponent polygonComponent = ComponentRetriever.get(entity, PolygonComponent.class);
        PhysicsBodyComponent physicsComponent = ComponentRetriever.get(entity, PhysicsBodyComponent.class);
        TransformComponent transformComponent = ComponentRetriever.get(entity, TransformComponent.class);

        if (polygonComponent != null && physicsComponent != null && polygonComponent.vertices != null) {
            Array<Float> chainArray = new Array<>();

            for (int i = 0; i < polygonComponent.vertices.length; i++) {
                for (int j = 0; j < polygonComponent.vertices[i].length; j++) {
                    Vector2 point = polygonComponent.vertices[i][j];
                    tmp.set(point).sub(transformComponent.originX, transformComponent.originY);
                    chainArray.add(tmp.x, tmp.y);
                }
            }
            Vector2 point = polygonComponent.vertices[0][0];
            tmp.set(point).sub(transformComponent.originX, transformComponent.originY);
            chainArray.add(tmp.x, tmp.y);

            int i = 0;
            float[] chain = new float[chainArray.size];
            for (Float f : chainArray) {
                chain[i++] = (f != null ? f* PhysicsBodyLoader.getScale() : Float.NaN);
            }

            Color lightColor = new Color(color[0], color[1], color[2], color[3]);
            lightObject = new ChainLight(rayHandler, rays, lightColor, distance * PhysicsBodyLoader.getScale(), rayDirection, chain);
            lightObject.attachToBody(physicsComponent.body);
        }
    }
}
