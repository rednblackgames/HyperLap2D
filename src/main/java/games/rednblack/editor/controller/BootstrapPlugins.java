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
import games.rednblack.editor.proxy.PluginManager;
import games.rednblack.editor.proxy.SettingsManager;
import games.rednblack.editor.splash.SplashScreenAdapter;
import games.rednblack.h2d.common.plugins.H2DPlugin;
import net.mountainblade.modular.Module;
import net.mountainblade.modular.ModuleManager;
import net.mountainblade.modular.impl.DefaultModuleManager;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.command.SimpleCommand;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

public class BootstrapPlugins extends SimpleCommand {

    public void execute(INotification notification) {
        super.execute(notification);
        facade = HyperLap2DFacade.getInstance();

        SettingsManager settingsManager = facade.retrieveProxy(SettingsManager.NAME);
        if (!settingsManager.editorConfigVO.enablePlugins)
            return;

        facade.sendNotification(SplashScreenAdapter.UPDATE_SPLASH, "Loading Plugins...");

        PluginManager pluginManager = new PluginManager();
        facade.registerProxy(pluginManager);

        ModuleManager manager = new DefaultModuleManager();
        for (File pluginDir : settingsManager.pluginDirs) {
            Collection<Module> loadedPlugins = manager.loadModules(pluginDir);

            pluginManager.setPluginDir(pluginDir.getAbsolutePath());
            pluginManager.setCacheDir(settingsManager.cacheDir.getAbsolutePath());
            System.out.println("Plugins directory: " + pluginDir.getAbsolutePath());
            System.out.println("Plugins loaded: " + loadedPlugins.size());

            for(Module module: loadedPlugins) {
                try {
                    pluginManager.initPlugin((H2DPlugin) module.getClass().getDeclaredConstructor().newInstance());
                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
