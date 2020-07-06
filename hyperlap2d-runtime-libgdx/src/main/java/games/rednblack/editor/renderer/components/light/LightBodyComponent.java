package games.rednblack.editor.renderer.components.light;

import box2dLight.ChainLight;
import games.rednblack.editor.renderer.components.RemovableComponent;

public class LightBodyComponent implements RemovableComponent {

    public float[] color;
    public int rays;
    public float distance;
    public int rayDirection;
    public float softnessLength;
    public boolean isStatic;
    public boolean isXRay;
    public boolean isSoft;
    public boolean isActive;

    public ChainLight lightObject;
    public boolean needToRefreshLight = false;

    public LightBodyComponent() {
        color = new float[]{1f, 1f, 1f, 1f};
        rays = 4;
        distance = 30;
        rayDirection = 1;
        softnessLength = 1f;
        isStatic = false;
        isXRay = false;
        isSoft = true;
        isActive = true;
    }

    @Override
    public void onRemove() {
        if (lightObject != null) {
            lightObject.remove();
            lightObject = null;
        }
    }
}
