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

package games.rednblack.editor.proxy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.renderer.data.CompositeItemVO;
import games.rednblack.editor.renderer.data.MainItemVO;
import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.editor.renderer.utils.HyperJson;
import org.apache.commons.io.FileUtils;
import org.puremvc.java.patterns.proxy.Proxy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by sargis on 3/23/15.
 */
public class SceneDataManager extends Proxy {

    private static final String TAG = SceneDataManager.class.getCanonicalName();
    public static final String NAME = TAG;

    public SceneDataManager() {
        super(NAME);
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();
    }

    public SceneVO createNewScene(String name) {
        SceneVO vo = new SceneVO();
        vo.sceneName = name;
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        try {
            String projPath = projectManager.getCurrentProjectPath();
            FileUtils.writeStringToFile(new File(projPath + "/project.dt"), projectManager.currentProjectInfoVO.constructJsonString(), "utf-8");
            FileUtils.writeStringToFile(new File(projPath + "/scenes/" + vo.sceneName + ".dt"), vo.constructJsonString(), "utf-8");
            projectManager.currentProjectInfoVO.scenes.add(vo);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return vo;
    }

    public String getCurrProjectScenePathByName(String sceneName) {
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        return projectManager.getCurrentProjectPath() + "/scenes/" + sceneName + ".dt";
    }

    public void saveScene(SceneVO vo) {
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        try {
            FileUtils.writeStringToFile(new File(projectManager.getCurrentProjectPath() + "/scenes/" + vo.sceneName + ".dt"),
                    vo.constructJsonString(), "utf-8");
            CommandManager commandManager = facade.retrieveProxy(CommandManager.NAME);
            commandManager.saveEvent();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void deleteCurrentScene() {
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        if (projectManager.currentProjectVO.lastOpenScene.equals("MainScene")) {
            return;
        }
        deleteScene(projectManager.currentProjectVO.lastOpenScene);
    }

    private void deleteScene(String sceneName) {
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        ArrayList<SceneVO> scenes = projectManager.currentProjectInfoVO.scenes;
        SceneVO sceneToDelete = null;
        for (SceneVO scene : scenes) {
            if (scene.sceneName.equals(sceneName)) {
                sceneToDelete = scene;
                break;
            }
        }
        if (sceneToDelete != null) {
            scenes.remove(sceneToDelete);
        }
        projectManager.currentProjectInfoVO.scenes = scenes;
        String projPath = projectManager.getCurrentProjectPath();
        try {
            FileUtils.writeStringToFile(new File(projPath + "/project.dt"), projectManager.currentProjectInfoVO.constructJsonString(), "utf-8");
            FileUtils.forceDelete(new File(projPath + "/scenes/" + sceneName + ".dt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void buildScenes(String targetPath) {
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        String srcPath = projectManager.getCurrentProjectPath() + "/scenes";
        FileHandle scenesDirectoryHandle = Gdx.files.absolute(srcPath);
        try {
            for (File scene : scenesDirectoryHandle.file().listFiles()) {
                File fileTarget = new File(targetPath + File.separator + scenesDirectoryHandle.name() + File.separator + scene.getName());
                Json json = HyperJson.getJson();
                SceneVO sceneToExport = json.fromJson(SceneVO.class, FileUtils.readFileToString(scene, "utf-8"));
                clearCompositesForExport(sceneToExport.composite);
                FileUtils.writeStringToFile(fileTarget, sceneToExport.constructJsonString(), "utf-8");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //copy project dt
        try {
            FileUtils.copyFile(new File(projectManager.getCurrentProjectPath() + "/project.dt"), new File(targetPath + "/project.dt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearCompositesForExport(CompositeItemVO compositeVO) {
        if (compositeVO == null)
            return;

        compositeVO.sStickyNotes.clear();
        Array<MainItemVO> sComposites = compositeVO.content.get(CompositeItemVO.class.getName());
        if (sComposites != null) {
            for (MainItemVO mainItemVO : sComposites) {
                CompositeItemVO c = (CompositeItemVO) mainItemVO;
                clearCompositesForExport(c);
            }
        }
    }
}
