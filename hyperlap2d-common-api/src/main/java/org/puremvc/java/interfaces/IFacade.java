//
//  PureMVC Java Standard
//
//  Copyright(c) 2019 Saad Shams <saad.shams@puremvc.org>
//  Your reuse is governed by the Creative Commons Attribution 3.0 License
//

package org.puremvc.java.interfaces;

import java.util.function.Supplier;

/**
 * <P>The interface definition for a PureMVC Facade.</P>
 *
 * <P>The Facade Pattern suggests providing a single
 * class to act as a central point of communication
 * for a subsystem.</P>
 *
 * <P>In PureMVC, the Facade acts as an interface between
 * the core MVC actors (Model, View, Controller) and
 * the rest of your application.</P>
 *
 * @see IModel IModel
 * @see IView IView
 * @see org.puremvc.java.interfaces.IController IController
 * @see org.puremvc.java.interfaces.ICommand ICommand
 * @see org.puremvc.java.interfaces.INotification INotification
 */
public interface IFacade extends INotifier {

    /**
     * <P>Register an <code>IProxy</code> with the <code>Model</code> by name.</P>
     *
     * @param proxy the <code>IProxy</code> to be registered with the <code>Model</code>.
     */
    void registerProxy(IProxy proxy);

    /**
     * <P>Retrieve a <code>IProxy</code> from the <code>Model</code> by name.</P>
     *
     * @param proxyName the name of the <code>IProxy</code> instance to be retrieved.
     * @return the <code>IProxy</code> previously regisetered by <code>proxyName</code> with the <code>Model</code>.
     */
    <T extends IProxy> T retrieveProxy(String proxyName);

    /**
     * <P>Remove an <code>IProxy</code> instance from the <code>Model</code> by name.</P>
     *
     * @param proxyName the <code>IProxy</code> to remove from the <code>Model</code>.
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

    /**
     * <P>Register an <code>ICommand</code> with the <code>Controller</code>.</P>
     *
     * @param notificationName the name of the <code>INotification</code> to associate the <code>ICommand</code> with.
     * @param commandSupplier a reference to the Command Supplier Function of the <code>ICommand</code>.
     */
    void registerCommand(String notificationName, Supplier<ICommand> commandSupplier);

    /**
     * <P>Remove a previously registered <code>ICommand</code> to <code>INotification</code> mapping from the Controller.</P>
     *
     * @param notificationName the name of the <code>INotification</code> to remove the <code>ICommand</code> mapping for
     */
    void removeCommand(String notificationName);

    /**
     * <P>Check if a Command is registered for a given Notification</P>
     *
     * @param notificationName notification name
     * @return whether a Command is currently registered for the given <code>notificationName</code>.
     */
    boolean hasCommand(String notificationName);

    /**
     * <P>Register an <code>IMediator</code> instance with the <code>View</code>.</P>
     *
     * @param mediator a reference to the <code>IMediator</code> instance
     */
    void registerMediator(IMediator mediator);

    /**
     * <P>Retrieve an <code>IMediator</code> instance from the <code>View</code>.</P>
     *
     * @param mediatorName the name of the <code>IMediator</code> instance to retrievve
     * @return the <code>IMediator</code> previously registered with the given <code>mediatorName</code>.
     */
    <T extends IMediator> T retrieveMediator(String mediatorName);

    /**
     * <P>Remove a <code>IMediator</code> instance from the <code>View</code>.</P>
     *
     * @param mediatorName name of the <code>IMediator</code> instance to be removed.
     * @return the <code>IMediator</code> instance previously registered with the given <code>mediatorName</code>.
     */
    <T extends IMediator> T removeMediator(String mediatorName);

    /**
     * <P>Check if a Mediator is registered or not</P>
     *
     * @param mediatorName mediator name
     * @return whether a Mediator is registered with the given <code>mediatorName</code>.
     */
    boolean hasMediator(String mediatorName);

    /**
     * <P>Notify the <code>IObservers</code> for a particular <code>INotification</code>.</P>
     *
     * <P>All previously attached <code>IObservers</code> for this <code>INotification</code>'s
     * list are notified and are passed a reference to the <code>INotification</code> in
     * the order in which they were registered.</P>
     *
     * <P>NOTE: Use this method only if you are sending custom Notifications. Otherwise
     * use the sendNotification method which does not require you to create the
     * Notification instance.</P>
     *
     * @param notification the <code>INotification</code> to notify <code>IObservers</code> of.
     */
    void notifyObservers(INotification notification);
}
