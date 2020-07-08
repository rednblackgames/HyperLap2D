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

package games.rednblack.editor.view.ui.widget.actors;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.kotcrab.vis.ui.widget.VisLabel;
import games.rednblack.editor.view.ui.widget.actors.basic.PixelRect;
import games.rednblack.editor.view.stage.Sandbox;

/**
 * Created by sargis on 7/10/14.
 */
public class ResolutionBounds extends Group {
    private float width;
    private float height;

	 private PixelRect resolutionBounds;
	 private Label label;

    public ResolutionBounds(Sandbox sandbox) {
        detectDimensions(sandbox);
        crateBoundsRectangle();
        crateResolutionIndicator();
    }

    private void detectDimensions(Sandbox baseStage) {
    	//TODO fix and uncomment 
//        HyperLap2DFacade facade = HyperLap2DFacade.getInstance();
//        ResolutionManager resolutionManager = facade.retrieveProxy(ResolutionManager.NAME);
//        ResolutionEntryVO resolutionEntryVO = baseStage.projectManager.getCurrentProjectInfoVO().getResolution(resolutionManager.currentResolutionName);
//        if (resolutionEntryVO == null) {
//            resolutionEntryVO = baseStage.projectManager.getCurrentProjectInfoVO().originalResolution;
//        }
//        width = resolutionEntryVO.width;
//        height = resolutionEntryVO.height;
    }

    private void crateResolutionIndicator() {
        label = new VisLabel((int) width + " x " + (int) height);
        label.setX(width - label.getWidth());
        label.setY(height);
        addActor(label);
    }

    private void crateBoundsRectangle() {
        resolutionBounds = new PixelRect(width, height);
        addActor(resolutionBounds);
    }

	 @Override
	 public void act(float delta) {
		  super.act(delta);

		  // change size according to zoom
		  OrthographicCamera camera = (OrthographicCamera)getStage().getCamera();
		  resolutionBounds.setThickness(camera.zoom);
	 }
}
