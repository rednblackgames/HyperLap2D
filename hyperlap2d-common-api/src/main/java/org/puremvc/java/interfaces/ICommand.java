//
//  PureMVC Java Standard
//
//  Copyright(c) 2019 Saad Shams <saad.shams@puremvc.org>
//  Your reuse is governed by the Creative Commons Attribution 3.0 License
//

package org.puremvc.java.interfaces;

/**
 * The interface definition for a PureMVC Command.
 *
 * @see org.puremvc.java.interfaces INotification
 */
public interface ICommand extends INotifier {

    /**
     * <P>Execute the <code>ICommand</code>'s logic to handle a given <code>INotification</code>.</P>
     *
     * @param notification an <code>INotification</code> to handle.
     */
    void execute(INotification notification);

}
