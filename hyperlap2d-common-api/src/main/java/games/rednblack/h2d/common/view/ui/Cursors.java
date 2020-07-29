package games.rednblack.h2d.common.view.ui;

import com.badlogic.gdx.graphics.Cursor;
import games.rednblack.h2d.common.vo.CursorData;

public class Cursors {
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
}
