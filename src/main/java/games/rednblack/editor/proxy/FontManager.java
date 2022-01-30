package games.rednblack.editor.proxy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.utils.AppConfig;
import games.rednblack.editor.utils.NativeDialogs;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.SystemUtils;
import org.puremvc.java.patterns.proxy.Proxy;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.*;

/**
 * Created by azakhary on 4/24/2015.
 */
public class FontManager extends Proxy {

    private static final String TAG = FontManager.class.getCanonicalName();
    public static final String NAME = TAG;

    private static final String cache_name = "hyperlap2d-fonts-cache";

    private Preferences prefs;

    private HashMap<String, String> systemFontMap = new HashMap<>();

    public FontManager() {
        super(NAME);
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();
        prefs = Gdx.app.getPreferences(cache_name);
        generateFontsMap();
    }

    public String[] getSystemFontsPaths() {
        String[] result;
        if (SystemUtils.IS_OS_WINDOWS) {
            result = new String[1];
            String path = System.getenv("WINDIR");
            result[0] = path + "\\" + "Fonts";
            return result;
        } else if (SystemUtils.IS_OS_MAC_OSX || SystemUtils.IS_OS_MAC) {
            result = new String[3];
            result[0] = System.getProperty("user.home") + File.separator + "Library/Fonts";
            result[1] = "/Library/Fonts";
            result[2] = "/System/Library/Fonts";
            return result;
        } else if (SystemUtils.IS_OS_LINUX) {
            String[] pathsToCheck = {
                    System.getProperty("user.home") + File.separator + ".local/share/fonts",
                    "/usr/share/fonts/truetype",
                    "/usr/share/fonts/TTF"
            };
            ArrayList<String> resultList = new ArrayList<>();

            for (int i = pathsToCheck.length - 1; i >= 0; i--) {
                String path = pathsToCheck[i];
                File tmp = new File(path);
                if (tmp.exists() && tmp.isDirectory() && tmp.canRead()) {
                    resultList.add(path);
                }
            }

            if (resultList.isEmpty()) {
                NativeDialogs.showError("No Font detected on your System.\n"
                        + SystemUtils.OS_NAME + " " + SystemUtils.OS_VERSION
                        + " (HyperLap2D v" + AppConfig.getInstance().versionString + ")");
                result = new String[0];
            }
            else {
                result = new String[resultList.size()];
                result = resultList.toArray(result);
            }

            return result;
        }

        return null;
    }

    public List<File> getSystemFontFiles() {
        // only retrieving ttf files
        String[] extensions = new String[]{"ttf", "TTF"};
        String[] paths = getSystemFontsPaths();

        ArrayList<File> files = new ArrayList<>();

        for (int i = 0; i < paths.length; i++) {
            File fontDirectory = new File(paths[i]);
            if (!fontDirectory.exists()) break;
            files.addAll(FileUtils.listFiles(fontDirectory, extensions, true));
        }

        return files;
    }

    public void preCacheSystemFontsMap() {
        List<File> fontFiles = getSystemFontFiles();

        for (File file : fontFiles) {
            Font f;
            try {
                if (!systemFontMap.containsValue(file.getAbsolutePath())) {
                    f = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(file.getAbsolutePath()));
                    systemFontMap.put(f.getName(), file.getAbsolutePath());
                }
            } catch (FontFormatException | IOException e) {
                System.err.println("Could not load " + file.getName() + " font file.");
            }
        }

        prefs.put(systemFontMap);
        prefs.flush();
    }

    public void loadCachedSystemFontMap() {
        systemFontMap = (HashMap<String, String>) prefs.get();
    }

    public void generateFontsMap() {
        loadCachedSystemFontMap();
        preCacheSystemFontsMap();
    }

    public HashMap<String, String> getFontsMap() {
        return systemFontMap;
    }

    public Array<String> getFontNamesFromMap() {
        AlphabeticalComparator comparator = new AlphabeticalComparator();
        Array<String> fontNames = new Array<>();

        for (Map.Entry<String, String> entry : systemFontMap.entrySet()) {
            fontNames.add(entry.getKey());
        }
        fontNames.sort(comparator);

        return fontNames;
    }

    public FileHandle getTTFByName(String fontName) {
        return new FileHandle(systemFontMap.get(fontName));
    }

    public String getShortName(String longName) {
        String path = systemFontMap.get(longName);
        return FilenameUtils.getBaseName(path);
    }

    public String getFontFilePath(String fontFaily) {
        return systemFontMap.get(fontFaily);
    }


    public static class AlphabeticalComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    }
}
