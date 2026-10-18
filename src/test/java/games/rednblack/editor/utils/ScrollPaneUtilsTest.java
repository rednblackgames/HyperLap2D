package games.rednblack.editor.utils;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Proxy;

import static org.junit.Assert.assertEquals;

public class ScrollPaneUtilsTest {

    private static final int ROWS = 10;
    private static final float ROW_HEIGHT = 50f;

    private final Actor[] rows = new Actor[ROWS];
    private ScrollPane pane;

    @Before
    public void setUp() {
        //a table's cells reach for Gdx.files the first time one is made, and there is no application here
        if (Gdx.files == null) Gdx.files = new Lwjgl3Files();
        //a scroll pane's gesture listener makes a Timer task, which only asks that an application exists
        if (Gdx.app == null) Gdx.app = (Application) Proxy.newProxyInstance(Application.class.getClassLoader(),
                new Class<?>[]{Application.class}, (proxy, method, args) -> defaultValue(method.getReturnType()));

        //the rows two tables down, the way a ListView keeps them inside the pane
        Table items = new Table();
        for (int i = 0; i < ROWS; i++) {
            rows[i] = new Actor();
            items.add(rows[i]).size(100f, ROW_HEIGHT).row();
        }
        Table content = new Table();
        content.top().left();
        content.add(items).growX();

        pane = new ScrollPane(content, new ScrollPane.ScrollPaneStyle());
        pane.setSize(100f, 100f);
        pane.layout();
    }

    private static Object defaultValue(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0f;
        if (type == double.class) return 0d;
        return null;
    }

    @Test
    public void reachesTheFirstRowFromTheBottom() {
        pane.setScrollY(pane.getMaxY());

        ScrollPaneUtils.scrollIntoView(pane, rows[0]);

        assertEquals(0f, pane.getScrollY(), 0.001f);
    }

    @Test
    public void reachesTheLastRowFromTheTop() {
        ScrollPaneUtils.scrollIntoView(pane, rows[ROWS - 1]);

        assertEquals(pane.getMaxY(), pane.getScrollY(), 0.001f);
    }

    @Test
    public void goingDownStopsWithTheRowOnTheBottomEdge() {
        ScrollPaneUtils.scrollIntoView(pane, rows[4]);

        //row 4 spans 200..250 from the top, the view is 100 tall
        assertEquals(150f, pane.getScrollY(), 0.001f);
    }

    @Test
    public void goingUpStopsWithTheRowOnTheTopEdge() {
        pane.setScrollY(pane.getMaxY());

        ScrollPaneUtils.scrollIntoView(pane, rows[4]);

        assertEquals(200f, pane.getScrollY(), 0.001f);
    }

    @Test
    public void leavesARowAlreadyInSightWhereItIs() {
        pane.setScrollY(180f);

        ScrollPaneUtils.scrollIntoView(pane, rows[4]);

        assertEquals(180f, pane.getScrollY(), 0.001f);
    }

    @Test
    public void ignoresAnActorOutsideThePane() {
        pane.setScrollY(120f);

        ScrollPaneUtils.scrollIntoView(pane, new Actor());

        assertEquals(120f, pane.getScrollY(), 0.001f);
    }
}
