package games.rednblack.editor.code;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.util.highlight.Highlight;
import com.kotcrab.vis.ui.util.highlight.HighlightRule;
import com.kotcrab.vis.ui.widget.HighlightTextArea;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultilineRegexHighlightRule implements HighlightRule {
    private Color color;
    private Pattern pattern;

    public MultilineRegexHighlightRule (Color color, String regex) {
        this.color = color;
        pattern = Pattern.compile(regex, Pattern.MULTILINE);
    }

    @Override
    public void process (HighlightTextArea textArea, Array<Highlight> highlights) {
        Matcher matcher = pattern.matcher(textArea.getText());
        while (matcher.find()) {
            highlights.add(new Highlight(color, matcher.start(), matcher.end()));
        }
    }
}
