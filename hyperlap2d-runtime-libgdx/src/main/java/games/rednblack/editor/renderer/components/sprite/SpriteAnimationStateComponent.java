package games.rednblack.editor.renderer.components.sprite;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.renderer.data.FrameRange;

import java.util.Comparator;

public class SpriteAnimationStateComponent implements Component {
    public Array<TextureAtlas.AtlasRegion> allRegions;
	public Animation<TextureRegion> currentAnimation;
	public float time = 0.0f;

    public  boolean paused = false;

    public SpriteAnimationStateComponent(Array<TextureAtlas.AtlasRegion> allRegions) {
        this.allRegions = sortAndGetRegions(allRegions);
    }
	
	public Animation<TextureRegion> get() {
		return currentAnimation;
	}

    public void set(SpriteAnimationComponent sac) {
        set(sac.frameRangeMap.get(sac.currentAnimation), sac.fps, sac.playMode);
    }

    public void set(FrameRange range, int fps, Animation.PlayMode playMode) {
        Array<TextureAtlas.AtlasRegion> textureRegions = new Array<>(range.endFrame - range.startFrame + 1);
        for (int r = range.startFrame; r <= range.endFrame; r++) {
            textureRegions.add(allRegions.get(r));
        }
        currentAnimation =  new Animation<>(1f/fps, textureRegions, playMode);
        time = 0.0f;
    }

    private Array<TextureAtlas.AtlasRegion> sortAndGetRegions(Array<TextureAtlas.AtlasRegion> regions) {
        regions.sort(new SortRegionsComparator());

        return regions;
    }

    private static class SortRegionsComparator implements Comparator<TextureAtlas.AtlasRegion> {
        @Override
        public int compare(TextureAtlas.AtlasRegion o1, TextureAtlas.AtlasRegion o2) {
            return Integer.compare(o1.index, o2.index);
        }
    }
}
