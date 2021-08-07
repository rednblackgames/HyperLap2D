package games.rednblack.editor.view.ui.dialog;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.view.menu.ResourcesMenu;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.h2d.common.MsgAPI;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.mediator.Mediator;

import java.io.File;

public class CreatePlaceholderDialogMediator extends Mediator<CreatePlaceholderDialog> {
    private static final String TAG = CreatePlaceholderDialogMediator.class.getCanonicalName();
    private static final String NAME = TAG;

    public CreatePlaceholderDialogMediator() {
        super(NAME, new CreatePlaceholderDialog());
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                ResourcesMenu.CREATE_PLACEHOLDER,
                CreatePlaceholderDialog.ADD_NEW_PLACEHOLDER
        };
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        Sandbox sandbox = Sandbox.getInstance();
        UIStage uiStage = sandbox.getUIStage();

        switch (notification.getName()) {
            case ResourcesMenu.CREATE_PLACEHOLDER:
                viewComponent.show(uiStage);
                break;
            case CreatePlaceholderDialog.ADD_NEW_PLACEHOLDER:
                int w = viewComponent.placeholderWidth();
                int h = viewComponent.placeholderHeight();
                String name = viewComponent.getName();

                createPlaceholder(name, w, h);
                break;
        }
    }

    private void createPlaceholder(String name, int width , int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(1, 1, 1, 1f));
        pixmap.fill();

        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        String imagesPath = projectManager.getCurrentRawImagesPath() + File.separator + name + ".png";
        FileHandle path = new FileHandle(imagesPath);
        PixmapIO.writePNG(path, pixmap);
        pixmap.dispose();

        projectManager.getCurrentProjectInfoVO().imagesPacks.get("main").regions.add(name);

        facade.sendNotification(MsgAPI.ACTION_REPACK);
    }
}
