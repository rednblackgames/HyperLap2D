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
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.SettingsManager;
import games.rednblack.editor.view.menu.FileMenu;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.editor.view.ui.widget.actors.basic.SandboxBackUI;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.editor.view.stage.input.SandboxInputAdapter;
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
    private final Vector2 screenSize;

    private boolean isDrawingBgLogo;

    public HyperLap2DScreen() {
        facade = HyperLap2DFacade.getInstance();
        defaultBackgroundColor = new Color(0.15f, 0.15f, 0.15f, 1.0f);
        SettingsManager settingsManager = facade.retrieveProxy(SettingsManager.NAME);
        backgroundColor = settingsManager.editorConfigVO.backgroundColor;
        isDrawingBgLogo = true;
        bgLogo = new Image(new Texture(Gdx.files.internal("style/bglogo.png")));
        bgLogo.getColor().a = 0.3f;
        screenSize = new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
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
            bgLogo.setPosition(screenSize.x / 2 - bgLogo.getWidth() / 2f, screenSize.y / 2 - bgLogo.getHeight() / 2f);
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
        bgLogo.setPosition(screenSize.x / 2 - bgLogo.getWidth() / 2f, screenSize.y / 2 - bgLogo.getHeight() / 2f);

        updateCameraPosition();
    }

    @Override
    public boolean keyDown(int keycode) {
        if (Gdx.input.isKeyPressed(Input.Keys.SYM) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
            switch (keycode) {
                case Input.Keys.N:
                    facade.sendNotification(FileMenu.NEW_PROJECT, null, FileMenu.FILE_MENU);
                    break;
                case Input.Keys.O:
                    facade.sendNotification(FileMenu.OPEN_PROJECT, null, FileMenu.FILE_MENU);
                    break;
                case Input.Keys.I:
                    if (sandbox.sceneControl.getCurrentSceneVO() != null) {
                        facade.sendNotification(FileMenu.IMPORT_TO_LIBRARY, null, FileMenu.FILE_MENU);
                    }
                    break;
                case Input.Keys.S:
                    if (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT)) {
                        facade.sendNotification(FileMenu.SETTINGS, null, FileMenu.FILE_MENU);
                    } else if (sandbox.sceneControl.getCurrentSceneVO() != null) {
                        facade.sendNotification(FileMenu.SAVE_PROJECT, null, FileMenu.FILE_MENU);
                    }
                    break;
                case Input.Keys.E:
                    if (sandbox.sceneControl.getCurrentSceneVO() != null) {
                        facade.sendNotification(MsgAPI.ACTION_EXPORT_PROJECT);
                    }
                    break;
            }
        }

        if (Gdx.input.isKeyPressed(Input.Keys.SYM) && keycode == Input.Keys.Q) {
            facade.sendNotification(MsgAPI.CHECK_EDITS_ACTION, (Runnable) () -> Gdx.app.exit());
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
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
    public boolean scrolled(int amount) {
        return false;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public void setBackUI(SandboxBackUI sandboxBackUI) {
        this.sandboxBackUI = sandboxBackUI;
    }
}
