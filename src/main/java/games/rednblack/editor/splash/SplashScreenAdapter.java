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

package games.rednblack.editor.splash;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import games.rednblack.editor.HyperLap2DApp;
import games.rednblack.editor.Sync;
import games.rednblack.editor.utils.AppConfig;
import org.lwjgl.glfw.GLFW;

/**
 * Created by azakhary on 5/15/2015.
 */
public class SplashScreenAdapter extends Game {

    private static final String prefix = "games.rednblack.editor.splash";
    public static final String UPDATE_SPLASH = prefix + ".UPDATE";
    public static final String CLOSE_SPLASH = prefix + ".CLOSE";

    private SplashScreen screen;

    @Override
    public void create () {
        screen = new SplashScreen();
        setScreen(screen);
    }

    public void loadedData() {
        screen.loadedData();
    }

    public void setProgressStatus(String status) {
        screen.setProgressStatus(status);
    }
}
