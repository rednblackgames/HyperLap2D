package games.rednblack.editor.data.migrations;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import games.rednblack.editor.data.migrations.migrators.VersionMigTo110;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class VersionMigTo110Test {

    /** A button of the old system, sitting in a library item to also cover the walk down the tree. */
    private static final String OLD_PROJECT = "{\"libraryItems\":{\"playButton\":{"
            + "\"uniqueId\":\"btn\",\"tags\":[\"button\",\"ui\"],"
            + "\"layers\":[{\"layerName\":\"Default\",\"isVisible\":true},"
            + "{\"layerName\":\"normal\",\"isVisible\":true},"
            + "{\"layerName\":\"pressed\",\"isVisible\":true}],"
            + "\"content\":{\"SimpleImageVO\":["
            + "{\"class\":\"SimpleImageVO\",\"uniqueId\":\"bg\",\"layerName\":\"Default\",\"zIndex\":0,\"imageName\":\"bg\"},"
            + "{\"class\":\"SimpleImageVO\",\"uniqueId\":\"up\",\"layerName\":\"normal\",\"zIndex\":0,\"imageName\":\"btn-up\"},"
            + "{\"class\":\"SimpleImageVO\",\"uniqueId\":\"down\",\"layerName\":\"pressed\",\"zIndex\":0,\"imageName\":\"btn-down\"}],"
            + "\"LabelVO\":[{\"class\":\"LabelVO\",\"uniqueId\":\"caption\",\"layerName\":\"normal\",\"zIndex\":1,\"text\":\"Play\"}]}"
            + "}}}";

    private JsonValue root;
    private JsonValue button;

    @Before
    public void setUp() {
        root = new JsonReader().parse(OLD_PROJECT);
        assertTrue(VersionMigTo110.migrate(root));
        button = root.get("libraryItems").get("playButton");
    }

    private JsonValue child(String uniqueId) {
        for (JsonValue group = button.get("content").child; group != null; group = group.next) {
            for (JsonValue item = group.child; item != null; item = item.next) {
                if (uniqueId.equals(item.getString("uniqueId", ""))) return item;
            }
        }
        return null;
    }

    private String hiddenIn(JsonValue child, String state) {
        JsonValue part = child.get("widgetPart");
        if (part == null) return null;
        JsonValue stateNode = part.get("overrides").get(state);
        return stateNode == null ? null : stateNode.get("values").getString("visible", null);
    }

    @Test
    public void theCompositeBecomesAButtonWidget() {
        JsonValue widget = button.get("widget");
        assertNotNull(widget);
        assertEquals("button", widget.getString("widgetType"));
        assertArrayEquals(new String[]{"normal", "hover", "pressed", "checked", "disabled"},
                widget.get("states").asStringArray());
    }

    @Test
    public void theButtonTagIsGoneAndTheOthersStay() {
        assertArrayEquals(new String[]{"ui"}, button.get("tags").asStringArray());
    }

    @Test
    public void theLayersOfTheOldSystemAreGone() {
        assertEquals(1, button.get("layers").size);
        assertEquals("Default", button.get("layers").child.getString("layerName"));

        for (String id : new String[]{"bg", "up", "down", "caption"}) {
            assertEquals(id, "Default", child(id).getString("layerName"));
        }
    }

    @Test
    public void theDrawingOrderIsKept() {
        // used to be Default, then normal, then pressed, and inside a layer by index
        assertEquals(0, child("bg").getInt("zIndex"));
        assertEquals(1, child("up").getInt("zIndex"));
        assertEquals(2, child("caption").getInt("zIndex"));
        assertEquals(3, child("down").getInt("zIndex"));
    }

    @Test
    public void whatTheLayersMeantIsNowWrittenOnTheChildren() {
        assertEquals("false", hiddenIn(child("up"), "pressed"));
        assertEquals("false", hiddenIn(child("up"), "checked"));
        assertNull(hiddenIn(child("up"), "hover"));

        assertEquals("false", hiddenIn(child("down"), "normal"));
        assertEquals("false", hiddenIn(child("down"), "hover"));
        assertEquals("false", hiddenIn(child("down"), "disabled"));
        assertNull(hiddenIn(child("down"), "pressed"));

        // a child of any other layer was never touched by the old system
        assertNull(child("bg").get("widgetPart"));
    }

    @Test
    public void runningItAgainChangesNothing() {
        String migrated = root.toJson(com.badlogic.gdx.utils.JsonWriter.OutputType.json);

        assertFalse(VersionMigTo110.migrate(root));
        assertEquals(migrated, root.toJson(com.badlogic.gdx.utils.JsonWriter.OutputType.json));
    }

    @Test
    public void anythingThatIsNotAnOldButtonIsLeftAlone() {
        JsonValue plain = new JsonReader().parse("{\"composite\":{\"tags\":[\"ui\"],\"layers\":[{\"layerName\":\"normal\"}],"
                + "\"content\":{\"SimpleImageVO\":[{\"uniqueId\":\"a\",\"layerName\":\"normal\",\"zIndex\":0}]}}}");
        String before = plain.toJson(com.badlogic.gdx.utils.JsonWriter.OutputType.json);

        assertFalse(VersionMigTo110.migrate(plain));
        assertEquals(before, plain.toJson(com.badlogic.gdx.utils.JsonWriter.OutputType.json));
    }
}
