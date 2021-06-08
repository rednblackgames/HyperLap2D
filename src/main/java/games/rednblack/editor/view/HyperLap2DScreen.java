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

package games.rednblack.editor.view;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;

import games.rednblack.editor.HyperLap2DApp;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.SettingsManager;
import games.rednblack.editor.utils.KeyBindingsLayout;
import games.rednblack.editor.view.menu.FileMenu;
import games.rednblack.editor.view.menu.ResourcesMenu;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.editor.view.stage.input.SandboxInputAdapter;
import games.rednblack.editor.view.ui.widget.actors.basic.SandboxBackUI;
import games.rednblack.editor.view.ui.widget.actors.basic.WhitePixel;
import games.rednblack.h2d.common.MenuAPI;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.vo.SceneConfigVO;

public class HyperLap2DScreen implements Screen, InputProcessor {
    private static final String TAG = HyperLap2DScreen.class.getCanonicalName();

    public UIStage uiStage;

    private Engine engine;

    private final HyperLap2DFacade facade;

    private Sandbox sandbox;
    private SandboxBackUI sandboxBackUI;

    private final Color defaultBackgroundColor;
    private final Color backgroundColor;
    private final Image bgLogo;
    private final Image blackOverlay;
    private final Vector2 screenSize;

    private boolean isDrawingBgLogo;

    public HyperLap2DScreen() {
        facade = HyperLap2DFacade.getInstance();
        defaultBackgroundColor = new Color(0.15f, 0.15f, 0.15f, 1.0f);
        SettingsManager settingsManager = facade.retrieveProxy(SettingsManager.NAME);
        backgroundColor = settingsManager.editorConfigVO.backgroundColor;
        isDrawingBgLogo = true;
        bgLogo = new Image(new Texture(Gdx.files.internal("style/bglogo.png")));
        bgLogo.setOrigin(Align.center);
        bgLogo.getColor().a = 0.3f;
        screenSize = new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        blackOverlay = new Image(WhitePixel.sharedInstance.texture);
        blackOverlay.setSize(screenSize.x, screenSize.y);
    }

    @Override
    public void render(float deltaTime) {
        if (isDrawingBgLogo) {
            Gdx.gl.glClearColor(defaultBackgroundColor.r, defaultBackgroundColor.g, defaultBackgroundColor.b, defaultBackgroundColor.a);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        } else {
            Gdx.gl.glClearColor(backgroundColor.r, backgroundColor.g, backgroundColor.b, backgroundColor.a);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            if (sandboxBackUI != null) sandboxBackUI.render(deltaTime);
            sandbox.render(deltaTime);
            engine.update(deltaTime);
        }

        uiStage.getViewport().apply();
        uiStage.act(deltaTime);
        uiStage.draw();
    }

    public void disableDrawingBgLogo() {
        if (!isDrawingBgLogo) return;

        this.isDrawingBgLogo = false;
        bgLogo.remove();
    }

