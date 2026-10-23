package games.rednblack.editor.plugin.ninepatch;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MainPanelMediatorTest {

    private static final String ATLAS = "pack.png\n"
            + "size: 2048, 2048\n"
            + "filter: Linear, Linear\n"
            + "base-confirm-window\n"
            + "\tsplit: 70, 69, 61, 70\n"
            + "\tbounds: 1, 1, 300, 250\n"
            + "confirm-window\n"
            + "\tbounds: 1029, 381, 200, 200\n";

    @Test
    public void aRegionIsFoundByItsOwnLineNotInsideALongerName() {
        int index = MainPanelMediator.regionIndex(ATLAS, "confirm-window");
        assertEquals(ATLAS.indexOf("\nconfirm-window\n") + 1, index);
        assertEquals("\tbounds: 1029", ATLAS.substring(ATLAS.indexOf("bounds: ", index) - 1).substring(0, 13));
    }

    @Test
    public void theLongerNameIsStillFound() {
        assertEquals(ATLAS.indexOf("base-confirm-window"), MainPanelMediator.regionIndex(ATLAS, "base-confirm-window"));
    }

    @Test
    public void aRegionOnTheFirstLineIsFound() {
        assertEquals(0, MainPanelMediator.regionIndex("button\n\tbounds: 1, 1, 2, 2\n", "button"));
    }

    @Test
    public void aMissingRegionIsNotFound() {
        assertEquals(-1, MainPanelMediator.regionIndex(ATLAS, "window"));
    }
}
