package games.rednblack.editor.controller.commands;

import com.badlogic.ashley.core.Entity;
import games.rednblack.editor.controller.SandboxCommand;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.utils.TransformCommandBuilder;
import games.rednblack.editor.view.ui.validator.FloatInputValidator;
import games.rednblack.h2d.common.view.ui.dialog.MultipleInputDialog;
import games.rednblack.h2d.common.view.ui.listener.MultipleInputDialogListener;
import org.puremvc.java.interfaces.INotification;

public class ChangeOriginPointPosition extends SandboxCommand {

    @Override
    public void execute(INotification notification) {
        super.execute(notification);

        Entity entity = notification.getBody();
        TransformComponent transformComponent = ComponentRetriever.get(entity, TransformComponent.class);

        MultipleInputDialog dialog = new MultipleInputDialog("Origin Position", new String[]{"X : ", "Y : "}, false, new FloatInputValidator(), new MultipleInputDialogListener() {
            @Override
            public void finished(String[] input) {
                TransformCommandBuilder commandBuilder = new TransformCommandBuilder();
                commandBuilder.begin(entity);
                commandBuilder.setOrigin(Float.parseFloat(input[0]), Float.parseFloat(input[1]));
                commandBuilder.execute();
            }

            @Override
            public void canceled() {

            }
        });

        dialog.setText(new String[]{transformComponent.originX+"", transformComponent.originY+""});
        sandbox.getUIStage().addActor(dialog.fadeIn());
    }
}
