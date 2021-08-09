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

package games.rednblack.editor;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowListener;
import com.badlogic.gdx.math.Vector2;
import com.kotcrab.vis.ui.VisUI;
import games.rednblack.editor.proxy.CommandManager;
import games.rednblack.editor.proxy.SettingsManager;
import games.rednblack.editor.splash.SplashScreenAdapter;
import games.rednblack.editor.view.ui.panel.ImportPanel;
import games.rednblack.editor.view.ui.widget.actors.basic.WhitePixel;
import games.rednblack.h2d.common.MsgAPI;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.puremvc.java.interfaces.IProxy;
import org.puremvc.java.patterns.observer.Notification;

import java.nio.IntBuffer;

public class HyperLap2D implements IProxy, ApplicationListener, Lwjgl3WindowListener {
    private static final String TAG = HyperLap2D.class.getCanonicalName();
    public static final String NAME = TAG;

    private HyperLap2DFacade facade;
    private Object data;

    private final Notification renderNotification;

    public HyperLap2DFacade getFacade() {
        return facade;
    }

    private long startTime = 0;

    private final SettingsManager settingsManager;

    public HyperLap2D(SettingsManager settingsManager) {
        renderNotification = new Notification(MsgAPI.RENDER, null, null);
        this.settingsManager = settingsManager;
    }

    @Override
    public void create() {
        facade = HyperLap2DFacade.getInstance();
        facade.registerProxy(settingsManager);
        facade.registerProxy(this);
    }

    private void startup() {
        facade.startup();
        sendNotification(MsgAPI.CREATE);
        facade.sendNotification(SplashScreenAdapter.CLOSE_SPLASH, "Initializing...");
        Lwjgl3Graphics graphics = (Lwjgl3Graphics)Gdx.graphics;

        IntBuffer w = BufferUtils.createIntBuffer(1);
        IntBuffer h = BufferUtils.createIntBuffer(1);
        GLFW.glfwGetWindowSize(graphics.getWindow().getWindowHandle(), w, h);
        int width = w.get(0);
        int height = h.get(0);

        sendNotification(MsgAPI.RESIZE, new int[]{width, height});

        startTime = System.currentTimeMillis();
    }

    @Override
    public void pause() {
        sendNotification(MsgAPI.PAUSE);
    }

    @Override
    public void resume() {
        sendNotification(MsgAPI.RESUME);
    }

    @Override
    public void render() {
        renderNotification.setBody(Math.min(Gdx.graphics.getDeltaTime(), 0.1f));
        facade.notifyObservers(renderNotification);
    }

    @Override
    public void resize(int width, int height) {
        sendNotification(MsgAPI.RESIZE, new int[]{width, height});
    }

    @Override
    public void dispose() {
        sendNotification(MsgAPI.DISPOSE);
    }

    @Override
    public void sendNotification(String notificationName, Object body, String type) {
        System.out.println("sendNotification: " + System.currentTimeMillis() + " " + type);
        facade.sendNotification(notificationName, body, type);
    }

    @Override
    public void sendNotification(String notificationName, Object body) {
        facade.sendNotification(notificationName, body);
    }

    @Override
    public void sendNotification(String notificationName) {
        facade.sendNotification(notificationName);
    }

    @Override
    public String getProxyName() {
        return NAME;
    }

    @Override
    public Object getData() {
        return data;
    }

    @Override
    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public void onRegister() {
        ConsoleInterceptor errInterceptor = new ConsoleInterceptor(System.err);
        errInterceptor.setPrefix("[FF0000]");
        errInterceptor.setSuffix("[FFFFFF]");
        System.setErr(errInterceptor);
        ConsoleInterceptor outInterceptor = new ConsoleInterceptor(System.out);
        System.setOut(outInterceptor);

        startup();
    }

    @Override
    public void onRemove() {

    }

    public boolean hasUnsavedStuff() {
        CommandManager commandManager = facade.retrieveProxy(CommandManager.NAME);
        return commandManager.isModified();
    }

    @Override
    public void created(Lwjgl3Window window) {

    }

    @Override
    public void iconified(boolean isIconified) {

    }

    @Override
    public void maximized(boolean isMaximized) {
        facade.sendNotification(MsgAPI.WINDOW_MAXIMIZED, isMaximized);
    }

    @Override
    public void focusLost() {

    }

    @Override
    public void focusGained() {

    }

    @Override
    public boolean closeRequested() {
        facade.sendNotification(MsgAPI.CHECK_EDITS_ACTION, (Runnable) () -> {
            WhitePixel.disposeShared();
            VisUI.dispose();

            SettingsManager settingsManager = facade.retrieveProxy(SettingsManager.NAME);
            settingsManager.editorConfigVO.totalSpentTime += System.currentTimeMillis() - startTime;
            settingsManager.saveEditorConfig();

            Gdx.app.exit();
        });
        return false;
    }

    @Override
    public void filesDropped(String[] files) {
        ImportPanel.DropBundle bundle = new ImportPanel.DropBundle();
        bundle.pos = new Vector2(Gdx.input.getX(),  Gdx.input.getY());
        bundle.paths = files;

        facade.sendNotification(MsgAPI.ACTION_FILES_DROPPED, bundle);
    }

    @Override
    public void refreshRequested() {

    }
}