package games.rednblack.editor.system;

import games.rednblack.editor.proxy.WidgetEditingProxy;
import games.rednblack.editor.renderer.components.widget.ScrollPaneComponent;
import games.rednblack.editor.renderer.components.widget.WidgetComponent;
import games.rednblack.editor.renderer.ecs.Engine;
import games.rednblack.editor.renderer.ecs.EngineConfigurationBuilder;
import games.rednblack.editor.renderer.systems.UIInputSystem;
import games.rednblack.editor.renderer.systems.WidgetStateSystem;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class EditorScrollPaneSystemTest {

    private final WidgetEditingProxy widgetEditing = new WidgetEditingProxy();

    @After
    public void tearDown() {
        widgetEditing.setInputForwarded(false);
    }

    private Engine engine() {
        Engine engine = new Engine(new EngineConfigurationBuilder()
                .with(new UIInputSystem())
                .with(new EditorScrollPaneSystem())
                .with(new WidgetStateSystem())
                .build());
        engine.process();
        return engine;
    }

    /** The entity aspect is read from the concrete class: a subclass has to declare it again. */
    @Test
    public void canBeRegisteredInAnEngine() {
        assertNotNull(engine().getSystem(EditorScrollPaneSystem.class));
    }

    /** What is being authored has to stay in sight, and a drag moves the item it started on. */
    @Test
    public void inTheSandboxTheBarsStayAndDragsDoNotScroll() {
        Engine engine = engine();
        EditorScrollPaneSystem system = engine.getSystem(EditorScrollPaneSystem.class);
        WidgetComponent widget = new WidgetComponent();
        ScrollPaneComponent pane = new ScrollPaneComponent();
        pane.fadeAlpha = 0.5f;
        engine.setDelta(0.5f);

        system.fade(pane, widget);

        assertEquals(1f, pane.fadeAlpha, 0.0001f);
        assertFalse(system.flickScrolls(widget));
    }

    /** Once the input is forwarded the pane is the one a player would meet, fading and flicks included. */
    @Test
    public void whileTheInputIsForwardedThePaneBehavesLikeInAGame() {
        Engine engine = engine();
        EditorScrollPaneSystem system = engine.getSystem(EditorScrollPaneSystem.class);
        WidgetComponent widget = new WidgetComponent();
        ScrollPaneComponent pane = new ScrollPaneComponent();
        pane.fadeAlpha = 1f;
        engine.setDelta(0.5f);

        widgetEditing.setInputForwarded(true);
        system.fade(pane, widget);

        assertTrue(pane.fadeAlpha < 1f);
        assertTrue(system.flickScrolls(widget));
    }
}
