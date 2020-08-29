package games.rednblack.editor.code.syntax;

import com.badlogic.gdx.graphics.Color;
import com.kotcrab.vis.ui.util.highlight.Highlighter;
import games.rednblack.editor.code.GroupRegexHighlightRule;

public class TypingLabelSyntax extends Highlighter {

    public TypingLabelSyntax() {
        String typingLabelToken = "EASE|ENDEASE|HANG|ENDHANG|JUMP|ENDJUMP|SHAKE|ENDSHAKE|SICK|ENDSICK|SLIDE|ENDSLIDE"
                + "|WAVE|ENDWAVE|WIND|ENDWIND|RAINBOW|ENDRAINBOW|GRADIENT|ENDGRADIENT|FADE|ENDFADE"
                + "|BLINK|ENDBLINK|WAIT|SPEED|SLOWER|SLOW|NORMAL|FAST|FASTER|COLOR|CLEARCOLOR|ENDCOLOR"
                + "|VAR|EVENT|RESET|SKIP";
        GroupRegexHighlightRule tokenRegex = new GroupRegexHighlightRule("\\{(" + typingLabelToken + ")(=(.*?))?\\}")
                .colors(Color.valueOf("66CCB3"), Color.valueOf("BED6FF"))
                .groups(1, 3);
        addRule(tokenRegex);
    }
}
