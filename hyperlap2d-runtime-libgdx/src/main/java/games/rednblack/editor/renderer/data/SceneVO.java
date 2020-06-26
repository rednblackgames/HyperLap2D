package games.rednblack.editor.renderer.data;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter.OutputType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class SceneVO {

    public String sceneName = "";

    public CompositeVO composite;

    public PhysicsPropertiesVO physicsPropertiesVO = new PhysicsPropertiesVO();
    public LightsPropertiesVO lightsPropertiesVO = new LightsPropertiesVO();

    public ArrayList<Float> verticalGuides = new ArrayList<Float>();
    public ArrayList<Float> horizontalGuides = new ArrayList<Float>();

    public SceneVO() {

    }

    public SceneVO(SceneVO vo) {
        sceneName = new String(vo.sceneName);
        composite = new CompositeVO(vo.composite);
        physicsPropertiesVO = new PhysicsPropertiesVO(vo.physicsPropertiesVO);
        lightsPropertiesVO = vo.lightsPropertiesVO;
    }

    public String constructJsonString() {
        String str = "";
        Json json = new Json();
        json.setOutputType(OutputType.json);
        str = json.toJson(this);
        return str;
    }
}
