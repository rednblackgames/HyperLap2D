package games.rednblack.editor.remote;

import games.rednblack.editor.renderer.ecs.Engine;
import games.rednblack.editor.renderer.ecs.EngineConfigurationBuilder;
import games.rednblack.editor.renderer.systems.WidgetStateSystem;
import games.rednblack.editor.renderer.widget.handlers.CoreStateOverrides;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/** The values set_widget_override takes, checked by the handler of each property. */
public class RemoteWidgetOpsTest {

    private WidgetStateSystem stateSystem;

    @Before
    public void setUp() {
        Engine engine = new Engine(new EngineConfigurationBuilder().with(new WidgetStateSystem()).build());
        stateSystem = engine.getSystem(WidgetStateSystem.class);
    }

    private String check(String key, String value) {
        return RemoteWidgetOps.checkValue(-1, stateSystem.getHandler(key), value);
    }

    @Test
    public void numbersMustReadAsNumbers() {
        assertNull(check(CoreStateOverrides.X, "12.5"));
        assertNotNull(check(CoreStateOverrides.X, "twelve"));
    }

    @Test
    public void colorsMustReadAsColors() {
        assertNull(check(CoreStateOverrides.TINT, "ff0000ff"));
        assertNotNull(check(CoreStateOverrides.TINT, "red-ish"));
    }

    @Test
    public void switchesAreTrueOrFalse() {
        assertNull(check(CoreStateOverrides.VISIBLE, "false"));
        assertNull(check(CoreStateOverrides.VISIBLE, "true"));
        assertNotNull(check(CoreStateOverrides.VISIBLE, "hidden"));
    }

    @Test
    public void textIsTakenAsItIs() {
        assertNull(check(CoreStateOverrides.LABEL_TEXT, "Pressed!"));
    }
}
