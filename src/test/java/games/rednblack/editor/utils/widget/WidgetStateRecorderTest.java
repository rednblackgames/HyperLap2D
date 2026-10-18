package games.rednblack.editor.utils.widget;

import games.rednblack.editor.renderer.components.LayoutComponent;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.NodeComponent;
import games.rednblack.editor.renderer.components.ParentNodeComponent;
import games.rednblack.editor.renderer.components.TintComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.components.widget.WidgetComponent;
import games.rednblack.editor.renderer.components.widget.WidgetPartComponent;
import games.rednblack.editor.renderer.ecs.Engine;
import games.rednblack.editor.renderer.ecs.EngineConfigurationBuilder;
import games.rednblack.editor.renderer.systems.WidgetStateSystem;
import games.rednblack.editor.renderer.widget.WidgetTypes;
import games.rednblack.editor.renderer.widget.handlers.CoreStateOverrides;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class WidgetStateRecorderTest {

    private static final String PRESSED = WidgetTypes.STATE_PRESSED;
    private static final String NORMAL = WidgetTypes.STATE_NORMAL;

    private Engine engine;
    private WidgetStateRecorder recorder;
    private int widget, part;

    @Before
    public void setUp() {
        engine = new Engine(new EngineConfigurationBuilder().with(new WidgetStateSystem()).build());
        recorder = new WidgetStateRecorder(engine);

        widget = engine.create();
        WidgetTypes.get(WidgetTypes.BUTTON).setup(engine.edit(widget).create(WidgetComponent.class));
        engine.edit(widget).create(NodeComponent.class);

        part = createChild(widget, "part");
        transform(part).x = 10;
        engine.process();
    }

    private int createChild(int parent, String uniqueId) {
        int entity = engine.create();
        engine.edit(entity).create(ParentNodeComponent.class).parentEntity = parent;
        engine.edit(entity).create(MainItemComponent.class).uniqueId = uniqueId;
        engine.edit(entity).create(TransformComponent.class);
        engine.edit(entity).create(TintComponent.class);
        engine.getMapper(NodeComponent.class).get(parent).addChild(entity);
        return entity;
    }

    private TransformComponent transform(int entity) {
        return engine.getMapper(TransformComponent.class).get(entity);
    }

    private WidgetPartComponent part(int entity) {
        return engine.getMapper(WidgetPartComponent.class).get(entity);
    }

    /** What a command followed by the recorder hook amounts to. */
    private void edit(Runnable change) {
        change.run();
        recorder.record(widget);
        engine.process();
    }

    @Test
    public void editsInTheDefaultStateChangeTheBaseLook() {
        edit(() -> transform(part).x = 30);

        assertNull(part(part));
        assertEquals(30, transform(part).x, 0);
    }

    @Test
    public void editsInAStateBecomeOverridesOfThatState() {
        recorder.switchState(widget, PRESSED);
        edit(() -> transform(part).x = 25);

        assertEquals("25.0", part(part).overrides.get(PRESSED).get(CoreStateOverrides.X));
        assertEquals(1, part(part).overrides.get(PRESSED).size);

        recorder.switchState(widget, NORMAL);
        engine.process();
        assertEquals(10, transform(part).x, 0);

        recorder.switchState(widget, PRESSED);
        engine.process();
        assertEquals(25, transform(part).x, 0);
    }

    @Test
    public void undoingAnEditRemovesItsOverride() {
        recorder.switchState(widget, PRESSED);
        edit(() -> transform(part).x = 25);
        edit(() -> transform(part).x = 10);

        assertEquals(0, part(part).overrides.size);
    }

    @Test
    public void aTransitionGoesAwayWithItsOverride() {
        recorder.switchState(widget, PRESSED);
        edit(() -> transform(part).x = 25);
        part(part).setTransition(PRESSED, CoreStateOverrides.X, 0.15f, "linear");

        // an edit of the value keeps it
        edit(() -> transform(part).x = 40);
        assertNotNull(part(part).getTransition(PRESSED, CoreStateOverrides.X));

        edit(() -> transform(part).x = 10);
        assertNull(part(part).getTransition(PRESSED, CoreStateOverrides.X));
    }

    @Test
    public void theEditedWidgetNeverShowsATransitionHalfWay() {
        recorder.switchState(widget, PRESSED);
        edit(() -> transform(part).x = 25);
        part(part).setTransition(PRESSED, CoreStateOverrides.X, 1f, "linear");

        // what is recorded is compared with live values: they have to be the final ones
        recorder.switchState(widget, NORMAL);
        assertEquals(10, transform(part).x, 0);
        recorder.switchState(widget, PRESSED);
        assertEquals(25, transform(part).x, 0);
        assertEquals("25.0", part(part).overrides.get(PRESSED).get(CoreStateOverrides.X));
    }

    @Test
    public void anOverrideCanBeEditedAgain() {
        recorder.switchState(widget, PRESSED);
        edit(() -> transform(part).x = 25);
        edit(() -> transform(part).x = 40);

        assertEquals("40.0", part(part).overrides.get(PRESSED).get(CoreStateOverrides.X));

        recorder.switchState(widget, NORMAL);
        engine.process();
        assertEquals(10, transform(part).x, 0);
    }

    @Test
    public void statesAreRecordedIndependently() {
        recorder.switchState(widget, PRESSED);
        edit(() -> transform(part).x = 25);

        recorder.switchState(widget, WidgetTypes.STATE_HOVER);
        engine.process();
        assertEquals(10, transform(part).x, 0);
        edit(() -> transform(part).rotation = 45);

        assertFalse(part(part).overrides.get(WidgetTypes.STATE_HOVER).containsKey(CoreStateOverrides.X));
        assertEquals("45.0", part(part).overrides.get(WidgetTypes.STATE_HOVER).get(CoreStateOverrides.ROTATION));
        assertEquals("25.0", part(part).overrides.get(PRESSED).get(CoreStateOverrides.X));
    }

    @Test
    public void itemsAddedWhileInAStateAreBaseItems() {
        recorder.switchState(widget, PRESSED);
        int[] added = new int[1];
        edit(() -> {
            added[0] = createChild(widget, "added");
            transform(added[0]).x = 77;
        });

        assertNull(part(added[0]));

        edit(() -> transform(added[0]).x = 80);
        assertEquals("80.0", part(added[0]).overrides.get(PRESSED).get(CoreStateOverrides.X));
    }

    @Test
    public void partsOfANestedWidgetAreLeftToIt() {
        int inner = createChild(widget, "inner");
        WidgetTypes.get(WidgetTypes.BUTTON).setup(engine.edit(inner).create(WidgetComponent.class));
        engine.edit(inner).create(NodeComponent.class);
        int innerPart = createChild(inner, "innerPart");
        engine.process();

        recorder.switchState(widget, PRESSED);
        edit(() -> {
            transform(inner).x = 5;
            transform(innerPart).x = 5;
        });

        assertNotNull(part(inner));
        assertNull(part(innerPart));
    }

    @Test
    public void layoutMarginsAndBiasBelongToTheStateTheyAreSetIn() {
        LayoutComponent layout = engine.edit(part).create(LayoutComponent.class);
        layout.left = new LayoutComponent.ConstraintData();
        layout.left.margin = 10;
        engine.process();

        recorder.switchState(widget, PRESSED);
        edit(() -> {
            layout.left.margin = 25;
            layout.horizontalBias = 0.8f;
        });

        assertEquals("25.0", part(part).overrides.get(PRESSED).get(CoreStateOverrides.MARGIN_LEFT));
        assertEquals("0.8", part(part).overrides.get(PRESSED).get(CoreStateOverrides.HORIZONTAL_BIAS));

        // the base keeps what it had, and gets it back on the way out
        recorder.switchState(widget, NORMAL);
        assertEquals(10, layout.left.margin, 0);
        assertEquals(0.5f, layout.horizontalBias, 0);

        recorder.switchState(widget, PRESSED);
        assertEquals(25, layout.left.margin, 0);
    }

    @Test
    public void aMarginIsNoPropertyWhileItsSideIsUnconstrained() {
        LayoutComponent layout = engine.edit(part).create(LayoutComponent.class);
        layout.left = new LayoutComponent.ConstraintData();
        engine.process();

        recorder.switchState(widget, PRESSED);
        edit(() -> layout.left.margin = 25);

        assertFalse(part(part).overrides.get(PRESSED).containsKey(CoreStateOverrides.MARGIN_RIGHT));
    }

    private void becomes(String type) {
        WidgetTypes.get(type).setup(engine.getMapper(WidgetComponent.class).get(widget));
        engine.process();
    }

    private String tintOf(int entity) {
        return engine.getMapper(games.rednblack.editor.renderer.components.TintComponent.class).get(entity).color.toString();
    }

    @Test
    public void aChildStateRecordsOnlyWhatDiffersFromItsParent() {
        becomes(WidgetTypes.CHECKBOX);

        recorder.switchState(widget, WidgetTypes.STATE_CHECKED);
        edit(() -> transform(part).x = 25);

        recorder.switchState(widget, WidgetTypes.STATE_CHECKED_HOVER);
        assertEquals(25, transform(part).x, 0);
        edit(() -> engine.getMapper(games.rednblack.editor.renderer.components.TintComponent.class).get(part).color.set(1, 0, 0, 1));

        // the check mark stays where checked put it; hover only adds its tint
        assertEquals("ff0000ff", part(part).overrides.get(WidgetTypes.STATE_CHECKED_HOVER).get(CoreStateOverrides.TINT));
        assertFalse(part(part).overrides.get(WidgetTypes.STATE_CHECKED_HOVER).containsKey(CoreStateOverrides.X));
        assertEquals("25.0", part(part).overrides.get(WidgetTypes.STATE_CHECKED).get(CoreStateOverrides.X));
    }

    @Test
    public void aChildValueBackOnItsParentsIsInheritedAgain() {
        becomes(WidgetTypes.CHECKBOX);
        recorder.switchState(widget, WidgetTypes.STATE_CHECKED);
        edit(() -> transform(part).x = 25);

        recorder.switchState(widget, WidgetTypes.STATE_CHECKED_HOVER);
        edit(() -> transform(part).x = 30);
        assertEquals("30.0", part(part).overrides.get(WidgetTypes.STATE_CHECKED_HOVER).get(CoreStateOverrides.X));

        edit(() -> transform(part).x = 25);
        assertNull(part(part).overrides.get(WidgetTypes.STATE_CHECKED_HOVER));

        // and the base is still known, so leaving puts the part back where it started
        recorder.switchState(widget, NORMAL);
        assertEquals(10, transform(part).x, 0);
    }

    @Test
    public void aPartTheWidgetDrivesIsNotRecorded() {
        becomes(WidgetTypes.PROGRESS_BAR);
        engine.edit(part).create(WidgetPartComponent.class).role = WidgetTypes.ROLE_KNOB;
        engine.process();

        recorder.switchState(widget, WidgetTypes.STATE_DISABLED);
        edit(() -> {
            transform(part).x = 99;
            engine.getMapper(games.rednblack.editor.renderer.components.TintComponent.class).get(part).color.set(1, 0, 0, 1);
        });

        // the bar places the knob, but how the knob looks in a state is the state's
        assertFalse(part(part).overrides.get(WidgetTypes.STATE_DISABLED).containsKey(CoreStateOverrides.X));
        assertEquals("ff0000ff", part(part).overrides.get(WidgetTypes.STATE_DISABLED).get(CoreStateOverrides.TINT));
    }

    @Test
    public void positionsDrivenByLayoutAreNotRecorded() {
        LayoutComponent layout = engine.edit(part).create(LayoutComponent.class);
        layout.left = new LayoutComponent.ConstraintData();
        engine.process();

        recorder.switchState(widget, PRESSED);
        edit(() -> {
            transform(part).x = 99;
            transform(part).y = 99;
        });

        assertFalse(part(part).overrides.get(PRESSED).containsKey(CoreStateOverrides.X));
        assertEquals("99.0", part(part).overrides.get(PRESSED).get(CoreStateOverrides.Y));
    }
}
