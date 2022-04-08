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

package games.rednblack.editor.controller.commands.component;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import games.rednblack.editor.controller.commands.EntityModifyRevertibleCommand;
import games.rednblack.editor.renderer.components.label.LabelComponent;
import games.rednblack.editor.renderer.factory.component.LabelComponentFactory;
import games.rednblack.editor.renderer.resources.IResourceRetriever;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.MsgAPI;

/**
 * Created by azakhary on 6/11/2015.
 *
 */
public class UpdateLabelDataCommand extends EntityModifyRevertibleCommand {

    Integer entityId;

    String prevFontName;
    int prevFontSize;
    int prevLabelAlign;
    int prevLineAlign;
    String prevText;
    Label.LabelStyle prevStyle;
    boolean prevWrap;
    boolean prevMono;
    String prevBitmapFont;

    @Override
    public void doAction() {
        Object[] payload = getNotification().getBody();
        int entity = (int) payload[0];
        entityId = EntityUtils.getEntityId(entity);

        LabelComponent labelComponent = SandboxComponentRetriever.get(entity, LabelComponent.class);

        this.prevFontName = labelComponent.fontName;
        this.prevFontSize = labelComponent.fontSize;
        this.prevLabelAlign = labelComponent.labelAlign;
        this.prevLineAlign = labelComponent.lineAlign;
        this.prevStyle = labelComponent.getStyle();
        this.prevText = (String) payload[5];
        this.prevWrap = labelComponent.wrap;
        this.prevMono = labelComponent.mono;
        this.prevBitmapFont = labelComponent.bitmapFont;

        labelComponent.fontName = (String) payload[1];
        labelComponent.fontSize = (int) payload[2];
        labelComponent.setAlignment((Integer) payload[3]);
        labelComponent.setText((String) payload[4]);
        labelComponent.setWrap((Boolean) payload[6]);
        labelComponent.mono = (Boolean) payload[7];
        labelComponent.bitmapFont = (String) payload[8];

        if (labelComponent.bitmapFont != null) {
            labelComponent.setStyle(getNewStyle(labelComponent.bitmapFont));
        } else {
            labelComponent.setStyle(getNewStyle(labelComponent.fontName, labelComponent.fontSize, labelComponent.mono));
        }

        facade.sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    private Label.LabelStyle getNewStyle(String fontName) {
        IResourceRetriever rm = Sandbox.getInstance().getSceneControl().sceneLoader.getRm();
        return LabelComponentFactory.generateStyle(rm, fontName);
    }

    private Label.LabelStyle getNewStyle(String fontName, int fontSize, boolean mono) {

        IResourceRetriever rm = Sandbox.getInstance().getSceneControl().sceneLoader.getRm();
        final boolean hasBitmapFont = rm.getFont(fontName, fontSize, mono) != null;

        if(!hasBitmapFont) {
            games.rednblack.editor.proxy.ResourceManager resourceManager = facade.retrieveProxy(games.rednblack.editor.proxy.ResourceManager.NAME);
            resourceManager.prepareEmbeddingFont(fontName, fontSize, mono);
        }
        return LabelComponentFactory.generateStyle(rm, fontName, fontSize, mono);
    }

    @Override
    public void undoAction() {
        final int entity = EntityUtils.getByUniqueId(entityId);
        final LabelComponent labelComponent = SandboxComponentRetriever.get(entity, LabelComponent.class);

        labelComponent.fontName = prevFontName;
        labelComponent.fontSize = prevFontSize;
        labelComponent.setAlignment(prevLabelAlign, prevLineAlign);
        labelComponent.setText(prevText);
        labelComponent.setStyle(prevStyle);
        labelComponent.setWrap(prevWrap);
        labelComponent.mono = prevMono;
        labelComponent.bitmapFont = prevBitmapFont;

        facade.sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }
}
