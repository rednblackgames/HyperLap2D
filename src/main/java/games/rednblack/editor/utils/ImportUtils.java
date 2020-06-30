/*
 * ******************************************************************************
 *  * Copyright 2015 See AUTHORS file.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *****************************************************************************
 */

package games.rednblack.editor.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.file.FileTypeFilter;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.ProjectManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class ImportUtils {

    private static ImportUtils instance;

    public static final int TYPE_FAILED = -3;
    public static final int TYPE_MIXED = -2;
    public static final int TYPE_UNKNOWN = -1;
    public static final int TYPE_UNSUPPORTED = 0;
    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_ANIMATION_PNG_SEQUENCE = 2;
    public static final int TYPE_SPRITE_ANIMATION_ATLAS = 3;
    public static final int TYPE_SPINE_ANIMATION = 4;
    public static final int TYPE_SPRITER_ANIMATION = 5;
    public static final int TYPE_TTF_FONT = 6;
    public static final int TYPE_BITMAP_FONT = 7;
    public static final int TYPE_PARTICLE_EFFECT = 8;
    public static final int TYPE_TEXTURE_ATLAS = 9;
    public static final int TYPE_SHADER = 10;
    public static final int TYPE_HYPERLAP2D_LIBRARY = 11;
    public static final int TYPE_HYPERLAP2D_INTERNAL_LIBRARY = 12;

    private final ArrayList<Integer> supportedTypes = new ArrayList<>();
    private final FileTypeFilter fileTypeFilter;

    private ImportUtils() {
        supportedTypes.add(TYPE_IMAGE);
        supportedTypes.add(TYPE_ANIMATION_PNG_SEQUENCE);
        supportedTypes.add(TYPE_SPRITE_ANIMATION_ATLAS);
        supportedTypes.add(TYPE_SPINE_ANIMATION);
        supportedTypes.add(TYPE_SPRITER_ANIMATION);
        supportedTypes.add(TYPE_PARTICLE_EFFECT);
        supportedTypes.add(TYPE_SHADER);
        supportedTypes.add(TYPE_HYPERLAP2D_LIBRARY);
        // TODO: not yet supported, and probably never because they are useless IMO
        //supportedTypes.add(TYPE_TEXTURE_ATLAS);
        //supportedTypes.add(TYPE_TTF_FONT);
        //supportedTypes.add(TYPE_BITMAP_FONT);

        fileTypeFilter = new FileTypeFilter(false);

        fileTypeFilter.addRule("All Supported (*.png, *.atlas, *.p, *.json, *.vert, *.frag, *.h2dlib)", "png", "atlas", "p", "json", "vert", "frag", "h2dlib");
        fileTypeFilter.addRule("PNG File (*.png)", "png");
        fileTypeFilter.addRule("Sprite Animation Atlas File (*.atlas)", "atlas");
        fileTypeFilter.addRule("Particle Effect (*.p)", "p");
        fileTypeFilter.addRule("Spine Animation (*.json)", "json");
        //fileTypeFilter.addRule("Spriter Animation (*.scml)", "scml");
        fileTypeFilter.addRule("Shader (*.vert, *.frag)", "vert", "frag");
        fileTypeFilter.addRule("HyperLap2D Library (*.h2dlib)", "h2dlib");
    }

    public static ImportUtils getInstance() {
        if (instance == null) {
            instance = new ImportUtils();
        }

        return instance;
    }

    public FileTypeFilter getFileTypeFilter() {
        return fileTypeFilter;
    }

    public static int getImportType(String[] paths) {
        int mainType = TYPE_MIXED;
        String[] names = new String[paths.length];
        for (int i = 0; i < paths.length; i++) {
            String path = paths[i];
            int type = getFileType(path);
            if (i == 0) mainType = type;
            if (mainType != type) {
                return TYPE_MIXED;
            }
            names[i] = FilenameUtils.getBaseName(path);
        }

        if (mainType == TYPE_IMAGE) {
            // check they are a PNG sequence;
            boolean isSequence = isAnimationSequence(names);
            if (isSequence) {
                mainType = TYPE_ANIMATION_PNG_SEQUENCE;
            }
        }

        if (mainType > 0 && !ImportUtils.getInstance().supportedTypes.contains(mainType)) {
            mainType = TYPE_UNSUPPORTED;
        }

        return mainType;
    }

    public static int getFileType(String path) {
        int type = checkFileTypeByExtension(path);
        if (type == TYPE_UNKNOWN) {
            // we have to check by getting into the file
            type = checkFileTypeByContent(path);
        }

        return type;
    }

    public static int checkFileTypeByExtension(String path) {
        String ext = FilenameUtils.getExtension(path).toLowerCase();
        if (ext.equals("png")) {
            return TYPE_IMAGE;
        }

        if (ext.equals("ttf")) {
            return TYPE_TTF_FONT;
        }
        if (ext.equals("scml")) {
            return TYPE_SPRITER_ANIMATION;
        }

        if (ext.equals("vert") || ext.equals("frag")) {
            return TYPE_SHADER;
        }

        if (ext.equals("h2dlib")) {
            return TYPE_HYPERLAP2D_LIBRARY;
        }

        if (ext.equals("lib")) {
            return TYPE_HYPERLAP2D_INTERNAL_LIBRARY;
        }

        return TYPE_UNKNOWN;
    }

    public static int checkFileTypeByContent(String path) {
        File file = new File(path);
        long fileSizeInBytes = file.length();
        // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
        long fileSizeInKB = fileSizeInBytes / 1024;

        if (fileSizeInKB > 1000) {
            return TYPE_UNKNOWN;
        }

        int type = TYPE_UNKNOWN;

        try {
            String contents = FileUtils.readFileToString(file);

            // checking for atlas file
            if (contents.contains("format: ") && contents.contains("filter: ") && contents.contains("xy: ")) {
                type = TYPE_TEXTURE_ATLAS;
                // need to figure out if atlas is animation or just files.
                TextureAtlas atlas = new TextureAtlas(new FileHandle(file));

                boolean isSequence = isAtlasAnimationSequence(atlas.getRegions());
                if (isSequence) {
                    type = TYPE_SPRITE_ANIMATION_ATLAS;
                }

                return type;
            }
            System.out.println("is spine?");

            // checking for spine animation
            if (contents.contains("\"skeleton\":{") || contents.contains("\"skeleton\": {") || contents.contains("{\"bones\":[")) {
                type = TYPE_SPINE_ANIMATION;
                System.out.println("is spine");
                return type;
            }

            // checking for particle effect
            if (contents.contains("- Options - ") && contents.contains("- Image Paths -") && contents.contains("- Duration -")) {
                type = TYPE_PARTICLE_EFFECT;
                return type;
            }

        } catch (IOException ignore) {
        }

        return type;
    }

    public static boolean isAnimationSequence(String[] names) {
        if (names.length < 2) return false;
        int[] sequenceArray = new int[names.length];
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            // try to remove extension if any
            if (name.indexOf(".") > 0) name = name.substring(0, name.indexOf("."));
            try {
                int intValue = Integer.parseInt(name.replaceAll("(.+)_", ""));
                sequenceArray[i] = intValue;
            } catch (Exception e) {
                sequenceArray[i] = -10;
            }
        }
        Arrays.sort(sequenceArray);
        if (sequenceArray[0] == 0 && sequenceArray[sequenceArray.length - 1] == sequenceArray.length - 1) {
            return true;
        }

        return sequenceArray[0] == 1 && sequenceArray[sequenceArray.length - 1] == sequenceArray.length;
    }

    public static boolean isAtlasAnimationSequence(Array<TextureAtlas.AtlasRegion> regions) {
        if (regions.size < 2) return false;

        //Check old .atlas format
        String[] regionNames = new String[regions.size];
        for (int i = 0; i < regions.size; i++) {
            regionNames[i] = regions.get(i).name;
        }

        if (isAnimationSequence(regionNames))
            return true;

        //New .atlas format
        String animName = regions.get(0).name;
        for (int i = 1; i < regions.size; i++) {
            if (!animName.equals(regions.get(i).name)) {
                return false;
            }
        }

        return regions.get(regions.size - 1).index == regions.size - 1;
    }

    public boolean checkAssetExistence(int type, Array<FileHandle> fileHandles) {
        if (type == ImportUtils.TYPE_HYPERLAP2D_LIBRARY) {
            return checkLibraryItemExistence(fileHandles);
        }

        boolean exists = false;
        ProjectManager projectManager = HyperLap2DFacade.getInstance().retrieveProxy(ProjectManager.NAME);

        String dir = ProjectManager.IMAGE_DIR_PATH;
        String ext = "png";
        switch (type) {
            case ImportUtils.TYPE_IMAGE:
            case ImportUtils.TYPE_TEXTURE_ATLAS:
                dir = ProjectManager.IMAGE_DIR_PATH;
                ext = "png";
                break;
            case ImportUtils.TYPE_PARTICLE_EFFECT:
                dir = ProjectManager.PARTICLE_DIR_PATH;
                ext = "p";
                break;
            case ImportUtils.TYPE_SPRITER_ANIMATION:
            case ImportUtils.TYPE_SPINE_ANIMATION:
                dir = ProjectManager.SPINE_DIR_PATH;
                ext = "json";
                break;
            case ImportUtils.TYPE_SPRITE_ANIMATION_ATLAS:
            case ImportUtils.TYPE_ANIMATION_PNG_SEQUENCE:
                dir = ProjectManager.SPRITE_DIR_PATH;
                ext = "atlas";
                break;
            case ImportUtils.TYPE_SHADER:
                dir = ProjectManager.SHADER_DIR_PATH;
                ext = "frag";
                break;
        }

        for (FileHandle file : fileHandles) {
            File f = new File(projectManager.getCurrentProjectPath() + File.separator + dir + File.separator + file.nameWithoutExtension() + "." + ext);
            exists = f.exists();
            if (exists)
                break;
        }

        return exists;
    }

    //TODO too weak, all assets inside the package should be checked for possible duplicate
    private boolean checkLibraryItemExistence(Array<FileHandle> fileHandles) {
        boolean exists = false;
        for (FileHandle file : fileHandles) {
            String itemName = ZipUtils.getZipContent(file.file(), "lib").get(0).replace(".lib", "");
            ProjectManager projectManager = HyperLap2DFacade.getInstance().retrieveProxy(ProjectManager.NAME);
            if (projectManager.getCurrentProjectInfoVO().libraryItems.get(itemName) != null) {
                exists = true;
            }
        }
        return exists;
    }
}
