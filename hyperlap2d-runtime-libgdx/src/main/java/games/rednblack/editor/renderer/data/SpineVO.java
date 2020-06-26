package games.rednblack.editor.renderer.data;

import com.badlogic.ashley.core.Entity;
import games.rednblack.editor.renderer.components.SpineDataComponent;

public class SpineVO extends MainItemVO {

    public String animationName = "";
    public String currentAnimationName = "";

    public SpineVO() {

    }

    public SpineVO(SpineVO vo) {
        super(vo);
        animationName = vo.animationName;
        currentAnimationName = vo.currentAnimationName;
    }

    @Override
    public void loadFromEntity(Entity entity) {
        super.loadFromEntity(entity);

        SpineDataComponent spineDataComponent = entity.getComponent(SpineDataComponent.class);
        animationName = spineDataComponent.animationName;
        currentAnimationName = spineDataComponent.currentAnimationName;
    }
}
