package games.rednblack.editor.view.ui.followers;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.scenes.scene2d.Group;
import org.puremvc.java.interfaces.INotification;

/**
 * Created by CyberJoe on 7/2/2015.
 */
public abstract class SubFollower extends Group {

    protected Entity entity;
    protected BasicFollower parentFollower;

    public SubFollower(Entity entity) {
        setItem(entity);
        create();
    }

    private void setItem(Entity entity) {
        this.entity = entity;
    }


    public void handleNotification(INotification notification) {

    }

    public void setParentFollower(BasicFollower parent) {
        this.parentFollower = parent;
    }

    @Override
    public boolean remove() {
        parentFollower = null;
        return super.remove();
    }

    public abstract void create();
    public abstract void update();
}
