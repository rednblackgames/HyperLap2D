package games.rednblack.editor.system;

import games.rednblack.editor.proxy.WidgetEditingProxy;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.NodeComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.components.ZIndexComponent;
import games.rednblack.editor.renderer.components.widget.WidgetComponent;
import games.rednblack.editor.renderer.components.widget.WindowComponent;
import games.rednblack.editor.renderer.ecs.Engine;
import games.rednblack.editor.renderer.ecs.EngineConfigurationBuilder;
import games.rednblack.editor.renderer.ecs.annotations.All;
import games.rednblack.editor.renderer.systems.UIInputSystem;
import games.rednblack.editor.renderer.systems.WidgetStateSystem;
import games.rednblack.editor.renderer.widget.WidgetTypes;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class EditorWindowSystemTest {

    private final WidgetEditingProxy widgetEditing = new WidgetEditingProxy();

    /**
     * Without the state bar's question, which needs the editor's sandbox: outside preview mode the
     * window being edited keeps its state, and there is no sandbox to ask which one that is.
     */
    @All({WindowComponent.class, WidgetComponent.class})
    private static class TestableEditorWindowSystem extends EditorWindowSystem {
        @Override
        protected void updateWidgetState(int entity, WidgetComponent widget, WindowComponent window) {
        }
    }

    @After
    public void tearDown() {
        widgetEditing.setInputForwarded(false);
    }

    private Engine engine() {
        Engine engine = new Engine(new EngineConfigurationBuilder()
                .with(new UIInputSystem())
                .with(new TestableEditorWindowSystem())
                .with(new WidgetStateSystem())
                .build());
        engine.process();
        return engine;
    }

    private int window(Engine engine) {
        int entity = engine.create();
        engine.edit(entity).create(NodeComponent.class);
        engine.edit(entity).create(MainItemComponent.class);
        engine.edit(entity).create(ZIndexComponent.class);
        TransformComponent transform = engine.edit(entity).create(TransformComponent.class);
        transform.x = 100;
        transform.y = 50;
        DimensionsComponent dimensions = engine.edit(entity).create(DimensionsComponent.class);
        dimensions.width = 300;
        dimensions.height = 200;
        WidgetTypes.get(WidgetTypes.WINDOW).setup(engine.edit(entity).create(WidgetComponent.class));
        engine.edit(entity).create(WindowComponent.class);
        engine.process();
        return entity;
    }

    /** The entity aspect is read from the concrete class: a subclass has to declare it again. */
    @Test
    public void canBeRegisteredInAnEngine() {
        assertNotNull(engine().getSystem(TestableEditorWindowSystem.class));
    }

    @Test
    public void outsidePreviewModeTheWindowIsDesignedNotUsed() {
        EditorWindowSystem system = engine().getSystem(TestableEditorWindowSystem.class);
        assertFalse(system.interactive(0));

        widgetEditing.setInputForwarded(true);
        assertTrue(system.interactive(0));
    }

    /** Trying the scene out leaves nothing behind: where the window was, its order and whether it was open. */
    @Test
    public void whatPreviewModeChangedIsPutBackWhenItEnds() {
        Engine engine = engine();
        int window = window(engine);

        widgetEditing.setInputForwarded(true);
        engine.process();

        TransformComponent transform = engine.getMapper(TransformComponent.class).get(window);
        transform.x = 400;
        transform.y = 300;
        engine.getMapper(ZIndexComponent.class).get(window).setZIndex(7);
        engine.getMapper(WindowComponent.class).get(window).close();
        engine.process();

        widgetEditing.setInputForwarded(false);
        engine.process();

        assertEquals(100, transform.x, 0.001f);
        assertEquals(50, transform.y, 0.001f);
        assertEquals(0, engine.getMapper(ZIndexComponent.class).get(window).getZIndex());
        assertTrue(engine.getMapper(WindowComponent.class).get(window).isOpen());
    }
}
