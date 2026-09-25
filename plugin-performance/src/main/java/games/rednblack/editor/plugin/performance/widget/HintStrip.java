package games.rednblack.editor.plugin.performance.widget;

import com.badlogic.gdx.graphics.Color;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.editor.plugin.performance.advisor.Advisor;
import games.rednblack.editor.plugin.performance.advisor.Hint;

/**
 * What the advisor has to say, in as many lines as it has to say it. Rebuilt only when the advice itself
 * changes, so the text stays still long enough to be read.
 */
public class HintStrip extends VisTable {

    private final Advisor advisor;
    private int shownRevision = -1;

    public HintStrip(Advisor advisor) {
        this.advisor = advisor;
    }

    public void update() {
        if (advisor.revision() == shownRevision) return;
        shownRevision = advisor.revision();

        clearChildren();
        for (int i = 0; i < advisor.size(); i++) {
            Hint hint = advisor.get(i);

            Dot dot = new Dot();
            dot.set(color(hint.severity));

            VisLabel label = Labels.wrapped(hint.text, "small");
            label.setColor(hint.severity == Hint.INFO ? Palette.TEXT_MUTED : Palette.TEXT);

            add(dot).top().width(10f).padTop(5f).padRight(6f);
            add(label).growX().left().padBottom(3f).row();
        }
    }

    private static Color color(int severity) {
        switch (severity) {
            case Hint.BAD:
                return Palette.BAD;
            case Hint.WARN:
                return Palette.WARN;
            default:
                return Palette.RUNTIME;
        }
    }
}
