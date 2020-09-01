//
//  PureMVC Java Standard
//
//  Copyright(c) 2019 Saad Shams <saad.shams@puremvc.org>
//  Your reuse is governed by the Creative Commons Attribution 3.0 License
//

package org.puremvc.java.interfaces;

/**
 * <P>The interface definition for a PureMVC View.</P>
 *
 * <P>In PureMVC, <code>IView</code> implementors assume these responsibilities:</P>
 *
 * <P>In PureMVC, the <code>View</code> class assumes these responsibilities:</P>
 *
 * <UL>
 * <LI>Maintain a cache of <code>IMediator</code> instances.</LI>
 * <LI>Provide methods for registering, retrieving, and removing <code>IMediators</code>.</LI>
 * <LI>Managing the observer lists for each <code>INotification</code> in the application.</LI>
 * <LI>Providing a method for attaching <code>IObservers</code> to an <code>INotification</code>'s observer list.</LI>
 * <LI>Providing a method for broadcasting an <code>INotification</code>.</LI>
 * <LI>Notifying the <code>IObservers</code> of a given <code>INotification</code> when it broadcast.</LI>
 * </UL>
 *
 * @see org.puremvc.java.interfaces.IMediator IMediator
 * @see org.puremvc.java.interfaces.IObserver IObserver
 * @see org.puremvc.java.interfaces.INotification INotification
 */
public interface IView {

    /**
     * <P>Register an <code>IObserver</code> to be notified
     * of <code>INotifications</code> with a given name.</P>
     *
     * @param notificationName the name of the <code>INotifications</code> to notify this <code>IObserver</code> of
     * @param observer the <code>IObserver</code> to register
     */
    void registerObserver(String notificationName, IObserver observer);

    /**
     * <P>Remove a group of observers from the observer list for a given Notification name.</P>
     *
     * @param notificationName which observer list to remove from
     * @param notifyContext removed the observers with this object as their notifyContext
     */
    void removeObserver(String notificationName, Object notifyContext);

    /**
     * <P>Notify the <code>IObservers</code> for a particular <code>INotification</code>.</P>
     *
     * <P>All previously attached <code>IObservers</code> for this <code>INotification</code>'s
     * list are notified and are passed a reference to the <code>INotification</code> in
     * the order in which they were registered.</P>
     *
     * @param notification the <code>INotification</code> to notify <code>IObservers</code> of.
     */
    void notifyObservers(INotification notification);

    /**
     * <P>Register an <code>IMediator</code> instance with the <code>View</code>.</P>
     *
     * <P>Registers the <code>IMediator</code> so that it can be retrieved by name,
     * and further interrogates the <code>IMediator</code> for its
     * <code>INotification</code> interests.</P>
     *
     * <P>If the <code>IMediator</code> returns any <code>INotification</code>
     * names to be notified about, an <code>Observer</code> is created encapsulating
     * the <code>IMediator</code> instance's <code>handleNotification</code> method
     * and registering it as an <code>Observer</code> for all <code>INotifications</code> the
     * <code>IMediator</code> is interested in.</p>
     *
     * @param mediator a reference to the <code>IMediator</code> instance
     */
    void registerMediator(IMediator mediator);

    /**
     * <P>Retrieve an <code>IMediator</code> from the <code>View</code>.</P>
     *
     * @param mediatorName the name of the <code>IMediator</code> instance to retrieve.
     * @return the <code>IMediator</code> instance previously registered with the given <code>mediatorName</code>.
     */
    <T extends IMediator> T retrieveMediator(String mediatorName);

    /**
     * <P>Remove an <code>IMediator</code> from the <code>View</code>.</P>
     *
     * @param mediatorName name of the <code>IMediator</code> instance to be removed.
     * @return the <code>IMediator</code> that was removed from the <code>View</code>
     */
    <T extends IMediator> T removeMediator(String mediatorName);

    /**
     * <P>Check if a Mediator is registered or not</P>
     *
     * @param mediatorName mediator name
     * @return whether a Mediator is registered with the given <code>mediatorName</code>.
     */
    boolean hasMediator(String mediatorName);
}
