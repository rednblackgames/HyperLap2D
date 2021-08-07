package games.rednblack.editor.view.ui.dialog;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.utils.PerlinNoiseGenerator;
import games.rednblack.editor.view.menu.ResourcesMenu;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.h2d.common.MsgAPI;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.mediator.Mediator;

import java.io.File;

public class CreateNoiseDialogMediator extends Mediator<CreateNoiseDialog>  {

    private static final String TAG = CreateNoiseDialogMediator.class.getCanonicalName();
    private static final String NAME = TAG;

    public CreateNoiseDialogMediator() {
        super(NAME, new CreateNoiseDialog());
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                ResourcesMenu.CREATE_NOISE,
                CreateNoiseDialog.ADD_NEW_PLACEHOLDER
        };
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        Sandbox sandbox = Sandbox.getInstance();
        UIStage uiStage = sandbox.getUIStage();

        switch (notification.getName()) {
            case ResourcesMenu.CREATE_NOISE:
                viewComponent.show(uiStage);
                break;
            case CreateNoiseDialog.ADD_NEW_PLACEHOLDER:
                int w = viewComponent.placeholderWidth();
                int h = viewComponent.placeholderHeight();
                int min = viewComponent.getMin();
                int max = viewComponent.getMax();
                String name = viewComponent.getName();

                createNoise(name, w, h, min, max);
                break;
        }
    }

    private void createNoise(String name, int width, int height, int min, int max) {
        Pixmap pixmap = PerlinNoiseGenerator.generatePixmap(width, height, min, max,8);

        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        String imagesPath = projectManager.getCurrentRawImagesPath() + File.separator + name + ".png";
        FileHandle path = new FileHandle(imagesPath);
        PixmapIO.writePNG(path, pixmap);
        pixmap.dispose();

        projectManager.getCurrentProjectInfoVO().imagesPacks.get("main").regions.add(name);

        facade.sendNotification(MsgAPI.ACTION_REPACK);
    }
}
