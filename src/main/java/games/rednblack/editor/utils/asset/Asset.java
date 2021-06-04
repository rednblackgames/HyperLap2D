package games.rednblack.editor.utils.asset;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.ResolutionManager;
import games.rednblack.editor.proxy.ResourceManager;
import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.editor.utils.ImportUtils;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.ProgressHandler;
import org.puremvc.java.patterns.facade.Facade;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Asset implements IAsset {

    protected Facade facade;
    protected ProjectManager projectManager;
    protected ResolutionManager resolutionManager;
    protected ResourceManager resourceManager;

    public Asset() {
        facade = HyperLap2DFacade.getInstance();
        projectManager = facade.retrieveProxy(ProjectManager.NAME);
        resolutionManager = facade.retrieveProxy(ResolutionManager.NAME);
        resourceManager = facade.retrieveProxy(ResourceManager.NAME);
    }

    @Override
    public int matchType(Array<FileHandle> files) {
        for (FileHandle file : new Array.ArrayIterator<>(files)) {
            if (!matchMimeType(file))
                return ImportUtils.TYPE_UNKNOWN;
        }
        return getType();
    }

    protected abstract boolean matchMimeType(FileHandle file);

    protected abstract int getType();

    public void asyncImport(Array<FileHandle> files, ProgressHandler progressHandler, boolean skipRepack) {
        if (files == null) {
            progressHandler.progressChanged(100);
            progressHandler.progressComplete();
            return;
        }

        // save before importing
        SceneVO vo = Sandbox.getInstance().sceneVoFromItems();
        projectManager.saveCurrentProject(vo);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> importAsset(files, progressHandler, skipRepack));
        executor.execute(() -> {
            progressHandler.progressChanged(100);
            projectManager.saveCurrentProject();
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            progressHandler.progressComplete();
        });
        executor.shutdown();
    }
}
