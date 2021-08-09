package games.rednblack.editor.view.ui.box.resourcespanel;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.util.ActorUtils;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.editor.event.CheckBoxChangeListener;
import games.rednblack.h2d.common.filters.IAbstractResourceFilter;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

public class UIFilterMenu extends VisTable {
    private static final float TRANSITION_TIME = 0.15f;

    private static final String CLASS_NAME = "games.rednblack.editor.view.ui.box.resourcespanel.UIItemsTreeBox";
    public static final String SHOW_FILTER_MENU = CLASS_NAME + ".SHOW_FILTER_MENU";

    private boolean removing = false;
    private final InputListener stageListener;

    public UIFilterMenu() {
        setTransform(true);
        background("popup-menu");
        pad(10);
        defaults().left();

        stageListener = new InputListener() {
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                if (!contains(x, y)) {
                    remove();
                }
                return true;
            }
        };

        setTouchable(Touchable.enabled);
    }

    public void addFilter(IAbstractResourceFilter filter) {
        VisCheckBox checkBox = StandardWidgetsFactory.createCheckBox(filter.name);
        checkBox.setChecked(filter.isActive());
        checkBox.addListener(new CheckBoxChangeListener(UIResourcesTabMediator.CHANGE_ACTIVE_FILTER, filter.id));
        add(checkBox).padTop(2).padBottom(2).row();
        pack();
    }

    public boolean contains (float x, float y) {
        return getX() < x && getX() + getWidth() > x && getY() < y && getY() + getHeight() > y;
    }

    @Override
    protected void setStage (Stage stage) {
        super.setStage(stage);
        if (stage != null) stage.addListener(stageListener);
    }

    public void showMenu(Stage stage, float x, float y) {
        if (getStage() == stage) return;

        setPosition(x, y - getHeight());
        if (stage.getHeight() - getY() > stage.getHeight()) setY(getY() + getHeight());
        ActorUtils.keepWithinStage(stage, this);
        stage.addActor(this);

        setOrigin(Align.topRight);
        setScale(0);
        getColor().a = 0;
        clearActions();
        addAction(Actions.parallel(
                Actions.alpha(1, TRANSITION_TIME),
                Actions.scaleTo(1, 1, TRANSITION_TIME, Interpolation.pow5Out)
        ));
        removing = false;
    }

    @Override
    public boolean remove () {
        if (!removing) {
            removing = true;
            clearActions();
            addAction(Actions.sequence(
                    Actions.parallel(
                            Actions.alpha(0, TRANSITION_TIME),
                            Actions.scaleTo(0, 0, TRANSITION_TIME, Interpolation.pow2Out)
                    ),
                    Actions.run(() -> {
                        if (getStage() != null) getStage().removeListener(stageListener);
                        UIFilterMenu.super.remove();
                    })));
        }
        return removing;
    }
}
