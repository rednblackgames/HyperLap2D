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

package games.rednblack.editor.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import games.rednblack.editor.HyperLap2D;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.view.ui.UIWindowAction;
import games.rednblack.editor.view.ui.UIWindowActionMediator;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.SystemUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;

/**
 * Created by sargis on 4/1/15.
 */
public class HyperLap2DUtils {
    public static final FilenameFilter PNG_FILTER = new SuffixFileFilter(".png");
    public static final FilenameFilter TTF_FILTER = new SuffixFileFilter(".ttf");
    public static final FilenameFilter JSON_FILTER = new SuffixFileFilter(".json");
    public static final FilenameFilter SCML_FILTER = new SuffixFileFilter(".scml");
    public static final FilenameFilter DT_FILTER = new SuffixFileFilter(".dt");
    public static final String MY_DOCUMENTS_PATH = getMyDocumentsLocation();


    private static String getMyDocumentsLocation() {
        String myDocuments = null;
        try {
            if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) {
                myDocuments = System.getProperty("user.home") + File.separator + "Documents";
            }
            if (SystemUtils.IS_OS_WINDOWS) {
                Process p = Runtime.getRuntime().exec("reg query \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders\" /v personal");
                p.waitFor();

                InputStream in = p.getInputStream();
                byte[] b = new byte[in.available()];
                in.read(b);
                in.close();

                myDocuments = new String(b);
                myDocuments = myDocuments.split("\\s\\s+")[4];
            }
            if (SystemUtils.IS_OS_LINUX) {
                myDocuments = System.getProperty("user.home") + File.separator + "Documents";
            }


        } catch (Throwable t) {
            t.printStackTrace();
        }

        return myDocuments;
    }


    public static void setWindowDragListener(Actor actor) {
        actor.addListener(new InputListener() {
            private final long context = GLFW.glfwGetCurrentContext();
            private float startX = 0;
            private float startY = 0;
            private final DoubleBuffer cursorX = BufferUtils.createDoubleBuffer(1);
            private final DoubleBuffer cursorY = BufferUtils.createDoubleBuffer(1);
            private final IntBuffer windowX = BufferUtils.createIntBuffer(1);
            private final IntBuffer windowY = BufferUtils.createIntBuffer(1);

            private int getX() {
                return MathUtils.floor((float) cursorX.get(0));
            }
            private int getY() {
                return MathUtils.floor((float) cursorY.get(0));
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                UIWindowActionMediator uiWindowActionMediator = HyperLap2DFacade.getInstance().retrieveMediator(UIWindowActionMediator.NAME);
                UIWindowAction uiWindowAction = uiWindowActionMediator.getViewComponent();
                if (uiWindowAction.isMaximized())
                    return false;
                GLFW.glfwGetCursorPos(context, cursorX, cursorY);
                startX = getX();
                startY = getY();
                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                GLFW.glfwGetCursorPos(context, cursorX, cursorY);
                float offsetX = getX() - startX;
                float offsetY = getY() - startY;
                GLFW.glfwGetWindowPos(context, windowX, windowY);
                GLFW.glfwSetWindowPos(context, (int)(windowX.get(0) + offsetX), (int)(windowY.get(0) + offsetY));
            }
        });
    }
}
