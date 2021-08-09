package games.rednblack.editor.view.ui.box.bottom;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.ui.box.UIBaseBox;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

public class UISceneBox extends UIBaseBox {
    private static final String prefix = "games.rednblack.editor.view.ui.box.bottom.UISceneBox";

    public static final String CREATE_NEW_SCENE_BTN_CLICKED = prefix + ".CREATE_NEW_SCENE_BTN_CLICKED";
    public static final String CHANGE_SCENE_BTN_CLICKED = prefix + ".CHANGE_SCENE_BTN_CLICKED";
    public static final String DELETE_CURRENT_SCENE_BTN_CLICKED = prefix + ".DELETE_CURRENT_SCENE_BTN_CLICKED";

    final private ProjectManager projectManager;

    private VisSelectBox<SceneVO> visSelectBox;
    private VisImageButton deleteBtn;

    private final Array<SceneVO> sceneEntryVOs = new Array<>();

    public UISceneBox() {
        projectManager = facade.retrieveProxy(ProjectManager.NAME);
        setVisible(false);
        init();
    }

    private void init() {
        VisImageButton.VisImageButtonStyle visImageButtonStyle = new VisImageButton.VisImageButtonStyle(VisUI.getSkin().get("dark", VisImageButton.VisImageButtonStyle.class));
        visImageButtonStyle.imageUp = VisUI.getSkin().getDrawable("icon-trash");
        visImageButtonStyle.imageOver = VisUI.getSkin().getDrawable("icon-trash-over");
        visImageButtonStyle.imageDisabled = VisUI.getSkin().getDrawable("icon-trash-disabled");
        deleteBtn = new VisImageButton("dark");
        deleteBtn.setStyle(visImageButtonStyle);
        deleteBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                if (!deleteBtn.isDisabled())
                    facade.sendNotification(DELETE_CURRENT_SCENE_BTN_CLICKED, visSelectBox.getSelected().sceneName);
            }
        });

        visSelectBox = StandardWidgetsFactory.createSelectBox(SceneVO.class);
        visSelectBox.addListener(new SceneChangeListener());

        add("Scene:").padRight(4);
        add(visSelectBox).padRight(11).width(156);
        add(deleteBtn).height(25);
    }

    @Override
    public void update() {
        setVisible(true);
        updateSceneList();
        setCurrentScene();
        deleteBtn.setDisabled(visSelectBox.getSelectedIndex() <= 1);
    }

    private class SceneChangeListener extends ChangeListener {
        @Override
        public void changed(ChangeEvent changeEvent, Actor actor) {
            int selectedIndex = visSelectBox.getSelectedIndex();
            deleteBtn.setDisabled(selectedIndex <= 1);

            if (!visSelectBox.getScrollPane().hasParent()) {
                return;
            }

            HyperLap2DFacade facade = HyperLap2DFacade.getInstance();
            if (selectedIndex == 0) {
                facade.sendNotification(CREATE_NEW_SCENE_BTN_CLICKED);
                return;
            }

            facade.sendNotification(CHANGE_SCENE_BTN_CLICKED, visSelectBox.getSelected().sceneName);
        }
    }

    private void updateSceneList() {
        sceneEntryVOs.clear();
        SceneVO newSceneEntryVO = new SceneVO();
        newSceneEntryVO.sceneName = "Create New Scene ...";
        sceneEntryVOs.add(newSceneEntryVO);
        for (SceneVO sceneVO : projectManager.currentProjectInfoVO.scenes) {
            sceneEntryVOs.add(sceneVO);
        }
        visSelectBox.setItems(sceneEntryVOs);
    }

    public void setCurrentScene() {
        Array<SceneVO> array = visSelectBox.getItems();
        for (int i = 0; i < array.size; ++i) {
            SceneVO sceneVO = array.get(i);
            if (sceneVO.sceneName.equals(Sandbox.getInstance().currentLoadedSceneFileName)) {
                visSelectBox.setSelectedIndex(i);
                break;
            }
        }
    }
}
