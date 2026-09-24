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
import games.rednblack.editor.proxy.PluginUIBridge;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import games.rednblack.editor.HyperLap2DApp;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.SettingsManager;
import games.rednblack.editor.proxy.WidgetEditingProxy;
import games.rednblack.editor.renderer.ecs.Engine;
import games.rednblack.editor.utils.FullscreenUtils;
import games.rednblack.editor.utils.KeyBindingsLayout;
import games.rednblack.editor.utils.MenuIcons;
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
import games.rednblack.puremvc.Facade;

import java.util.Collections;
import java.util.Set;

public class HyperLap2DScreen extends InputAdapter implements Screen {
    private static final String TAG = HyperLap2DScreen.class.getCanonicalName();

    public UIStage uiStage;

    private Engine engine;

    private final Facade facade;

    private Sandbox sandbox;
    private SandboxBackUI sandboxBackUI;

    /** True while the editor is out of the way and the scene has the input. */
    private boolean previewing = false;
    /** What is left on screen to say why nothing answers the way it usually does. */
    private Image previewWatermark;
    private static final float WATERMARK_MARGIN = 32f;
    /** Left there rather than laid over the scene: it says something, it does not take over. */
    private static final float WATERMARK_ALPHA = 0.7f;

    private final Color defaultBackgroundColor;
    private final Color backgroundColor;
    private final Image bgLogo;
    private final Image blackOverlay;
    private final Vector2 screenSize;

    private boolean isDrawingBgLogo;

