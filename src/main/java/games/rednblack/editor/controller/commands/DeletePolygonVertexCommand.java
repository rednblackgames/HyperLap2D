package games.rednblack.editor.controller.commands;

import com.puremvc.patterns.observer.Notification;
import games.rednblack.editor.controller.SandboxCommand;

public class DeletePolygonVertexCommand extends SandboxCommand {

    @Override
    public void execute(Notification notification) {
        super.execute(notification);
        System.out.println("Should delete");
    }
}
