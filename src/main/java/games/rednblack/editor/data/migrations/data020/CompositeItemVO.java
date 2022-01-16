package games.rednblack.editor.data.migrations.data020;

import games.rednblack.editor.renderer.data.MainItemVO;

public class CompositeItemVO extends MainItemVO {
    public CompositeVO composite;

    public float width;
    public float height;
    public boolean automaticResize = true;
    public boolean scissorsEnabled = false;
    public boolean renderToFBO = false;

    @Override
    public String getResourceName() {
        return null;
    }
}
