package games.rednblack.editor.view.stage.tools;

import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.SandboxMediator;
import games.rednblack.editor.view.ui.FollowersUIMediator;
import games.rednblack.editor.view.ui.followers.BasicFollower;
import games.rednblack.editor.view.ui.followers.NormalSelectionFollower;
import games.rednblack.h2d.common.view.tools.Tool;
import games.rednblack.puremvc.Facade;
import games.rednblack.puremvc.interfaces.INotification;

import java.util.Set;

/**
 * Created by CyberJoe on 5/2/2015.
 */
public abstract class SimpleTool implements Tool {

    /**
     * Injected by {@code UIToolBoxMediator} when the editor tools are created, so
     * editor tools stop calling {@code Facade.getInstance()}/{@code Sandbox.getInstance()}
     * (plugin tools implement {@link Tool} directly and are unaffected).
     */
    protected Facade facade;

    /** Lazily retrieves the Sandbox via the facade (SandboxMediator is registered after UIToolBoxMediator). */
    protected Sandbox getSandbox() {
        return facade.retrieveMediator(SandboxMediator.NAME).getViewComponent();
    }

    /** Called by {@code UIToolBoxMediator} after constructing each editor tool. */
    public void initFacade(Facade facade) {
        this.facade = facade;
    }

    @Override
    public void initTool() {
        Sandbox sandbox = getSandbox();
        Set<Integer> currSelection = sandbox.getSelector().getCurrentSelection();
        FollowersUIMediator followersUIMediator = facade.retrieveMediator(FollowersUIMediator.NAME);
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
