package games.rednblack.editor.view.ui.dialog;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.kotcrab.vis.ui.VisUI;
import games.rednblack.editor.system.HyperLap2dRendererMiniMap;
import games.rednblack.editor.view.stage.Sandbox;

public class MiniMapDialog extends Table {
    private final TextureRegionDrawable drawable;
    private final TextureRegion region;
    private final Image miniMap;

    private Rectangle miniMapBounds;

    public MiniMapDialog() {
        drawable = new TextureRegionDrawable();
        region = new TextureRegion();
        drawable.setRegion(region);
        miniMap = new Image(drawable);
        miniMap.addListener(new ClickListener() {
            private final Vector2 touchPoint = new Vector2();

            @Override
            public void clicked(InputEvent event, float x, float y) {
                touchPoint.set(x - miniMap.getImageX(), y - miniMap.getImageY());

                float relativeX = (touchPoint.x) / miniMap.getImageWidth();
                float relativeY = (touchPoint.y) / miniMap.getImageHeight();

                float transformedX = miniMapBounds.x + relativeX * miniMapBounds.width;
                float transformedY = miniMapBounds.y + relativeY * miniMapBounds.height;

                Sandbox.getInstance().panSceneTo(transformedX, transformedY);
            }
        });
        miniMap.setAlign(Align.center);
        add(miniMap);

        setBackground(VisUI.getSkin().getDrawable("panel"));
    }

    public void update() {
        Sandbox sandbox = Sandbox.getInstance();
        HyperLap2dRendererMiniMap rendererMiniMap = sandbox.getEngine().getSystem(HyperLap2dRendererMiniMap.class);

        Texture texture = rendererMiniMap.getMiniMapTexture(sandbox.getRootEntity());
        region.setRegion(texture);
        drawable.setRegion(region);
        miniMap.setScaling(Scaling.contain);
        miniMap.setDrawable(drawable);

        miniMapBounds = rendererMiniMap.getMiniMapBounds();
    }
}
