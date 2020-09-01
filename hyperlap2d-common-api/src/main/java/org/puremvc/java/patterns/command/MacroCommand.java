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

import java.util.Vector;
import java.util.function.Supplier;

/**
 * <P>A base <code>ICommand</code> implementation that executes other <code>ICommand</code>s.</P>
 *
 * <P>A <code>MacroCommand</code> maintains an list of
 * <code>ICommand</code> Class references called <i>SubCommands</i>.</P>
 *
 * <P>When <code>execute</code> is called, the <code>MacroCommand</code>
 * instantiates and calls <code>execute</code> on each of its <i>SubCommands</i> turn.
 * Each <i>SubCommand</i> will be passed a reference to the original
 * <code>INotification</code> that was passed to the <code>MacroCommand</code>'s
 * <code>execute</code> method.</P>
 *
 * <P>Unlike <code>SimpleCommand</code>, your subclass
 * should not override <code>execute</code>, but instead, should
 * override the <code>initializeMacroCommand</code> method,
 * calling <code>addSubCommand</code> once for each <i>SubCommand</i>
 * to be executed.</P>
 *
 * @see org.puremvc.java.core.Controller Controller
 * @see org.puremvc.java.patterns.observer.Notification Notification
 * @see org.puremvc.java.patterns.command.SimpleCommand SimpleCommand
 */
public class MacroCommand extends Notifier implements ICommand {

    private Vector<Supplier<ICommand>> subCommands;

    /**
     * <P>Constructor.</P>
     *
     * <P>You should not need to define a constructor,
     * instead, override the <code>initializeMacroCommand</code>
     * method.</P>
     *
     * <P>If your subclass does define a constructor, be
     * sure to call <code>super()</code>.</P>
     */
    public MacroCommand() {
        subCommands = new Vector<Supplier<ICommand>>();
        initializeMacroCommand();
    }

    /**
     * <P>Initialize the <code>MacroCommand</code>.</P>
     *
     * <P>In your subclass, override this method to
     * initialize the <code>MacroCommand</code>'s <i>SubCommand</i>
     * list with <code>ICommand</code> class references like
     * this:</P>
     *
     * <pre>
     * {@code
     * // Initialize MyMacroCommand
     * protected void initializeMacroCommand( )
     * {
     *      addSubCommand( () -> new com.me.myapp.controller.FirstCommand() );
     *      addSubCommand( () -> new com.me.myapp.controller.SecondCommand() );
     *      addSubCommand( () -> new com.me.myapp.controller.ThirdCommand() );
     * }
     * }
     * </pre>
     *
     * <P>Note that <i>SubCommand</i>s may be any <code>ICommand</code> implementor,
     * <code>MacroCommand</code>s or <code>SimpleCommands</code> are both acceptable.</P>
     */
    protected void initializeMacroCommand() {
    }

    /**
     * <P>Add a <i>SubCommand</i>.</P>
     *
     * <P>The <i>SubCommands</i> will be called in First In/First Out (FIFO)
     * order.</P>
     *
     * @param factory a reference to the factory of the <code>ICommand</code>.
     */
    protected void addSubCommand(Supplier<ICommand> factory) {
        subCommands.add(factory);
    }

    /**
     * <P>Execute this <code>MacroCommand</code>'s <i>SubCommands</i>.</P>
     *
     * <P>The <i>SubCommands</i> will be called in First In/First Out (FIFO)
     * order.</P>
     *
     * @param notification the <code>INotification</code> object to be passsed to each <i>SubCommand</i>.
     */
    public void execute(INotification notification) {
        while(!subCommands.isEmpty()) {
            Supplier<ICommand> commandSupplier = subCommands.remove(0);
            ICommand command = commandSupplier.get();
            command.execute(notification);
        }
    }
}
