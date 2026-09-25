package games.rednblack.editor.plugin.performance.widget;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import games.rednblack.editor.plugin.performance.data.Scope;

/**
 * Runtime, editor, or both. The one control the panel is built around: everything else answers to it.
 */
public class ScopeSelector extends VisTable {

    public interface Listener {
        void scopeChanged(Scope scope);
    }

    private final ButtonGroup<VisTextButton> group = new ButtonGroup<>();
    private Scope scope = Scope.BOTH;
    private Listener listener;

    public ScopeSelector() {
        for (final Scope option : Scope.values()) {
            VisTextButton button = new VisTextButton(option.label, "toggle");
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (!((VisTextButton) actor).isChecked() || scope == option) return;

                    scope = option;
                    if (listener != null) listener.scopeChanged(option);
                }
            });

            group.add(button);
            add(button).height(24f).padRight(1f);
        }

        group.setMaxCheckCount(1);
        group.setMinCheckCount(1);
        setScope(Scope.BOTH);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
        group.getButtons().get(scope.ordinal()).setChecked(true);
    }
}
