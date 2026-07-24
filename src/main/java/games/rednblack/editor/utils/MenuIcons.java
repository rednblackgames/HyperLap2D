package games.rednblack.editor.utils;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.VisUI;

/**
 * Shared resolver for menu/context-menu item icons packed into the VisUI skin atlas
 * (regions named {@code icon-menu-*}). Returns {@code null} when the region is missing so callers
 * can fall back to a text-only item instead of crashing.
 */
public final class MenuIcons {

    private MenuIcons() {
    }

    public static Drawable get(String region) {
        Skin skin = VisUI.getSkin();
        if (region == null || skin.getAtlas().findRegion(region) == null) return null;
        return skin.getDrawable(region);
    }
}
