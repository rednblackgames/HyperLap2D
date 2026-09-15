package games.rednblack.editor.utils.asset.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.renderer.data.TexturePackVO;
import games.rednblack.editor.utils.AssetsUtils;
import games.rednblack.editor.utils.asset.Asset;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.ProgressHandler;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AtlasAsset extends Asset {

    @Override
    public int getType() {
        return AssetsUtils.TYPE_TEXTURE_ATLAS;
    }

    @Override
    protected boolean matchMimeType(FileHandle file) {
        if (!file.extension().equals("atlas")) return false;
        try {
            TextureAtlas.TextureAtlasData atlas = new TextureAtlas.TextureAtlasData(file, file.parent(), false);
            return !AssetsUtils.isAtlasAnimationSequence(atlas.getRegions());
        } catch (Exception ignore) {
        }
        return false;
    }

    @Override
    public boolean checkExistence(Array<FileHandle> files) {
        //TODO
        return false;
    }

    @Override
    public void importAsset(Array<FileHandle> files, ProgressHandler progressHandler, boolean skipRepack) {
        try {
            for (FileHandle fileHandle : new Array.ArrayIterator<>(files)) {
                FileHandle tmpDir = new FileHandle(projectManager.getCurrentProjectPath() + File.separator + "tmp");
                if (tmpDir.exists())
                    FileUtils.forceDelete(tmpDir.file());
                FileUtils.forceMkdir(tmpDir.file());
                AssetsUtils.unpackAtlasIntoTmpFolder(fileHandle.file(), null, tmpDir.path());
                Array<FileHandle> images = new Array<>(tmpDir.list());
                projectManager.copyImageFilesForAllResolutionsIntoProject(images, true, progressHandler);

                // Frames of a sequence (name_00, name_01, ...) are sprite animations, not images: they are
                // registered like a PNG sequence import, the rest of the regions go to the atlas' image pack.
                ObjectMap<String, Array<FileHandle>> sequences = findSequences(images);
                for (ObjectMap.Entry<String, Array<FileHandle>> sequence : sequences) {
                    registerSpriteAnimation(sequence.key, sequence.value);
                }
                FileUtils.forceDelete(tmpDir.file());

                String name = fileHandle.nameWithoutExtension();
                if (name.equals("pack")) name = name + "Import";

                TexturePackVO texturePackVO = projectManager.getCurrentProjectInfoVO().imagesPacks.get(name);
                if (texturePackVO == null) {
                    texturePackVO = new TexturePackVO();
                    texturePackVO.name = name;

                    projectManager.getCurrentProjectInfoVO().imagesPacks.put(texturePackVO.name, texturePackVO);
                }

                for (FileHandle image : images) {
                    String regionName = image.nameWithoutExtension().replace(".9", "");
                    // ObjectMap rejects null keys, and most regions have no sequence suffix at all
                    String sequence = sequenceName(regionName);
                    if (sequence != null && sequences.containsKey(sequence)) continue;
                    texturePackVO.regions.add(regionName);
                }
            }

            resolutionManager.rePackProjectImagesForAllResolutionsSync(false);

            Gdx.app.postRunnable(() -> facade.sendNotification(MsgAPI.UPDATE_ATLAS_PACK_LIST));
        } catch (Exception e) {
            // Any failure here leaves the project half imported (files copied, packs not rebuilt):
            // it must reach the console and the import dialog instead of silently killing the worker.
            e.printStackTrace();
            progressHandler.progressFailed();
        }
    }

    /** Frame number suffix of a sequence file, e.g. {@code button-over_07}. */
    private static final Pattern SEQUENCE_SUFFIX = Pattern.compile("^(.+)_(\\d+)$");

    /** Name of the sequence a region belongs to by its suffix, or null when it has none. */
    private static String sequenceName(String regionName) {
        Matcher matcher = SEQUENCE_SUFFIX.matcher(regionName);
        return matcher.matches() ? matcher.group(1) : null;
    }

    /**
     * Groups the unpacked files whose names form a complete frame sequence (the same rule the PNG
     * sequence importer uses), keyed by animation name.
     */
    private static ObjectMap<String, Array<FileHandle>> findSequences(Array<FileHandle> images) {
        ObjectMap<String, Array<FileHandle>> candidates = new ObjectMap<>();
        for (FileHandle image : images) {
            String sequence = sequenceName(image.nameWithoutExtension().replace(".9", ""));
            if (sequence == null) continue;
            Array<FileHandle> frames = candidates.get(sequence);
            if (frames == null) {
                frames = new Array<>();
                candidates.put(sequence, frames);
            }
            frames.add(image);
        }

        ObjectMap<String, Array<FileHandle>> sequences = new ObjectMap<>();
        Array<String> names = new Array<>();
        for (ObjectMap.Entry<String, Array<FileHandle>> candidate : candidates) {
            names.clear();
            for (FileHandle frame : candidate.value) names.add(frame.nameWithoutExtension());
            if (AssetsUtils.isAnimationSequence(names)) sequences.put(candidate.key, candidate.value);
        }
        return sequences;
    }

    /**
     * Registers frames already copied into the project images as a sprite animation: its own folder with
     * an atlas of the frames (what the editor lists animations from) and an entry in the main animations
     * pack, exactly what {@link SpriteAnimationSequenceAsset} produces.
     */
    private void registerSpriteAnimation(String animationName, Array<FileHandle> frames) throws IOException {
        String targetPath = projectManager.getCurrentProjectPath() + File.separator
                + ProjectManager.SPRITE_DIR_PATH + File.separator + animationName;
        File targetDir = new File(targetPath);
        if (targetDir.exists()) FileUtils.deleteDirectory(targetDir);
        FileUtils.forceMkdir(targetDir);

        String imagesPath = projectManager.getCurrentProjectPath() + File.separator + ProjectManager.IMAGE_DIR_PATH;
        TexturePacker.Settings settings = projectManager.getTexturePackerSettings();
        TexturePacker tp = new TexturePacker(settings);
        for (FileHandle frame : frames) {
            tp.addImage(new File(imagesPath + File.separator + frame.name()));
        }
        tp.pack(targetDir, animationName);

        projectManager.getCurrentProjectInfoVO().animationsPacks.get("main").regions.add(animationName);
    }

    @Override
    public boolean deleteAsset(int root, String name) {
        return false;
    }
}
