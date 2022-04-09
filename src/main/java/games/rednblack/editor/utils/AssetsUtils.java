package games.rednblack.editor.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.file.FileTypeFilter;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class AssetsUtils {

    private static AssetsUtils instance;

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
    public static final int TYPE_TINY_VG = 15;

    private final FileTypeFilter fileTypeFilter;

    private AssetsUtils() {
        fileTypeFilter = new FileTypeFilter(false);

        fileTypeFilter.addRule("All Supported (*.png, *.atlas, *.p, *.json, *.vert, *.frag, *.fnt, *.h2dlib, *.h2daction)", "png", "atlas", "p", "json", "vert", "frag", "fnt", "tvg", "h2dlib", "h2daction");
        fileTypeFilter.addRule("PNG File (*.png)", "png");
        fileTypeFilter.addRule("Sprite Animation Atlas File (*.atlas)", "atlas");
        fileTypeFilter.addRule("libGDX/Talos Particle Effect (*.p)", "p");
        fileTypeFilter.addRule("Spine Animation (*.json)", "json");
        fileTypeFilter.addRule("Shader (*.vert, *.frag)", "vert", "frag");
        fileTypeFilter.addRule("BitmapFont (*.fnt)", "fnt");
        fileTypeFilter.addRule("TinyVG (*.tvg)", "tvg");
        fileTypeFilter.addRule("HyperLap2D Library (*.h2dlib)", "h2dlib");
        fileTypeFilter.addRule("HyperLap2D Action (*.h2daction)", "h2daction");
    }

    public static AssetsUtils getInstance() {
        if (instance == null) {
            instance = new AssetsUtils();
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

        return regions.get(regions.size - 1).index == regions.size - 1 + regions.get(0).index;
    }

    public static void unpackAtlasIntoTmpFolder(File atlasFile, String prefix, String tmpDir) {
        FileHandle atlasFileHandle = new FileHandle(atlasFile);
        TextureAtlas.TextureAtlasData atlasData = new TextureAtlas.TextureAtlasData(atlasFileHandle, atlasFileHandle.parent(), false);
        TextureUnpacker unpacker = new TextureUnpacker();
        unpacker.splitAtlas(atlasData, prefix, tmpDir);
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

    public static boolean deleteDirectory(String path) {
        File file = new File(path);
        if (file.exists()) {
            try {
                FileUtils.deleteDirectory(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!file.exists()) {
                return true;
            }
        }
        return false;
    }
}
