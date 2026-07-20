package games.rednblack.editor.proxy;

import games.rednblack.puremvc.Facade;
import games.rednblack.puremvc.Proxy;

import java.util.SortedSet;

/**
 * Read access to the resources-panel multi-selection set, so commands
 * (e.g. {@code DeleteMultipleResources}) don't retrieve the view mediator
 * {@code BoxItemResourceSelectionUIMediator} directly (Phase 1 back-edge cut).
 * The mediator sets the set reference in {@code onRegister}; callers read via
 * {@link #get(Facade)}.
 */
public class ResourceSelectionProxy extends Proxy {

    public static final String NAME = ResourceSelectionProxy.class.getCanonicalName();

    private SortedSet<String> selectedResources;

    public ResourceSelectionProxy() {
        super(NAME, null);
    }

    public void setSelectedResources(SortedSet<String> selectedResources) {
        this.selectedResources = selectedResources;
    }

    public SortedSet<String> getSelectedResources() {
        return selectedResources;
    }

    public static ResourceSelectionProxy get(Facade facade) {
        return facade.retrieveProxy(NAME);
    }
}