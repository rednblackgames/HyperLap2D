package games.rednblack.editor.plugin.tiled.view;

import com.kotcrab.vis.ui.util.FloatDigitsOnlyFilter;
import com.kotcrab.vis.ui.widget.VisTextField;
import games.rednblack.editor.plugin.tiled.data.AttributeVO;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

/**
 * Number field bound to an {@link AttributeVO}: every edit writes the value back to the VO.
 *
 * Created by mariam on 2/5/16.
 */
public final class Attribute {

    private Attribute() {
    }

    public static VisTextField createField(AttributeVO attributeVO) {
        VisTextField field = StandardWidgetsFactory.createTextField();
        field.setTextFieldFilter(new FloatDigitsOnlyFilter(attributeVO.acceptNegativeValues));
        field.setMaxLength(5);
        field.setText(attributeVO.value + "");
        field.setTextFieldListener((VisTextField textField, char c) -> {
            if (!textField.getText().equals("")) {
                attributeVO.value = Float.parseFloat(textField.getText());
            }
        });
        return field;
    }
}
