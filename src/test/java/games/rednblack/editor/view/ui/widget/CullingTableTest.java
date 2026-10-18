package games.rednblack.editor.view.ui.widget;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CullingTableTest {

    @Before
    public void setUp() {
        //a table's cells reach for Gdx.files the first time one is made, and there is no application here
        if (Gdx.files == null) Gdx.files = new Lwjgl3Files();
    }

    @Test
    public void movesTheAreaIntoTheItemTableCoordinates() {
        CullingTable root = new CullingTable();
        Table items = new Table();
        root.add(items);
        root.setCullingTarget(items);

        //where the layout would put a tall table inside a top aligned root
        items.setPosition(0f, 120f);
        root.setCullingArea(new Rectangle(0f, 40f, 300f, 100f));

        root.applyCulling();

        Rectangle forwarded = items.getCullingArea();
        assertEquals(0f, forwarded.x, 0.001f);
        assertEquals(-80f, forwarded.y, 0.001f);
        assertEquals(300f, forwarded.width, 0.001f);
        assertEquals(100f, forwarded.height, 0.001f);
    }

    @Test
    public void addsUpThePositionsOfATargetSeveralLevelsDown() {
        //a ListView: the pane's table holds the list's own table, which holds the items
        CullingTable root = new CullingTable();
        Table scrollTable = new Table();
        Table items = new Table();
        root.add(scrollTable);
        scrollTable.add(items);
        root.setCullingTarget(items);

        scrollTable.setPosition(5f, 30f);
        items.setPosition(3f, 90f);
        root.setCullingArea(new Rectangle(0f, 40f, 300f, 100f));

        root.applyCulling();

        Rectangle forwarded = items.getCullingArea();
        assertEquals(-8f, forwarded.x, 0.001f);
        assertEquals(-80f, forwarded.y, 0.001f);
        assertEquals(300f, forwarded.width, 0.001f);
        assertEquals(100f, forwarded.height, 0.001f);
    }

    @Test
    public void leavesATargetAloneOnceItsBranchIsTakenOut() {
        CullingTable root = new CullingTable();
        Table scrollTable = new Table();
        Table items = new Table();
        root.add(scrollTable);
        scrollTable.add(items);
        root.setCullingTarget(items);

        root.clearChildren();
        root.setCullingArea(new Rectangle(0f, 0f, 300f, 100f));

        root.applyCulling();

        assertNull(items.getCullingArea());
    }

    @Test
    public void leavesTheItemTableAloneWhileThePlaceholderIsUp() {
        CullingTable root = new CullingTable();
        Table items = new Table();
        root.add(items);
        root.setCullingTarget(items);

        //what showEmptyHint does: the items table is taken out and the hint takes its place
        root.clearChildren();
        root.setCullingArea(new Rectangle(0f, 0f, 300f, 100f));

        root.applyCulling();

        assertNull(items.getCullingArea());
    }

    @Test
    public void doesNothingWithoutATarget() {
        CullingTable root = new CullingTable();
        root.setCullingArea(new Rectangle(0f, 0f, 300f, 100f));

        root.applyCulling();
    }
}
