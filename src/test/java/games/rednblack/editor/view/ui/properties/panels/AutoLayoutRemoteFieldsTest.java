package games.rednblack.editor.view.ui.properties.panels;

import games.rednblack.editor.renderer.components.AutoLayoutComponent;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** Remote edits name the auto layout's choices the way the scene file does. */
public class AutoLayoutRemoteFieldsTest {

    @Test
    public void constantsAreFoundByNameWhateverTheCase() {
        assertEquals(AutoLayoutComponent.Mode.GRID.ordinal(), UIAutoLayoutProperties.enumIndex(AutoLayoutComponent.Mode.values(), "GRID"));
        assertEquals(AutoLayoutComponent.Justify.SPACE_BETWEEN.ordinal(),
                UIAutoLayoutProperties.enumIndex(AutoLayoutComponent.Justify.values(), "space_between"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void anUnknownNameIsRefused() {
        UIAutoLayoutProperties.enumIndex(AutoLayoutComponent.Align.values(), "MIDDLE");
    }
}
