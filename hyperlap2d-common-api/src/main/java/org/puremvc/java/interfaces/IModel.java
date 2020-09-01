//
//  PureMVC Java Standard
//
//  Copyright(c) 2019 Saad Shams <saad.shams@puremvc.org>
//  Your reuse is governed by the Creative Commons Attribution 3.0 License
//

package org.puremvc.java.interfaces;

/**
 * <P>The interface definition for a PureMVC Model.</P>
 *
 * <P>In PureMVC, <code>IModel</code> implementors provide
 * access to <code>IProxy</code> objects by named lookup. </P>
 *
 * <P>An <code>IModel</code> assumes these responsibilities:</P>
 *
 * <UL>
 * <LI>Maintain a cache of <code>IProxy</code> instances</LI>
 * <LI>Provide methods for registering, retrieving, and removing <code>IProxy</code> instances</LI>
 * </UL>
 */
public interface IModel {

    /**
     * <P>Register an <code>IProxy</code> instance with the <code>Model</code>.</P>
     *
     * @param proxy an object reference to be held by the <code>Model</code>.
     */
    void registerProxy(IProxy proxy);

    /**
     * <P>Retrieve an <code>IProxy</code> instance from the Model.</P>
     *
     * @param proxyName proxy name
     * @return the <code>IProxy</code> instance previously registered with the given <code>proxyName</code>.
     */
    <T extends IProxy> T retrieveProxy(String proxyName);

    /**
     * <P>Remove an <code>IProxy</code> instance from the Model.</P>
     *
     * @param proxyName name of the <code>IProxy</code> instance to be removed.
     * @return the <code>IProxy</code> that was removed from the <code>Model</code>
     */
    <T extends IProxy> T removeProxy(String proxyName);

    /**
     * <P>Check if a Proxy is registered</P>
     *
     * @param proxyName proxy name
     * @return whether a Proxy is currently registered with the given <code>proxyName</code>.
     */
    boolean hasProxy(String proxyName);
}
