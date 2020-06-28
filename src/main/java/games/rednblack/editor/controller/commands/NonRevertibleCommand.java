package games.rednblack.editor.controller.commands;

import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.puremvc.patterns.observer.Notification;
import games.rednblack.editor.controller.SandboxCommand;
import games.rednblack.editor.proxy.CommandManager;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.renderer.data.CompositeItemVO;

import java.util.HashMap;

/**
 * Created by azakhary on 11/29/2015.
 */
public abstract class NonRevertibleCommand extends SandboxCommand {

    protected CommandManager commandManager;
    protected Notification notification;

    protected boolean isCancelled = false;
    protected final HashMap<String, CompositeItemVO> libraryItems;
    protected final ProjectManager projectManager;

    public NonRevertibleCommand() {
        this.projectManager = facade.retrieveProxy(ProjectManager.NAME);
        this.libraryItems = projectManager.getCurrentProjectInfoVO().libraryItems;
    }

    @Override
    public void execute(Notification notification) {
        commandManager = facade.retrieveProxy(CommandManager.NAME);
        this.notification = notification;
        Dialogs.showConfirmDialog(sandbox.getUIStage(),
                confirmDialogTitle(), confirmDialogMessage(),
                new String[]{"Cancel", confirmAction()}, new Integer[]{0, 1}, r -> {
                    if (r == 1) {
                        callDoAction();
                        if (!isCancelled) commandManager.clearHistory();
                    }
                });
    }

    public abstract void doAction();

    public void callDoAction() {
        doAction();
    }

    public void cancel() {
        isCancelled = true;
    }

    protected String confirmDialogTitle() {
        return "Non Revertible Action";
    }

    protected String confirmDialogMessage() {
        return "Do you want to proceed?\nThis action cannot be undone.";
    }

    protected String confirmAction() {
        return "Yes";
    }
}
