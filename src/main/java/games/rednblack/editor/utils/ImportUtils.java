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
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
    public static final int TYPE_TTF_FONT = 6;
    public static final int TYPE_BITMAP_FONT = 7;
    public static final int TYPE_PARTICLE_EFFECT = 8;
    public static final int TYPE_TEXTURE_ATLAS = 9;
    public static final int TYPE_SHADER = 10;
    public static final int TYPE_HYPERLAP2D_LIBRARY = 11;
    public static final int TYPE_HYPERLAP2D_INTERNAL_LIBRARY = 12;
    public static final int TYPE_TALOS_VFX = 13;
    public static final int TYPE_HYPERLAP2D_ACTION = 14;

    private final ArrayList<Integer> supportedTypes = new ArrayList<>();
    private final FileTypeFilter fileTypeFilter;

    private ImportUtils() {
        supportedTypes.add(TYPE_IMAGE);
        supportedTypes.add(TYPE_ANIMATION_PNG_SEQUENCE);
        supportedTypes.add(TYPE_SPRITE_ANIMATION_ATLAS);
        supportedTypes.add(TYPE_SPINE_ANIMATION);
        supportedTypes.add(TYPE_PARTICLE_EFFECT);
        supportedTypes.add(TYPE_TALOS_VFX);
        supportedTypes.add(TYPE_SHADER);
        supportedTypes.add(TYPE_HYPERLAP2D_LIBRARY);
        supportedTypes.add(TYPE_HYPERLAP2D_ACTION);
        // TODO: not supported yet
        //supportedTypes.add(TYPE_TEXTURE_ATLAS);

        fileTypeFilter = new FileTypeFilter(false);

        fileTypeFilter.addRule("All Supported (*.png, *.atlas, *.p, *.json, *.vert, *.frag, *.h2dlib, *.h2daction)", "png", "atlas", "p", "json", "vert", "frag", "h2dlib", "h2daction");
        fileTypeFilter.addRule("PNG File (*.png)", "png");
        fileTypeFilter.addRule("Sprite Animation Atlas File (*.atlas)", "atlas");
        fileTypeFilter.addRule("libGDX/Talos Particle Effect (*.p)", "p");
        fileTypeFilter.addRule("Spine Animation (*.json)", "json");
        fileTypeFilter.addRule("Shader (*.vert, *.frag)", "vert", "frag");
        fileTypeFilter.addRule("HyperLap2D Library (*.h2dlib)", "h2dlib");
        fileTypeFilter.addRule("HyperLap2D Action (*.h2daction)", "h2daction");
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

    public static boolean isAtlasAnimationSequence(Array<TextureAtlas.TextureAtlasData.Region> regions) {
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

    public static Array<File> getAtlasPages(FileHandle fileHandle) {
        Array<File> imgs = new Array<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileHandle.read()), 64);
            while (true) {
                String line = reader.readLine();
                if (line == null) break;
                //In atlas file format the name of the png is is preceded by an empty line
                if (line.trim().length() == 0) {
                    line = reader.readLine();
                    imgs.add(new File(FilenameUtils.getFullPath(fileHandle.path()) + line));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imgs;
    }

    public static String getAtlasName(FileHandle fileHandle) {
        String name = "atlas";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileHandle.read()), 64);
            while (true) {
                String line = reader.readLine();
                if (line == null) break;
                if (line.trim().contains("repeat:")) {
                    line = reader.readLine();
                    name = line;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return name;
    }

    public static Array<FileHandle> getAtlasPageHandles(FileHandle fileHandle) {
        Array<File> imgs = getAtlasPages(fileHandle);

        Array<FileHandle> imgHandles = new Array<>();
        for (int i = 0; i < imgs.size; i++) {
            imgHandles.add(new FileHandle(imgs.get(i)));
        }

        return imgHandles;
    }
}
