package games.rednblack.editor.proxy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.puremvc.patterns.proxy.BaseProxy;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.utils.HyperLap2DUtils;
import games.rednblack.h2d.common.vo.EditorConfigVO;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class SettingsManager extends BaseProxy {
    private static final String TAG = SettingsManager.class.getCanonicalName();
    public static final String NAME = TAG;

    private String DEFAULT_FOLDER = "HyperLap2D";

    private String defaultWorkspacePath;
    public EditorConfigVO editorConfigVO;

    public SettingsManager() {
        super(NAME);
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();
        initWorkspace();
    }

    private void initWorkspace() {
        try {
            editorConfigVO = getEditorConfig();
            String myDocPath = HyperLap2DUtils.MY_DOCUMENTS_PATH;
            defaultWorkspacePath = myDocPath + File.separator + DEFAULT_FOLDER;
            FileUtils.forceMkdir(new File(defaultWorkspacePath));
        } catch (IOException e) {
            e.printStackTrace();
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

    private EditorConfigVO getEditorConfig() {
        EditorConfigVO editorConfig = new EditorConfigVO();
        String configFilePath = getRootPath() + File.separator + "configs" + File.separator + EditorConfigVO.EDITOR_CONFIG_FILE;
        File configFile = new File(configFilePath);
        if (!configFile.exists()) {
            try {
                FileUtils.writeStringToFile(new File(configFilePath), editorConfig.constructJsonString(), "utf-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Json gson = new Json();
            String editorConfigJson = null;
            try {
                editorConfigJson = FileUtils.readFileToString(Gdx.files.absolute(configFilePath).file());
                editorConfig = gson.fromJson(EditorConfigVO.class, editorConfigJson);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return editorConfig;
    }

    public String getRootPath() {
        File root = new File(new File(".").getAbsolutePath()).getParentFile();
        return root.getAbsolutePath() + File.separator + ".hyperlap2d";
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
            String configFilePath = getRootPath() + File.separator + "configs" + File.separator +  EditorConfigVO.EDITOR_CONFIG_FILE;
            FileUtils.writeStringToFile(new File(configFilePath), editorConfigVO.constructJsonString(), "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
