package games.rednblack.editor.data.migrations.data020;

import games.rednblack.editor.renderer.data.LightsPropertiesVO;
import games.rednblack.editor.renderer.data.PhysicsPropertiesVO;

import java.util.ArrayList;

public class SceneVO {
    public String sceneName = "";

    public CompositeVO composite;

    public PhysicsPropertiesVO physicsPropertiesVO = new PhysicsPropertiesVO();
    public LightsPropertiesVO lightsPropertiesVO = new LightsPropertiesVO();

    public ArrayList<Float> verticalGuides = new ArrayList<Float>();
    public ArrayList<Float> horizontalGuides = new ArrayList<Float>();
}
