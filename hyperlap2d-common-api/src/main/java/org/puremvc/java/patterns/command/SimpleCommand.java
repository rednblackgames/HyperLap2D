//
//  PureMVC Java Standard
//
//  Copyright(c) 2019 Saad Shams <saad.shams@puremvc.org>
//  Your reuse is governed by the Creative Commons Attribution 3.0 License
//

package org.puremvc.java.patterns.command;

import org.puremvc.java.interfaces.ICommand;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.observer.Notifier;

/**
 * <P>A base <code>ICommand</code> implementation.</P>
 *
 * <P>Your subclass should override the <code>execute</code>
 * method where your business logic will handle the <code>INotification</code>.</P>
 *
 * @see org.puremvc.java.core.Controller Controller
 * @see org.puremvc.java.patterns.observer.Notification Notification
 * @see MacroCommand MacroCommand
 */
public class SimpleCommand extends Notifier implements ICommand {

    /**
     * <P>Fulfill the use-case initiated by the given <code>INotification</code>.</P>
     *
     * <P>In the Command Pattern, an application use-case typically
     * begins with some user action, which results in an <code>INotification</code> being broadcast, which
     * is handled by business logic in the <code>execute</code> method of an
     * <code>ICommand</code>.</P>
     *
     * @param notification the <code>INotification</code> to handle.
     */
    public void execute(INotification notification) {
    }
}
