package games.rednblack.editor.view.ui.box.resourcespanel;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.editor.utils.MenuIcons;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

/**
 * Placeholder shown inside a resources tab instead of the thumbnail list when there is nothing to
 * list: an icon, a short title and a hint on how to get content there. The icon comes from the
 * skin atlas ({@code icon-empty-*} regions) and is simply omitted when the region is missing.
 */
public class EmptyResourcesHint extends VisTable {

    public EmptyResourcesHint(String iconRegion, String title, String hint) {
        Drawable icon = MenuIcons.get(iconRegion);
        if (icon != null) {
            add(new VisImage(icon)).padBottom(6);
            row();
        }

        // Wrapped labels report no preferred width, so the list table never grows past the panel and
        // the scroll pane hands the hint the panel width to wrap into instead of a horizontal bar.
        VisLabel titleLabel = StandardWidgetsFactory.createLabel(title, "property-label-large", Align.center);
        titleLabel.setWrap(true);
        add(titleLabel).growX().padBottom(2);
        row();

        VisLabel hintLabel = StandardWidgetsFactory.createLabel(hint, "property-label", Align.center);
        hintLabel.setWrap(true);
        add(hintLabel).growX();

        pad(12, 8, 12, 8);
    }
}
