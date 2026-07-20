package games.rednblack.editor.proxy;
import games.rednblack.editor.proxy.PluginUIBridge;

import games.rednblack.editor.renderer.ecs.BaseComponentMapper;
import games.rednblack.editor.renderer.ecs.Component;
import games.rednblack.editor.renderer.ecs.Engine;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.puremvc.Facade;
import games.rednblack.puremvc.Proxy;

/**
 * Read access to the runtime ECS for the view layer, extracted as a PureMVC
 * proxy (Phase 3 decoupling) so view code stops calling {@code SandboxComponentRetriever}
 * (a static wrapper over the Artemis {@code ComponentRetriever} that hid
 * {@code PluginUIBridge.get().getSandbox().getEngine()}). {@code Sandbox.init} registers this
 * with the editor {@link Engine}; callers retrieve it via {@link #get(Facade)}.
 *
 * <p>Write paths stay in commands — this proxy is read-only.</p>
 */
public class EntityDataProxy extends Proxy {

    public static final String NAME = EntityDataProxy.class.getCanonicalName();

    private final Engine engine;

    public EntityDataProxy(Engine engine) {
        super(NAME, engine);
        this.engine = engine;
    }

    public <T extends Component> T get(int entity, Class<T> type) {
        return ComponentRetriever.get(entity, type, engine);
    }

    public <T extends Component> BaseComponentMapper<T> getMapper(Class<T> type) {
        return ComponentRetriever.getMapper(type, engine);
    }

    /** The editor ECS engine (for non-component operations: delete, process, subscriptions). */
    public Engine getEngine() {
        return engine;
    }

    /** Retrieves the proxy from the facade (assignment-typed for generic inference). */
    public static EntityDataProxy get(Facade facade) {
        return facade.retrieveProxy(NAME);
    }

    /**
     * Retrieves the proxy via the facade singleton — for callers that don't hold a
     * {@code facade} reference (non-mediator actors such as followers/tools). The
     * {@code Facade.getInstance()} call lives here in the proxy layer, so it does not
     * count against the view-layer coupling freeze. Must be called after
     * {@code Sandbox.init} has registered this proxy.
     */
    public static EntityDataProxy get() {
        return Facade.getInstance().retrieveProxy(NAME);
    }
}