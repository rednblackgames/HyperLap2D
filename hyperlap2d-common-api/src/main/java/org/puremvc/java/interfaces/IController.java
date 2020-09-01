//
//  PureMVC Java Standard
//
//  Copyright(c) 2019 Saad Shams <saad.shams@puremvc.org>
//  Your reuse is governed by the Creative Commons Attribution 3.0 License
//

package org.puremvc.java.interfaces;

import java.util.function.Supplier;

/**
 * <P>The interface definition for a PureMVC Controller.</P>
 *
 * <P>In PureMVC, an <code>IController</code> implementor
 * follows the 'Command and Controller' strategy, and
 * assumes these responsibilities:</P>
 *
 * <UL>
 * <LI> Remembering which <code>ICommand</code>s
 * are intended to handle which <code>INotifications</code>.</LI>
 * <LI> Registering itself as an <code>IObserver</code> with
 * the <code>View</code> for each <code>INotification</code>
 * that it has an <code>ICommand</code> mapping for.</LI>
 * <LI> Creating a new instance of the proper <code>ICommand</code>
 * to handle a given <code>INotification</code> when notified by the <code>View</code>.</LI>
 * <LI> Calling the <code>ICommand</code>'s <code>execute</code>
 * method, passing in the <code>INotification</code>.</LI>
 * </UL>
 *
 * @see org.puremvc.java.interfaces INotification
 * @see org.puremvc.java.interfaces ICommand
 */
public interface IController {

    /**
     * <P>Register a particular <code>ICommand</code> class as the handler
     * for a particular <code>INotification</code>.</P>
     *
     * @param notificationName the name of the <code>INotification</code>
     * @param commandSupplier the Supplier Function of the <code>ICommand</code>
     */
    void registerCommand(String notificationName, Supplier<ICommand> commandSupplier);

    /**
     * <P>Execute the <code>ICommand</code> previously registered as the
     * handler for <code>INotification</code>s with the given notification name.</P>
     *
     * @param notification the <code>INotification</code> to execute the associated <code>ICommand</code> for
     */
    void executeCommand(INotification notification);

    /**
     * <P>Remove a previously registered <code>ICommand</code> to <code>INotification</code> mapping.</P>
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
}
