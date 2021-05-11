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

package games.rednblack.editor.controller;

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.*;
import games.rednblack.editor.splash.SplashScreenAdapter;
import games.rednblack.h2d.common.proxy.CursorManager;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.command.SimpleCommand;

/**
 * Created by sargis on 4/1/15.
 */
public class BootstrapProxyCommand extends SimpleCommand {
    @Override
    public void execute(INotification notification) {
        super.execute(notification);
        facade = HyperLap2DFacade.getInstance();
        facade.sendNotification(SplashScreenAdapter.UPDATE_SPLASH, "Loading Proxies...");

        facade.registerProxy(new WindowTitleManager());
        facade.registerProxy(new FontManager());
        facade.registerProxy(new CommandManager());
        facade.registerProxy(new CursorManager());
        facade.registerProxy(new ResolutionManager());
        facade.registerProxy(new ResourceManager());
        facade.registerProxy(new ProjectManager());
        facade.registerProxy(new SceneDataManager());
    }
}
