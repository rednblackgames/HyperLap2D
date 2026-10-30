package games.rednblack.editor.view.ui.properties.panels;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.util.Validators;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextArea;
import com.kotcrab.vis.ui.widget.VisValidatableTextField;
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel;
import com.kotcrab.vis.ui.widget.spinner.Spinner;
import games.rednblack.editor.code.syntax.TypingLabelSyntax;
import games.rednblack.editor.event.CheckBoxChangeListener;
import games.rednblack.editor.event.KeyboardListener;
import games.rednblack.editor.event.NumberSelectorOverlapListener;
import games.rednblack.editor.event.SelectBoxChangeListener;
import games.rednblack.editor.view.ui.properties.RemoteEditablePanel;
import games.rednblack.editor.view.ui.properties.RemoteEditableSupport;
import games.rednblack.editor.view.ui.properties.UIItemCollapsibleProperties;
import games.rednblack.editor.view.ui.widget.actors.ExpandableTextArea;
import games.rednblack.h2d.common.view.ui.PropertyGrid;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.h2d.common.view.ui.widget.TintButton;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by azakhary on 4/24/15.
 */
public class UILabelItemProperties extends UIItemCollapsibleProperties implements RemoteEditablePanel {

    public static final String prefix = "games.rednblack.editor.view.ui.properties.panels.UILabelItemProperties";

    public static final String LABEL_TEXT_CHAR_TYPED = prefix + ".LABEL_TEXT_CHANGED";
    public static final String LABEL_TEXT_EXPAND_SAVED = prefix + ".LABEL_TEXT_EXPAND_SAVED";
    public static final String BORDER_COLOR_BUTTON_CLICKED = prefix + ".BORDER_COLOR_BUTTON_CLICKED";
    public static final String NONE_BITMAP_FONT = "<None>";

    private HashMap<Integer, String> alignMap = new HashMap<>();
    private Array<String> alignNames = new Array<>();

    private VisSelectBox<String> fontFamilySelectBox, bitmapFontSelectBox;
    private VisSelectBox<String> alignSelectBox;
    private VisCheckBox boldCheckBox;
    private VisCheckBox wrapCheckBox;
    private VisCheckBox monoCheckBox;
    private VisCheckBox fitWidthCheckBox;
    private VisCheckBox fitHeightCheckBox;
    private VisCheckBox italicCheckBox;
    private Spinner fontSizeField;
    private VisValidatableTextField borderWidthField;
    private TintButton borderColorButton;
    private VisTextArea textArea;

