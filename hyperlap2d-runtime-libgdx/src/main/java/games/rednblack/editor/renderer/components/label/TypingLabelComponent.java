package games.rednblack.editor.renderer.components.label;

import com.rafaskoberg.gdx.typinglabel.TypingLabel;
import games.rednblack.editor.renderer.components.RemovableComponent;

public class TypingLabelComponent implements RemovableComponent {

    public TypingLabel typingLabel;

    @Override
    public void reset() {
        if (typingLabel != null)
            typingLabel.remove();
        typingLabel = null;
    }

    @Override
    public void onRemove() {
        reset();
    }
}
