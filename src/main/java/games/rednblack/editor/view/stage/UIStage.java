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

package games.rednblack.editor.view.stage;

import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.util.ToastManager;
import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.SettingsManager;
import games.rednblack.editor.renderer.utils.TextureArrayCpuPolygonSpriteBatch;
import games.rednblack.editor.view.ui.*;
import games.rednblack.h2d.common.proxy.CursorManager;

public class UIStage extends Stage {

    public static final int SANDBOX_TOP_MARGIN = 104;
    public static final int SANDBOX_BOTTOM_MARGIN = 38;
    public static final int SANDBOX_LEFT_MARGIN = 40;

    private final HyperLap2DFacade facade;
    public Group dummyTarget;
    public UIMainTable uiMainTable;
    public Group contextMenuContainer;
    private final ToastManager toastManager;

	public Group midUI;

    public UIStage() {
        super(new ScreenViewport(), new TextureArrayCpuPolygonSpriteBatch(10_000));

        facade = HyperLap2DFacade.getInstance();

        getViewport().getCamera().position.setZero();
        updateViewportDensity();

        //dummy target is basically the target of drop of items from resoruce panel
        dummyTarget = new Group();
        dummyTarget.setWidth(getWidth());
        dummyTarget.setHeight(getHeight() - SANDBOX_TOP_MARGIN);
        dummyTarget.setY(SANDBOX_BOTTOM_MARGIN);
        dummyTarget.setX(SANDBOX_LEFT_MARGIN);
        dummyTarget.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                CursorManager cursorManager = facade.retrieveProxy(CursorManager.NAME);
                cursorManager.displayCustomCursor();
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) {
                    CursorManager cursorManager = facade.retrieveProxy(CursorManager.NAME);
                    cursorManager.hideCustomCursor();
                }
            }

            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {
                CursorManager cursorManager = facade.retrieveProxy(CursorManager.NAME);
                cursorManager.displayCustomCursor();
                return false;
            }
        });

        addActor(dummyTarget);

        midUI = new Group();
        addActor(midUI);

        RulersUIMediator rulersUIMediator = facade.retrieveMediator(RulersUIMediator.NAME);
        Actor rulersGroup = rulersUIMediator.getViewComponent();

        FollowersUIMediator followersUIMediator = facade.retrieveMediator(FollowersUIMediator.NAME);
        Group followersGroup = followersUIMediator.getViewComponent();

        StickyNotesUIMediator stickyNotesUIMediator = facade.retrieveMediator(StickyNotesUIMediator.NAME);
        Group stickyNotesGroup = stickyNotesUIMediator.getViewComponent();

        midUI.addActor(followersGroup);
        midUI.addActor(stickyNotesGroup);
        midUI.addActor(rulersGroup);

        contextMenuContainer = new Group();
        uiMainTable = new UIMainTable();

        addActor(uiMainTable);
        addActor(contextMenuContainer);

		VisTable mainBottomTable = new VisTable();
		mainBottomTable.setFillParent(true);
		mainBottomTable.bottom();
		UIBottomMenuBar bottomMenuBar = new UIBottomMenuBar();
		mainBottomTable.add(bottomMenuBar).fillX().expandX().height(38);
		addActor(mainBottomTable);

        setListeners();

        toastManager = new ToastManager(this);
        toastManager.setAlignment(Align.bottomLeft);
    }

    public ToastManager getToastManager() {
        return toastManager;
    }

    public void resize(int width, int height) {
        getViewport().update(width, height, true);
        dummyTarget.setWidth(width);
        dummyTarget.setHeight(height - SANDBOX_TOP_MARGIN);
    }

    public void setKeyboardFocus() {
        setKeyboardFocus(dummyTarget);
    }

    public void setListeners() {
        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return event.getTarget() != getRoot() && event.getTarget() != dummyTarget;
            }
        });
    }

    @Override
    public boolean keyDown(int keyCode) {
        return super.keyDown(keyCode);
    }

    public void updateViewportDensity() {
        ScreenViewport screenViewport = (ScreenViewport) getViewport();
        screenViewport.setUnitsPerPixel(getUIScaleDensity());
        screenViewport.update(screenViewport.getScreenWidth(), screenViewport.getScreenHeight(), true);
    }

    public float getUIScaleDensity() {
        SettingsManager settingsManager = facade.retrieveProxy(SettingsManager.NAME);
        return settingsManager.editorConfigVO.uiScaleDensity;
    }
}
