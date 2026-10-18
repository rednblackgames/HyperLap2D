package games.rednblack.editor.proxy;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WidgetEditingProxyTest {

    private final WidgetEditingProxy proxy = new WidgetEditingProxy();

    @After
    public void tearDown() {
        proxy.setInputForwarded(false);
    }

    @Test
    public void theSandboxKeepsItsInputUntilItIsHandedOver() {
        assertFalse(WidgetEditingProxy.isInputForwarded());

        proxy.setInputForwarded(true);
        assertTrue(WidgetEditingProxy.isInputForwarded());

        proxy.setInputForwarded(false);
        assertFalse(WidgetEditingProxy.isInputForwarded());
    }

    /** The point of forwarding is seeing the widgets react, the one being authored most of all. */
    @Test
    public void whileTheInputIsForwardedNothingCountsAsBeingEdited() {
        proxy.setInputForwarded(true);

        assertFalse(WidgetEditingProxy.isBeingEdited(1, null));
    }

    @Test
    public void aNewSceneTakesTheInputBack() {
        proxy.setInputForwarded(true);

        proxy.reset();

        assertFalse(WidgetEditingProxy.isInputForwarded());
    }
}
