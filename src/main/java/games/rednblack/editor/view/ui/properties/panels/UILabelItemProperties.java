package games.rednblack.editor.view.ui.properties.panels;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextArea;
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel;
import com.kotcrab.vis.ui.widget.spinner.Spinner;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.code.syntax.TypingLabelSyntax;
import games.rednblack.editor.event.CheckBoxChangeListener;
import games.rednblack.editor.event.KeyboardListener;
import games.rednblack.editor.event.NumberSelectorOverlapListener;
import games.rednblack.editor.event.SelectBoxChangeListener;
import games.rednblack.editor.view.ui.properties.UIItemCollapsibleProperties;
import games.rednblack.editor.view.ui.widget.actors.ExpandableTextArea;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by azakhary on 4/24/15.
 */
public class UILabelItemProperties extends UIItemCollapsibleProperties {

    public static final String prefix = "games.rednblack.editor.view.ui.properties.panels.UILabelItemProperties";

    public static final String LABEL_TEXT_CHAR_TYPED = prefix + ".LABEL_TEXT_CHANGED";
    public static final String LABEL_TEXT_EXPAND_SAVED = prefix + ".LABEL_TEXT_EXPAND_SAVED";
    public static final String NONE_BITMAP_FONT = "<None>";

    private HashMap<Integer, String> alignMap = new HashMap<>();
    private Array<String> alignNames = new Array<>();

    private HyperLap2DFacade facade;

    private VisSelectBox<String> fontFamilySelectBox, bitmapFontSelectBox;
    private VisSelectBox<String> alignSelectBox;
    private VisCheckBox boldCheckBox;
    private VisCheckBox wrapCheckBox;
    private VisCheckBox monoCheckBox;
    private VisCheckBox italicCheckBox;
    private Spinner fontSizeField;
    private VisTextArea textArea;

    public UILabelItemProperties() {
        super("Label");
        facade = HyperLap2DFacade.getInstance();

        bitmapFontSelectBox = StandardWidgetsFactory.createSelectBox(String.class);
        fontFamilySelectBox = StandardWidgetsFactory.createSelectBox(String.class);
        alignSelectBox = StandardWidgetsFactory.createSelectBox(String.class);
        boldCheckBox = StandardWidgetsFactory.createCheckBox();
        italicCheckBox = StandardWidgetsFactory.createCheckBox();
        wrapCheckBox = StandardWidgetsFactory.createCheckBox("Wrap");
        monoCheckBox = StandardWidgetsFactory.createCheckBox("Mono Space");
        fontSizeField = StandardWidgetsFactory.createNumberSelector(12, 1, 500);

        fontFamilySelectBox.setMaxListCount(10);
        alignSelectBox.setMaxListCount(10);

        VisTable textEditTable = new VisTable();
        ExpandableTextArea textAreaTable = new ExpandableTextArea(facade, LABEL_TEXT_EXPAND_SAVED);
        textAreaTable.setSyntax(new TypingLabelSyntax());
        textArea = textAreaTable.getTextArea();

        mainTable.add(StandardWidgetsFactory.createLabel("Bitmap Font", Align.right)).padRight(5).width(90).left();
        mainTable.add(bitmapFontSelectBox).width(90).padRight(5);
        mainTable.row().padTop(5);

        mainTable.add(StandardWidgetsFactory.createLabel("Font Name", Align.right)).padRight(5).width(90).left();
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
        mainTable.add(wrapCheckBox).padRight(5);
        mainTable.add(monoCheckBox).padRight(5);
        mainTable.row().padTop(5);

        textEditTable.add(textAreaTable).width(200);

        setListeners();
        setAlignList();
    }

    public String getBitmapFont() {
        return bitmapFontSelectBox.getSelected();
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

    public boolean isMono() {
        return monoCheckBox.isChecked();
    }

    public String getText() {
        return textArea.getText();
    }

    public void setText(String text) {
        textArea.setText(text);
    }

    public void setWrap(boolean wrap) {
        wrapCheckBox.setChecked(wrap);
    }

    public void setMono(boolean mono) {
        monoCheckBox.setChecked(mono);
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

    public void setBitmapFontList(HashMap<String, BitmapFont> fontFamilies) {
        Array<String> tmp = new Array<>();
        tmp.add(NONE_BITMAP_FONT);
        for (String name : fontFamilies.keySet())
            tmp.add(name);
        bitmapFontSelectBox.setItems(tmp);
    }

    public void setBitmapFontFamily(String name) {
        bitmapFontSelectBox.setSelected(name);
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
        bitmapFontSelectBox.addListener(new SelectBoxChangeListener(eventName));
        bitmapFontSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                fontFamilySelectBox.setDisabled(bitmapFontSelectBox.getSelectedIndex() != 0);
                fontSizeField.setDisabled(bitmapFontSelectBox.getSelectedIndex() != 0);
            }
        });
        fontFamilySelectBox.addListener(new SelectBoxChangeListener(eventName));
        alignSelectBox.addListener(new SelectBoxChangeListener(eventName));
        boldCheckBox.addListener(new CheckBoxChangeListener(eventName));
        italicCheckBox.addListener(new CheckBoxChangeListener(eventName));
        wrapCheckBox.addListener(new CheckBoxChangeListener(eventName));
        monoCheckBox.addListener(new CheckBoxChangeListener(eventName));
        fontSizeField.addListener(new NumberSelectorOverlapListener(eventName));
        textArea.addListener(new KeyboardListener(eventName));
    }
}
