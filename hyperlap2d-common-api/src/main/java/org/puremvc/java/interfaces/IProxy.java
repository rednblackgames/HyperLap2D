//
//  PureMVC Java Standard
//
//  Copyright(c) 2019 Saad Shams <saad.shams@puremvc.org>
//  Your reuse is governed by the Creative Commons Attribution 3.0 License
//

package org.puremvc.java.interfaces;

/**
 * <P>The interface definition for a PureMVC Proxy.</P>
 *
 * <P>In PureMVC, <code>IProxy</code> implementors assume these responsibilities:</P>
 *
 * <UL>
 * <LI>Implement a common method which returns the name of the Proxy.</LI>
 * <LI>Provide methods for setting and getting the data object.</LI>
 * </UL>
 *
 * <P>Additionally, <code>IProxy</code>s typically:</P>
 *
 * <UL>
 * <LI>Maintain references to one or more pieces of model data.</LI>
 * <LI>Provide methods for manipulating that data.</LI>
 * <LI>Generate <code>INotifications</code> when their model data changes.</LI>
 * <LI>Expose their name as a <code>public static const</code> called <code>NAME</code>, if they are not instantiated multiple times.</LI>
 * <LI>Encapsulate interaction with local or remote services used to fetch and persist model data.</LI>
 * </UL>
 */
public interface IProxy extends INotifier {

    /**
     * <P>Get the Proxy name</P>
     *
     * @return the Proxy instance name
     */
    String getProxyName();

    /**
     * <P>Set the data object</P>
     *
     * @param data the data object
     */
    void setData(Object data);

    /**
     * <P>Get the data object</P>
     *
     * @return the data as type Object
     */
    Object getData();

    /**
     * <P>Called by the Model when the Proxy is registered</P>
     */
    void onRegister();

    /**
     * <P>Called by the Model when the Proxy is removed</P>
     */
    void onRemove();
}
