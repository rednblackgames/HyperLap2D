package games.rednblack.editor.renderer.components;

import com.badlogic.ashley.core.Component;

public interface RemovableComponent extends Component {
    void onRemove();
}
