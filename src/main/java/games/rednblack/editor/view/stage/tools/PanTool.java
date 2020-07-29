package games.rednblack.editor.view.stage.tools;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import games.rednblack.h2d.common.proxy.CursorManager;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.ResourceManager;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.h2d.common.view.ui.Cursors;
import games.rednblack.h2d.common.vo.SceneConfigVO;

/**
 * Created by CyberJoe on 5/1/2015.
 */
public class PanTool extends SimpleTool {
    private static final String EVENT_PREFIX = "games.rednblack.editor.view.stage.tools.PanTool";
    public static final String SCENE_PANNED = EVENT_PREFIX + ".SCENE_PANNED";

    public static final String NAME = "PAN_TOOL";

    private Vector2 lastCoordinates;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getShortcut() {
        return null;
    }

    @Override
    public String getTitle() {
        return "Pan Tool";
    }

    @Override
    public void initTool() {
        CursorManager cursorManager = HyperLap2DFacade.getInstance().retrieveProxy(CursorManager.NAME);
        cursorManager.setCursor(Cursors.HAND);
    }

    @Override
    public boolean stageMouseDown(float x, float y) {
        lastCoordinates = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        return false;
    }

    @Override
    public void stageMouseUp(float x, float y) {

    }

    @Override
    public void stageMouseDragged(float x, float y) {
        doPanning(x, y);
    }

    @Override
    public void stageMouseScrolled(int amount) {

    }

    @Override
    public void stageMouseDoubleClick(float x, float y) {

    }

    @Override
    public boolean itemMouseDown(Entity entity, float x, float y) {
        lastCoordinates = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        return true;
    }

    @Override
    public void itemMouseUp(Entity entity, float x, float y) {

    }

    @Override
    public void itemMouseDragged(Entity entity, float x, float y) {
        doPanning(x, y);
    }

    @Override
    public void itemMouseDoubleClick(Entity entity, float x, float y) {

    }

    private void doPanning(float x, float y) {
        Sandbox sandbox = Sandbox.getInstance();

        ResourceManager resourceManager = HyperLap2DFacade.getInstance().retrieveProxy(ResourceManager.NAME);
        OrthographicCamera camera = sandbox.getCamera();

        float currX = camera.position.x + (lastCoordinates.x - Gdx.input.getX()) * camera.zoom / resourceManager.getProjectVO().pixelToWorld;
        float currY = camera.position.y + (Gdx.input.getY() - lastCoordinates.y) * camera.zoom / resourceManager.getProjectVO().pixelToWorld;

        sandbox.getCamera().position.set(currX, currY, 0);

        lastCoordinates = new Vector2(Gdx.input.getX(), Gdx.input.getY());

        HyperLap2DFacade.getInstance().sendNotification(SCENE_PANNED);

        // Save the current position
        // TODO: (this has to move to some kind of mediator that listens to scene panned event)
        ProjectManager projectManager = HyperLap2DFacade.getInstance().retrieveProxy(ProjectManager.NAME);
        SceneConfigVO sceneConfigVO = projectManager.getCurrentSceneConfigVO();
        sceneConfigVO.cameraPosition[0] = sandbox.getCamera().position.x;
        sceneConfigVO.cameraPosition[1] = sandbox.getCamera().position.y;
    }
}
