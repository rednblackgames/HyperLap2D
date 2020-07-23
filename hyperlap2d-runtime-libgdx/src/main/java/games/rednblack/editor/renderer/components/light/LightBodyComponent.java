package games.rednblack.editor.renderer.components.light;

import box2dLight.ChainLight;
import games.rednblack.editor.renderer.components.RemovableComponent;

public class LightBodyComponent implements RemovableComponent {

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
    public boolean needToRefreshLight = false;

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
        onRemove();

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

        needToRefreshLight = false;
    }
}
