package games.rednblack.editor.proxy;

import games.rednblack.editor.view.stage.ItemSelector;
import games.rednblack.puremvc.Facade;
import games.rednblack.puremvc.Proxy;

/**
 * Exposes the editor's {@link ItemSelector} (selection state + alignment ops) as
 * a PureMVC proxy so callers can retrieve it via {@code facade.retrieveProxy(
 * SelectionProxy.NAME)} instead of {@code PluginUIBridge.get().getSandbox().getSelector()}
 * (Phase 3 decoupling). {@code Sandbox} creates the {@code ItemSelector} and
 * registers this wrapper in {@code init()}.
 */
public class SelectionProxy extends Proxy {

    public static final String NAME = SelectionProxy.class.getCanonicalName();

    private final ItemSelector selector;

    public SelectionProxy(ItemSelector selector) {
        super(NAME, selector);
        this.selector = selector;
    }

    public ItemSelector getSelector() {
        return selector;
    }

    /**
     * Retrieves the selection {@link ItemSelector} from the facade without going
     * through the {@code Sandbox} singleton. Assignment-typed retrieval so the
     * generic {@code retrieveProxy} infers {@code SelectionProxy} correctly.
     */
    public static ItemSelector get(Facade facade) {
        SelectionProxy proxy = facade.retrieveProxy(NAME);
        return proxy.getSelector();
    }
}