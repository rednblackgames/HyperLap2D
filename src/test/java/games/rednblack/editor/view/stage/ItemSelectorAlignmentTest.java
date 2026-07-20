package games.rednblack.editor.view.stage;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the pure alignment math extracted from {@link ItemSelector}'s
 * eight duplicated {@code alignSelections*} methods. The math is verified
 * independently of the ECS engine; the {@code align(...)} loop that applies
 * these targets is covered by manual editor verification.
 */
public class ItemSelectorAlignmentTest {

    private static final float REF_LOW = 100f;
    private static final float REF_SIZE = 50f;   // reference high edge = 150
    private static final float ENT_SIZE = 30f;
    private static final float EPS = 0.0001f;

    @Test
    public void alignsLowEdgesToReferenceLowEdge() {
        // alignSelectionsByX(false) / alignSelectionsByY(false)
        float target = ItemSelector.alignTarget(REF_LOW, REF_SIZE, ENT_SIZE, false, false, false);
        assertEquals(REF_LOW, target, EPS);
    }

    @Test
    public void alignsHighEdgesToReferenceHighEdge() {
        // alignSelectionsByX(true) / alignSelectionsByY(true)
        float target = ItemSelector.alignTarget(REF_LOW, REF_SIZE, ENT_SIZE, true, true, false);
        assertEquals(REF_LOW + REF_SIZE - ENT_SIZE, target, EPS); // 120
    }

    @Test
    public void alignsRightEdgeToReferenceLeftEdge() {
        // alignSelectionsAtLeftEdge
        float target = ItemSelector.alignTarget(REF_LOW, REF_SIZE, ENT_SIZE, false, true, false);
        assertEquals(REF_LOW - ENT_SIZE, target, EPS); // 70
    }

    @Test
    public void alignsLeftEdgeToReferenceRightEdge() {
        // alignSelectionsAtRightEdge / alignSelectionsAtTopEdge
        float target = ItemSelector.alignTarget(REF_LOW, REF_SIZE, ENT_SIZE, true, false, false);
        assertEquals(REF_LOW + REF_SIZE, target, EPS); // 150
    }

    @Test
    public void alignsTopEdgeToReferenceBottomEdge() {
        // alignSelectionsAtBottomEdge
        float target = ItemSelector.alignTarget(REF_LOW, REF_SIZE, ENT_SIZE, false, true, false);
        assertEquals(REF_LOW - ENT_SIZE, target, EPS); // 70
    }

    @Test
    public void centersEntityOnReference() {
        // alignSelectionsVerticallyCentered / alignSelectionsHorizontallyCentered
        float target = ItemSelector.alignTarget(REF_LOW, REF_SIZE, ENT_SIZE, false, false, true);
        assertEquals(REF_LOW + (REF_SIZE - ENT_SIZE) / 2f, target, EPS); // 110
    }

    @Test
    public void centeredIgnoresEdgeFlags() {
        // centered mode must produce the same result regardless of edge flags
        float a = ItemSelector.alignTarget(REF_LOW, REF_SIZE, ENT_SIZE, true, true, true);
        float b = ItemSelector.alignTarget(REF_LOW, REF_SIZE, ENT_SIZE, false, false, true);
        assertEquals(a, b, EPS);
        assertEquals(REF_LOW + (REF_SIZE - ENT_SIZE) / 2f, a, EPS);
    }
}