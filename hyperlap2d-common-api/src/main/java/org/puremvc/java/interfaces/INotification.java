//
//  PureMVC Java Standard
//
//  Copyright(c) 2019 Saad Shams <saad.shams@puremvc.org>
//  Your reuse is governed by the Creative Commons Attribution 3.0 License
//

package org.puremvc.java.interfaces;

/**
 * <P>The interface definition for a PureMVC Notification.</P>
 *
 * <P>PureMVC does not rely upon underlying event models such
 * as the one provided with Flash, and ActionScript 3 does
 * not have an inherent event model.</P>
 *
 * <P>The Observer Pattern as implemented within PureMVC exists
 * to support event-driven communication between the
 * application and the actors of the MVC triad.</P>
 *
 * <P>Notifications are not meant to be a replacement for Events
 * in Flex/Flash/AIR. Generally, <code>IMediator</code> implementors
 * place event listeners on their view components, which they
 * then handle in the usual way. This may lead to the broadcast of <code>Notification</code>s to
 * trigger <code>ICommand</code>s or to communicate with other <code>IMediators</code>. <code>IProxy</code> and <code>ICommand</code>
 * instances communicate with each other and <code>IMediator</code>s
 * by broadcasting <code>INotification</code>s.</P>
 *
 * <P>A key difference between Flash <code>Event</code>s and PureMVC
 * <code>Notification</code>s is that <code>Event</code>s follow the
 * 'Chain of Responsibility' pattern, 'bubbling' up the display hierarchy
 * until some parent component handles the <code>Event</code>, while
 * PureMVC <code>Notification</code>s follow a 'Publish/Subscribe'
 * pattern. PureMVC classes need not be related to each other in a
 * parent/child relationship in order to communicate with one another
 * using <code>Notification</code>s.</P>
 *
 * @see IView IView
 * @see IObserver IObserver
 */
public interface INotification {

    /**
     * <P>Get the name of the <code>INotification</code> instance.
     * No setter, should be set by constructor only</P>
     *
     * @return notification name
     */
    String getName();

    /**
     * <P>Set the body of the <code>INotification</code> instance</P>
     *
     * @param body notification body
     */
    void setBody(Object body);

    /**
     * <P>Get the body of the <code>INotification</code> instance</P>
     *
     * @return notification body
     */
    <T> T getBody();

    /**
     * <P>Set the type of the <code>INotification</code> instance</P>
     *
     * @param type notification type
     */
    void setType(String type);

    /**
     * <P>Get the type of the <code>INotification</code> instance</P>
     *
     * @return notification type
     */
    String getType();

    /**
     * <P>Get the string representation of the <code>INotification</code> instance</P>
     *
     * @return string representation of <code>INotification</code>
     */
    String toString();

}
