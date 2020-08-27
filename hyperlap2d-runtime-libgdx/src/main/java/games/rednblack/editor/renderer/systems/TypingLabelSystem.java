package games.rednblack.editor.renderer.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.rafaskoberg.gdx.typinglabel.TypingLabel;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.label.LabelComponent;
import games.rednblack.editor.renderer.components.label.TypingLabelComponent;

public class TypingLabelSystem extends IteratingSystem {
    private final ComponentMapper<LabelComponent> labelComponentMapper = ComponentMapper.getFor(LabelComponent.class);
    private final ComponentMapper<TypingLabelComponent> typingLabelComponentMapper = ComponentMapper.getFor(TypingLabelComponent.class);
    private final ComponentMapper<DimensionsComponent> dimensionsComponentMapper = ComponentMapper.getFor(DimensionsComponent.class);

    public TypingLabelSystem() {
        super(Family.all(LabelComponent.class, TypingLabelComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        TypingLabelComponent typingLabelComponent = typingLabelComponentMapper.get(entity);
        LabelComponent labelComponent = labelComponentMapper.get(entity);
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
        } else if (!typingLabelComponent.typingLabel.getOriginalText().equals(labelComponent.text)){
            typingLabelComponent.typingLabel.setText(labelComponent.text);
        }

        typingLabelComponent.typingLabel.act(deltaTime);
    }
}
