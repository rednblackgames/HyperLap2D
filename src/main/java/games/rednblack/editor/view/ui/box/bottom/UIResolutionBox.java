/*
 * ******************************************************************************
 *  * Copyright 2015 See AUTHORS file.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *****************************************************************************
 */

package games.rednblack.editor.view.ui.box.bottom;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.ResolutionManager;
import games.rednblack.editor.renderer.data.ResolutionEntryVO;
import games.rednblack.editor.view.ui.box.UIBaseBox;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

public class UIResolutionBox extends UIBaseBox {
	private static final String prefix = "games.rednblack.editor.view.ui.box.bottom.UIResolutionBox";

    public static final String CREATE_NEW_RESOLUTION_BTN_CLICKED = prefix + ".CREATE_NEW_RESOLUTION_BTN_CLICKED";
    public static final String CHANGE_RESOLUTION_BTN_CLICKED = prefix + ".CHANGE_RESOLUTION_BTN_CLICKED";
    public static final String DELETE_RESOLUTION_BTN_CLICKED = prefix + ".DELETE_RESOLUTION_BTN_CLICKED";

    private final ResolutionManager resolutionManager;
    private final Skin skin;
    private VisSelectBox<ResolutionEntryVO> visSelectBox;

    private VisImageButton deleteBtn;

    public UIResolutionBox() {
        resolutionManager = facade.retrieveProxy(ResolutionManager.NAME);
        setVisible(false);
        skin = VisUI.getSkin();
        init();
    }

    private void init() {
        addSeparator(true).padRight(13).padLeft(13);

        VisImageButton.VisImageButtonStyle visImageButtonStyle = new VisImageButton.VisImageButtonStyle(skin.get("dark", VisImageButton.VisImageButtonStyle.class));
        visImageButtonStyle.imageUp = skin.getDrawable("icon-trash");
        visImageButtonStyle.imageOver = skin.getDrawable("icon-trash-over");
        visImageButtonStyle.imageDisabled = skin.getDrawable("icon-trash-disabled");
        deleteBtn = new VisImageButton("dark");
        deleteBtn.setStyle(visImageButtonStyle);
        deleteBtn.addListener(new UIResolutionBoxButtonClickListener(DELETE_RESOLUTION_BTN_CLICKED));

        visSelectBox = StandardWidgetsFactory.createSelectBox(ResolutionEntryVO.class);
        visSelectBox.addListener(new ResolutionChangeListener());
        add("Resolution:").padRight(4);
        add(visSelectBox).padRight(11).width(156);

        add(deleteBtn).height(25);
    }

    @Override
    public void update() {
        setVisible(true);

        Array<ResolutionEntryVO> resolutionEntryVOs = new Array<>();
        ResolutionEntryVO newResolutionEntryVO = new ResolutionEntryVO();
        newResolutionEntryVO.name = "Create New ...";
        resolutionEntryVOs.add(newResolutionEntryVO);
        resolutionEntryVOs.add(resolutionManager.getOriginalResolution());
        resolutionEntryVOs.addAll(resolutionManager.getResolutions());
        visSelectBox.setItems(resolutionEntryVOs);

		setCurrentResolution();
    }

    private void setCurrentResolution(String currentResolutionName) {
        Array<ResolutionEntryVO> array = visSelectBox.getItems();
        for (int i = 0; i < array.size; ++i) {
            ResolutionEntryVO resolutionEntryVO = array.get(i);
            if (resolutionEntryVO.name.equals(currentResolutionName)) {
                visSelectBox.setSelectedIndex(i);
                break;
            }
        }
    }

    public void setCurrentResolution() {
		setCurrentResolution(resolutionManager.currentResolutionName);

		int selectedIndex = visSelectBox.getSelectedIndex();
		deleteBtn.setDisabled(selectedIndex <= 1);
    }

    private class UIResolutionBoxButtonClickListener extends ClickListener {
        private final String btnClicked;

        public UIResolutionBoxButtonClickListener(String btnClicked) {
            this.btnClicked = btnClicked;
        }

        @Override
        public void clicked(InputEvent event, float x, float y) {
            super.clicked(event, x, y);

            HyperLap2DFacade facade = HyperLap2DFacade.getInstance();
            switch (btnClicked) {
                case DELETE_RESOLUTION_BTN_CLICKED:
                	if (!deleteBtn.isDisabled())
                    	facade.sendNotification(btnClicked, visSelectBox.getSelected());
                    break;
            }
        }
    }

    private class ResolutionChangeListener extends ChangeListener {

        @Override
        public void changed(ChangeEvent changeEvent, Actor actor) {
			int selectedIndex = visSelectBox.getSelectedIndex();
			deleteBtn.setDisabled(selectedIndex <= 1);

            if (!visSelectBox.getScrollPane().hasParent()) {
                return;
            }

            HyperLap2DFacade facade = HyperLap2DFacade.getInstance();
            if (selectedIndex == 0) {
                facade.sendNotification(CREATE_NEW_RESOLUTION_BTN_CLICKED);
                return;
            }
            facade.sendNotification(CHANGE_RESOLUTION_BTN_CLICKED, visSelectBox.getSelected());
        }
    }
}