    public HyperLap2DScreen() {
        facade = Facade.getInstance();
        defaultBackgroundColor = new Color(0.15f, 0.15f, 0.15f, 1.0f);
        SettingsManager settingsManager = facade.retrieveProxy(SettingsManager.NAME);
        backgroundColor = settingsManager.editorConfigVO.backgroundColor;
        isDrawingBgLogo = true;
        bgLogo = new Image(new Texture(Gdx.files.internal("style/bglogo.png")));
        bgLogo.setOrigin(Align.center);
        bgLogo.getColor().a = 0.6f;
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
            engine.process();
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
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
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
        sandbox = PluginUIBridge.get().getSandbox();
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
        if (PluginUIBridge.get().getSandbox().getViewport() != null) {
            PluginUIBridge.get().getSandbox().getViewport().update(width, height, true);
        }

        if (width == 0 && height == 0) return;

        uiStage.resize(width, height);
        screenSize.set(width, height);
        placeWatermark();

        updateCameraPosition();

        updateActorSize();
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
            bgLogo.addAction(Actions.sequence(Actions.alpha(0, .2f, Interpolation.smooth), Actions.scaleTo(uiStage.getUIScaleDensity(), uiStage.getUIScaleDensity()), Actions.removeActor()));
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        if (previewing) return previewKeyDown(keycode);

        switch (KeyBindingsLayout.mapAction(keycode)) {
            case KeyBindingsLayout.NEW_PROJECT:
                facade.sendNotification(FileMenu.NEW_PROJECT, null, MenuAPI.FILE_MENU);
                break;
            case KeyBindingsLayout.OPEN_PROJECT:
                facade.sendNotification(FileMenu.OPEN_PROJECT, null, MenuAPI.FILE_MENU);
                break;
            case KeyBindingsLayout.SAVE_PROJECT:
                if (sandbox.getSceneControl().getCurrentSceneVO() != null) {
                    facade.sendNotification(FileMenu.SAVE_PROJECT, null, MenuAPI.FILE_MENU);
                }
                break;
            case KeyBindingsLayout.EXPORT_PROJECT:
                if (sandbox.getSceneControl().getCurrentSceneVO() != null) {
                    facade.sendNotification(MsgAPI.ACTION_EXPORT_PROJECT);
                }
                break;
            case KeyBindingsLayout.IMPORT_TO_LIBRARY:
                if (sandbox.getSceneControl().getCurrentSceneVO() != null) {
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
                togglePreview();
                break;
            case KeyBindingsLayout.TOGGLE_FULL_SCREEN:
                boolean fullScreen = FullscreenUtils.isFullscreen();
                FullscreenUtils.setFullscreen(!fullScreen);
                break;
            case KeyBindingsLayout.OPEN_CONSOLE:
                facade.sendNotification(MsgAPI.OPEN_CONSOLE);
                break;
            case KeyBindingsLayout.SHOW_MINI_MAP:
                facade.sendNotification(MsgAPI.SHOW_MINI_MAP);
                break;
        }
        return false;
    }

    /**
     * While the scene has the input the editor keeps two keys only: the one that gives it back and
     * the full screen switch. Everything else is a character or a shortcut the scene may want, so it
     * falls through to the widgets.
     */
    private boolean previewKeyDown(int keycode) {
        if (keycode == Input.Keys.ESCAPE) {
            togglePreview();
            return true;
        }

        switch (KeyBindingsLayout.mapAction(keycode)) {
            case KeyBindingsLayout.HIDE_GUI:
                togglePreview();
                return true;
            case KeyBindingsLayout.TOGGLE_FULL_SCREEN:
                FullscreenUtils.setFullscreen(!FullscreenUtils.isFullscreen());
                return true;
        }
        return false;
    }

    /**
     * Puts the editor away and hands the sandbox to the scene, or brings it back. What is left is the
     * scene as a game would show it - no panels, no selection gizmos - and its widgets answer the
     * mouse, the wheel and the keyboard. The middle button still pans, so it can be looked around.
     */
    public void togglePreview() {
        previewing = !previewing;

        if (previewing) {
            //a panel still holding the keys would eat what the scene is meant to read
            uiStage.setKeyboardFocus(null);
            uiStage.setScrollFocus(null);
            uiStage.addAction(Actions.parallel(Actions.fadeOut(0.1f), Actions.touchable(Touchable.disabled)));
            facade.sendNotification(MsgAPI.HIDE_SELECTIONS, selection());
            WidgetEditingProxy.get().setInputForwarded(true);
            showWatermark(true);
        } else {
            showWatermark(false);
            //first the scene lets go of its gestures, then the editor draws itself again
            WidgetEditingProxy.get().setInputForwarded(false);
            uiStage.addAction(Actions.parallel(Actions.touchable(Touchable.enabled), Actions.fadeIn(0.1f)));
            facade.sendNotification(MsgAPI.SHOW_SELECTIONS, selection());
        }
    }

    /**
     * Brings the badge in, or takes it away. It lives on the same stage as everything else and is
     * simply the one thing that does not fade with it.
     */
    private void showWatermark(boolean show) {
        if (previewWatermark == null) {
            Drawable drawable = MenuIcons.get("widget-preview-watermark");
            if (drawable == null) return;

            previewWatermark = new PreviewWatermark(drawable);
        }

        if (!show) {
            previewWatermark.clearActions();
            previewWatermark.addAction(Actions.sequence(Actions.fadeOut(0.15f), Actions.removeActor()));
            return;
        }

        placeWatermark();
        previewWatermark.clearActions();
        previewWatermark.getColor().a = 0;
        uiStage.getRoot().addActor(previewWatermark);
        previewWatermark.addAction(Actions.alpha(WATERMARK_ALPHA, 0.15f));
    }

    /** Top left, out of the way of whatever the scene is doing in the middle. */
    private void placeWatermark() {
        if (previewWatermark == null) return;

        previewWatermark.setPosition(WATERMARK_MARGIN,
                uiStage.getHeight() - previewWatermark.getHeight() - WATERMARK_MARGIN);
    }

    /** The badge the fading does not reach: everything above it has gone, and that is the point. */
    private static class PreviewWatermark extends Image {

        PreviewWatermark(Drawable drawable) {
            super(drawable);
            setTouchable(Touchable.disabled);
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            super.draw(batch, 1f);
        }
    }

    private Set<Integer> selection() {
        return sandbox == null || sandbox.getSelector() == null
                ? Collections.emptySet()
                : sandbox.getSelector().getCurrentSelection();
    }

    @Override
    public boolean keyUp(int keycode) {
        if (previewing) return false;

        switch (KeyBindingsLayout.mapAction(keycode)) {
            case KeyBindingsLayout.SHOW_MINI_MAP:
                facade.sendNotification(MsgAPI.HIDE_MINI_MAP);
                break;
        }
        return false;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public void setBackUI(SandboxBackUI sandboxBackUI) {
        this.sandboxBackUI = sandboxBackUI;
    }
}
