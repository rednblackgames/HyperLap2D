package games.rednblack.editor.system;

import games.rednblack.editor.proxy.WidgetEditingProxy;
import games.rednblack.editor.renderer.ecs.Engine;
import games.rednblack.editor.renderer.ecs.EngineConfigurationBuilder;
import games.rednblack.editor.renderer.systems.UIInputSystem;
import games.rednblack.editor.renderer.systems.WidgetStateSystem;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class EditorSelectBoxSystemTest {

    private final WidgetEditingProxy widgetEditing = new WidgetEditingProxy();

    @After
    public void tearDown() {
        widgetEditing.setInputForwarded(false);
    }

    private EditorSelectBoxSystem system() {
        Engine engine = new Engine(new EngineConfigurationBuilder()
                .with(new UIInputSystem())
                .with(new EditorSelectBoxSystem())
                .with(new WidgetStateSystem())
                .build());
        engine.process();
        return engine.getSystem(EditorSelectBoxSystem.class);
    }

    /** The entity aspect is read from the concrete class: a subclass has to declare it again. */
    @Test
    public void canBeRegisteredInAnEngine() {
        assertNotNull(system());
    }

    /** While it is being designed a box never opens and no row copies exist, so none can be saved. */
    @Test
    public void inTheSandboxTheBoxStaysClosedAndMakesNoRows() {
        EditorSelectBoxSystem system = system();

        assertFalse(system.interactive(0));
        assertFalse(system.buildsRows(0));
    }

    @Test
    public void whileTheInputIsForwardedTheBoxWorksLikeInAGame() {
        EditorSelectBoxSystem system = system();

        widgetEditing.setInputForwarded(true);

        assertTrue(system.interactive(0));
        assertTrue(system.buildsRows(0));
    }
}
