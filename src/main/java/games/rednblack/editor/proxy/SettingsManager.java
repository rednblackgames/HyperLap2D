package games.rednblack.editor.proxy;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import games.rednblack.editor.CustomExceptionHandler;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.Main;
import games.rednblack.editor.renderer.utils.HyperJson;
import games.rednblack.editor.utils.HyperLap2DUtils;
import games.rednblack.editor.utils.KeyBindingsLayout;
import games.rednblack.h2d.common.vo.EditorConfigVO;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.puremvc.java.patterns.proxy.Proxy;

import java.io.*;
import java.util.List;

public class SettingsManager extends Proxy {
    private static final String TAG = SettingsManager.class.getCanonicalName();
    public static final String NAME = TAG;

    private String DEFAULT_FOLDER = "HyperLap2D";

    private String defaultWorkspacePath;
    public EditorConfigVO editorConfigVO;

    public File[] pluginDirs;
    public File cacheDir;

    public SettingsManager() {
        super(NAME);
        initWorkspace();
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();
        KeyBindingsLayout.init();
    }

    private void initWorkspace() {
        try {
            editorConfigVO = getEditorConfig();
            String myDocPath = HyperLap2DUtils.MY_DOCUMENTS_PATH;
            defaultWorkspacePath = myDocPath + File.separator + DEFAULT_FOLDER;
            FileUtils.forceMkdir(new File(defaultWorkspacePath));
            FileUtils.forceMkdir(new File(HyperLap2DUtils.getKeyMapPath()));

            pluginDirs = new File[]{new File(Main.getJarContainingFolder(Main.class) + File.separator + "plugins"),
                    new File(HyperLap2DUtils.getRootPath() + File.separator + "plugins"),
                    new File(System.getProperty("user.dir") + File.separator + "plugins")};
            cacheDir = new File(HyperLap2DUtils.getRootPath() + File.separator + "cache");
            FileUtils.forceMkdir(cacheDir);
        } catch (IOException e) {
            final Writer result = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(result);
            e.printStackTrace(printWriter);
            e.printStackTrace();
            String stacktrace = result.toString();
            CustomExceptionHandler.showErrorDialog(stacktrace);
        }
    }

    public FileHandle getWorkspacePath() {
        if (!editorConfigVO.lastOpenedSystemPath.isEmpty()) {
            return new FileHandle(editorConfigVO.lastOpenedSystemPath);
        }
        return new FileHandle(defaultWorkspacePath);
    }

    public FileHandle getImportPath() {
        if (!editorConfigVO.lastImportedSystemPath.isEmpty()) {
            return new FileHandle(editorConfigVO.lastImportedSystemPath);
        }
        return null;
    }

    public String[] getKeyMappingFiles() {
        File mappingDir = new File(HyperLap2DUtils.getKeyMapPath());
        String[] extensions = new String[]{"keymap"};
        List<File> files = (List<File>) FileUtils.listFiles(mappingDir, extensions, true);
        String[] maps = new String[files.size() + 1];
        maps[0] = "default";
        for (int i = 0; i < files.size(); i++) {
            maps[i + 1] = FilenameUtils.removeExtension(files.get(i).getName());
        }
        return maps;
    }

    private EditorConfigVO getEditorConfig() {
        EditorConfigVO editorConfig = new EditorConfigVO();
        String configFilePath = HyperLap2DUtils.getRootPath() + File.separator + "configs" + File.separator + EditorConfigVO.EDITOR_CONFIG_FILE;
        File configFile = new File(configFilePath);
        if (!configFile.exists()) {
            try {
                FileUtils.writeStringToFile(new File(configFilePath), editorConfig.constructJsonString(), "utf-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Json json = HyperJson.getJson();
            String editorConfigJson;
            try {
                editorConfigJson = FileUtils.readFileToString(configFile, "utf-8");
                editorConfig = json.fromJson(EditorConfigVO.class, editorConfigJson);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return editorConfig;
    }

    public void setLastOpenedPath(String path) {
        editorConfigVO.lastOpenedSystemPath = path;
        saveEditorConfig();
    }

    public void setLastImportedPath(String path) {
        editorConfigVO.lastImportedSystemPath = path;
        saveEditorConfig();
    }

    public void saveEditorConfig() {
        try {
            String configFilePath = HyperLap2DUtils.getRootPath() + File.separator + "configs" + File.separator + EditorConfigVO.EDITOR_CONFIG_FILE;
            FileUtils.writeStringToFile(new File(configFilePath), editorConfigVO.constructJsonString(), "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
