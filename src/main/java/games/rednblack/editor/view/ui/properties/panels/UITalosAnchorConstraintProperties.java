package games.rednblack.editor.view.ui.properties.panels;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.util.Validators;
import com.kotcrab.vis.ui.widget.*;
import games.rednblack.editor.event.KeyboardListener;
import games.rednblack.editor.event.SelectBoxChangeListener;
import games.rednblack.editor.renderer.components.LayoutComponent;
import games.rednblack.editor.view.ui.properties.UIRemovableProperties;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.puremvc.Facade;

public class UITalosAnchorConstraintProperties extends UIRemovableProperties {

    public static final String prefix = "games.rednblack.editor.view.ui.properties.panels.UITalosAnchorConstraintProperties";
    public static final String CLOSE_CLICKED = prefix + ".CLOSE_CLICKED";

    private static final String PARENT_TARGET = "Parent";

    private final Array<BindingRow> bindingRows = new Array<>();
    private final Validators.FloatValidator floatValidator = new Validators.FloatValidator();

    private Array<String> horizontalSides;
    private Array<String> verticalSides;
    private Array<String> currentTargets;

    public UITalosAnchorConstraintProperties() {
        super("Talos Anchor Constraints");

        horizontalSides = new Array<>();
        horizontalSides.add(LayoutComponent.ConstraintSide.LEFT.name());
        horizontalSides.add(LayoutComponent.ConstraintSide.RIGHT.name());

        verticalSides = new Array<>();
        verticalSides.add(LayoutComponent.ConstraintSide.BOTTOM.name());
        verticalSides.add(LayoutComponent.ConstraintSide.TOP.name());

        currentTargets = new Array<>();
        currentTargets.add(PARENT_TARGET);
    }

    public void setSiblingList(Array<String> siblings) {
        currentTargets = new Array<>();
        currentTargets.add(PARENT_TARGET);
        currentTargets.addAll(siblings);

        for (BindingRow row : bindingRows) {
            row.leftTarget.setItems(currentTargets);
            row.rightTarget.setItems(currentTargets);
            row.bottomTarget.setItems(currentTargets);
            row.topTarget.setItems(currentTargets);
        }
    }

    public void rebuildRows(Array<Integer> scopeKeys) {
        mainTable.clearChildren();
        bindingRows.clear();

        for (int key : scopeKeys) {
            BindingRow row = new BindingRow(key);
            bindingRows.add(row);
            addBindingRowToTable(row);
        }
    }

    private void addBindingRowToTable(BindingRow row) {
        // Header: main enabled checkbox + scope key label
        mainTable.add(row.enabled).left().width(24);
        mainTable.add(new VisLabel("Scope " + row.scopeKey)).left().expandX().colspan(3);
        mainTable.row().padTop(2);

        // L constraint row
        mainTable.add(row.leftEnabled).left().width(34);
        mainTable.add(row.leftTarget).width(90).padLeft(1);
        mainTable.add(row.leftSide).width(62).padLeft(1);
        mainTable.add(row.leftMargin).width(36).padLeft(1);
        mainTable.row().padTop(1);

        // R constraint row
        mainTable.add(row.rightEnabled).left().width(34);
        mainTable.add(row.rightTarget).width(90).padLeft(1);
        mainTable.add(row.rightSide).width(62).padLeft(1);
        mainTable.add(row.rightMargin).width(36).padLeft(1);
        mainTable.row().padTop(1);

        // B constraint row
        mainTable.add(row.bottomEnabled).left().width(34);
        mainTable.add(row.bottomTarget).width(90).padLeft(1);
        mainTable.add(row.bottomSide).width(62).padLeft(1);
        mainTable.add(row.bottomMargin).width(36).padLeft(1);
        mainTable.row().padTop(1);

        // T constraint row
        mainTable.add(row.topEnabled).left().width(34);
        mainTable.add(row.topTarget).width(90).padLeft(1);
        mainTable.add(row.topSide).width(62).padLeft(1);
        mainTable.add(row.topMargin).width(36).padLeft(1);
        mainTable.row().padTop(2);

        // Bias row
        mainTable.add(new VisLabel("H Bias:", Align.right)).padRight(2);
        mainTable.add(row.horizontalBiasField).growX().colspan(3);
        mainTable.row().padTop(1);
        mainTable.add(new VisLabel("V Bias:", Align.right)).padRight(2);
        mainTable.add(row.verticalBiasField).growX().colspan(3);
        mainTable.row().padTop(6);
    }

    public Array<BindingRow> getBindingRows() {
        return bindingRows;
    }

    public static boolean isParentTarget(String target) {
        return PARENT_TARGET.equals(target);
    }

    @Override
    public void onRemove() {
        Facade.getInstance().sendNotification(CLOSE_CLICKED);
    }

    public class BindingRow {
        public final int scopeKey;
        public final VisCheckBox enabled;

        // Left
        public final VisCheckBox leftEnabled;
        public final VisSelectBox<String> leftTarget;
        public final VisSelectBox<String> leftSide;
        public final VisValidatableTextField leftMargin;

        // Right
        public final VisCheckBox rightEnabled;
        public final VisSelectBox<String> rightTarget;
        public final VisSelectBox<String> rightSide;
        public final VisValidatableTextField rightMargin;

        // Bottom
        public final VisCheckBox bottomEnabled;
        public final VisSelectBox<String> bottomTarget;
        public final VisSelectBox<String> bottomSide;
        public final VisValidatableTextField bottomMargin;

        // Top
        public final VisCheckBox topEnabled;
        public final VisSelectBox<String> topTarget;
        public final VisSelectBox<String> topSide;
        public final VisValidatableTextField topMargin;

