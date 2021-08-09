package games.rednblack.editor.view.ui.followers;

/**
 * Created by CyberJoe on 8/2/2015.
 */
public class ImageFollower extends NormalSelectionFollower {

    public ImageFollower(int entity) {
        super(entity);
    }

    @Override
    public void update() {
        /*TextureRegionComponent textureRegionComponent = ComponentRetriever.get(getEntity(), TextureRegionComponent.class);
        if(textureRegionComponent.isPolygon) {
            pixelRect.setVisible(false);
        } else {
            pixelRect.setVisible(true);
        }*/
        super.update();
    }
}
