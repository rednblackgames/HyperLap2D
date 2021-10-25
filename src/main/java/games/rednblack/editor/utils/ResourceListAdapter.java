package games.rednblack.editor.utils;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.kotcrab.vis.ui.util.adapter.SimpleListAdapter;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.ResourceManager;

public class ResourceListAdapter extends SimpleListAdapter<String> {
    public ResourceListAdapter(Array<String> array) {
        super(array);
    }

    public ResourceListAdapter(Array<String> array, String styleName) {
        super(array, styleName);
    }

    public ResourceListAdapter(Array<String> array, SimpleListAdapterStyle style) {
        super(array, style);
    }

    @Override
    protected VisTable createView(String item) {
        ResourceManager rm = HyperLap2DFacade.getInstance().retrieveProxy(ResourceManager.NAME);

        VisTable table = new VisTable();
        table.left();
        Image icon = new Image(new TextureRegionDrawable(rm.getTextureRegion(item)), Scaling.contain, Align.center);
        table.add(icon).width(45).height(45);
        table.add(new VisLabel(item)).padLeft(10).row();
        return table;
    }
}
