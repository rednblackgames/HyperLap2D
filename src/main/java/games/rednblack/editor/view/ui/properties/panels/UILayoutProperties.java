package games.rednblack.editor.view.ui.properties.panels;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.util.Validators;
import com.kotcrab.vis.ui.widget.*;
import games.rednblack.editor.event.CheckBoxChangeListener;
import games.rednblack.editor.event.KeyboardListener;
import games.rednblack.editor.event.SelectBoxChangeListener;
import games.rednblack.editor.renderer.components.LayoutComponent;
import games.rednblack.editor.view.ui.properties.UIRemovableProperties;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.puremvc.Facade;

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

        // Left constraint section
        leftEnabled = StandardWidgetsFactory.createCheckBox("Left");
        leftTarget = StandardWidgetsFactory.createSelectBox(String.class);
        leftSide = StandardWidgetsFactory.createSelectBox(String.class);
        leftSide.setItems(horizontalSides);
        leftSide.setSelected(LayoutComponent.ConstraintSide.LEFT.name());
        leftMargin = StandardWidgetsFactory.createValidableTextField(floatValidator);

        mainTable.add(leftEnabled).left().colspan(4);
        mainTable.row().padTop(2);
        mainTable.add(new VisLabel("Target:", Align.right)).padRight(5).fillX();
        mainTable.add(leftTarget).width(100);
        mainTable.add(new VisLabel("Side:", Align.right)).padRight(5).padLeft(5);
        mainTable.add(leftSide).width(70);
        mainTable.row().padTop(2);
        mainTable.add(new VisLabel("Margin:", Align.right)).padRight(5).fillX();
        mainTable.add(leftMargin).width(100).colspan(3).left();
        mainTable.row().padTop(8);

        // Right constraint section
        rightEnabled = StandardWidgetsFactory.createCheckBox("Right");
        rightTarget = StandardWidgetsFactory.createSelectBox(String.class);
        rightSide = StandardWidgetsFactory.createSelectBox(String.class);
        rightSide.setItems(horizontalSides);
        rightSide.setSelected(LayoutComponent.ConstraintSide.RIGHT.name());
        rightMargin = StandardWidgetsFactory.createValidableTextField(floatValidator);

        mainTable.add(rightEnabled).left().colspan(4);
        mainTable.row().padTop(2);
        mainTable.add(new VisLabel("Target:", Align.right)).padRight(5).fillX();
        mainTable.add(rightTarget).width(100);
        mainTable.add(new VisLabel("Side:", Align.right)).padRight(5).padLeft(5);
        mainTable.add(rightSide).width(70);
        mainTable.row().padTop(2);
        mainTable.add(new VisLabel("Margin:", Align.right)).padRight(5).fillX();
        mainTable.add(rightMargin).width(100).colspan(3).left();
        mainTable.row().padTop(8);

        // Bottom constraint section
        bottomEnabled = StandardWidgetsFactory.createCheckBox("Bottom");
        bottomTarget = StandardWidgetsFactory.createSelectBox(String.class);
        bottomSide = StandardWidgetsFactory.createSelectBox(String.class);
        bottomSide.setItems(verticalSides);
        bottomSide.setSelected(LayoutComponent.ConstraintSide.BOTTOM.name());
        bottomMargin = StandardWidgetsFactory.createValidableTextField(floatValidator);

        mainTable.add(bottomEnabled).left().colspan(4);
        mainTable.row().padTop(2);
        mainTable.add(new VisLabel("Target:", Align.right)).padRight(5).fillX();
        mainTable.add(bottomTarget).width(100);
        mainTable.add(new VisLabel("Side:", Align.right)).padRight(5).padLeft(5);
        mainTable.add(bottomSide).width(70);
        mainTable.row().padTop(2);
        mainTable.add(new VisLabel("Margin:", Align.right)).padRight(5).fillX();
        mainTable.add(bottomMargin).width(100).colspan(3).left();
        mainTable.row().padTop(8);

        // Top constraint section
        topEnabled = StandardWidgetsFactory.createCheckBox("Top");
        topTarget = StandardWidgetsFactory.createSelectBox(String.class);
        topSide = StandardWidgetsFactory.createSelectBox(String.class);
        topSide.setItems(verticalSides);
        topSide.setSelected(LayoutComponent.ConstraintSide.TOP.name());
        topMargin = StandardWidgetsFactory.createValidableTextField(floatValidator);

        mainTable.add(topEnabled).left().colspan(4);
        mainTable.row().padTop(2);
        mainTable.add(new VisLabel("Target:", Align.right)).padRight(5).fillX();
        mainTable.add(topTarget).width(100);
        mainTable.add(new VisLabel("Side:", Align.right)).padRight(5).padLeft(5);
        mainTable.add(topSide).width(70);
        mainTable.row().padTop(2);
        mainTable.add(new VisLabel("Margin:", Align.right)).padRight(5).fillX();
        mainTable.add(topMargin).width(100).colspan(3).left();
        mainTable.row().padTop(8);

        // Bias section
        horizontalBiasField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        verticalBiasField = StandardWidgetsFactory.createValidableTextField(floatValidator);

        mainTable.add(new VisLabel("H Bias:", Align.right)).padRight(5).fillX();
        mainTable.add(horizontalBiasField).width(100);
        mainTable.add(new VisLabel("V Bias:", Align.right)).padRight(5).padLeft(5);
        mainTable.add(verticalBiasField).width(70);
        mainTable.row().padTop(5);

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

    public static boolean isParentTarget(String target) {
        return PARENT_TARGET.equals(target);
    }

    @Override
    public void onRemove() {
        Facade.getInstance().sendNotification(CLOSE_CLICKED);
    }
}
