package games.rednblack.editor.view.ui.box;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ObjectMap;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.PopupMenu;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.editor.view.ui.widget.actors.basic.WhitePixel;
import games.rednblack.h2d.common.view.ui.Cursors;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.h2d.common.view.ui.listener.CursorListener;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * Floating bar with the states of the widget being edited. The selected one is the state the
 * sandbox shows, and the one edits are recorded into. It can be dragged around by the grip on its
 * left, and stays where it was put for the rest of the session.
 *
 * Each row of states is a segmented control, and a state's children hang below it as a smaller
 * one, joined to it by a stem, so a state building on another reads as part of it.
 */
public class UIWidgetStateStrip extends UIBaseBox {

    private static final String PREFIX = "games.rednblack.editor.view.ui.box.UIWidgetStateStrip";
    public static final String STATE_CLICKED = PREFIX + ".STATE_CLICKED";

    private static final float TOP_GAP = 12;
    private static final float CONTENT_PAD = 6;

    private StateTree tree;
    /** Type, states and parents the bar was built for, so it is only rebuilt when one of them changes. */
    private String shownShape = null;
    private String currentState = null;
    /** false until the bar has been given its default place, or after the user has moved it */
    private boolean placed = false;

    public UIWidgetStateStrip() {
        super();

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
     * @param parents    child state -> the state it builds on, laid out under it
     */
    public void setStates(String widgetType, Array<String> states, ObjectMap<String, String> parents, String currentState) {
        if (states == null || states.size == 0) {
            setVisible(false);
            return;
        }

        StringBuilder shape = new StringBuilder(String.valueOf(widgetType)).append('|');
        for (int i = 0; i < states.size; i++) {
            String state = states.get(i);
            shape.append(state).append('<').append(parents == null ? null : parents.get(state)).append(' ');
        }
        if (!shape.toString().equals(shownShape)) {
            shownShape = shape.toString();
            // a copy, so nothing done meanwhile can reach into the widget's own list
            rebuild(widgetType, new Array<>(states), parents);
        }

        setVisible(true);
        setCurrentState(currentState);
    }

    private void rebuild(String widgetType, Array<String> states, ObjectMap<String, String> parents) {
        clearChildren();

        tree = new StateTree();
        for (int i = 0; i < states.size; i++) {
            String state = states.get(i);
            String parent = parents == null ? null : parents.get(state);
            // a parent the widget does not declare puts its child back at the top
            if (parent != null && !states.contains(parent, false)) parent = null;
            tree.add(state, parent);
        }
        tree.setSelected(currentState);

        add(new Grip()).width(10).fillY().padLeft(4).padRight(8);

        // the type on a line of its own above the states, so it does not add to the width of the bar
        VisLabel title = StandardWidgetsFactory.createLabel(typeLabel(widgetType));
        title.setColor(VisUI.getSkin().getColor("hyperlap2d-menuitem-grey"));
        VisTable content = new VisTable();
        content.add(title).left().padBottom(4).row();
        content.add(tree).left();
        // room around the pills, so they never touch the edge of the bar
        add(content).top().pad(CONTENT_PAD, 0, CONTENT_PAD, CONTENT_PAD);

        pack();
    }

    /** The type as the context menu names it: checkBox reads "CheckBox". */
    private static String typeLabel(String widgetType) {
        if (widgetType == null || widgetType.isEmpty()) return "State";
        return Character.toUpperCase(widgetType.charAt(0)) + widgetType.substring(1);
    }

    /**
     * Under its parent a state needs only its own part of the name, since the tree says whose child
     * it is: checkedHover under checked reads "hover". A name not starting with its parent's is kept.
     */
    static String childLabel(String state, String parent) {
        if (parent != null && state.length() > parent.length() && state.startsWith(parent)
                && Character.isUpperCase(state.charAt(parent.length()))) {
            return stateLabel(state.substring(parent.length()));
        }
        return stateLabel(state);
    }

    /** A state name as words, the way the other states read: checkedHover becomes "checked hover". */
    public static String stateLabel(String state) {
        StringBuilder label = new StringBuilder(state.length() + 4);
        for (int i = 0; i < state.length(); i++) {
            char c = state.charAt(i);
            if (i > 0 && Character.isUpperCase(c) && !Character.isUpperCase(state.charAt(i - 1))) label.append(' ');
            label.append(Character.toLowerCase(c));
        }
        return label.toString();
    }

    public void setCurrentState(String state) {
        currentState = state;
        if (tree != null) tree.setSelected(state);
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

    /**
     * The states as rows of segmented controls: the states building on nothing in one row, and under
     * each state the states building on it in a smaller row, centred under it where there is room
     * and joined to it by a stem. Drawn with a shape drawer; the segments only hold their label.
     */
    private class StateTree extends WidgetGroup {
        static final float ROOT_HEIGHT = 22;
        static final float CHILD_HEIGHT = 18;
        private static final float SEGMENT_PAD = 9;
        /** Room between two rows, where the stem runs. */
        private static final float LEVEL_GAP = 10;
        private static final float PILL_GAP = 8;
        /** How far from a pill's rounded end the stem may land, so it never meets a curve. */
        private static final float STEM_MARGIN = 12;
        private static final float STEM_WIDTH = 2f;
        private static final float SEPARATOR_INSET = 5;
        /** How far up a state may build on others, well beyond anything sensible. */
        private static final int MAX_DEPTH = 8;

        private final Color pillColor = new Color(0, 0, 0, 0.28f);
        private final Color hoverColor = new Color(1, 1, 1, 0.07f);
        private final Color separatorColor = new Color(1, 1, 1, 0.1f);
        private final Color stemColor = new Color(1, 1, 1, 0.45f);
        private final Color accentColor;
        private final Color textColor;
        private final Color selectedTextColor;

        /** One state: a label in its slot of a pill, clickable. */
        private class Segment extends WidgetGroup {
            final String state;
            final VisLabel label;
            final ClickListener click;
            final int level;

            Segment(final String state, String text, int level) {
                this.state = state;
                this.level = level;

                Skin skin = VisUI.getSkin();
                // a style of its own with a white font, so the colour of the label alone decides how it reads
                Label.LabelStyle style = new Label.LabelStyle(skin.get(level == 0 ? "default" : "small", Label.LabelStyle.class));
                style.fontColor = Color.WHITE;
                label = new VisLabel(text, style);
                label.setAlignment(Align.center);
                label.setTouchable(Touchable.disabled);
                addActor(label);

                click = new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        facade.sendNotification(STATE_CLICKED, state);
                    }
                };
                addListener(click);
                setTouchable(Touchable.enabled);
                StandardWidgetsFactory.addFingerCursor(this);
            }

            @Override
            public float getPrefWidth() {
                return label.getPrefWidth() + 2 * SEGMENT_PAD;
            }

            @Override
            public float getPrefHeight() {
                return level == 0 ? ROOT_HEIGHT : CHILD_HEIGHT;
            }

            @Override
            public void layout() {
                label.setBounds(0, 0, getWidth(), getHeight());
            }
        }

        /** A row of segments drawn as one control, with the segment it hangs from, if any. */
        private class Pill {
            final Array<Segment> segments = new Array<>();
            Segment parent;
            /** The row the parent segment sits in. */
            Pill parentPill;
            int level;
            float x, top, width, height;
        }

        private final Array<String> order = new Array<>();
        private final ObjectMap<String, String> parents = new ObjectMap<>();
        private final Array<Pill> pills = new Array<>();
        private String selected;
        private ShapeDrawer shapeDrawer;
        private float prefWidth, prefHeight;
        private boolean measured = false;

        StateTree() {
            setTransform(false);
            Skin skin = VisUI.getSkin();
            accentColor = skin.getColor("vis-blue");
            textColor = skin.getColor("hyperlap2d-menuitem-grey");
            selectedTextColor = skin.getColor("hyperlap2d-button-text-white");
        }

        /** In the order the widget declares them, which is the order they are shown in. */
        void add(String state, String parent) {
            order.add(state);
            if (parent != null) parents.put(state, parent);
            measured = false;
        }

        void setSelected(String state) {
            selected = state;
        }

        /**
         * The selected state and every state it builds on are lit: a child shows its parent's look
         * under its own, so the path from the selected state up to its row on top reads as one.
         */
        private boolean isLit(String state) {
            int depth = 0;
            for (String link = selected; link != null && depth++ < MAX_DEPTH; link = parents.get(link)) {
                if (link.equals(state)) return true;
            }
            return false;
        }

        /** Builds the pills, parents before children, and works out where each one goes. */
        private void measure() {
            if (measured) return;
            measured = true;

            clearChildren();
            pills.clear();

            // Indexed loops throughout: libGDX arrays share their iterators, so an array walked again
            // from inside a walk over it would fail.
            Pill roots = new Pill();
            for (int i = 0; i < order.size; i++) {
                String state = order.get(i);
                if (!parents.containsKey(state)) roots.segments.add(segment(state, null, 0));
            }
            pills.add(roots);

            // breadth first, so a whole row is known before the next one is laid out
            for (int p = 0; p < pills.size; p++) {
                Pill pill = pills.get(p);
                for (int s = 0; s < pill.segments.size; s++) {
                    Segment segment = pill.segments.get(s);
                    Pill children = null;
                    for (int i = 0; i < order.size; i++) {
                        String state = order.get(i);
                        if (!segment.state.equals(parents.get(state))) continue;
                        if (children == null) {
                            children = new Pill();
                            children.parent = segment;
                            children.parentPill = pill;
                            children.level = pill.level + 1;
                        }
                        children.segments.add(segment(state, segment.state, children.level));
                    }
                    if (children != null) pills.add(children);
                }
            }

            FloatArray nextFree = new FloatArray();
            float right = 0;
            int depth = 0;
            for (int p = 0; p < pills.size; p++) {
                Pill pill = pills.get(p);
                pill.height = pill.level == 0 ? ROOT_HEIGHT : CHILD_HEIGHT;
                pill.top = topOf(pill.level);
                pill.width = 0;
                for (int s = 0; s < pill.segments.size; s++) pill.width += pill.segments.get(s).getPrefWidth();

                if (pill.parent == null) {
                    pill.x = 0;
                } else {
                    Pill above = pill.parentPill;
                    float centre = pill.parent.getX() + pill.parent.getPrefWidth() / 2;
                    float x = centre - pill.width / 2;
                    // tucked under the row above where it would stick out, as long as it still hangs from its parent
                    x = Math.min(x, above.x + above.width - pill.width);
                    x = Math.max(x, centre - pill.width + STEM_MARGIN);
                    x = Math.min(x, centre - STEM_MARGIN);
                    // never over the row before it at the same depth, nor left of the tree
                    if (nextFree.size > pill.level) x = Math.max(x, nextFree.get(pill.level));
                    pill.x = Math.max(0, x);
                }

                float x = pill.x;
                for (int s = 0; s < pill.segments.size; s++) {
                    Segment segment = pill.segments.get(s);
                    segment.setX(x);
                    x += segment.getPrefWidth();
                }

                while (nextFree.size <= pill.level) nextFree.add(0);
                nextFree.set(pill.level, pill.x + pill.width + PILL_GAP);
                right = Math.max(right, pill.x + pill.width);
                depth = Math.max(depth, pill.level);
            }

            prefWidth = right;
            prefHeight = topOf(depth) + (depth == 0 ? ROOT_HEIGHT : CHILD_HEIGHT);
        }

        private Segment segment(String state, String parent, int level) {
            Segment segment = new Segment(state, childLabel(state, parent), level);
            addActor(segment);
            return segment;
        }

        private float topOf(int level) {
            return level == 0 ? 0 : ROOT_HEIGHT + LEVEL_GAP + (level - 1) * (CHILD_HEIGHT + LEVEL_GAP);
        }

        @Override
        public float getPrefWidth() {
            measure();
            return prefWidth;
        }

        @Override
        public float getPrefHeight() {
            measure();
            return prefHeight;
        }

        @Override
        public void layout() {
            measure();
            for (int p = 0; p < pills.size; p++) {
                Pill pill = pills.get(p);
                for (int s = 0; s < pill.segments.size; s++) {
                    Segment segment = pill.segments.get(s);
                    segment.setBounds(segment.getX(), getHeight() - pill.top - pill.height, segment.getPrefWidth(), pill.height);
                }
            }
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            validate();
            if (shapeDrawer == null) {
                shapeDrawer = new ShapeDrawer(batch, WhitePixel.sharedInstance.textureRegion) {
                    @Override
                    protected int estimateSidesRequired(float radiusX, float radiusY) {
                        // smooth ends even on these small curves
                        return Math.max(16, (int) (Math.max(radiusX, radiusY) * 3));
                    }
                };
            }

            float previous = shapeDrawer.getPackedColor();
            for (int p = 0; p < pills.size; p++) drawPill(pills.get(p), parentAlpha);
            shapeDrawer.setColor(previous);

            for (int p = 0; p < pills.size; p++) {
                Pill pill = pills.get(p);
                for (int s = 0; s < pill.segments.size; s++) {
                    Segment segment = pill.segments.get(s);
                    Color color = isLit(segment.state) ? selectedTextColor : textColor;
                    segment.label.setColor(color.r, color.g, color.b, color.a);
                }
            }
            super.draw(batch, parentAlpha);
        }

        private void drawPill(Pill pill, float parentAlpha) {
            float x = getX() + pill.x;
            float y = getY() + getHeight() - pill.top - pill.height;
            float height = pill.height;

            if (pill.parent != null) {
                // the stem, from the middle of the parent down into the pill
                float stemX = getX() + pill.parent.getX() + pill.parent.getWidth() / 2;
                float parentBottom = getY() + pill.parent.getY();
                setColor(stemColor, parentAlpha);
                shapeDrawer.line(stemX, parentBottom, stemX, y + height, STEM_WIDTH);
            }

            setColor(pillColor, parentAlpha);
            roundedBar(x, y, pill.width, height, true, true);

            int count = pill.segments.size;
            for (int i = 0; i < count; i++) {
                Segment segment = pill.segments.get(i);
                boolean isSelected = isLit(segment.state);
                if (!isSelected && !segment.click.isOver()) continue;

                setColor(isSelected ? accentColor : hoverColor, parentAlpha);
                roundedBar(getX() + segment.getX(), y, segment.getWidth(), height, i == 0, i == count - 1);
            }

            // separators between segments, left out next to a lit one where they would only add noise
            setColor(separatorColor, parentAlpha);
            for (int i = 1; i < count; i++) {
                Segment left = pill.segments.get(i - 1), segment = pill.segments.get(i);
                if (isLit(left.state) || isLit(segment.state)) continue;
                float separatorX = getX() + segment.getX();
                shapeDrawer.line(separatorX, y + SEPARATOR_INSET, separatorX, y + height - SEPARATOR_INSET, 1);
            }
        }

        /**
         * A bar whose left and right ends are round or square, as the pill's ends need them. The ends
         * are half discs meeting the middle edge to edge: the colours are translucent, so any overlap
         * would be blended twice and show as a darker half moon inside each end.
         */
        private void roundedBar(float x, float y, float width, float height, boolean roundLeft, boolean roundRight) {
            float radius = height / 2;
            float left = roundLeft ? x + radius : x;
            float right = roundRight ? x + width - radius : x + width;
            if (right > left) shapeDrawer.filledRectangle(left, y, right - left, height);
            if (roundLeft) shapeDrawer.sector(x + radius, y + radius, radius, MathUtils.HALF_PI, MathUtils.PI);
            if (roundRight) shapeDrawer.sector(x + width - radius, y + radius, radius, -MathUtils.HALF_PI, MathUtils.PI);
        }

        private void setColor(Color color, float parentAlpha) {
            shapeDrawer.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        }
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
