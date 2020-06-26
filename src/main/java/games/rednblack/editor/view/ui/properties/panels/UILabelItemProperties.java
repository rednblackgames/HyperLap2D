package games.rednblack.editor.view.ui.properties.panels;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.*;
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel;
import com.kotcrab.vis.ui.widget.spinner.Spinner;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.event.*;
import games.rednblack.editor.utils.StandardWidgetsFactory;
import games.rednblack.editor.view.ui.properties.UIItemCollapsibleProperties;

/**
 * Created by azakhary on 4/24/15.
 */
public class UILabelItemProperties extends UIItemCollapsibleProperties {

    public static final String prefix = "games.rednblack.editor.view.ui.properties.panels.UILabelItemProperties";

    public static final String LABEL_TEXT_CHAR_TYPED = prefix + ".LABEL_TEXT_CHANGED";

    private HashMap<Integer, String> alignMap = new HashMap<>();
    private Array<String> alignNames = new Array<>();

    private HyperLap2DFacade facade;

    private VisSelectBox<String> fontFamilySelectBox;
    private VisSelectBox<String> alignSelectBox;
    private VisCheckBox boldCheckBox;
    private VisCheckBox wrapCheckBox;
    private VisCheckBox italicCheckBox;
    private Spinner fontSizeField;
    private VisTextArea textArea;

    public UILabelItemProperties() {
        super("Label");
        facade = HyperLap2DFacade.getInstance();

        fontFamilySelectBox = StandardWidgetsFactory.createSelectBox(String.class);
        alignSelectBox = StandardWidgetsFactory.createSelectBox(String.class);
        boldCheckBox = StandardWidgetsFactory.createCheckBox();
        italicCheckBox = StandardWidgetsFactory.createCheckBox();
        wrapCheckBox = StandardWidgetsFactory.createCheckBox();
        fontSizeField = StandardWidgetsFactory.createNumberSelector(12, 0, 100);

        fontFamilySelectBox.setMaxListCount(10);
        alignSelectBox.setMaxListCount(10);

        VisTable textEditTable = new VisTable();
        textArea = StandardWidgetsFactory.createTextArea();

        mainTable.add(StandardWidgetsFactory.createLabel("Font Family", Align.right)).padRight(5).width(90).left();
        mainTable.add(fontFamilySelectBox).width(90).padRight(5);
        mainTable.row().padTop(5);

        /*mainTable.add(StandardWidgetsFactory.createLabel("Bold", Align.right)).padRight(5).width(90).left();
        mainTable.add(boldCheckBox).width(55).padRight(5);
        mainTable.row().padTop(5);
        mainTable.add(StandardWidgetsFactory.createLabel("Italic", Align.right)).padRight(5).width(90).left();
        mainTable.add(italicCheckBox).width(55).padRight(5);
        mainTable.row().padTop(5);*/

        mainTable.add(StandardWidgetsFactory.createLabel("Font Size", Align.right)).padRight(5).width(90).left();
        mainTable.add(fontSizeField).width(55).padRight(5);
        mainTable.row().padTop(5);
        mainTable.add(StandardWidgetsFactory.createLabel("Align", Align.right)).padRight(5).width(90).left();
        mainTable.add(alignSelectBox).width(90).padRight(5);
        mainTable.row().padTop(5);
        mainTable.add(textEditTable).colspan(2).width(200);
        mainTable.row().padTop(5);
        mainTable.add(StandardWidgetsFactory.createLabel("Wrap", Align.right)).padRight(5).width(90).left();
        mainTable.add(wrapCheckBox).width(55).padRight(5);
        mainTable.row().padTop(5);

        textEditTable.add(textArea).width(200).height(65);

        setListeners();
        setAlignList();
    }

    public String getFontFamily() {
        return fontFamilySelectBox.getSelected();
    }

    public boolean isBold() {
        return boldCheckBox.isChecked();
    }

    public boolean isItalic() {
        return italicCheckBox.isChecked();
    }

    public boolean isWrap() {
        return wrapCheckBox.isChecked();
    }

    public String getText() {
        return textArea.getText().replace("\\n", "\n");
    }

    public void setText(String text) {
        textArea.setText(text.replace("\n", "\\n"));
    }

    public void setWrap(boolean wrap) {
        wrapCheckBox.setChecked(wrap);
    }

    public void setAlignList() {
        alignMap.clear();
        alignNames.clear();

        alignMap.put(Align.topLeft, "Top Left");
        alignNames.add("Top Left");
        alignMap.put(Align.top, "Top");
        alignNames.add("Top");
        alignMap.put(Align.topRight, "Top Right");
        alignNames.add("Top Right");
        alignMap.put(Align.left, "Left");
        alignNames.add("Left");
        alignMap.put(Align.center, "Center");
        alignNames.add("Center");
        alignMap.put(Align.right, "Right");
        alignNames.add("Right");
        alignMap.put(Align.bottomLeft, "Bottom Left");
        alignNames.add("Bottom Left");
        alignMap.put(Align.bottom, "Bottom");
        alignNames.add("Bottom");
        alignMap.put(Align.bottomRight, "Bottom Right");
        alignNames.add("Bottom Right");

        alignSelectBox.setItems(alignNames);
    }

    public void setAlignValue(int align) {
        alignSelectBox.setSelected(alignMap.get(align));
    }

    public int getAlignValue() {
        for (Map.Entry<Integer, String> entry : alignMap.entrySet()) {
            Integer key = entry.getKey();
            String value = entry.getValue();
            if(alignSelectBox.getSelected().equals(value)) {
                return key;
            }
        }
        return 0;
    }


    public void setFontFamilyList(Array<String> fontFamilies) {
        fontFamilySelectBox.setItems(fontFamilies);
    }

    public void setFontFamily(String name) {
        fontFamilySelectBox.setSelected(name);
    }

    public void setStyle(boolean bold, boolean italic) {
        boldCheckBox.setChecked(bold);
        italicCheckBox.setChecked(italic);
    }

    public int getFontSize() {
        return ((IntSpinnerModel)fontSizeField.getModel()).getValue();
    }

    public void setFontSize(int fontSize) {
        ((IntSpinnerModel)fontSizeField.getModel()).setValue(fontSize);
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    private void setListeners() {
        final String eventName = getUpdateEventName();
        fontFamilySelectBox.addListener(new SelectBoxChangeListener(eventName));
        alignSelectBox.addListener(new SelectBoxChangeListener(eventName));
        boldCheckBox.addListener(new CheckBoxChangeListener(eventName));
        italicCheckBox.addListener(new CheckBoxChangeListener(eventName));
        wrapCheckBox.addListener(new CheckBoxChangeListener(eventName));
        fontSizeField.addListener(new NumberSelectorOverlapListener(eventName));
        textArea.addListener(new KeyboardListener(eventName));
//        textArea.addListener(textArea.new TextAreaListener() {
//            @Override
//            public boolean keyTyped(InputEvent event, char character) {
//                facade.sendNotification(LABEL_TEXT_CHAR_TYPED, null);
//                return true;//super.keyTyped(event, character);
//            }
//        });

    }


}
