package games.rednblack.editor.data.migrations.data020;

import games.rednblack.editor.renderer.data.*;
import games.rednblack.h2d.extension.spine.SpineVO;
import games.rednblack.h2d.extension.talos.TalosVO;

import java.util.ArrayList;
import java.util.HashMap;

public class CompositeVO {
    public ArrayList<SimpleImageVO> sImages = new ArrayList<>(1);
    public ArrayList<Image9patchVO> sImage9patchs = new ArrayList<>(1);
    public ArrayList<LabelVO> sLabels = new ArrayList<>(1);
    public ArrayList<CompositeItemVO> sComposites = new ArrayList<>(1);
    public ArrayList<ParticleEffectVO> sParticleEffects = new ArrayList<>(1);
    public ArrayList<TalosVO> sTalosVFX = new ArrayList<>(1);
    public ArrayList<LightVO> sLights = new ArrayList<>(1);
    public ArrayList<SpineVO> sSpineAnimations = new ArrayList<>(1);
    public ArrayList<SpriteAnimationVO> sSpriteAnimations = new ArrayList<>(1);
    public ArrayList<ColorPrimitiveVO> sColorPrimitives = new ArrayList<>(1);

    public ArrayList<LayerItemVO> layers = new ArrayList<LayerItemVO>();

    public HashMap<String, StickyNoteVO> sStickyNotes = new HashMap<>(1);
}