    private void updateCameraPosition() {
        ProjectManager projectManager = HyperLap2DFacade.getInstance().retrieveProxy(ProjectManager.NAME);
        SceneConfigVO sceneConfigVO = projectManager.getCurrentSceneConfigVO();
        if (sceneConfigVO != null)
            sandbox.getCamera().position.set(sceneConfigVO.cameraPosition[0], sceneConfigVO.cameraPosition[1], 0);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {
        updateCameraPosition();
    }

    @Override
    public void dispose() {

    }

    @Override
    public void show() {
        sandbox = Sandbox.getInstance();
        uiStage = sandbox.getUIStage();

        if (isDrawingBgLogo) {
            uiStage.getRoot().addActorAt(0, bgLogo);
            bgLogo.setPosition((uiStage.getWidth() - bgLogo.getWidth()) * 0.5f, (uiStage.getHeight() - bgLogo.getHeight()) * 0.5f);
        }

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(this);
        multiplexer.addProcessor(uiStage);
        multiplexer.addProcessor(new SandboxInputAdapter());
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void hide() {
        uiStage.dispose();
    }

    @Override
    public void resize(int width, int height) {
        if (Sandbox.getInstance().getViewport() != null) {
            Sandbox.getInstance().getViewport().update(width, height, true);
        }

        if (width == 0 && height == 0) return;

        uiStage.resize(width, height);
        screenSize.set(width, height);

        updateCameraPosition();
    }

    public void updateActorSize() {
        bgLogo.setScale(uiStage.getUIScaleDensity());
        bgLogo.setPosition((uiStage.getWidth() - bgLogo.getWidth()) * 0.5f, (uiStage.getHeight() - bgLogo.getHeight()) * 0.5f);
        blackOverlay.setSize(uiStage.getWidth(), uiStage.getHeight());
    }

    public void showBlackOverlay() {
        blackOverlay.getColor().set(0, 0, 0, 0);
        uiStage.addActor(blackOverlay);
        blackOverlay.addAction(Actions.alpha(0.5f, .3f, Interpolation.smooth));

        if (!isDrawingBgLogo) {
            bgLogo.getColor().a = 0;
            bgLogo.setScale(.5f * bgLogo.getScaleX());
            bgLogo.addAction(Actions.forever(
                    Actions.sequence(
                            Actions.alpha(.5f, 1f, Interpolation.slowFast),
                            Actions.alpha(.3f, 1f, Interpolation.slowFast)
                    )
            ));
            uiStage.addActor(bgLogo);
        }
    }

    public void hideBlackOverlay() {
        blackOverlay.addAction(Actions.sequence(Actions.alpha(0, .2f, Interpolation.smooth), Actions.removeActor()));

        if (!isDrawingBgLogo) {
            bgLogo.clearActions();
            bgLogo.addAction(Actions.sequence(Actions.alpha(0, .2f, Interpolation.smooth), Actions.removeActor()));
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (KeyBindingsLayout.mapAction(keycode)) {
            case KeyBindingsLayout.NEW_PROJECT:
                facade.sendNotification(FileMenu.NEW_PROJECT, null, MenuAPI.FILE_MENU);
                break;
            case KeyBindingsLayout.OPEN_PROJECT:
                facade.sendNotification(FileMenu.OPEN_PROJECT, null, MenuAPI.FILE_MENU);
                break;
            case KeyBindingsLayout.SAVE_PROJECT:
                if (sandbox.sceneControl.getCurrentSceneVO() != null) {
                    facade.sendNotification(FileMenu.SAVE_PROJECT, null, MenuAPI.FILE_MENU);
                }
                break;
            case KeyBindingsLayout.EXPORT_PROJECT:
                if (sandbox.sceneControl.getCurrentSceneVO() != null) {
                    facade.sendNotification(MsgAPI.ACTION_EXPORT_PROJECT);
                }
                break;
            case KeyBindingsLayout.IMPORT_TO_LIBRARY:
                if (sandbox.sceneControl.getCurrentSceneVO() != null) {
                    facade.sendNotification(ResourcesMenu.IMPORT_TO_LIBRARY, null, MenuAPI.FILE_MENU);
                }
                break;
            case KeyBindingsLayout.OPEN_SETTINGS:
                facade.sendNotification(FileMenu.SETTINGS, null, MenuAPI.FILE_MENU);
                break;
            case KeyBindingsLayout.EXIT_APP:
                HyperLap2DApp.getInstance().hyperlap2D.closeRequested();
                break;
            case KeyBindingsLayout.HIDE_GUI:
                uiStage.addAction(Actions.parallel(Actions.fadeOut(0.1f), Actions.touchable(Touchable.disabled)));
                break;
            case KeyBindingsLayout.TOGGLE_FULL_SCREEN:
                boolean fullScreen = Gdx.graphics.isFullscreen();
                Graphics.DisplayMode currentMode = Gdx.graphics.getDisplayMode();
                if (fullScreen)
                    Gdx.graphics.setWindowedMode(currentMode.width, currentMode.height);
                else
                    Gdx.graphics.setFullscreenMode(currentMode);
                break;
            case KeyBindingsLayout.OPEN_CONSOLE:
                facade.sendNotification(MsgAPI.OPEN_CONSOLE);
                break;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        switch (KeyBindingsLayout.mapAction(keycode)) {
            case KeyBindingsLayout.HIDE_GUI:
                uiStage.addAction(Actions.parallel(Actions.touchable(Touchable.enabled), Actions.fadeIn(0.1f)));
                break;
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public void setBackUI(SandboxBackUI sandboxBackUI) {
        this.sandboxBackUI = sandboxBackUI;
    }
}
