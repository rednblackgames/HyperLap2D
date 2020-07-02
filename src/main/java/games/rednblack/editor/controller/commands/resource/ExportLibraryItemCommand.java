package games.rednblack.editor.controller.commands.resource;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.kotcrab.vis.ui.widget.file.FileTypeFilter;
import games.rednblack.editor.controller.commands.NonRevertibleCommand;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.ResourceManager;
import games.rednblack.editor.renderer.data.*;
import games.rednblack.editor.utils.ImportUtils;
import games.rednblack.editor.utils.ZipUtils;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.vo.ExportMapperVO;
import games.rednblack.h2d.common.vo.ExportMapperVO.ExportedAsset;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class ExportLibraryItemCommand extends NonRevertibleCommand {

    private static final String CLASS_NAME = "games.rednblack.editor.controller.commands.resource.ExportLibraryItemCommand";
    public static final String DONE = CLASS_NAME + "DONE";
    private final Json json = new Json(JsonWriter.OutputType.json);

    private final String currentProjectPath;
    private final ResourceManager resourceManager;
    private final ExportMapperVO exportMapperVO;

    public ExportLibraryItemCommand() {
        cancel();
        setShowConfirmDialog(false);
        currentProjectPath = projectManager.getCurrentProjectPath() + File.separator;
        resourceManager = facade.retrieveProxy(ResourceManager.NAME);
        exportMapperVO = new ExportMapperVO();
    }

    @Override
    public void doAction() {
        exportMapperVO.mapper.clear();
        String libraryItemName = notification.getBody();

        FileChooser fileChooser = new FileChooser(FileChooser.Mode.SAVE);
        FileTypeFilter typeFilter = new FileTypeFilter(false);
        typeFilter.addRule("HyperLap2D Library (*.h2dlib)", "h2dlib");
        fileChooser.setFileTypeFilter(typeFilter);

        fileChooser.setMultiSelectionEnabled(false);

        FileHandle workspacePath = (projectManager.getWorkspacePath() == null || !projectManager.getWorkspacePath().exists()) ?
                Gdx.files.absolute(System.getProperty("user.home")) : projectManager.getWorkspacePath();
        fileChooser.setDirectory(workspacePath);
        fileChooser.setDefaultFileName(libraryItemName);

        fileChooser.setListener(new FileChooserAdapter() {
            @Override
            public void selected(Array<FileHandle> files) {
                try {
                    if (files.get(0).exists()) {
                        FileUtils.forceDelete(files.get(0).file());
                    }
                    doExport(libraryItemName, files.get(0).pathWithoutExtension());

                    facade.sendNotification(DONE, libraryItemName);
					facade.sendNotification(MsgAPI.SHOW_NOTIFICATION, "'" + libraryItemName +"' successfully exported");
                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        FileUtils.deleteDirectory(new File(files.get(0).pathWithoutExtension() + "TMP"));
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        });
        sandbox.getUIStage().addActor(fileChooser.fadeIn());
    }

    private void doExport(String libraryItemName, String destFile) throws IOException  {
        File tempDir = new File(destFile + "TMP");
        FileUtils.forceMkdir(tempDir);

        CompositeItemVO compositeItemVO = libraryItems.get(libraryItemName);

        FileUtils.writeStringToFile(new File(tempDir.getPath() + File.separator + libraryItemName + ".lib"), json.toJson(compositeItemVO), "utf-8");

        exportAllAssets(compositeItemVO.composite, tempDir);

        exportMapperVO.mapper.add(new ExportedAsset(ImportUtils.TYPE_HYPERLAP2D_INTERNAL_LIBRARY, libraryItemName + ".lib"));

        FileUtils.writeStringToFile(new File(tempDir.getPath() + File.separator + "mapper"), json.toJson(exportMapperVO), "utf-8");

        ZipUtils.pack(tempDir.getPath(), destFile + ".h2dlib");
        FileUtils.deleteDirectory(tempDir);
    }

    private void exportAllAssets(CompositeVO compositeVO, File tmpDir) throws IOException {
        for (SimpleImageVO imageVO : compositeVO.sImages) {
            File fileSrc = new File(currentProjectPath + ProjectManager.IMAGE_DIR_PATH + File.separator + imageVO.imageName + ".png");
            FileUtils.copyFileToDirectory(fileSrc, tmpDir);
            exportMapperVO.mapper.add(new ExportedAsset(ImportUtils.TYPE_IMAGE, fileSrc.getName()));
        }

        for (Image9patchVO imageVO : compositeVO.sImage9patchs) {
            File fileSrc = new File(currentProjectPath + ProjectManager.IMAGE_DIR_PATH + File.separator + imageVO.imageName + ".9.png");
            FileUtils.copyFileToDirectory(fileSrc, tmpDir);
            exportMapperVO.mapper.add(new ExportedAsset(ImportUtils.TYPE_IMAGE, fileSrc.getName()));
        }

        for (SpineVO imageVO : compositeVO.sSpineAnimations) {
            File fileSrc = new File(currentProjectPath + ProjectManager.SPINE_DIR_PATH + File.separator + imageVO.animationName);
            FileUtils.copyDirectory(fileSrc, tmpDir);
            exportMapperVO.mapper.add(new ExportedAsset(ImportUtils.TYPE_SPINE_ANIMATION, fileSrc.getName() + ".json"));
        }

        for (SpriteAnimationVO imageVO : compositeVO.sSpriteAnimations) {
            File fileSrc = new File(currentProjectPath + ProjectManager.SPRITE_DIR_PATH + File.separator + imageVO.animationName);
            FileUtils.copyDirectory(fileSrc, tmpDir);
            exportMapperVO.mapper.add(new ExportedAsset(ImportUtils.TYPE_SPRITE_ANIMATION_ATLAS, fileSrc.getName() + ".atlas"));
        }

        for (SpriterVO imageVO : compositeVO.sSpriterAnimations) {
            File fileSrc = new File(currentProjectPath + ProjectManager.SPRITER_DIR_PATH + File.separator + imageVO.animationName);
            FileUtils.copyDirectory(fileSrc, tmpDir);
            exportMapperVO.mapper.add(new ExportedAsset(ImportUtils.TYPE_SPRITER_ANIMATION, fileSrc.getName() + ".scml"));
        }

        for (ParticleEffectVO imageVO : compositeVO.sParticleEffects) {
            File fileSrc = new File(currentProjectPath + ProjectManager.PARTICLE_DIR_PATH + File.separator + imageVO.particleName);
            FileUtils.copyFileToDirectory(fileSrc, tmpDir);
            exportMapperVO.mapper.add(new ExportedAsset(ImportUtils.TYPE_PARTICLE_EFFECT, fileSrc.getName()));
            ParticleEffect particleEffect = new ParticleEffect(resourceManager.getParticleEffect(imageVO.particleName));
            for (ParticleEmitter emitter : particleEffect.getEmitters()) {
                for (String path : emitter.getImagePaths()) {
                    File f = new File(currentProjectPath + ProjectManager.IMAGE_DIR_PATH + File.separator + path);
                    FileUtils.copyFileToDirectory(f, tmpDir);
                }
            }
        }

        for (CompositeItemVO compositeItemVO : compositeVO.sComposites) {
            exportAllAssets(compositeItemVO.composite, tmpDir);
        }
    }
}
