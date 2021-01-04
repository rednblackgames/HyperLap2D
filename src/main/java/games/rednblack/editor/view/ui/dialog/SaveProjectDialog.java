package games.rednblack.editor.view.ui.dialog;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.H2DDialog;

public class SaveProjectDialog extends H2DDialog {

    private VisLabel messageLabel;
    private Runnable action;

    public SaveProjectDialog() {
        super("Save Project");

        messageLabel = new VisLabel();
        getContentTable().pad(10);
        getContentTable().add(messageLabel);
        VisTextButton yesButton = new VisTextButton("Save");
        VisTextButton noButton = new VisTextButton("Don't Save");
        VisTextButton cancelButton = new VisTextButton("Cancel");

        getButtonsTable().add(noButton).width(95).pad(8).padLeft(16);
        getButtonsTable().add(cancelButton).width(95).pad(8).padLeft(32).padRight(6);
        getButtonsTable().add(yesButton).width(95).pad(8).padRight(16).padLeft(6);

        yesButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Sandbox sandbox = Sandbox.getInstance();
                HyperLap2DFacade facade = HyperLap2DFacade.getInstance();
                ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
                SceneVO vo = sandbox.sceneVoFromItems();
                projectManager.saveCurrentProject(vo);

                SaveProjectDialog.this.close();
                action.run();
            }
        });

        noButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SaveProjectDialog.this.close();
                action.run();
            }
        });

        cancelButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SaveProjectDialog.this.close();
            }
        });
    }

    public void updateDialog(String projectTitle, Runnable runnable) {
        if (runnable == null) throw new IllegalArgumentException("Runnable action cannot be null");
        action = runnable;
        messageLabel.setText("Save changes to '" + projectTitle + "' before closing?");
    }
}
