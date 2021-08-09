package games.rednblack.editor.view.ui;

import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.editor.utils.Guide;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.MsgAPI;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.mediator.Mediator;

/**
 * Created by azakhary on 7/18/2015.
 */
public class RulersUIMediator extends Mediator<RulersUI> {
    private static final String TAG = RulersUIMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    /**
     * Constructor.
     */
    public RulersUIMediator() {
        super(NAME, new RulersUI());
    }

    @Override
    public void onRegister() {
        facade = HyperLap2DFacade.getInstance();
        viewComponent.setVisible(false);
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                MsgAPI.SCENE_LOADED,
                RulersUI.ACTION_GUIDES_MODIFIED,
                MsgAPI.LOCK_LINES_CHANGED
        };
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);

        SceneVO sceneVO = Sandbox.getInstance().getSceneControl().getCurrentSceneVO();

        switch (notification.getName()) {
            case MsgAPI.SCENE_LOADED:
                Array<Guide> guides = new Array<>();
                for(int i  = 0; i < sceneVO.verticalGuides.size(); i++) {
                    Guide tmp = new Guide(true);
                    tmp.pos = sceneVO.verticalGuides.get(i);
                    guides.add(tmp);
                }
                for(int i  = 0; i < sceneVO.horizontalGuides.size(); i++) {
                    Guide tmp = new Guide(false);
                    tmp.pos = sceneVO.horizontalGuides.get(i);
                    guides.add(tmp);
                }

                viewComponent.setGuides(guides);

                viewComponent.setVisible(true);
                break;
            case RulersUI.ACTION_GUIDES_MODIFIED:
                guides = viewComponent.getGuides();
                sceneVO.verticalGuides.clear();
                sceneVO.horizontalGuides.clear();

                for(int i  = 0; i < guides.size; i++) {
                    if(guides.get(i).isVertical) {
                        sceneVO.verticalGuides.add(guides.get(i).pos);
                    } else {
                        sceneVO.horizontalGuides.add(guides.get(i).pos);
                    }
                }

                break;
            case MsgAPI.LOCK_LINES_CHANGED:
                Boolean lockLines = notification.getBody();
                if (lockLines != null) {
                    viewComponent.setLockLines(lockLines);
                }
                break;
        }
    }
}
