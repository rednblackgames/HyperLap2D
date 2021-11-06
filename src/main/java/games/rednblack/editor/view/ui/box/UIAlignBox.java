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

package games.rednblack.editor.view.ui.box;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

/**
 * Created by sargis on 4/10/15.
 */
public class UIAlignBox extends UICollapsibleBox {

    private static final String prefix = "games.rednblack.editor.view.ui.box.UIAlignBox.ALIGN_";

    //Simple Align
    private static final int ALIGN_TOP = 0;
    public static final String ALIGN_TOP_BTN_CLICKED = prefix + ALIGN_TOP;
    private static final int ALIGN_LEFT = 1;
    public static final String ALIGN_LEFT_BTN_CLICKED = prefix + ALIGN_LEFT;
    private static final int ALIGN_BOTTOM = 2;
    public static final String ALIGN_BOTTOM_BTN_CLICKED = prefix + ALIGN_BOTTOM;
    private static final int ALIGN_RIGHT = 3;
    public static final String ALIGN_RIGHT_BTN_CLICKED = prefix + ALIGN_RIGHT;

    //Center Align
    private static final int ALIGN_CENTER_VERTICAL = 4;
    public static final String ALIGN_CENTER_LEFT_BTN_CLICKED = prefix + ALIGN_CENTER_VERTICAL;
    private static final int ALIGN_CENTER_HORIZONTAL = 5;
    public static final String ALIGN_CENTER_BOTTOM_BTN_CLICKED = prefix + ALIGN_CENTER_HORIZONTAL;

    //At Edge Align
    private static final int ALIGN_AT_EDGE_TOP = 6;
    public static final String ALIGN_AT_EDGE_TOP_BTN_CLICKED = prefix + ALIGN_AT_EDGE_TOP;
    private static final int ALIGN_AT_EDGE_LEFT = 7;
    public static final String ALIGN_AT_EDGE_LEFT_BTN_CLICKED = prefix + ALIGN_AT_EDGE_LEFT;
    private static final int ALIGN_AT_EDGE_BOTTOM = 8;
    public static final String ALIGN_AT_EDGE_BOTTOM_BTN_CLICKED = prefix + ALIGN_AT_EDGE_BOTTOM;
    private static final int ALIGN_AT_EDGE_RIGHT = 9;
    public static final String ALIGN_AT_EDGE_RIGHT_BTN_CLICKED = prefix + ALIGN_AT_EDGE_RIGHT;

    //
    private final HyperLap2DFacade faced;

    public UIAlignBox() {
        super("Align", 166);
        setMovable(false);
        faced = HyperLap2DFacade.getInstance();
        VisTable alignButtonsTable = new VisTable();
        //
        alignButtonsTable.row().padTop(6);
        alignButtonsTable.add("Simple:").padRight(8).right();
        alignButtonsTable.add(createAlignButton("align-simple-up", ALIGN_TOP, "Top")).padRight(4).left();
        alignButtonsTable.add(createAlignButton("align-simple-left", ALIGN_LEFT, "Left")).padRight(4).left();
        alignButtonsTable.add(createAlignButton("align-simple-down", ALIGN_BOTTOM, "Bottom")).padRight(4).left();
        alignButtonsTable.add(createAlignButton("align-simple-right", ALIGN_RIGHT, "Right")).expandX().left();
        //
        alignButtonsTable.row().padTop(6);
        alignButtonsTable.add("Center:").padRight(8).right();
        alignButtonsTable.add(createAlignButton("align-center-vertical", ALIGN_CENTER_VERTICAL, "Vertical")).padRight(4).left();
        alignButtonsTable.add(createAlignButton("align-center-horisontal", ALIGN_CENTER_HORIZONTAL, "Horizontal")).expandX().left().colspan(3);
        //
        alignButtonsTable.row().padTop(6);
        alignButtonsTable.add("At Edge:").padRight(8).right();
        alignButtonsTable.add(createAlignButton("align-edge-right", ALIGN_AT_EDGE_LEFT, "Left")).padRight(4).left();
        alignButtonsTable.add(createAlignButton("align-edge-up", ALIGN_AT_EDGE_BOTTOM, "Bottom")).padRight(4).left();
        alignButtonsTable.add(createAlignButton("align-edge-left", ALIGN_AT_EDGE_RIGHT, "Right")).padRight(4).left();
        alignButtonsTable.add(createAlignButton("align-edge-down", ALIGN_AT_EDGE_TOP, "Top")).expandX().left();
        //
        createCollapsibleWidget(alignButtonsTable);
    }

    private VisImageButton createAlignButton(String styleName, int alignment, String title) {
        VisImageButton visImageButton = new VisImageButton(styleName);
        visImageButton.addListener(new AlignmentButtonClickListener(alignment));
        StandardWidgetsFactory.addTooltip(visImageButton, title);
        return visImageButton;
    }

    private static class AlignmentButtonClickListener extends ClickListener {

        private final int alignment;

        public AlignmentButtonClickListener(int alignment) {
            this.alignment = alignment;
        }

        @Override
        public void clicked(InputEvent event, float x, float y) {
            super.clicked(event, x, y);
            String notification = "";
            switch (alignment) {
                case ALIGN_TOP:
                    notification = ALIGN_TOP_BTN_CLICKED;
                    break;
                case ALIGN_LEFT:
                    notification = ALIGN_LEFT_BTN_CLICKED;
                    break;
                case ALIGN_BOTTOM:
                    notification = ALIGN_BOTTOM_BTN_CLICKED;
                    break;
                case ALIGN_RIGHT:
                    notification = ALIGN_RIGHT_BTN_CLICKED;
                    break;
                case ALIGN_CENTER_VERTICAL:
                    notification = ALIGN_CENTER_LEFT_BTN_CLICKED;
                    break;
                case ALIGN_CENTER_HORIZONTAL:
                    notification = ALIGN_CENTER_BOTTOM_BTN_CLICKED;
                    break;
                case ALIGN_AT_EDGE_TOP:
                    notification = ALIGN_AT_EDGE_TOP_BTN_CLICKED;
                    break;
                case ALIGN_AT_EDGE_LEFT:
                    notification = ALIGN_AT_EDGE_LEFT_BTN_CLICKED;
                    break;
                case ALIGN_AT_EDGE_BOTTOM:
                    notification = ALIGN_AT_EDGE_BOTTOM_BTN_CLICKED;
                    break;
                case ALIGN_AT_EDGE_RIGHT:
                    notification = ALIGN_AT_EDGE_RIGHT_BTN_CLICKED;
                    break;
            }
            HyperLap2DFacade facade = HyperLap2DFacade.getInstance();
            facade.sendNotification(notification);
        }
    }
}
