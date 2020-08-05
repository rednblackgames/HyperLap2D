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

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class Main {

    public static void main(String[] argv) {
        Graphics.DisplayMode dm = Lwjgl3ApplicationConfiguration.getDisplayMode();

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowedMode(467,415);
        config.setResizable(false);
        config.setDecorated(false);
        config.setInitialVisible(false);
        config.useVsync(false);
        config.setIdleFPS(60);
        config.setTitle("HyperLap2D");
        config.setWindowIcon("hyperlap_icon_96.png");

        Thread.currentThread().setUncaughtExceptionHandler(new CustomExceptionHandler());

        new Lwjgl3Application(HyperLap2DApp.initInstance(dm.width, dm.height), config);
    }
}
