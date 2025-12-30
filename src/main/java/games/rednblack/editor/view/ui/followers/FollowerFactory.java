package games.rednblack.editor.view.ui.followers;

import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.h2d.extension.spine.SpineItemType;
import games.rednblack.h2d.extension.talos.TalosItemType;

public class FollowerFactory {
    public static BasicFollower createFollower(int entity) {
        return switch (EntityUtils.getType(entity)) {
            case EntityFactory.IMAGE_TYPE -> new ImageFollower(entity);
            case EntityFactory.LIGHT_TYPE -> new LightFollower(entity);
            case TalosItemType.TALOS_TYPE -> new TalosFollower(entity);
            case EntityFactory.PARTICLE_TYPE -> new ParticleFollower(entity);
            case SpineItemType.SPINE_TYPE -> new SpineFollower(entity);
            default -> new NormalSelectionFollower(entity);
        };
    }
}
