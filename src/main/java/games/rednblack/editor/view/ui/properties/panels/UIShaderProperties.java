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

package games.rednblack.editor.view.ui.properties.panels;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.event.ButtonToNotificationListener;
import games.rednblack.editor.event.SelectBoxChangeListener;
import games.rednblack.editor.renderer.data.MainItemVO;
import games.rednblack.editor.view.ui.properties.UIRemovableProperties;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by azakhary on 8/12/2015.
 */
public class UIShaderProperties extends UIRemovableProperties {

    public static final String prefix = "games.rednblack.editor.view.ui.properties.panels.UIShaderProperties";
    public static final String CLOSE_CLICKED = prefix + ".CLOSE_CLICKED";

    public static final String EDIT_BUTTON_CLICKED = prefix + ".EDIT_BUTTON_CLICKED";
    public static final String EDIT_SHADER_DONE = prefix + ".EDIT_SHADER_DONE";
    public static final String UNIFORMS_BUTTON_CLICKED = prefix + ".UNIFORMS_BUTTON_CLICKED";

    private final VisSelectBox<String> shadersSelector, renderingLaterSelector;

    private final HashMap<String, MainItemVO.RenderingLayer> renderingLayerMap = new HashMap<>();

    public UIShaderProperties() {
        super("Custom Shader");

        shadersSelector = StandardWidgetsFactory.createSelectBox(String.class);
        shadersSelector.addListener(new SelectBoxChangeListener(getUpdateEventName()));

        renderingLaterSelector = StandardWidgetsFactory.createSelectBox(String.class);
        renderingLaterSelector.addListener(new SelectBoxChangeListener(getUpdateEventName()));
        renderingLayerMap.put("Screen", MainItemVO.RenderingLayer.SCREEN);
        renderingLayerMap.put("Screen Reading", MainItemVO.RenderingLayer.SCREEN_READING);

        renderingLaterSelector.setItems(renderingLayerMap.keySet().toArray(new String[0]));
    }

    public void initView(HashMap<String, ShaderProgram> shaders) {
        mainTable.clear();

        Array<String> shaderNames = new Array<>();
        shaderNames.add("Default");
        shaders.keySet().forEach(shaderNames::add);

        shadersSelector.setItems(shaderNames);

        mainTable.add(new VisLabel("Shader: ", Align.right)).padRight(5).width(75).right();
        mainTable.add(shadersSelector).width(100).left().row();

        TextButton editButton = StandardWidgetsFactory.createTextButton("Edit");
        editButton.addListener(new ButtonToNotificationListener(EDIT_BUTTON_CLICKED));
        mainTable.add(editButton).padTop(5).padRight(3);

        TextButton uniformsButton = StandardWidgetsFactory.createTextButton("Uniforms");
        uniformsButton.addListener(new ButtonToNotificationListener(UNIFORMS_BUTTON_CLICKED));
        mainTable.add(uniformsButton).padTop(5).row();

        mainTable.addSeparator().padTop(5).padBottom(5).colspan(2);
        mainTable.add(StandardWidgetsFactory.createLabel("Rendering Layer:")).padRight(5).right();
        mainTable.add(renderingLaterSelector).width(100).left().row();
    }

    @Override
    public void onRemove() {
        HyperLap2DFacade.getInstance().sendNotification(CLOSE_CLICKED);
    }

    public void setSelected(String currShaderName) {
        shadersSelector.setSelected(currShaderName);
    }

    public MainItemVO.RenderingLayer getRenderingLayer() {
        return renderingLayerMap.get(renderingLaterSelector.getSelected());
    }

    public void setRenderingLayer(MainItemVO.RenderingLayer layer) {
        for (Map.Entry<String, MainItemVO.RenderingLayer> me : renderingLayerMap.entrySet()) {
            if (me.getValue() == layer)
                renderingLaterSelector.setSelected(me.getKey());
        }
    }

    public String getShader() {
        return shadersSelector.getSelected();
    }
}
