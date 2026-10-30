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
import com.badlogic.gdx.graphics.Color;
import games.rednblack.editor.proxy.PluginUIBridge;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import games.rednblack.editor.controller.commands.EntityModifyRevertibleCommand;
import games.rednblack.editor.renderer.components.DimensionsComponent;
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

    String entityId;

    String prevFontName;
    int prevFontSize;
    int prevLabelAlign;
    int prevLineAlign;
    String prevText;
    Label.LabelStyle prevStyle;
    boolean prevWrap;
    boolean prevMono;
    boolean prevFitWidth;
    boolean prevFitHeight;
    /** The size it had before: fitting the text changes it, and undoing gives it back. */
    float prevWidth, prevHeight;
    String prevBitmapFont;
    float prevBorderWidth;
    final Color prevBorderColor = new Color();

    @Override
    public void doAction() {
        LabelDataPayload payload = getNotification().getBody();
        int entity = payload.entity();
        entityId = EntityUtils.getEntityId(entity);

        LabelComponent labelComponent = SandboxComponentRetriever.get(entity, LabelComponent.class);

        this.prevFontName = labelComponent.fontName;
        this.prevFontSize = labelComponent.fontSize;
        this.prevLabelAlign = labelComponent.labelAlign;
        this.prevLineAlign = labelComponent.lineAlign;
        this.prevStyle = labelComponent.getStyle();
        this.prevText = payload.prevText();
        this.prevWrap = labelComponent.wrap;
        this.prevMono = labelComponent.mono;
        this.prevFitWidth = labelComponent.fitWidth;
        this.prevFitHeight = labelComponent.fitHeight;
        this.prevWidth = SandboxComponentRetriever.get(entity, DimensionsComponent.class).width;
        this.prevHeight = SandboxComponentRetriever.get(entity, DimensionsComponent.class).height;
        this.prevBitmapFont = labelComponent.bitmapFont;
        this.prevBorderWidth = labelComponent.borderWidth;
        this.prevBorderColor.set(labelComponent.borderColor);

        labelComponent.fontName = payload.fontName();
        labelComponent.fontSize = payload.fontSize();
        labelComponent.setAlignment(payload.align());
        labelComponent.setText(payload.text());
        labelComponent.setWrap(payload.wrap());
        labelComponent.mono = payload.mono();
        labelComponent.fitWidth = payload.fitWidth();
        labelComponent.fitHeight = payload.fitHeight();
        labelComponent.bitmapFont = payload.bitmapFont();
        labelComponent.borderWidth = payload.borderWidth();
        labelComponent.borderColor.set(payload.borderColor());

        if (labelComponent.bitmapFont != null) {
            labelComponent.setStyle(getNewStyle(labelComponent.bitmapFont));
        } else {
            labelComponent.setStyle(getNewStyle(labelComponent));
        }

        facade.sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    private Label.LabelStyle getNewStyle(String fontName) {
        IResourceRetriever rm = PluginUIBridge.get().getSandbox().getSceneControl().sceneLoader.getRm();
        return LabelComponentFactory.generateStyle(rm, fontName);
    }

    private Label.LabelStyle getNewStyle(LabelComponent label) {

        IResourceRetriever rm = PluginUIBridge.get().getSandbox().getSceneControl().sceneLoader.getRm();
        int borderColor = Color.rgba8888(label.borderColor);
        final boolean hasBitmapFont = rm.getFont(label.fontName, label.fontSize, label.mono, label.borderWidth, borderColor) != null;

        if(!hasBitmapFont) {
            games.rednblack.editor.proxy.ResourceManager resourceManager = facade.retrieveProxy(games.rednblack.editor.proxy.ResourceManager.NAME);
            resourceManager.prepareEmbeddingFont(label.fontName, label.fontSize, label.mono, label.borderWidth, borderColor);
        }
        return LabelComponentFactory.generateStyle(rm, label);
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
        labelComponent.fitWidth = prevFitWidth;
        labelComponent.fitHeight = prevFitHeight;
        SandboxComponentRetriever.get(entity, DimensionsComponent.class).width = prevWidth;
        SandboxComponentRetriever.get(entity, DimensionsComponent.class).height = prevHeight;
        labelComponent.bitmapFont = prevBitmapFont;
        labelComponent.borderWidth = prevBorderWidth;
        labelComponent.borderColor.set(prevBorderColor);

        facade.sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }
}
