package games.rednblack.editor.view.ui.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.view.menu.ResourcesMenu;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.h2d.common.MsgAPI;
import org.apache.commons.io.FileUtils;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.mediator.Mediator;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImportSpriteSheetDialogMediator extends Mediator<ImportSpriteSheetDialog> {
    private static final String TAG = ImportSpriteSheetDialogMediator.class.getCanonicalName();
    private static final String NAME = TAG;

    public ImportSpriteSheetDialogMediator() {
        super(NAME, new ImportSpriteSheetDialog());
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                ResourcesMenu.IMPORT_SPRITE_SHEET,
                ImportSpriteSheetDialog.IMPORT_SPRITE_SHEET
        };
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        Sandbox sandbox = Sandbox.getInstance();
        UIStage uiStage = sandbox.getUIStage();

        switch (notification.getName()) {
            case ResourcesMenu.IMPORT_SPRITE_SHEET:
                viewComponent.show(uiStage);
                break;
            case ImportSpriteSheetDialog.IMPORT_SPRITE_SHEET:
                FileHandle spriteSheet = notification.getBody();
                importSpriteSheet(spriteSheet);
                break;
        }
    }

    private void importSpriteSheet(FileHandle spriteSheet) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            facade.sendNotification(MsgAPI.SHOW_LOADING_DIALOG);

            byte[] image = spriteSheet.readBytes();
            Pixmap pixmap = new Pixmap(image, 0, image.length);
            String name = spriteSheet.nameWithoutExtension();

            int spriteW = viewComponent.getSpriteWidth();
            int spriteH = viewComponent.getSpriteHeight();

            ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
            projectManager.getCurrentProjectInfoVO().animationsPacks.get("main").regions.add(name);

            String targetPath = projectManager.getCurrentProjectPath() + File.separator
                    + ProjectManager.SPRITE_DIR_PATH + File.separator + name;
            File targetDir = new File(targetPath);
            try {
                FileUtils.forceMkdir(targetDir);
            } catch (IOException e) {
                e.printStackTrace();
                facade.sendNotification(MsgAPI.HIDE_LOADING_DIALOG);
                return;
            }

            TexturePacker.Settings settings = projectManager.getTexturePackerSettings();
            TexturePacker tp = new TexturePacker(settings);

            int i = 0;
            for (int x = 0; x < pixmap.getWidth(); x += spriteW) {
                for (int y = 0; y < pixmap.getHeight(); y += spriteH) {
                    int w = x + spriteW <= pixmap.getWidth() ? spriteW : pixmap.getWidth() - x;
                    int h = y + spriteH <= pixmap.getHeight() ? spriteH : pixmap.getHeight() - y;
                    Pixmap tilePixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
                    tilePixmap.drawPixmap(pixmap, 0, 0, x, y, w, h);

                    String imagesPath = projectManager.getCurrentRawImagesPath() + File.separator + name + "_" + i + ".png";
                    FileHandle path = new FileHandle(imagesPath);
                    PixmapIO.writePNG(path, tilePixmap);

                    tilePixmap.dispose();
                    tp.addImage(path.file());

                    i++;
                }
            }

            pixmap.dispose();

            tp.pack(targetDir, name);

            viewComponent.close();

            facade.sendNotification(MsgAPI.HIDE_LOADING_DIALOG);

            Gdx.app.postRunnable(() -> {
                facade.sendNotification(MsgAPI.UPDATE_ATLAS_PACK_LIST);
                facade.sendNotification(MsgAPI.ACTION_REPACK);
            });
        });
        executor.shutdown();
    }
}
