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
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import games.rednblack.editor.proxy.CommandManager;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.splash.SplashScreenAdapter;
import games.rednblack.editor.view.frame.FileDropListener;
import games.rednblack.editor.view.ui.dialog.ImportDialog;
import games.rednblack.h2d.common.MsgAPI;
import com.kotcrab.vis.ui.VisUI;
import com.puremvc.patterns.proxy.Proxy;
import games.rednblack.editor.proxy.EditorTextureManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWDropCallback;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;

public class HyperLap2D implements Proxy, ApplicationListener {
    private static final String TAG = HyperLap2D.class.getCanonicalName();
    public static final String NAME = TAG;

    public EditorTextureManager textureManager;
    private HyperLap2DFacade facade;
    private Object data;
    private boolean isStartup = false;
    private AssetManager assetManager;

    public HyperLap2DFacade getFacade() {
        return facade;
    }

    private final Sync sync = new Sync();

    public HyperLap2D() {
    }

    @Override
    public void create() {
        isStartup = false;
        assetManager = new AssetManager();
        assetManager.load("style/uiskin.json", Skin.class);

        facade = HyperLap2DFacade.getInstance();
    }

    private void startup() {
        isStartup = true;
        VisUI.load((Skin) assetManager.get("style/uiskin.json"));
        VisUI.setDefaultTitleAlign(Align.center);

        facade.startup(this);
        sendNotification(MsgAPI.CREATE);
        facade.sendNotification(SplashScreenAdapter.CLOSE_SPLASH, "Initializing...");
        Lwjgl3Graphics graphics = (Lwjgl3Graphics)Gdx.graphics;
        GLFW.glfwSetDropCallback(graphics.getWindow().getWindowHandle(), new GLFWDropCallback() {
            @Override
            public void invoke (long window, int count, long names) {
                PointerBuffer nameBuffer = MemoryUtil.memPointerBuffer(names, count);
                String[] filesPaths = new String[count];
                for (int i = 0; i < count; i++) {
                    String pathToObject = MemoryUtil.memUTF8(MemoryUtil.memByteBufferNT1(nameBuffer.get(i)));
                    filesPaths[i] = pathToObject;
                }

                ImportDialog.DropBundle bundle = new ImportDialog.DropBundle();
                bundle.pos = new Vector2(Gdx.input.getX(),  Gdx.input.getY());
                bundle.paths = filesPaths;

                facade.sendNotification(FileDropListener.ACTION_DROP, bundle);
            }
        });

        IntBuffer w = BufferUtils.createIntBuffer(1);
        IntBuffer h = BufferUtils.createIntBuffer(1);
        GLFW.glfwGetWindowSize(graphics.getWindow().getWindowHandle(), w, h);
        int width = w.get(0);
        int height = h.get(0);

        sendNotification(MsgAPI.RESIZE, new int[]{width, height});
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
        if(!isStartup && assetManager.update()) {
            startup();
        } else {
            sync.sync(60);
            sendNotification(MsgAPI.RENDER, Gdx.graphics.getDeltaTime());
        }
    }

    @Override
    public void resize(int width, int height) {
        sendNotification(MsgAPI.RESIZE, new int[]{width, height});
    }

    @Override
    public void dispose() {
        sendNotification(MsgAPI.DISPOSE);
        VisUI.dispose();
        facade.sendNotification(MsgAPI.APP_EXIT);
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

    }

    @Override
    public void onRemove() {

    }

    public boolean hasUnsavedStuff() {
        CommandManager commandManager = facade.retrieveProxy(CommandManager.NAME);
        return commandManager.isModified();
    }

}