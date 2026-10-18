package games.rednblack.editor.utils;

import com.badlogic.gdx.utils.Array;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * What the keymap panel reads: the actions are bare numbers everywhere else, so the names, the
 * groups and the written out combinations are the part worth pinning down.
 *
 * Nothing here calls {@code init()}, so the answers come from the default layout, which is what a
 * fresh editor shows.
 */
public class KeyBindingsLayoutTest {

    /** Every action constant declared on the class, by name. */
    private static Array<Field> actionConstants() throws Exception {
        Array<Field> fields = new Array<>();
        for (Field field : KeyBindingsLayout.class.getDeclaredFields()) {
            if (!Modifier.isPublic(field.getModifiers()) || !Modifier.isStatic(field.getModifiers())) continue;
            if (field.getType() != int.class) continue;
            fields.add(field);
        }
        return fields;
    }

    /** A new action must be given a name and a place, or it would be missing from the panel. */
    @Test
    public void everyActionIsListedOnceWithAName() throws Exception {
        Set<Integer> listed = new HashSet<>();
        for (KeyBindingsLayout.Binding binding : new Array.ArrayIterator<>(KeyBindingsLayout.getBindings())) {
            assertTrue("An action is listed twice: " + binding.name, listed.add(binding.action));
            assertFalse(binding.name.isEmpty());
            assertFalse(binding.group.isEmpty());
        }

        for (Field field : new Array.ArrayIterator<>(actionConstants())) {
            int action = field.getInt(null);
            assertTrue("No name for " + field.getName(), listed.contains(action));
        }
    }

    /** The groups read one after another: a group is never picked up again further down the list. */
    @Test
    public void theGroupsComeInOneRunEach() {
        Set<String> finished = new HashSet<>();
        String current = null;

        for (KeyBindingsLayout.Binding binding : new Array.ArrayIterator<>(KeyBindingsLayout.getBindings())) {
            if (binding.group.equals(current)) continue;

            assertTrue("The group comes back later: " + binding.group, finished.add(binding.group));
            current = binding.group;
        }
    }

    /** A combination is a key each, modifiers first, which is how the panel draws its chips. */
    @Test
    public void aCombinationIsSplitIntoItsKeys() {
        Array<String[]> redo = KeyBindingsLayout.getShortcutVariants(KeyBindingsLayout.REDO);

        assertEquals(1, redo.size);
        assertArrayEquals(new String[]{KeyBindingsLayout.getControlLabel(), "Shift", "Z"}, redo.get(0));
        assertEquals(KeyBindingsLayout.getControlLabel() + " Shift Z", KeyBindingsLayout.getShortcutText(KeyBindingsLayout.REDO));
        assertEquals("F12", KeyBindingsLayout.getShortcutText(KeyBindingsLayout.HIDE_GUI));
    }

    /** Two keys doing the same thing are one row with both on it, not two rows saying the same. */
    @Test
    public void theKeysThatDoTheSameThingShareARow() {
        Array<String[]> variants = KeyBindingsLayout.getShortcutVariants(KeyBindingsLayout.DELETE);

        assertEquals(2, variants.size);
        assertArrayEquals(new String[]{"Backspace"}, variants.get(0));
        assertArrayEquals(new String[]{"Delete"}, variants.get(1));
        assertEquals("Backspace or Delete", KeyBindingsLayout.getShortcutText(KeyBindingsLayout.DELETE));
    }

    /** A modifier belongs to every variant, so reading one never suggests a bare key. */
    @Test
    public void everyVariantCarriesTheModifiers() {
        Array<String[]> variants = KeyBindingsLayout.getShortcutVariants(KeyBindingsLayout.RESET_CAMERA);

        assertEquals(2, variants.size);
        for (String[] variant : new Array.ArrayIterator<>(variants)) {
            assertEquals(KeyBindingsLayout.getControlLabel(), variant[0]);
        }
    }

    /** A key a keyboard shows as a symbol is written as that symbol, not spelled out. */
    @Test
    public void theSymbolKeysAreWrittenAsSymbols() {
        String control = KeyBindingsLayout.getControlLabel();

        assertEquals(control + " + or " + control + " ]", KeyBindingsLayout.getShortcutText(KeyBindingsLayout.ZOOM_MINUS));
        assertEquals(control + " - or " + control + " /", KeyBindingsLayout.getShortcutText(KeyBindingsLayout.ZOOM_PLUS));
    }

    /** Two actions on the same keys would leave one of them unreachable. */
    @Test
    public void noTwoActionsAnswerToTheSameKeys() {
        Set<String> taken = new HashSet<>();
        for (KeyBindingsLayout.Binding binding : new Array.ArrayIterator<>(KeyBindingsLayout.getBindings())) {
            String shortcut = KeyBindingsLayout.getShortcutText(binding.action);
            if (shortcut.isEmpty()) continue;

            for (String variant : shortcut.split(" or ")) {
                assertTrue(binding.name + " shares " + variant.trim(), taken.add(variant.trim()));
            }
        }
    }
}
