//
//  PureMVC Java Standard
//
//  Copyright(c) 2019 Saad Shams <saad.shams@puremvc.org>
//  Your reuse is governed by the Creative Commons Attribution 3.0 License
//

package org.puremvc.java.patterns.proxy;

import org.puremvc.java.interfaces.IProxy;
import org.puremvc.java.patterns.observer.Notifier;

/**
 * <P>A base <code>IProxy</code> implementation.</P>
 *
 * <P>In PureMVC, <code>Proxy</code> classes are used to manage parts of the
 * application's data model.</P>
 *
 * <P>A <code>Proxy</code> might simply manage a reference to a local data
 * object, in which case interacting with it might involve setting and getting
 * of its data in synchronous fashion.</P>
 *
 * <P><code>Proxy</code> classes are also used to encapsulate the application's
 * interaction with remote services to save or retrieve data, in which case, we
 * adopt an asyncronous idiom; setting data (or calling a method) on the
 * <code>Proxy</code> and listening for a <code>Notification</code> to be
 * sent when the <code>Proxy</code> has retrieved the data from the service.</P>
 *
 * @see org.puremvc.java.core.Model Model
 */
public class Proxy extends Notifier implements IProxy {

    // the proxy name
    public static final String NAME = "Proxy";

    // the data object
    protected String proxyName;

    // the data object
    protected Object data;

    /**
     * <P>Constructor</P>
     *
     * @param proxyName proxy name
     * @param data data object
     */
    public Proxy(String proxyName, Object data) {
        this.proxyName = (proxyName != null) ? proxyName : NAME;
        if(data != null) setData(data);
    }

    /**
     * <P>Constructor</P>
     *
     * @param proxyName Name of the <code>Proxy</code>
     */
    public Proxy(String proxyName) {
        this(proxyName, null);
    }

    /**
     * <P>Constructor</P>
     */
    public Proxy(){
        this(null, null);
    }

    /**
     * <P>Called by the Model when the Proxy is registered</P>
     */
    public void onRegister() {

    }

    /**
     * <P>Called by the Model when the Proxy is removed</P>
     */
    public void onRemove() {

    }

    /**
     * <P>Get the proxy name</P>
     *
     * @return the proxy name
     */
    public String getProxyName() {
        return proxyName;
    }

    /**
     * <P>Get the data object</P>
     *
     * @return the data object
     */
    public Object getData() {
        return data;
    }

    /**
     * <P>Set the data object</P>
     *
     * @param data data object
     */
    public void setData(Object data) {
        this.data = data;
    }

}
