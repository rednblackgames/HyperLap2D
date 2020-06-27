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
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.puremvc.patterns.proxy.BaseProxy;
import games.rednblack.h2d.common.vo.CursorData;

/**
 * Created by azakhary on 5/15/2015.
 */
public class CursorManager extends BaseProxy {
    private static final String TAG = CursorManager.class.getCanonicalName();
    public static final String NAME = TAG;

    public static CursorData NORMAL = new CursorData(Cursor.SystemCursor.Arrow);
    public static CursorData CROSS = new CursorData("cross", 14, 14);
    public static CursorData TEXT = new CursorData(Cursor.SystemCursor.Ibeam);
    public static CursorData TEXT_TOOL = new CursorData("label-tool", 15, 17);
    public static CursorData FINGER = new CursorData("fingerpoint", 16, 9);
    public static CursorData EYEDROPPER = new CursorData("eyedropper", 10, 23);
    public static CursorData HAND = new CursorData(Cursor.SystemCursor.Hand);

    public static CursorData ROTATION_LB = new CursorData("left-rotate-down", 15, 18);
    public static CursorData ROTATION_LT = new CursorData("left-rotate-up", 15, 15);
    public static CursorData ROTATION_RT = new CursorData("right-rotate-up", 18, 15);
    public static CursorData ROTATION_RB = new CursorData("right-rotate-down", 18, 18);

    public static CursorData TRANSFORM_LEFT_RIGHT = new CursorData("left-down-up", 17, 16);
    public static CursorData TRANSFORM_RIGHT_LEFT = new CursorData("right-down-up", 17, 16);
    public static CursorData TRANSFORM_HORIZONTAL = new CursorData("left-right", 17, 16);
    public static CursorData TRANSFORM_VERTICAL = new CursorData("up-down", 17, 16);

    private CursorData cursor;
    private CursorData overrideCursor = null;

    public CursorManager() {
        super(NAME);

        setCursor(NORMAL);
    }

    public void setCursor(CursorData cursor, TextureRegion region) {
        this.cursor = cursor;

        setCursorPixmap(region);
    }

    public void setCursor(CursorData cursor) {
        setCursor(cursor, null);
    }

    public void setOverrideCursor(CursorData cursor) {
        overrideCursor = cursor;
        setCursorPixmap(null);
    }

    public void removeOverrideCursor() {
        setOverrideCursor(null);
    }

    private void setCursorPixmap(TextureRegion region) {
        CursorData currentCursor = overrideCursor;
        if(currentCursor == null) {
            currentCursor = cursor;
        }

        if (currentCursor.systemCursor != null) {
            Gdx.graphics.setSystemCursor(currentCursor.systemCursor);
            return;
        }

        Pixmap cursorPm;
        if (region == null) {
            cursorPm = new Pixmap(Gdx.files.internal("cursors/" + currentCursor.region + ".png"));
        } else {
            Texture texture = region.getTexture();
            if (!texture.getTextureData().isPrepared()) {
                texture.getTextureData().prepare();
            }
            cursorPm = texture.getTextureData().consumePixmap();
        }

        Cursor cursorObj = Gdx.graphics.newCursor(cursorPm, currentCursor.getHotspotX(), currentCursor.getHotspotY());
        Gdx.graphics.setCursor(cursorObj);
        cursorPm.dispose();
    }
}
