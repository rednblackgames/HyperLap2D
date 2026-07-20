package games.rednblack.editor.proxy;

import games.rednblack.puremvc.Facade;
import games.rednblack.puremvc.Proxy;

import java.util.function.Supplier;

/**
 * Read access to the currently selected layer name, so commands
 * ({@code PasteItemsCommand}, {@code ConvertToCompositeCommand}) and
 * {@code PluginUIBridgeMediator} don't retrieve the view mediator
 * {@code UILayerBoxMediator} directly (Phase 1 back-edge cut).
 * The mediator sets a {@code Supplier} in {@code onRegister}; callers read via
 * {@link #get(Facade)}.
 */
public class LayerSelectionProxy extends Proxy {

    public static final String NAME = LayerSelectionProxy.class.getCanonicalName();

    private Supplier<String> layerNameSupplier;

    public LayerSelectionProxy() {
        super(NAME, null);
    }

    public void setLayerNameSupplier(Supplier<String> supplier) {
        this.layerNameSupplier = supplier;
    }

    public String getCurrentLayerName() {
        return layerNameSupplier != null ? layerNameSupplier.get() : null;
    }

    public static LayerSelectionProxy get(Facade facade) {
        return facade.retrieveProxy(NAME);
    }
}