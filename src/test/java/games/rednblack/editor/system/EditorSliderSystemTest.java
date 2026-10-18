package games.rednblack.editor.system;

import games.rednblack.editor.renderer.ecs.Engine;
import games.rednblack.editor.renderer.ecs.EngineConfigurationBuilder;
import games.rednblack.editor.renderer.systems.ProgressBarSystem;
import games.rednblack.editor.renderer.systems.UIInputSystem;
import games.rednblack.editor.renderer.systems.WidgetStateSystem;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class EditorSliderSystemTest {

    /** The entity aspect is read from the concrete class: a subclass has to declare it again. */
    @Test
    public void canBeRegisteredInAnEngine() {
        Engine engine = new Engine(new EngineConfigurationBuilder()
                .with(new UIInputSystem())
                .with(new ProgressBarSystem())
                .with(new EditorSliderSystem())
                .with(new WidgetStateSystem())
                .build());
        engine.process();

        assertNotNull(engine.getSystem(EditorSliderSystem.class));
    }
}
