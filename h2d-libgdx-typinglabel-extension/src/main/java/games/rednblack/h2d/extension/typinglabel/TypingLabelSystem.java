package games.rednblack.h2d.extension.typinglabel;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.rafaskoberg.gdx.typinglabel.TypingLabel;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.label.LabelComponent;

@All(LabelComponent.class)
public class TypingLabelSystem extends IteratingSystem {
    protected ComponentMapper<LabelComponent> labelComponentMapper;
    protected ComponentMapper<TypingLabelComponent> typingLabelComponentMapper;
    protected ComponentMapper<DimensionsComponent> dimensionsComponentMapper;

    @Override
    protected void process(int entity) {
        LabelComponent labelComponent = labelComponentMapper.get(entity);
        TypingLabelComponent typingLabelComponent = typingLabelComponentMapper.get(entity);
        labelComponent.typingEffect = typingLabelComponent != null;
        if (!labelComponent.typingEffect) return;

        DimensionsComponent dimensionsComponent = dimensionsComponentMapper.get(entity);

        if (typingLabelComponent.typingLabel == null) {
            typingLabelComponent.typingLabel = new TypingLabel(labelComponent.text, labelComponent.style);
            BitmapFont font = typingLabelComponent.typingLabel.getBitmapFontCache().getFont();

            float fontScaleX = labelComponent.fontScaleX;
            float fontScaleY = labelComponent.fontScaleY;

            if (fontScaleX != 1 || fontScaleY != 1) font.getData().setScale(fontScaleX, fontScaleY);
            typingLabelComponent.typingLabel.setSize(dimensionsComponent.width, dimensionsComponent.height);
            typingLabelComponent.typingLabel.setWrap(labelComponent.wrap);
            typingLabelComponent.typingLabel.setAlignment(labelComponent.labelAlign, labelComponent.lineAlign);
        } else {
            if (!typingLabelComponent.typingLabel.getOriginalText().equals(labelComponent.text)){
                typingLabelComponent.typingLabel.setText(labelComponent.text);
            }
            if (typingLabelComponent.typingLabel.getWrap() != labelComponent.wrap) {
                typingLabelComponent.typingLabel.setWrap(labelComponent.wrap);
            }
            if (typingLabelComponent.typingLabel.getLabelAlign() != labelComponent.labelAlign
                    || typingLabelComponent.typingLabel.getLineAlign() != labelComponent.lineAlign) {
                typingLabelComponent.typingLabel.setAlignment(labelComponent.labelAlign, labelComponent.lineAlign);
            }
            if (typingLabelComponent.typingLabel.getWidth() != dimensionsComponent.width) {
                typingLabelComponent.typingLabel.setWidth(dimensionsComponent.width);
            }
            if (typingLabelComponent.typingLabel.getHeight() != dimensionsComponent.height) {
                typingLabelComponent.typingLabel.setHeight(dimensionsComponent.height);
            }
        }

        typingLabelComponent.typingLabel.act(world.getDelta());
    }
}

