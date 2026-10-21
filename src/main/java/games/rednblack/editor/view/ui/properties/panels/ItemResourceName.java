package games.rednblack.editor.view.ui.properties.panels;

import games.rednblack.editor.proxy.EntityDataProxy;
import games.rednblack.editor.renderer.components.NinePatchComponent;
import games.rednblack.editor.renderer.components.TextureRegionComponent;
import games.rednblack.editor.renderer.components.label.LabelComponent;
import games.rednblack.editor.renderer.components.light.LightObjectComponent;
import games.rednblack.editor.renderer.components.particle.ParticleComponent;
import games.rednblack.editor.renderer.components.sprite.SpriteAnimationComponent;
import games.rednblack.editor.renderer.components.widget.WidgetComponent;
import games.rednblack.h2d.extension.spine.SpineComponent;
import games.rednblack.h2d.extension.talos.TalosComponent;
import games.rednblack.h2d.extension.tinyvg.TinyVGComponent;

/**
 * What the properties panel writes under the item's kind: the asset it shows, the font a label is written in, or
 * the kind of widget a composite is. Null when there is nothing to say, a plain composite or a primitive.
 */
final class ItemResourceName {

    private ItemResourceName() {
    }

    static String of(int entity) {
        EntityDataProxy data = EntityDataProxy.get();

        WidgetComponent widget = data.get(entity, WidgetComponent.class);
        if (widget != null) return words(widget.widgetType) + " widget";

        SpineComponent spine = data.get(entity, SpineComponent.class);
        if (spine != null) return spine.animationName;
        TalosComponent talos = data.get(entity, TalosComponent.class);
        if (talos != null) return talos.particleName;
        TinyVGComponent tinyVG = data.get(entity, TinyVGComponent.class);
        if (tinyVG != null) return tinyVG.imageName;
        ParticleComponent particle = data.get(entity, ParticleComponent.class);
        if (particle != null) return particle.particleName;
        SpriteAnimationComponent sprite = data.get(entity, SpriteAnimationComponent.class);
        if (sprite != null) return sprite.animationName;
        NinePatchComponent ninePatch = data.get(entity, NinePatchComponent.class);
        if (ninePatch != null) return ninePatch.textureRegionName;

        LabelComponent label = data.get(entity, LabelComponent.class);
        if (label != null) {
            if (label.bitmapFont != null && !label.bitmapFont.isEmpty()) return label.bitmapFont;
            return label.fontName + ", " + label.fontSize + " px";
        }

        LightObjectComponent light = data.get(entity, LightObjectComponent.class);
        if (light != null) return light.type == LightObjectComponent.LightType.CONE ? "Cone light" : "Point light";

        // last: other kinds keep a texture region of their own too
        TextureRegionComponent region = data.get(entity, TextureRegionComponent.class);
        if (region != null) return region.regionName;
        return null;
    }

    /** "textField" as "Text field". */
    private static String words(String camelCase) {
        StringBuilder text = new StringBuilder(camelCase.length() + 4);
        for (int i = 0; i < camelCase.length(); i++) {
            char c = camelCase.charAt(i);
            if (i == 0) text.append(Character.toUpperCase(c));
            else if (Character.isUpperCase(c)) text.append(' ').append(Character.toLowerCase(c));
            else text.append(c);
        }
        return text.toString();
    }
}
