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

import games.rednblack.editor.Main;
import games.rednblack.editor.renderer.utils.Version;

import java.io.*;
import java.util.Properties;

public class AppConfig  {

    public static AppConfig instance;

    public String versionString;
    public Version version;
    public String build;

    public Properties properties;

    private AppConfig() {}

    public static AppConfig getInstance() {
        if(instance == null) {
            instance = new AppConfig();
            instance.loadProperties();
        }

        return instance;
    }

    private void loadProperties() {
        File root = new File(new File(".").getAbsolutePath()).getParentFile();
        File configDir = new File(root.getAbsolutePath() + File.separator + "configs");

        properties = new Properties();
        InputStream propertiesInput = null;

        File file = new File(configDir.getAbsolutePath() + File.separator + "app.properties");
        if (file.exists()) {
            try {
                propertiesInput = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            propertiesInput = Main.class.getClassLoader().getResourceAsStream("configs/app.properties");
        }

        if (propertiesInput != null) {
            try {
                properties.load(propertiesInput);
                versionString = properties.getProperty("version");
                version = new Version(versionString.replaceAll("[^0-9\\\\.]", ""));
                build = properties.getProperty("build");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