    public UILabelItemProperties() {
        super("Label");

        bitmapFontSelectBox = StandardWidgetsFactory.createSelectBox(String.class);
        fontFamilySelectBox = StandardWidgetsFactory.createSelectBox(String.class);
        alignSelectBox = StandardWidgetsFactory.createSelectBox(String.class);
        boldCheckBox = StandardWidgetsFactory.createCheckBox();
        italicCheckBox = StandardWidgetsFactory.createCheckBox();
        wrapCheckBox = StandardWidgetsFactory.createSwitch();
        monoCheckBox = StandardWidgetsFactory.createSwitch();
        fitWidthCheckBox = StandardWidgetsFactory.createSwitch();
        fitHeightCheckBox = StandardWidgetsFactory.createSwitch();
        fontSizeField = StandardWidgetsFactory.createNumberSelector(12, 1, 500);
        borderWidthField = StandardWidgetsFactory.createValidableTextField(new Validators.FloatValidator());
        borderColorButton = StandardWidgetsFactory.createTintButton();

        fontFamilySelectBox.setMaxListCount(10);
        alignSelectBox.setMaxListCount(10);

        VisTable textEditTable = new VisTable();
        ExpandableTextArea textAreaTable = new ExpandableTextArea(facade, LABEL_TEXT_EXPAND_SAVED);
        textAreaTable.setSyntax(new TypingLabelSyntax());
        textArea = textAreaTable.getTextArea();

        PropertyGrid grid = PropertyGrid.on(mainTable);
        grid.row("Bitmap font", bitmapFontSelectBox);
        grid.row("Font name", fontFamilySelectBox);
        grid.row("Font size", fontSizeField);
        grid.row("Outline", borderWidthField);
        grid.row("Outline color", borderColorButton);
        grid.row("Align", alignSelectBox);
        grid.wideFill(textEditTable);
        grid.toggle("Wrap", wrapCheckBox);
        grid.toggle("Mono space", monoCheckBox);
        grid.toggle("Fit text width", fitWidthCheckBox);
        grid.toggle("Fit text height", fitHeightCheckBox);

        PropertyGrid.elastic(textEditTable.add(textAreaTable));

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

    /** As wide as its text, on one line: the label sizes itself, and never wraps. */
    public boolean isFitWidth() {
        return fitWidthCheckBox.isChecked();
    }

    /** The widget holding this label writes its words, so there is nothing here to type. */
    public void setTextOwnedByWidget(boolean owned) {
        textArea.setDisabled(owned);
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

    public void setFitWidth(boolean fitWidth) {
        fitWidthCheckBox.setChecked(fitWidth);
    }

    /** As tall as its text, wrapped in the width it has: the label sizes itself. */
    public boolean isFitHeight() {
        return fitHeightCheckBox.isChecked();
    }

    public void setFitHeight(boolean fitHeight) {
        fitHeightCheckBox.setChecked(fitHeight);
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

    /** The outline FreeType draws around the glyphs, in pixels at the font's size: 0 for none. */
    public float getBorderWidth() {
        return Math.max(0, NumberUtils.toFloat(borderWidthField.getText()));
    }

    public void setBorderWidth(float borderWidth) {
        borderWidthField.setText(borderWidth + "");
    }

    public Color getBorderColor() {
        return borderColorButton.getColorValue();
    }

    public void setBorderColor(Color color) {
        borderColorButton.setColorValue(color);
    }

    // ---- RemoteEditablePanel ----

    @Override
    public void setFieldValue(String key, Object value) {
        if (value == null) throw new IllegalArgumentException("null value for field: " + key);
        switch (key) {
            case "text": setText(value.toString()); break;
            case "fontFamily":
                if (!contains(fontFamilySelectBox, value.toString())) {
                    throw new IllegalArgumentException("fontFamily '" + value + "' not available");
                }
                setFontFamily(value.toString()); break;
            case "bitmapFont":
                if (!contains(bitmapFontSelectBox, value.toString())) {
                    throw new IllegalArgumentException("bitmapFont '" + value + "' not available");
                }
                setBitmapFontFamily(value.toString()); break;
            case "fontSize":
                setFontSize(value instanceof Number ? ((Number) value).intValue() : Integer.parseInt(value.toString())); break;
            case "align": {
                // Case-insensitive match against the allowed align names ("Top Left", "Center", ...).
                String want = value.toString();
                String match = null;
                for (String s : alignNames) {
                    if (s.equalsIgnoreCase(want)) { match = s; break; }
                }
                if (match == null) {
                    throw new IllegalArgumentException("align '" + value + "' not valid; allowed: " + alignMap.values());
                }
                alignSelectBox.setSelected(match); break;
            }
            case "wrap": setWrap(toBool(value)); break;
            case "mono": setMono(toBool(value)); break;
            case "fitWidth": setFitWidth(toBool(value)); break;
            case "fitHeight": setFitHeight(toBool(value)); break;
            case "borderWidth": setBorderWidth(Math.max(0, RemoteEditableSupport.toFloat(value))); break;
            case "borderColor": setBorderColor(RemoteEditableSupport.toColor(value)); break;
            default:
                throw new IllegalArgumentException("Unknown or unsupported field: " + key
                        + " (supported: text, fontFamily, bitmapFont, fontSize, align, wrap, mono, fitWidth, fitHeight, "
                        + "borderWidth, borderColor)");
        }
    }

    @Override
    public java.util.List<String> validateFieldValues() {
        java.util.List<String> errors = new java.util.ArrayList<>(); // allowed-values enforced in setFieldValue; fontSize clamped by spinner (1-500)
        RemoteEditableSupport.checkValid("borderWidth", borderWidthField, errors);
        return errors;
    }

    private static boolean contains(VisSelectBox<String> box, String v) {
        for (String s : box.getItems()) {
            if (s.equals(v)) return true;
        }
        return false;
    }

    private static boolean toBool(Object value) {
        if (value instanceof Boolean) return (Boolean) value;
        return Boolean.parseBoolean(value.toString());
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
                borderWidthField.setDisabled(bitmapFontSelectBox.getSelectedIndex() != 0);
            }
        });
        fontFamilySelectBox.addListener(new SelectBoxChangeListener(eventName));
        alignSelectBox.addListener(new SelectBoxChangeListener(eventName));
        boldCheckBox.addListener(new CheckBoxChangeListener(eventName));
        italicCheckBox.addListener(new CheckBoxChangeListener(eventName));
        wrapCheckBox.addListener(new CheckBoxChangeListener(eventName));
        monoCheckBox.addListener(new CheckBoxChangeListener(eventName));
        fitWidthCheckBox.addListener(new CheckBoxChangeListener(eventName));
        fitHeightCheckBox.addListener(new CheckBoxChangeListener(eventName));
        fontSizeField.addListener(new NumberSelectorOverlapListener(eventName));
        borderWidthField.addListener(new KeyboardListener(eventName));
        borderColorButton.addListener(new ClickListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                facade.sendNotification(BORDER_COLOR_BUTTON_CLICKED, borderColorButton.getColorValue(), null);
            }
        });
        textArea.addListener(new KeyboardListener(eventName));
    }
}
