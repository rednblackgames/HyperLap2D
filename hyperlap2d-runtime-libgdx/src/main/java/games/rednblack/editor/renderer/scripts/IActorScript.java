package games.rednblack.editor.renderer.scripts;

import games.rednblack.editor.renderer.scene2d.CompositeActor;


public interface IActorScript {
    public void init(CompositeActor entity);

    public void act(float delta);

    public void dispose();
}
