package games.rednblack.editor.view.ui.properties.panels;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.util.Validators;
import com.kotcrab.vis.ui.widget.*;
import games.rednblack.editor.event.KeyboardListener;
import games.rednblack.editor.event.SelectBoxChangeListener;
import games.rednblack.editor.renderer.components.LayoutComponent;
import games.rednblack.editor.view.ui.properties.UIRemovableProperties;
import games.rednblack.editor.view.ui.widget.actors.basic.WhitePixel;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.puremvc.Facade;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class UILayoutProperties extends UIRemovableProperties {

    public static final String prefix = "games.rednblack.editor.view.ui.properties.panels.UILayoutProperties";
    public static final String CLOSE_CLICKED = prefix + ".CLOSE_CLICKED";

    private static final String PARENT_TARGET = "Parent";

    // Left constraint
    private VisCheckBox leftEnabled;
    private VisSelectBox<String> leftTarget;
    private VisSelectBox<String> leftSide;
    private VisValidatableTextField leftMargin;

    // Right constraint
    private VisCheckBox rightEnabled;
    private VisSelectBox<String> rightTarget;
    private VisSelectBox<String> rightSide;
    private VisValidatableTextField rightMargin;

    // Bottom constraint
    private VisCheckBox bottomEnabled;
    private VisSelectBox<String> bottomTarget;
    private VisSelectBox<String> bottomSide;
    private VisValidatableTextField bottomMargin;

    // Top constraint
    private VisCheckBox topEnabled;
    private VisSelectBox<String> topTarget;
    private VisSelectBox<String> topSide;
    private VisValidatableTextField topMargin;

    // Bias
    private VisValidatableTextField horizontalBiasField;
    private VisValidatableTextField verticalBiasField;

    // Match constraint
    private VisCheckBox matchWidthCheckBox;
    private VisCheckBox matchHeightCheckBox;

    private ConstraintPreviewWidget previewWidget;

    public UILayoutProperties() {
        super("Layout");
        initView();
        initListeners();
    }

    private void initView() {
        Validators.FloatValidator floatValidator = new Validators.FloatValidator();

        Array<String> horizontalSides = new Array<>();
        horizontalSides.add(LayoutComponent.ConstraintSide.LEFT.name());
        horizontalSides.add(LayoutComponent.ConstraintSide.RIGHT.name());

        Array<String> verticalSides = new Array<>();
        verticalSides.add(LayoutComponent.ConstraintSide.BOTTOM.name());
        verticalSides.add(LayoutComponent.ConstraintSide.TOP.name());

        // Preview widget
        previewWidget = new ConstraintPreviewWidget();
        mainTable.add(previewWidget).height(120).growX().colspan(4).padBottom(4);
        mainTable.row();

        // Left constraint - single row
        leftEnabled = StandardWidgetsFactory.createCheckBox("L");
        leftTarget = StandardWidgetsFactory.createSelectBox(String.class);
        leftSide = StandardWidgetsFactory.createSelectBox(String.class);
        leftSide.setItems(horizontalSides);
        leftSide.setSelected(LayoutComponent.ConstraintSide.LEFT.name());
        leftMargin = StandardWidgetsFactory.createValidableTextField(floatValidator);

        mainTable.add(leftEnabled).left().width(34);
        mainTable.add(leftTarget).width(90).padLeft(1);
        mainTable.add(leftSide).width(62).padLeft(1);
        mainTable.add(leftMargin).width(36).padLeft(1);
        mainTable.row().padTop(2);

        // Right constraint - single row
        rightEnabled = StandardWidgetsFactory.createCheckBox("R");
        rightTarget = StandardWidgetsFactory.createSelectBox(String.class);
        rightSide = StandardWidgetsFactory.createSelectBox(String.class);
        rightSide.setItems(horizontalSides);
        rightSide.setSelected(LayoutComponent.ConstraintSide.RIGHT.name());
        rightMargin = StandardWidgetsFactory.createValidableTextField(floatValidator);

        mainTable.add(rightEnabled).left().width(34);
        mainTable.add(rightTarget).width(90).padLeft(1);
        mainTable.add(rightSide).width(62).padLeft(1);
        mainTable.add(rightMargin).width(36).padLeft(1);
        mainTable.row().padTop(2);

        // Bottom constraint - single row
        bottomEnabled = StandardWidgetsFactory.createCheckBox("B");
        bottomTarget = StandardWidgetsFactory.createSelectBox(String.class);
        bottomSide = StandardWidgetsFactory.createSelectBox(String.class);
        bottomSide.setItems(verticalSides);
        bottomSide.setSelected(LayoutComponent.ConstraintSide.BOTTOM.name());
        bottomMargin = StandardWidgetsFactory.createValidableTextField(floatValidator);

        mainTable.add(bottomEnabled).left().width(34);
        mainTable.add(bottomTarget).width(90).padLeft(1);
        mainTable.add(bottomSide).width(62).padLeft(1);
        mainTable.add(bottomMargin).width(36).padLeft(1);
        mainTable.row().padTop(2);

        // Top constraint - single row
        topEnabled = StandardWidgetsFactory.createCheckBox("T");
        topTarget = StandardWidgetsFactory.createSelectBox(String.class);
        topSide = StandardWidgetsFactory.createSelectBox(String.class);
        topSide.setItems(verticalSides);
        topSide.setSelected(LayoutComponent.ConstraintSide.TOP.name());
        topMargin = StandardWidgetsFactory.createValidableTextField(floatValidator);

        mainTable.add(topEnabled).left().width(34);
        mainTable.add(topTarget).width(90).padLeft(1);
        mainTable.add(topSide).width(62).padLeft(1);
        mainTable.add(topMargin).width(36).padLeft(1);
        mainTable.row().padTop(5);

        // Bias section
        horizontalBiasField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        verticalBiasField = StandardWidgetsFactory.createValidableTextField(floatValidator);

        mainTable.add(new VisLabel("H Bias:", Align.right)).padRight(2);
        mainTable.add(horizontalBiasField).growX().colspan(3);
        mainTable.row().padTop(3);
        mainTable.add(new VisLabel("V Bias:", Align.right)).padRight(2).padLeft(4);
        mainTable.add(verticalBiasField).growX().colspan(3);
        mainTable.row().padTop(5);

        // Match constraint section
        matchWidthCheckBox = StandardWidgetsFactory.createCheckBox("W = match constraint");
        matchHeightCheckBox = StandardWidgetsFactory.createCheckBox("H = match constraint");
        mainTable.add(matchWidthCheckBox).left().colspan(4);
        mainTable.row().padTop(2);
        mainTable.add(matchHeightCheckBox).left().colspan(4);
        mainTable.row();

        updateConstraintFieldsEnabled();
    }

    private void initListeners() {
        ChangeListener enabledListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateConstraintFieldsEnabled();
                Facade.getInstance().sendNotification(getUpdateEventName());
            }
        };

        leftEnabled.addListener(enabledListener);
        rightEnabled.addListener(enabledListener);
        bottomEnabled.addListener(enabledListener);
        topEnabled.addListener(enabledListener);

        leftTarget.addListener(new SelectBoxChangeListener(getUpdateEventName()));
        leftSide.addListener(new SelectBoxChangeListener(getUpdateEventName()));
        leftMargin.addListener(new KeyboardListener(getUpdateEventName()));

        rightTarget.addListener(new SelectBoxChangeListener(getUpdateEventName()));
        rightSide.addListener(new SelectBoxChangeListener(getUpdateEventName()));
        rightMargin.addListener(new KeyboardListener(getUpdateEventName()));

        bottomTarget.addListener(new SelectBoxChangeListener(getUpdateEventName()));
        bottomSide.addListener(new SelectBoxChangeListener(getUpdateEventName()));
        bottomMargin.addListener(new KeyboardListener(getUpdateEventName()));

        topTarget.addListener(new SelectBoxChangeListener(getUpdateEventName()));
        topSide.addListener(new SelectBoxChangeListener(getUpdateEventName()));
        topMargin.addListener(new KeyboardListener(getUpdateEventName()));

        horizontalBiasField.addListener(new KeyboardListener(getUpdateEventName()));
        verticalBiasField.addListener(new KeyboardListener(getUpdateEventName()));

        matchWidthCheckBox.addListener(enabledListener);
        matchHeightCheckBox.addListener(enabledListener);
    }

    private void updateConstraintFieldsEnabled() {
        boolean leftOn = leftEnabled.isChecked();
        leftTarget.setDisabled(!leftOn);
        leftSide.setDisabled(!leftOn);
        leftMargin.setDisabled(!leftOn);

        boolean rightOn = rightEnabled.isChecked();
        rightTarget.setDisabled(!rightOn);
        rightSide.setDisabled(!rightOn);
        rightMargin.setDisabled(!rightOn);

        boolean bottomOn = bottomEnabled.isChecked();
        bottomTarget.setDisabled(!bottomOn);
        bottomSide.setDisabled(!bottomOn);
        bottomMargin.setDisabled(!bottomOn);

        boolean topOn = topEnabled.isChecked();
        topTarget.setDisabled(!topOn);
        topSide.setDisabled(!topOn);
        topMargin.setDisabled(!topOn);
    }

    public void setSiblingList(Array<String> siblings) {
        Array<String> targets = new Array<>();
        targets.add(PARENT_TARGET);
        targets.addAll(siblings);

        leftTarget.setItems(targets);
        rightTarget.setItems(targets);
        bottomTarget.setItems(targets);
        topTarget.setItems(targets);
    }

    // Left constraint accessors
    public boolean isLeftEnabled() { return leftEnabled.isChecked(); }
    public void setLeftEnabled(boolean enabled) { leftEnabled.setChecked(enabled); updateConstraintFieldsEnabled(); }
    public String getLeftTarget() { return leftTarget.getSelected(); }
    public void setLeftTarget(String target) { leftTarget.setSelected(target != null ? target : PARENT_TARGET); }
    public String getLeftTargetUniqueId() { return PARENT_TARGET.equals(leftTarget.getSelected()) ? null : leftTarget.getSelected(); }
    public LayoutComponent.ConstraintSide getLeftSide() { return LayoutComponent.ConstraintSide.valueOf(leftSide.getSelected()); }
    public void setLeftSide(LayoutComponent.ConstraintSide side) { leftSide.setSelected(side.name()); }
    public VisValidatableTextField getLeftMarginField() { return leftMargin; }

    // Right constraint accessors
    public boolean isRightEnabled() { return rightEnabled.isChecked(); }
    public void setRightEnabled(boolean enabled) { rightEnabled.setChecked(enabled); updateConstraintFieldsEnabled(); }
    public String getRightTarget() { return rightTarget.getSelected(); }
    public void setRightTarget(String target) { rightTarget.setSelected(target != null ? target : PARENT_TARGET); }
    public String getRightTargetUniqueId() { return PARENT_TARGET.equals(rightTarget.getSelected()) ? null : rightTarget.getSelected(); }
    public LayoutComponent.ConstraintSide getRightSide() { return LayoutComponent.ConstraintSide.valueOf(rightSide.getSelected()); }
    public void setRightSide(LayoutComponent.ConstraintSide side) { rightSide.setSelected(side.name()); }
    public VisValidatableTextField getRightMarginField() { return rightMargin; }

    // Bottom constraint accessors
    public boolean isBottomEnabled() { return bottomEnabled.isChecked(); }
    public void setBottomEnabled(boolean enabled) { bottomEnabled.setChecked(enabled); updateConstraintFieldsEnabled(); }
    public String getBottomTarget() { return bottomTarget.getSelected(); }
    public void setBottomTarget(String target) { bottomTarget.setSelected(target != null ? target : PARENT_TARGET); }
    public String getBottomTargetUniqueId() { return PARENT_TARGET.equals(bottomTarget.getSelected()) ? null : bottomTarget.getSelected(); }
    public LayoutComponent.ConstraintSide getBottomSide() { return LayoutComponent.ConstraintSide.valueOf(bottomSide.getSelected()); }
    public void setBottomSide(LayoutComponent.ConstraintSide side) { bottomSide.setSelected(side.name()); }
    public VisValidatableTextField getBottomMarginField() { return bottomMargin; }

    // Top constraint accessors
    public boolean isTopEnabled() { return topEnabled.isChecked(); }
    public void setTopEnabled(boolean enabled) { topEnabled.setChecked(enabled); updateConstraintFieldsEnabled(); }
    public String getTopTarget() { return topTarget.getSelected(); }
    public void setTopTarget(String target) { topTarget.setSelected(target != null ? target : PARENT_TARGET); }
    public String getTopTargetUniqueId() { return PARENT_TARGET.equals(topTarget.getSelected()) ? null : topTarget.getSelected(); }
    public LayoutComponent.ConstraintSide getTopSide() { return LayoutComponent.ConstraintSide.valueOf(topSide.getSelected()); }
    public void setTopSide(LayoutComponent.ConstraintSide side) { topSide.setSelected(side.name()); }
    public VisValidatableTextField getTopMarginField() { return topMargin; }

    // Bias accessors
    public VisValidatableTextField getHorizontalBiasField() { return horizontalBiasField; }
    public VisValidatableTextField getVerticalBiasField() { return verticalBiasField; }

    // Match constraint accessors
    public boolean isMatchConstraintWidth() { return matchWidthCheckBox.isChecked(); }
    public void setMatchConstraintWidth(boolean v) { matchWidthCheckBox.setChecked(v); }
    public boolean isMatchConstraintHeight() { return matchHeightCheckBox.isChecked(); }
    public void setMatchConstraintHeight(boolean v) { matchHeightCheckBox.setChecked(v); }

    public void setMatchConstraintVisible(boolean visible) {
        matchWidthCheckBox.setVisible(visible);
        matchHeightCheckBox.setVisible(visible);
    }

    public static boolean isParentTarget(String target) {
        return PARENT_TARGET.equals(target);
    }

    @Override
    public void onRemove() {
        Facade.getInstance().sendNotification(CLOSE_CLICKED);
    }

    /**
     * A small ShapeDrawer-based preview widget that shows the constraint diagram at a glance.
     * Displays a parent rectangle, a centered entity rectangle, and colored lines/dots for
     * active constraints.
     */
    private class ConstraintPreviewWidget extends Actor {
        private static final float PAD = 6f;

        private static final float PARENT_ALPHA = 0.25f;
        private static final float ENTITY_ALPHA = 0.5f;
        private static final float LINE_WIDTH = 1.5f;
        private static final float DOT_RADIUS = 3f;

        private final Color hColor = new Color(0.3f, 0.5f, 1f, 0.9f);
        private final Color vColor = new Color(0.3f, 0.85f, 0.4f, 0.9f);
        private final Color parentColor = new Color(1f, 1f, 1f, PARENT_ALPHA);
        private final Color entityColor = new Color(0.8f, 0.8f, 0.8f, ENTITY_ALPHA);
        private final Color inactiveColor = new Color(1f, 1f, 1f, 0.12f);

        private ShapeDrawer sd;

        @Override
        protected void setStage(Stage stage) {
            super.setStage(stage);
            if (stage != null) {
                sd = new ShapeDrawer(stage.getBatch(), WhitePixel.sharedInstance.textureRegion);
            }
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            if (sd == null) return;
            sd.update();

            float x = getX(), y = getY(), w = getWidth(), h = getHeight();

            // Parent bounds
            float px = x + PAD, py = y + PAD, pw = w - PAD * 2, ph = h - PAD * 2;
            sd.setColor(parentColor);
            sd.rectangle(px, py, pw, ph, 1f);

            // Entity rect (centered, proportional)
            float ew = pw * 0.28f, eh = ph * 0.38f;
            float ex = x + (w - ew) / 2f;
            float ey = y + (h - eh) / 2f;
            sd.setColor(entityColor);
            sd.filledRectangle(ex, ey, ew, eh);
            sd.setColor(new Color(1f, 1f, 1f, 0.3f));
            sd.rectangle(ex, ey, ew, eh, 1f);

            // Edge midpoints
            float midL = ex;
            float midR = ex + ew;
            float midB = ey;
            float midT = ey + eh;
            float midY = ey + eh / 2f;
            float midX = ex + ew / 2f;

            // Draw constraint indicators
            drawSideIndicator(midL, midY, px, midY, leftEnabled.isChecked(), true);
            drawSideIndicator(midR, midY, px + pw, midY, rightEnabled.isChecked(), true);
            drawSideIndicator(midX, midB, midX, py, bottomEnabled.isChecked(), false);
            drawSideIndicator(midX, midT, midX, py + ph, topEnabled.isChecked(), false);
        }

        private void drawSideIndicator(float fromX, float fromY, float toX, float toY,
                                         boolean active, boolean horizontal) {
            if (active) {
                sd.setColor(horizontal ? hColor : vColor);
                drawDashed(fromX, fromY, toX, toY, LINE_WIDTH, 4f);
                sd.filledCircle(fromX, fromY, DOT_RADIUS);
                sd.filledCircle(toX, toY, DOT_RADIUS);
            } else {
                sd.setColor(inactiveColor);
                drawDashed(fromX, fromY, toX, toY, 1f, 3f);
            }
        }

        private void drawDashed(float x1, float y1, float x2, float y2, float lw, float dashLen) {
            float dx = x2 - x1, dy = y2 - y1;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            if (dist < 0.001f) return;
            float nx = dx / dist, ny = dy / dist;
            float drawn = 0;
            boolean on = true;
            while (drawn < dist) {
                float seg = Math.min(dashLen, dist - drawn);
                if (on) {
                    sd.line(x1 + nx * drawn, y1 + ny * drawn,
                            x1 + nx * (drawn + seg), y1 + ny * (drawn + seg), lw);
                }
                drawn += seg;
                on = !on;
            }
        }
    }
}
