package games.rednblack.editor.data.migrations.data020;

import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.renderer.data.*;

import java.util.ArrayList;
import java.util.HashMap;

public class ProjectInfoVO {
    public int pixelToWorld = 1;

    public ResolutionEntryVO originalResolution = new ResolutionEntryVO();

    public Array<ResolutionEntryVO> resolutions = new Array<ResolutionEntryVO>();
    public ArrayList<SceneVO> scenes = new ArrayList<SceneVO>();

    public HashMap<String, CompositeItemVO> libraryItems = new HashMap<>();
    public HashMap<String, GraphVO> libraryActions = new HashMap<>();

    public HashMap<String, TexturePackVO> imagesPacks = new HashMap<>();
    public HashMap<String, TexturePackVO> animationsPacks = new HashMap<>();
}
