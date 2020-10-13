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
package games.rednblack.h2d.common;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisImageButton;

/**
 * Created by azakhary on 5/12/2015.
 */
public class UIDraggablePanel extends H2DDialog {

    public boolean isOpen;

    public UIDraggablePanel(String title) {
        super(title);
        setMovable(true);
        setModal(false);
        setStyle(VisUI.getSkin().get("box", WindowStyle.class));
        getTitleLabel().setAlignment(Align.left);
        padTop(26);
        padLeft(10);
    }

    @Override
    public void addCloseButton() {
        VisImageButton closeButton = new VisImageButton("close-panel");
        this.getTitleTable().add(closeButton).padTop(1).padRight(-2);
        closeButton.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                close();
            }
        });
        closeButton.addListener(new ClickListener() {
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                event.cancel();
                return true;
            }
        });
    }

    public void invalidateHeight() {
        float heightOld = getHeight();
        pack();
        float heightDiff = heightOld - getHeight();
        setPosition(getX(), getY() + heightDiff);
    }

    @Override
    public VisDialog show(Stage stage) {
        isOpen = true;
        return super.show(stage);
    }

    @Override
    public void hide() {
        super.hide();
        isOpen = false;
    }

    @Override
    public void close() {
        super.close();
        isOpen = false;
    }
}
