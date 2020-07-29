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

import com.badlogic.gdx.Game;

public class SplashScreenAdapter extends Game {

    private static final String prefix = "games.rednblack.editor.splash";
    public static final String UPDATE_SPLASH = prefix + ".UPDATE";
    public static final String CLOSE_SPLASH = prefix + ".CLOSE";

    private SplashScreen screen;
    private boolean isLoading = true;

    @Override
    public void create () {
        screen = new SplashScreen(isLoading);
        setScreen(screen);
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }

    @Override
    public void dispose() {
        super.dispose();
        screen.dispose();
    }

    public void loadedData() {
        screen.loadedData();
    }

    public void setProgressStatus(String status) {
        screen.setProgressStatus(status);
    }
}
