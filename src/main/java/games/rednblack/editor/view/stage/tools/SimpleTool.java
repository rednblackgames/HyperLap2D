package games.rednblack.editor.view.stage.tools;

import com.badlogic.ashley.core.Entity;
import games.rednblack.h2d.common.view.tools.Tool;
import com.puremvc.patterns.observer.Notification;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.ui.FollowersUIMediator;
import games.rednblack.editor.view.ui.followers.BasicFollower;
import games.rednblack.editor.view.ui.followers.NormalSelectionFollower;

import java.util.Set;

/**
 * Created by CyberJoe on 5/2/2015.
 */
public abstract class SimpleTool implements Tool {

    @Override
    public void initTool() {
        Sandbox sandbox = Sandbox.getInstance();
        Set<Entity> currSelection = sandbox.getSelector().getCurrentSelection();
        FollowersUIMediator followersUIMediator = HyperLap2DFacade.getInstance().retrieveMediator(FollowersUIMediator.NAME);
        for(Entity entity: currSelection) {
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
    public boolean itemMouseDown(Entity entity, float x, float y) {
        return false;
    }

    @Override
    public void itemMouseUp(Entity entity, float x, float y) {

    }

    @Override
    public void itemMouseDragged(Entity entity, float x, float y) {

    }

    @Override
    public void itemMouseDoubleClick(Entity entity, float x, float y) {

    }

    @Override
    public void handleNotification(Notification notification) {

    }

    @Override
    public void keyDown(Entity entity, int keycode) {

    }

    @Override
    public void keyUp(Entity entity, int keycode) {

    }
}
