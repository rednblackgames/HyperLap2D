package games.rednblack.editor.code;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.util.highlight.Highlight;
import com.kotcrab.vis.ui.util.highlight.HighlightRule;
import com.kotcrab.vis.ui.widget.HighlightTextArea;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Highlighter rule using regex to detect text matches and select a single group
 */
public class GroupRegexHighlightRule implements HighlightRule {

    private Color[] colors;
    private Pattern pattern;
    private int[] groups;

    public GroupRegexHighlightRule (String regex) {
        pattern = Pattern.compile(regex);
    }

    public GroupRegexHighlightRule colors(Color... colors) {
        this.colors = colors;
        return this;
    }

    public GroupRegexHighlightRule groups(int... groups) {
        this.groups = groups;
        return this;
    }

    @Override
    public void process (HighlightTextArea textArea, Array<Highlight> highlights) {
        Matcher matcher = pattern.matcher(textArea.getText());
        while (matcher.find()) {
            for (int i = 0; i < groups.length; i++) {
                int group = groups[i];
                Color color = colors[i];

                if (group <= matcher.groupCount() && matcher.start(group) < matcher.end(group))
                    highlights.add(new Highlight(color, matcher.start(group), matcher.end(group)));
            }
        }
    }
}