        // Bias
        public final VisValidatableTextField horizontalBiasField;
        public final VisValidatableTextField verticalBiasField;

        BindingRow(int scopeKey) {
            this.scopeKey = scopeKey;

            enabled = StandardWidgetsFactory.createCheckBox("");

            // Left
            leftEnabled = StandardWidgetsFactory.createCheckBox("L");
            leftTarget = StandardWidgetsFactory.createSelectBox(String.class);
            leftTarget.setItems(currentTargets);
            leftSide = StandardWidgetsFactory.createSelectBox(String.class);
            leftSide.setItems(horizontalSides);
            leftSide.setSelected(LayoutComponent.ConstraintSide.LEFT.name());
            leftMargin = StandardWidgetsFactory.createValidableTextField(floatValidator);
            leftMargin.setText("0");

            // Right
            rightEnabled = StandardWidgetsFactory.createCheckBox("R");
            rightTarget = StandardWidgetsFactory.createSelectBox(String.class);
            rightTarget.setItems(currentTargets);
            rightSide = StandardWidgetsFactory.createSelectBox(String.class);
            rightSide.setItems(horizontalSides);
            rightSide.setSelected(LayoutComponent.ConstraintSide.RIGHT.name());
            rightMargin = StandardWidgetsFactory.createValidableTextField(floatValidator);
            rightMargin.setText("0");

            // Bottom
            bottomEnabled = StandardWidgetsFactory.createCheckBox("B");
            bottomTarget = StandardWidgetsFactory.createSelectBox(String.class);
            bottomTarget.setItems(currentTargets);
            bottomSide = StandardWidgetsFactory.createSelectBox(String.class);
            bottomSide.setItems(verticalSides);
            bottomSide.setSelected(LayoutComponent.ConstraintSide.BOTTOM.name());
            bottomMargin = StandardWidgetsFactory.createValidableTextField(floatValidator);
            bottomMargin.setText("0");

            // Top
            topEnabled = StandardWidgetsFactory.createCheckBox("T");
            topTarget = StandardWidgetsFactory.createSelectBox(String.class);
            topTarget.setItems(currentTargets);
            topSide = StandardWidgetsFactory.createSelectBox(String.class);
            topSide.setItems(verticalSides);
            topSide.setSelected(LayoutComponent.ConstraintSide.TOP.name());
            topMargin = StandardWidgetsFactory.createValidableTextField(floatValidator);
            topMargin.setText("0");

            // Bias
            horizontalBiasField = StandardWidgetsFactory.createValidableTextField(floatValidator);
            horizontalBiasField.setText("0.5");
            verticalBiasField = StandardWidgetsFactory.createValidableTextField(floatValidator);
            verticalBiasField.setText("0.5");

            updateFieldsEnabled();
            initRowListeners();
        }

        private void initRowListeners() {
            ChangeListener masterListener = new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    updateFieldsEnabled();
                    Facade.getInstance().sendNotification(getUpdateEventName());
                }
            };
            enabled.addListener(masterListener);

            ChangeListener constraintToggle = new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    updateConstraintFieldsEnabled();
                    Facade.getInstance().sendNotification(getUpdateEventName());
                }
            };
            leftEnabled.addListener(constraintToggle);
            rightEnabled.addListener(constraintToggle);
            bottomEnabled.addListener(constraintToggle);
            topEnabled.addListener(constraintToggle);

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
        }

        void updateFieldsEnabled() {
            boolean on = enabled.isChecked();
            leftEnabled.setDisabled(!on);
            rightEnabled.setDisabled(!on);
            bottomEnabled.setDisabled(!on);
            topEnabled.setDisabled(!on);
            horizontalBiasField.setDisabled(!on);
            verticalBiasField.setDisabled(!on);

            if (on) {
                updateConstraintFieldsEnabled();
            } else {
                setAllConstraintFieldsDisabled();
            }
        }

        private void updateConstraintFieldsEnabled() {
            boolean on = enabled.isChecked();

            boolean leftOn = on && leftEnabled.isChecked();
            leftTarget.setDisabled(!leftOn);
            leftSide.setDisabled(!leftOn);
            leftMargin.setDisabled(!leftOn);

            boolean rightOn = on && rightEnabled.isChecked();
            rightTarget.setDisabled(!rightOn);
            rightSide.setDisabled(!rightOn);
            rightMargin.setDisabled(!rightOn);

            boolean bottomOn = on && bottomEnabled.isChecked();
            bottomTarget.setDisabled(!bottomOn);
            bottomSide.setDisabled(!bottomOn);
            bottomMargin.setDisabled(!bottomOn);

            boolean topOn = on && topEnabled.isChecked();
            topTarget.setDisabled(!topOn);
            topSide.setDisabled(!topOn);
            topMargin.setDisabled(!topOn);
        }

        private void setAllConstraintFieldsDisabled() {
            leftTarget.setDisabled(true);
            leftSide.setDisabled(true);
            leftMargin.setDisabled(true);
            rightTarget.setDisabled(true);
            rightSide.setDisabled(true);
            rightMargin.setDisabled(true);
            bottomTarget.setDisabled(true);
            bottomSide.setDisabled(true);
            bottomMargin.setDisabled(true);
            topTarget.setDisabled(true);
            topSide.setDisabled(true);
            topMargin.setDisabled(true);
        }

        public String getTargetUniqueId(VisSelectBox<String> targetBox) {
            return PARENT_TARGET.equals(targetBox.getSelected()) ? null : targetBox.getSelected();
        }
    }
}
