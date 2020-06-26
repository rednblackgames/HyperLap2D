package games.rednblack.editor.renderer.data;

public class LightsPropertiesVO {
    public boolean enabled;
    public float[] ambientColor;
    public int blurNum;
    public String lightType; //DIFFUSE, DIRECTIONAL

    public int directionalRays;
    public float directionalDegree;
    public float[] directionalColor;

    public LightsPropertiesVO() {
        blurNum = 3;
        lightType = "DIFFUSE";
        enabled = false;
        directionalRays = 12;
        directionalDegree = 0;
        ambientColor = new float[]{1f, 1f, 1f, 1f};
        directionalColor = new float[]{1f, 1f, 1f, 1f};
    }

    public LightsPropertiesVO(LightsPropertiesVO lightsPropertiesVO) {
        this.enabled = lightsPropertiesVO.enabled;
        this.blurNum = lightsPropertiesVO.blurNum;
        this.lightType = lightsPropertiesVO.lightType;
        this.directionalRays = lightsPropertiesVO.directionalRays;
        this.directionalDegree = lightsPropertiesVO.directionalDegree;
        this.ambientColor = lightsPropertiesVO.ambientColor.clone();
        this.directionalColor = lightsPropertiesVO.directionalColor.clone();
    }
}
