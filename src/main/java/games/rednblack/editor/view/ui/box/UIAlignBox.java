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

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.puremvc.Facade;

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

    private static final float BUTTON_HEIGHT = 28f;
    private static final float TRACK_GAP = 8f;
    private static final float SIDE_PAD = 8f;
    private static final float CAPTION_INDENT = 2f;
    private static final float CAPTION_GAP = 3f;
    /**
     * The box lays its content out at the content's preferred width, so filling it does nothing: the rows are
     * given the box's width themselves.
     */
    private static final float ROW_WIDTH = LEFT_COLUMN_WIDTH - 2 * SIDE_PAD;

    private final Facade facade;
    private final Array<VisImageButton> buttons = new Array<>();

    public UIAlignBox() {
        super("Align", LEFT_COLUMN_WIDTH);
        setMovable(false);
        facade = Facade.getInstance();

        // segmented controls: each group is one track, its buttons the segments
        VisTable horizontal = track();
        horizontal.add(createAlignButton("align-left", ALIGN_LEFT_BTN_CLICKED, "Align left edges"));
        horizontal.add(createAlignButton("align-center", ALIGN_CENTER_LEFT_BTN_CLICKED, "Align horizontal centers"));
        horizontal.add(createAlignButton("align-right", ALIGN_RIGHT_BTN_CLICKED, "Align right edges"));

        VisTable vertical = track();
        vertical.add(createAlignButton("align-top", ALIGN_TOP_BTN_CLICKED, "Align top edges"));
        vertical.add(createAlignButton("align-middle", ALIGN_CENTER_BOTTOM_BTN_CLICKED, "Align vertical centers"));
        vertical.add(createAlignButton("align-bottom", ALIGN_BOTTOM_BTN_CLICKED, "Align bottom edges"));

        // each one butts the others against a side of the outermost item, which stays where it is
        VisTable nextTo = track();
        nextTo.add(createAlignButton("align-next-left", ALIGN_AT_EDGE_RIGHT_BTN_CLICKED, "Place right of the leftmost item"));
        nextTo.add(createAlignButton("align-next-right", ALIGN_AT_EDGE_LEFT_BTN_CLICKED, "Place left of the rightmost item"));
        nextTo.add(createAlignButton("align-next-up", ALIGN_AT_EDGE_BOTTOM_BTN_CLICKED, "Place under the topmost item"));
        nextTo.add(createAlignButton("align-next-down", ALIGN_AT_EDGE_TOP_BTN_CLICKED, "Place on top of the bottommost item"));

        float halfWidth = (ROW_WIDTH - TRACK_GAP) / 2f;
        VisTable alignRow = new VisTable();
        alignRow.add(caption("Horizontal")).left().padLeft(CAPTION_INDENT);
        alignRow.add(caption("Vertical")).left().padLeft(TRACK_GAP + CAPTION_INDENT).row();
        alignRow.add(horizontal).width(halfWidth).padTop(CAPTION_GAP);
        alignRow.add(vertical).width(halfWidth).padTop(CAPTION_GAP).padLeft(TRACK_GAP);

        VisTable content = new VisTable();
        content.add(alignRow).width(ROW_WIDTH).pad(6, SIDE_PAD, 0, SIDE_PAD).row();
        content.add(caption("Place next to")).left().padTop(TRACK_GAP).padLeft(SIDE_PAD + CAPTION_INDENT).row();
        content.add(nextTo).width(ROW_WIDTH).pad(CAPTION_GAP, SIDE_PAD, 6, SIDE_PAD).row();
        createCollapsibleWidget(content);

        // nothing is selected yet
        setAlignEnabled(false);
    }

    private static VisLabel caption(String text) {
        return new VisLabel(text, "property-label");
    }

    private static VisTable track() {
        VisTable track = new VisTable();
        track.setBackground("align-track");
        track.pad(2);
        track.defaults().height(BUTTON_HEIGHT).growX().minWidth(0).uniformX();
        return track;
    }

    private VisImageButton createAlignButton(String styleName, String notification, String title) {
        VisImageButton button = new VisImageButton(styleName);
        // a greyed out button shows no hover or press background, only its icon, greyed too
        VisImageButton.VisImageButtonStyle style = new VisImageButton.VisImageButtonStyle(button.getStyle());
        style.disabled = new BaseDrawable();
        button.setStyle(style);
        button.setGenerateDisabledImage(true);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                facade.sendNotification(notification);
            }
        });
        StandardWidgetsFactory.addTooltip(button, title);
        buttons.add(button);
        return button;
    }

    /** Aligning takes at least two items: with fewer selected the buttons are greyed out. */
    public void setAlignEnabled(boolean enabled) {
        for (VisImageButton button : buttons) button.setDisabled(!enabled);
    }
}
