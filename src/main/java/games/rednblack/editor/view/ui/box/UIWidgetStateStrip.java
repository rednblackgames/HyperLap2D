package games.rednblack.editor.view.ui.box;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.PopupMenu;
import com.kotcrab.vis.ui.widget.VisTextButton;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.h2d.common.view.ui.Cursors;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.h2d.common.view.ui.listener.CursorListener;

/**
 * Floating bar with one toggle per state of the widget being edited. The checked one is the state
 * the sandbox shows, and the one edits are recorded into. It can be dragged around by the grip on
 * its left, and stays where it was put for the rest of the session.
 */
public class UIWidgetStateStrip extends UIBaseBox {

    private static final String PREFIX = "games.rednblack.editor.view.ui.box.UIWidgetStateStrip";
    public static final String STATE_CLICKED = PREFIX + ".STATE_CLICKED";

    private static final float TOP_GAP = 12;

    private final ButtonGroup<VisTextButton> buttonGroup = new ButtonGroup<>();
    private final Array<String> shownStates = new Array<>();
    private String shownType = null;
    private boolean lockEvents = false;
    /** false until the bar has been given its default place, or after the user has moved it */
    private boolean placed = false;

    public UIWidgetStateStrip() {
        super();
        buttonGroup.setMinCheckCount(1);
        buttonGroup.setMaxCheckCount(1);

        setBackground(VisUI.getSkin().get(PopupMenu.PopupMenuStyle.class).background);
        setTouchable(Touchable.enabled);
        // a click on the bar itself is not a click on the scene underneath
        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });

        setVisible(false);
    }

    /**
     * @param widgetType shown in front of the states, may be empty
     * @param states     null or empty hides the bar
     */
    public void setStates(String widgetType, Array<String> states, String currentState) {
        if (states == null || states.size == 0) {
            setVisible(false);
            return;
        }

        if (!shownStates.equals(states) || !String.valueOf(widgetType).equals(shownType)) {
            shownStates.clear();
            shownStates.addAll(states);
            shownType = String.valueOf(widgetType);
            rebuild(widgetType);
        }

        setVisible(true);
        setCurrentState(currentState);
    }

    private void rebuild(String widgetType) {
        buttonGroup.clear();
        clearChildren();

        add(new Grip()).width(10).fillY().padLeft(4).padRight(8);

        String title = widgetType == null || widgetType.isEmpty() ? "State" : widgetType + " state";
        add(StandardWidgetsFactory.createLabel(title)).padRight(8);

        for (final String state : shownStates) {
            VisTextButton button = StandardWidgetsFactory.createTextButton(state, "toggle");
            button.setUserObject(state);
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (lockEvents || !((VisTextButton) actor).isChecked()) return;
                    facade.sendNotification(STATE_CLICKED, state);
                }
            });
            StandardWidgetsFactory.addFingerCursor(button);
            buttonGroup.add(button);
            add(button).padRight(2);
        }

        pack();
    }

    public void setCurrentState(String state) {
        lockEvents = true;
        // checking one unchecks the others, the group always keeps exactly one checked
        for (VisTextButton button : buttonGroup.getButtons()) {
            if (state != null && state.equals(button.getUserObject())) button.setChecked(true);
        }
        lockEvents = false;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!isVisible() || getStage() == null) return;

        if (!placed) {
            // top centre of the scene area, under the menu and tool bars
            setPosition(Math.round((getStage().getWidth() - getWidth()) / 2f),
                    Math.round(getStage().getHeight() - UIStage.SANDBOX_TOP_MARGIN - getHeight() - TOP_GAP));
            placed = true;
        }
        keepInsideStage();
    }

    private void keepInsideStage() {
        float maxX = Math.max(0, getStage().getWidth() - getWidth());
        float maxY = Math.max(0, getStage().getHeight() - getHeight());
        setPosition(MathUtils.clamp(getX(), 0, maxX), MathUtils.clamp(getY(), 0, maxY));
    }

    @Override
    public void update() {

    }

    /** Drag anchor: two columns of dots, moving the whole bar while dragged. */
    private class Grip extends Actor {
        private static final int ROWS = 3;
        private static final float DOT = 2, GAP = 3;

        private final Drawable dot = VisUI.getSkin().getDrawable("white");
        private final Color color = new Color(0xDEDEDE99);
        private float grabX, grabY;

        Grip() {
            addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    grabX = event.getStageX() - UIWidgetStateStrip.this.getX();
                    grabY = event.getStageY() - UIWidgetStateStrip.this.getY();
                    return true;
                }

                @Override
                public void touchDragged(InputEvent event, float x, float y, int pointer) {
                    UIWidgetStateStrip.this.setPosition(Math.round(event.getStageX() - grabX), Math.round(event.getStageY() - grabY));
                    keepInsideStage();
                }
            });
            //the move cursor of the transform tool: this is a handle to drag, not a button to click
            addListener(new CursorListener(Cursors.CROSS, facade));
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

            float width = 2 * DOT + GAP;
            float height = ROWS * DOT + (ROWS - 1) * GAP;
            float startX = getX() + Math.round((getWidth() - width) / 2f);
            float startY = getY() + Math.round((getHeight() - height) / 2f);

            for (int column = 0; column < 2; column++) {
                for (int row = 0; row < ROWS; row++) {
                    dot.draw(batch, startX + column * (DOT + GAP), startY + row * (DOT + GAP), DOT, DOT);
                }
            }
        }
    }
}
