package games.rednblack.editor.renderer.physics;

import com.badlogic.ashley.core.Entity;

public interface PhysicsContact {
    void beginContact(Entity contact);
    void endContact(Entity contact);
}
