//
//  PureMVC Java Standard
//
//  Copyright(c) 2019 Saad Shams <saad.shams@puremvc.org>
//  Your reuse is governed by the Creative Commons Attribution 3.0 License
//

package org.puremvc.java.core;

import org.puremvc.java.interfaces.IModel;
import org.puremvc.java.interfaces.IProxy;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

/**
 * <P>A Singleton <code>IModel</code> implementation.</P>
 *
 * <P>In PureMVC, the <code>Model</code> class provides
 * access to model objects (Proxies) by named lookup.</P>
 *
 * <P>The <code>Model</code> assumes these responsibilities:</P>
 *
 * <UL>
 * <LI>Maintain a cache of <code>IProxy</code> instances.</LI>
 * <LI>Provide methods for registering, retrieving, and removing
 * <code>IProxy</code> instances.</LI>
 * </UL>
 *
 * <P>Your application must register <code>IProxy</code> instances
 * with the <code>Model</code>. Typically, you use an
 * <code>ICommand</code> to create and register <code>IProxy</code>
 * instances once the <code>Facade</code> has initialized the Core
 * actors.</P>
 *
 * @see org.puremvc.java.patterns.proxy.Proxy Proxy
 * @see org.puremvc.java.interfaces.IProxy IProxy
 */
public class Model implements IModel {

    // Mapping of proxyNames to IProxy instances
    protected ConcurrentMap<String, IProxy> proxyMap;

    // Singleton instance
    protected static IModel instance;

    // Message Constants
    protected final String SINGLETON_MSG = "Model Singleton already constructed!";

    /**
     * <P>Constructor.</P>
     *
     * <P>This <code>IModel</code> implementation is a Singleton,
     * so you should not call the constructor
     * directly, but instead call the static Singleton
     * Factory method <code>Model.getInstance()</code></P>
     *
     * @throws Error Error if Singleton instance has already been constructed
     *
     */
    public Model() {
        if(instance != null) throw new Error(SINGLETON_MSG);
        instance = this;
        proxyMap = new ConcurrentHashMap<>();
        initializeModel();
    }

    /**
     * <P>Initialize the Singleton <code>Model</code> instance.</P>
     *
     * <P>Called automatically by the constructor, this
     * is your opportunity to initialize the Singleton
     * instance in your subclass without overriding the
     * constructor.</P>
     *
     */
    protected void initializeModel() {
    }

    /**
     * <P><code>Model</code> Singleton Factory method.</P>
     *
     * @param factory model supplier function
     * @return the Singleton instance
     */
    public synchronized static IModel getInstance(Supplier<IModel> factory) {
        if(instance == null) instance = factory.get();
        return instance;
    }

    /**
     * <P>Register an <code>IProxy</code> with the <code>Model</code>.</P>
     *
     * @param proxy an <code>IProxy</code> to be held by the <code>Model</code>.
     */
    public void registerProxy(IProxy proxy) {
        proxyMap.put(proxy.getProxyName(), proxy);
        proxy.onRegister();
    }

    /**
     * <P>Retrieve an <code>IProxy</code> from the <code>Model</code>.</P>
     *
     * @param proxyName proxy name
     * @return the <code>IProxy</code> instance previously registered with the given <code>proxyName</code>.
     */
    public IProxy retrieveProxy(String proxyName) {
        return proxyMap.get(proxyName);
    }

    /**
     * <P>Check if a Proxy is registered</P>
     *
     * @param proxyName proxy name
     * @return whether a Proxy is currently registered with the given <code>proxyName</code>.
     */
    public boolean hasProxy(String proxyName) {
        return proxyMap.containsKey(proxyName);
    }

    /**
     * <P>Remove an <code>IProxy</code> from the <code>Model</code>.</P>
     *
     * @param proxyName name of the <code>IProxy</code> instance to be removed.
     * @return the <code>IProxy</code> that was removed from the <code>Model</code>
     */
    public IProxy removeProxy(String proxyName) {
        IProxy proxy = proxyMap.get(proxyName);
        if(proxy != null) {
            proxyMap.remove(proxyName);
            proxy.onRemove();
        }
        return proxy;
    }

}
