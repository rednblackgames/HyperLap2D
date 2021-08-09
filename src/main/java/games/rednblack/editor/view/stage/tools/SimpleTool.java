package games.rednblack.editor.view.stage.tools;

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.ui.FollowersUIMediator;
import games.rednblack.editor.view.ui.followers.BasicFollower;
import games.rednblack.editor.view.ui.followers.NormalSelectionFollower;
import games.rednblack.h2d.common.view.tools.Tool;
import org.puremvc.java.interfaces.INotification;

import java.util.Set;

/**
 * Created by CyberJoe on 5/2/2015.
 */
public abstract class SimpleTool implements Tool {

    @Override
    public void initTool() {
        Sandbox sandbox = Sandbox.getInstance();
        Set<Integer> currSelection = sandbox.getSelector().getCurrentSelection();
        FollowersUIMediator followersUIMediator = HyperLap2DFacade.getInstance().retrieveMediator(FollowersUIMediator.NAME);
        for(int entity: currSelection) {
            BasicFollower follower = followersUIMediator.getFollower(entity);
            if(follower instanceof NormalSelectionFollower) {
                NormalSelectionFollower selectionFollower = (NormalSelectionFollower) follower;
                selectionFollower.clearSubFollowers();
            }
        }
    }

    @Override
    public String getName() {
        return "SIMPLE_TOOL";
    }

    @Override
    public void stageMouseDragged(float x, float y) {

    }

    @Override
    public boolean stageMouseDown(float x, float y) {
        return false;
    }

    @Override
    public void stageMouseUp(float x, float y) {

    }

    @Override
    public void stageMouseDoubleClick(float x, float y) {

    }

    @Override
    public boolean itemMouseDown(int entity, float x, float y) {
        return false;
    }

    @Override
    public void itemMouseUp(int entity, float x, float y) {

    }

    @Override
    public void itemMouseDragged(int entity, float x, float y) {

    }

    @Override
    public void itemMouseDoubleClick(int entity, float x, float y) {

    }

    @Override
    public void handleNotification(INotification notification) {

    }

    @Override
    public void keyDown(int entity, int keycode) {

    }

    @Override
    public void keyUp(int entity, int keycode) {

    }
}
