//
//  PureMVC Java Standard
//
//  Copyright(c) 2019 Saad Shams <saad.shams@puremvc.org>
//  Your reuse is governed by the Creative Commons Attribution 3.0 License
//

package org.puremvc.java.interfaces;

/**
 * <P>The interface definition for a PureMVC Mediator.</P>
 *
 * <P>In PureMVC, <code>IMediator</code> implementors assume these responsibilities:</P>
 *
 * <UL>
 * <LI>Implement a common method which returns a list of all <code>INotification</code>s
 * the <code>IMediator</code> has interest in.</LI>
 * <LI>Implement a notification callback method.</LI>
 * <LI>Implement methods that are called when the IMediator is registered or removed from the View.</LI>
 * </UL>
 *
 * <P>Additionally, <code>IMediator</code>s typically:</P>
 *
 * <UL>
 * <LI>Act as an intermediary between one or more view components such as text boxes or
 * list controls, maintaining references and coordinating their behavior.</LI>
 * <LI>In Flash-based apps, this is often the place where event listeners are
 * added to view components, and their handlers implemented.</LI>
 * <LI>Respond to and generate <code>INotifications</code>, interacting with of
 * the rest of the PureMVC app.</LI>
 * </UL>
 *
 * <P>When an <code>IMediator</code> is registered with the <code>IView</code>,
 * the <code>IView</code> will call the <code>IMediator</code>'s
 * <code>listNotificationInterests</code> method. The <code>IMediator</code> will
 * return an <code>Array</code> of <code>INotification</code> names which
 * it wishes to be notified about.</P>
 *
 * <P>The <code>IView</code> will then create an <code>Observer</code> object
 * encapsulating that <code>IMediator</code>'s (<code>handleNotification</code>) method
 * and register it as an Observer for each <code>INotification</code> name returned by
 * <code>listNotificationInterests</code>.</P>
 *
 * <P>A concrete IMediator implementor usually looks something like this:</P>
 *
 * <pre>
 * {@code import org.puremvc.as3.multicore.patterns.mediator.*;
 * import org.puremvc.as3.multicore.patterns.observer.*;
 * import org.puremvc.as3.multicore.core.view.*;
 *
 * import com.me.myapp.model.*;
 * import com.me.myapp.view.*;
 * import com.me.myapp.controller.*;
 *
 * import javax.swing.JComboBox;
 * import java.awt.event.ActionListener;
 *
 * public class MyMediator extends Mediator implements IMediator, ActionListener {
 *
 *     public MyMediator( Object viewComponent ) {
 *         super( viewComponent );
 *         combo.addActionListener( this );
 *     }
 *
 *     public String[] listNotificationInterests() {
 *         return [ MyFacade.SET_SELECTION,
 *                  MyFacade.SET_DATAPROVIDER ];
 *     }
 *
 *     public void handleNotification( INotification notification ) {
 *         switch ( notification.getName() ) {
 *             case MyFacade.SET_SELECTION:
 *                 setSelection(notification);
 *                 break;
 *             case MyFacade.SET_DATAPROVIDER:
 *                 setDataProvider(notification);
 *                 break;
 *         }
 *     }
 *
 *     // Set the data provider of the combo box
 *     protected void setDataProvider( INotification notification ) {
 *         combo.setModel(ComboBoxModel<String>(notification.getBody()));
 *     }
 *
 *     // Invoked when the combo box dispatches a change event, we send a
 *     // notification with the
 *     public void actionPerformed(ActionEvent event) {
 *         sendNotification( MyFacade.MYCOMBO_CHANGED, this );
 *     }
 *
 *     // A private getter for accessing the view object by class
 *     protected JComboBox getViewComponent() {
 *         return viewComponent;
 *     }
 *
 * }
 * }
 * </pre>
 *
 * @see org.puremvc.java.interfaces.INotification INotification
 */
public interface IMediator<V> extends INotifier {

    /**
     * <P>Get the <code>IMediator</code> instance name</P>
     *
     * @return the <code>IMediator</code> instance name
     */
    String getMediatorName();

    /**
     * <P>Get the <code>IMediator</code>'s view component.</P>
     *
     * @return Object the view component
     */
    V getViewComponent();

    /**
     * <P>Set the <code>IMediator</code>'s view component.</P>
     *
     * @param viewComponent the view component
     */
    void setViewComponent(V viewComponent);

    /**
     * <P>List <code>INotification</code> interests.</P>
     *
     * @return an <code>Array</code> of the <code>INotification</code> names this <code>IMediator</code> has an interest in.
     */
    String[] listNotificationInterests();

    /**
     * <P>Handle an <code>INotification</code>.</P>
     *
     * @param notification the <code>INotification</code> to be handled
     */
    void handleNotification(INotification notification);

    /**
     * <P>Called by the View when the Mediator is registered</P>
     */
    void onRegister();

    /**
     * <P>Called by the View when the Mediator is removed</P>
     */
    void onRemove();
}
