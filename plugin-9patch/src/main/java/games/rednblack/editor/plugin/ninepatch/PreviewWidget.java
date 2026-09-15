package games.rednblack.editor.plugin.ninepatch;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.renderer.data.FrameRange;
import games.rednblack.editor.renderer.data.TenPatchVO;
import games.rednblack.editor.renderer.tenpatch.TenPatchDrawable;
import games.rednblack.editor.renderer.tenpatch.TenPatchUtils;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * Shows the ten patch at three sizes over a checkerboard: a wide bar, a tall bar and a free box whose
 * size the user controls, to see stretching, tiling, crush modes and gradients at work. Tiles scroll
 * when a speed is set, exactly like at runtime.
 *
 * Created by azakhary on 8/19/2015.
 */
public class PreviewWidget extends Group {

    private static final float BAR = 44f;
    private static final float GAP = 6f;
    private static final Color OUTLINE = new Color(1f, 1f, 1f, 0.18f);

    private ShapeDrawer sd;
    private final Color tmp = new Color();

    private TextureAtlas.AtlasRegion region;
    private TenPatchVO vo;
    private Array<TextureAtlas.AtlasRegion> frames;
    private FrameRange frameRange;
    private int fps;
    private int playMode;
    private float customWidthFactor = 1f;
    private float customHeightFactor = 1f;

    private final Image wide = new Image();
    private final Image tall = new Image();
    private final Image custom = new Image();

    public PreviewWidget() {
        setTransform(false);
        addActor(wide);
        addActor(tall);
        addActor(custom);
    }

    /**
     * @param region graphic to preview
     * @param vo     configuration in pixels of {@code region}
     */
    public void update(TextureAtlas.AtlasRegion region, TenPatchVO vo) {
        update(region, vo, null, null, 24, TenPatchDrawable.PlayMode.LOOP);
    }

    /**
     * @param frames every frame of an animated 9-patch in order, null for a still one
     * @param range  frames to play, null for all of them
     */
    public void update(TextureAtlas.AtlasRegion region, TenPatchVO vo, Array<TextureAtlas.AtlasRegion> frames, FrameRange range, int fps, int playMode) {
        this.region = region;
        this.vo = vo;
        this.frames = frames;
        this.frameRange = range;
        this.fps = fps;
        this.playMode = playMode;
        rebuild();
    }

    /** Size of the free box as a fraction of the room it has, 0..1 on both axes. */
    public void setCustomSize(float widthFactor, float heightFactor) {
        customWidthFactor = widthFactor;
        customHeightFactor = heightFactor;
        rebuild();
    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        rebuild();
    }

    private void rebuild() {
        if (region == null || vo == null || getWidth() <= 0 || getHeight() <= 0) return;

        float slotWidth = getWidth() - BAR - GAP;
        float slotHeight = getHeight() - BAR - GAP;

        // the graphic is shown at one scale everywhere: the one that lets the free box hold it whole
        float scale = Math.min(1f, Math.min(slotWidth / region.originalWidth, slotHeight / region.originalHeight));

        wide.setDrawable(createDrawable(scale));
        wide.setBounds(0, getHeight() - BAR, getWidth(), BAR);

        tall.setDrawable(createDrawable(scale));
        tall.setBounds(0, 0, BAR, slotHeight);

        float customWidth = Math.max(1f, slotWidth * customWidthFactor);
        float customHeight = Math.max(1f, slotHeight * customHeightFactor);
        custom.setDrawable(createDrawable(scale));
        custom.setBounds(BAR + GAP + (slotWidth - customWidth) / 2f, (slotHeight - customHeight) / 2f, customWidth, customHeight);
    }

    private Drawable createDrawable(float scale) {
        TenPatchDrawable drawable = TenPatchUtils.createDrawable(region, vo);
        if (frames != null) TenPatchUtils.setAnimation(drawable, frames, frameRange, fps, playMode);
        TenPatchUtils.scaleDrawable(drawable, scale, scale);
        return drawable;
    }

    @Override
    protected void setStage(Stage stage) {
        super.setStage(stage);
        if (stage != null) {
            sd = new ShapeDrawer(stage.getBatch(), EditingZone.whiteRegion());
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (sd != null && region != null) {
            sd.update();
            float alpha = parentAlpha * getColor().a;
            drawBackdrop(wide, alpha);
            drawBackdrop(tall, alpha);
            drawBackdrop(custom, alpha);
        }
        super.draw(batch, parentAlpha);
    }

    private void drawBackdrop(Image image, float alpha) {
        float x = getX() + image.getX(), y = getY() + image.getY();
        EditingZone.drawCheckerboard(sd, x, y, image.getWidth(), image.getHeight(), alpha);
        tmp.set(OUTLINE);
        tmp.a *= alpha;
        sd.setColor(tmp);
        sd.rectangle(x, y, image.getWidth(), image.getHeight(), 1f);
    }
}
