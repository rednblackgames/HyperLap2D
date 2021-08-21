package games.rednblack.h2d.extension.typinglabel;

import com.artemis.PooledComponent;
import com.rafaskoberg.gdx.typinglabel.TypingLabel;
import games.rednblack.editor.renderer.components.RemovableObject;

public class TypingLabelComponent extends PooledComponent implements RemovableObject {

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
