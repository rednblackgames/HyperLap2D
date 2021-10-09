package games.rednblack.editor.view.ui.box;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisImageTextButton;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

/**
 * Created by CyberJoe on 4/22/2015.
 */
public class UICompositeHierarchy extends UIBaseBox {

    private static final String PREFIX = "games.rednblack.editor.view.ui.box.UICompositeHierarchy";
    public static final String SWITCH_VIEW_COMPOSITE_CLICKED = PREFIX + ".SWITCH_VIEW_COMPOSITE_CLICKED";

    private final HorizontalGroup mainGroup;

    public UICompositeHierarchy() {
        super();

        mainGroup = new HorizontalGroup();
		VisScrollPane scrollPane = StandardWidgetsFactory.createScrollPane(mainGroup);
        clearItems();

        add(scrollPane).left().fill();

        add().fill().expand();
        row();
    }

    public void addItem(String name, Integer id, boolean isRoot) {
        String classType = "hierarchy-item";
        if(isRoot) classType+="-root";

        VisImageTextButton button = StandardWidgetsFactory.createImageTextButton(name, classType);
        button.getLabelCell().padLeft(3);


        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                facade.sendNotification(SWITCH_VIEW_COMPOSITE_CLICKED, id);
            }
        });

        button.padLeft(5).padRight(3);
        if (!isRoot)
            mainGroup.addActor(new Image(VisUI.getSkin(), "hierarchy-separator"));
        else
            button.padLeft(10);
        mainGroup.addActor(button);
    }

    public void clearItems() {
        mainGroup.clear();
    }

    @Override
    public void update() {

    }
}
