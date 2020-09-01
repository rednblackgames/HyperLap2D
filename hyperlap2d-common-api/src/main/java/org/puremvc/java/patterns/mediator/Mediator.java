//
//  PureMVC Java Standard
//
//  Copyright(c) 2019 Saad Shams <saad.shams@puremvc.org>
//  Your reuse is governed by the Creative Commons Attribution 3.0 License
//

package org.puremvc.java.patterns.mediator;

import org.puremvc.java.interfaces.IMediator;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.observer.Notifier;

/**
 * <P>A base <code>IMediator</code> implementation.</P>
 *
 * @see org.puremvc.java.core.View View
 */
public class Mediator<V> extends Notifier implements IMediator<V> {

    /**
     * <P>The name of the <code>Mediator</code>.</P>
     *
     * <P>Typically, a <code>Mediator</code> will be written to serve
     * one specific control or group controls and so,
     * will not have a need to be dynamically named.</P>
     */
    public static final String NAME = "Mediator";

    // the mediator name
    protected String mediatorName;

    // The view component
    protected V viewComponent;

    /**
     * <P>Constructor.</P>
     *
     * @param mediatorName mediator name
     * @param viewComponent view component
     */
    public Mediator(String mediatorName, V viewComponent) {
        this.mediatorName = mediatorName != null ? mediatorName : NAME;
        this.viewComponent = viewComponent;
    }

    /**
     * <P>Constructor.</P>
     *
     * @param mediatorName mediator name
     */
    public Mediator(String mediatorName) {
        this(mediatorName, null);
    }

    /**
     * <P>Constructor.</P>
     */
    public Mediator() {
        this(null, null);
    }

    /**
     * <P>List the <code>INotification</code> names this
     * <code>Mediator</code> is interested in being notified of.</P>
     *
     * @return Array the list of <code>INotification</code> names
     */
    public String[] listNotificationInterests() {
        return new String[0];
    }

    /**
     * <P>Handle <code>INotification</code>s.</P>
     *
     * <P>Typically this will be handled in a switch statement,
     * with one 'case' entry per <code>INotification</code>
     * the <code>Mediator</code> is interested in.</P>
     */
    public void handleNotification(INotification notification) {

    }

    /**
     * <P>Called by the View when the Mediator is registered</P>
     */
    public void onRegister() {

    }

    /**
     * <P>Called by the View when the Mediator is removed</P>
     */
    public void onRemove() {

    }

    /**
     * <P>Get the name of the <code>Mediator</code>.</P>
     *
     * @return the Mediator name
     */
    public String getMediatorName() {
        return mediatorName;
    }

    /**
     * <P>Get the <code>Mediator</code>'s view component.</P>
     *
     * <P>Additionally, an implicit getter will usually
     * be defined in the subclass that casts the view
     * object to a type, like this:</P>
     *
     * {@code
     *		public javax.swing.JComboBox getViewComponent()
     *		{
     *			return viewComponent;
     *		}
     *}
     *
     * @return the view component
     */
    public V getViewComponent() {
        return viewComponent;
    }

    /**
     * <P>Set the <code>IMediator</code>'s view component.</P>
     *
     * @param viewComponent the view component
     */
    public void setViewComponent(V viewComponent) {
        this.viewComponent = viewComponent;
    }

}
