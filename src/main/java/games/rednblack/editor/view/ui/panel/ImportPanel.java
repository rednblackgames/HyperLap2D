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

package games.rednblack.editor.view.ui.panel;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisProgressBar;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.utils.AssetsUtils;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.UIDraggablePanel;

import java.util.HashMap;

public class ImportPanel extends UIDraggablePanel {
    public static final String CLASS_NAME = "games.rednblack.editor.view.ui.panel.ImportDialog";

    public static final String BROWSE_BTN_CLICKED = CLASS_NAME + ".BROWSE_BTN_CLICKED";

    public static final String IMPORT_FAILED = CLASS_NAME + ".IMPORT_FAILED";

    private HyperLap2DFacade facade;

    private VisTable mainTable;
    private Image dropRegion;
    private VisLabel errorLabel;

    private VisProgressBar progressBar;

    private HashMap<Integer, String> typeNames = new HashMap<>();

    ImportPanel() {
        super("Import Resources");
        setMovable(true);
        setModal(false);
        addCloseButton();
        setStyle(VisUI.getSkin().get("box", WindowStyle.class));
        getTitleLabel().setAlignment(Align.left);

        setWidth(250);
        setHeight(100);

        facade = HyperLap2DFacade.getInstance();

        fillTypeNames();

        mainTable = new VisTable();

        add(mainTable).fill().expand();
        row();

        setDroppingView();

        errorLabel = new VisLabel("File you selected was too sexy to import");
        errorLabel.setColor(Color.RED);
        errorLabel.setWidth(260);
        errorLabel.setWrap(true);
        errorLabel.getColor().a = 0;
        errorLabel.setTouchable(Touchable.disabled);

        mainTable.add(errorLabel).width(260).pad(6);
        mainTable.row().pad(5);
    }

    private void fillTypeNames() {
        typeNames.clear();

        typeNames.put(AssetsUtils.TYPE_ANIMATION_PNG_SEQUENCE, "PNG Sequence Animation");
        typeNames.put(AssetsUtils.TYPE_BITMAP_FONT, "Bitmap Font");
        typeNames.put(AssetsUtils.TYPE_IMAGE, "Texture");
        typeNames.put(AssetsUtils.TYPE_TEXTURE_ATLAS, "Texture Atlas");
        typeNames.put(AssetsUtils.TYPE_PARTICLE_EFFECT, "Particle Effect");
        typeNames.put(AssetsUtils.TYPE_SPINE_ANIMATION, "Spine Animation");
        typeNames.put(AssetsUtils.TYPE_SPRITE_ANIMATION_ATLAS, "Animation Atlas Pack");
        typeNames.put(AssetsUtils.TYPE_TTF_FONT, "TTF Font");
        typeNames.put(AssetsUtils.TYPE_HYPERLAP2D_LIBRARY, "HyperLap2D Library");
        typeNames.put(AssetsUtils.TYPE_HYPERLAP2D_ACTION, "HyperLap2D Action");
        typeNames.put(AssetsUtils.TYPE_SHADER, "Shader");
        typeNames.put(AssetsUtils.TYPE_TALOS_VFX, "Talos VFX");
        typeNames.put(AssetsUtils.TYPE_TINY_VG, "TinyVG Image");
    }

    public static class DropBundle {
        public String[] paths;
        public Vector2 pos;
    }

    public Image getDropRegion() {
        return dropRegion;
    }

    public boolean checkDropRegionHit(Vector2 mousePos) {
        Vector2 pos = Sandbox.getInstance().getUIStage().getViewport().unproject(mousePos);
        pos = dropRegion.stageToLocalCoordinates(pos);
        if(dropRegion.hit(pos.x, pos.y, false) != null) {
            return true;
        }

        dropRegion.getColor().a = 0.3f;

        return false;
    }

    public void dragOver() {
        dropRegion.getColor().a = 0.5f;
    }

    public void dragExit() {
        dropRegion.getColor().a = 0.3f;
    }


    public void setDroppingView() {
        mainTable.clear();

        VisLabel helpLbl = new VisLabel("Supported file types: images, sprite animations (atlas or img sequence), spine animations, particle effects");
        helpLbl.setWidth(260);
        helpLbl.setWrap(true);
        mainTable.add(helpLbl).width(260).padLeft(5);
        mainTable.row().padBottom(5);

        dropRegion = new Image(VisUI.getSkin().getDrawable("dropHere"));
        mainTable.add(dropRegion).padRight(6).padBottom(6).padTop(10);
        mainTable.row().pad(5);

        mainTable.add(new VisLabel("or browse files on file system"));
        mainTable.row().pad(5);

        VisTextButton showFileSelectBtn = new VisTextButton("Browse");
        mainTable.add(showFileSelectBtn).width(88);
        mainTable.row().pad(5);

        initDropListeners(showFileSelectBtn);

        dragExit();
        pack();
    }

    public void setImportingView(int type, int count) {
        mainTable.clear();

        errorLabel.getColor().a = 0;
        errorLabel.clearActions();

        String typeText = typeNames.get(type);
        if(count > 1) typeText+=" (" + count + ")";

        mainTable.add(new VisLabel("Currently importing: " + typeText)).left();
        mainTable.row().padBottom(5);

        progressBar = new VisProgressBar(0, 100, 1, false);
        mainTable.add(progressBar).fillX().padTop(5).width(250);
        mainTable.row().padBottom(5);

        pack();
    }

    private void initDropListeners(VisTextButton browseBtn) {
        browseBtn.addListener(new ClickListener() {
            public void clicked (InputEvent event, float x, float y) {
                facade.sendNotification(BROWSE_BTN_CLICKED);
            }
        });
    }

    public void showError(int type) {
        String text = "";
        if(type == AssetsUtils.TYPE_UNSUPPORTED || type == AssetsUtils.TYPE_UNKNOWN) {
            text = "unsupported file type/types";
        }
        if(type == AssetsUtils.TYPE_MIXED) {
            text = "Multiple import types, please use one";
        }
        switch (type) {
            case AssetsUtils.TYPE_UNSUPPORTED:
            case AssetsUtils.TYPE_UNKNOWN:
                text = "Unsupported file type/types";
                break;
            case AssetsUtils.TYPE_MIXED:
                text = "Multiple import types, please use one";
                break;
            case AssetsUtils.TYPE_FAILED:
                text = "Import has failed";
                break;
        }

        errorLabel.setText(text);

        errorLabel.addAction(Actions.fadeIn(0.3f));
        dragExit();
    }

    public VisProgressBar getProgressBar() {
        return progressBar;
    }
}
