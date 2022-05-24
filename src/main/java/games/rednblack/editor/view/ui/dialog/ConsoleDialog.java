package games.rednblack.editor.view.ui.dialog;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.util.highlight.Highlight;
import com.kotcrab.vis.ui.util.highlight.HighlightRule;
import com.kotcrab.vis.ui.util.highlight.Highlighter;
import com.kotcrab.vis.ui.widget.H2DHighlightTextArea;
import com.kotcrab.vis.ui.widget.HighlightTextArea;
import com.kotcrab.vis.ui.widget.VisImageButton;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.H2DDialog;
import games.rednblack.h2d.common.util.H2DHighlight;
import games.rednblack.h2d.common.view.ui.Cursors;
import games.rednblack.h2d.common.view.ui.listener.CursorListener;
import games.rednblack.h2d.common.view.ui.listener.ScrollFocusListener;
import org.apache.commons.lang3.RegExUtils;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RegEx to identify a valid color markup, format:
 *            Text color: [RRGGBB] or [RRGGBBAA]
 *            Background color: [@RRGGBB] or [@RRGGBBAA]
 *            Text Style : [NORMAL] or [UNDERLINE] or [STRIKE]
 *
 *            [RESET] : set text color to [FFFFFF], background color to [@00000000] and style to [NORMAL]
 */
public class ConsoleDialog extends H2DDialog {

    private final HighlightTextArea textArea;
    private final FixedRule fixedRule;

    private final String regex = "\\[(@?[A-F0-9a-f]{6}|@?[A-F0-9a-f]{8}|NORMAL|UNDERLINE|STRIKE|RESET)\\]";
    private final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);

    private final HashMap<String, Color> colorCache = new HashMap<>();
    private Color lastColor = Color.WHITE;
    private Color lastBackgroundColor = Color.CLEAR;
    private H2DHighlight.TextFormat lastTextFormat = H2DHighlight.TextFormat.NORMAL;

    public static int MAX_TEXT_LENGTH = 10000;

    public ConsoleDialog() {
        super("Console", "console");
        setModal(false);
        setResizable(true);
        addCloseButton();

        fixedRule = new FixedRule();

        textArea = new H2DHighlightTextArea("") {
            @Override
            protected boolean onKeyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.V)
                    return true;
                return super.onKeyDown(event, keycode);
            }

            @Override
            protected boolean onKeyTyped(InputEvent event, char character) {
                return true;
            }

            @Override
            protected boolean onKeyUp(InputEvent event, int keycode) {
                if (keycode == Input.Keys.V)
                    return true;
                return super.onKeyUp(event, keycode);
            }

            @Override
            public void draw(Batch batch, float parentAlpha) {
                try {
                    super.draw(batch, parentAlpha);
                } catch (Exception ignore) {
                    //Ignore any exception that may occurs while drawing this
                }
            }
        };

        Highlighter highlighter = new Highlighter();
        highlighter.addRule(fixedRule);
        textArea.setHighlighter(highlighter);

        ScrollPane scrollPane = textArea.createCompatibleScrollPane();
        scrollPane.addListener(new ScrollFocusListener());
        textArea.addListener(new CursorListener(Cursors.TEXT, HyperLap2DFacade.getInstance()));
        getContentTable().add(scrollPane).padTop(5).grow().row();
    }

    @Override
    public void addCloseButton() {
        VisImageButton closeButton = new VisImageButton("close-properties");
        this.getTitleTable().add(closeButton).padRight(0).padTop(0);
        closeButton.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                close();
            }
        });
        closeButton.addListener(new ClickListener() {
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                event.cancel();
                return true;
            }
        });
    }

    public void write(String s) {
        if (s.contains("\t"))
            s = s.replaceAll("\t", "    ");

        Matcher matcher = pattern.matcher(s);

        int lastIndex = 0;
        int markupAccumulator = 0;

        int previousLength = textArea.getText().length();

        while (matcher.find()) {
            String colorHex = matcher.group(1);
            H2DHighlight.TextFormat textFormat = matchTextFormat(colorHex);
            Color color = lastColor;
            Color backgroundColor = lastBackgroundColor;

            if (colorHex.startsWith("@")) {
                textFormat = lastTextFormat;

                colorHex = colorHex.replace("@", "");
                colorCache.computeIfAbsent(colorHex, Color::valueOf);
                backgroundColor = colorCache.get(colorHex);
            } else if (textFormat == null) {
                textFormat = lastTextFormat;

                colorCache.computeIfAbsent(colorHex, Color::valueOf);
                color = colorCache.get(colorHex);
            } else if (textFormat == H2DHighlight.TextFormat.RESET) {
                color = Color.WHITE;
                backgroundColor = Color.CLEAR;
                textFormat = H2DHighlight.TextFormat.NORMAL;
            }

            int start = matcher.start();
            int end = matcher.end();

            int ruleStart = lastIndex - markupAccumulator;
            int ruleEnd = start - markupAccumulator;
            if (ruleStart < ruleEnd)
                fixedRule.add(lastColor, lastBackgroundColor,ruleStart + previousLength, ruleEnd + previousLength, lastTextFormat);

            lastIndex = end;
            lastColor = color;
            lastBackgroundColor = backgroundColor;
            lastTextFormat = textFormat;
            markupAccumulator += end - start;
        }

        if (!s.equals("\n")) {
            int ruleStart = lastIndex - markupAccumulator;
            int ruleEnd = s.length() - markupAccumulator;
            if (ruleStart < ruleEnd)
                fixedRule.add(lastColor, lastBackgroundColor, ruleStart + previousLength, ruleEnd + previousLength, lastTextFormat);
        }

        String output = RegExUtils.removeAll(s, pattern);

        if (textArea.getText().length() > MAX_TEXT_LENGTH) {
            textArea.setText(textArea.getText().substring(textArea.getText().length() - MAX_TEXT_LENGTH));
        }
        textArea.appendText(output);
        textArea.processHighlighter();
    }

    @Override
    public float getPrefWidth() {
        return Sandbox.getInstance().getUIStage().getWidth() * 0.5f;
    }

    @Override
    public float getPrefHeight() {
        return Sandbox.getInstance().getUIStage().getHeight() * 0.5f;
    }

    private static class FixedRule implements HighlightRule {
        Array<H2DHighlight> highlights = new Array<>();

        @Override
        public void process(HighlightTextArea textArea, Array<Highlight> highlights) {
            highlights.addAll(this.highlights);
        }

        public void add(Color color, Color backgroundColor, int start, int end, H2DHighlight.TextFormat textFormat) {
            if (highlights.size > 0) {
                H2DHighlight highlight = highlights.get(highlights.size - 1);
                //Merge contiguous (or separated with blank newline) rules without create new `Highlight` object
                if (color.equals(highlight.getColor()) && backgroundColor.equals(highlight.getBackgroundColor()) && textFormat.equals(highlight.getTextFormat())
                        && (highlight.getEnd() + 1 == start || highlight.getEnd() == start)) {
                    highlight.setEnd(end);
                    return;
                }
            }
            highlights.add(new H2DHighlight(color, backgroundColor, start, end, textFormat));
        }
    }

    private H2DHighlight.TextFormat matchTextFormat(String str) {
        switch (str) {
            case "UNDERLINE":
                return H2DHighlight.TextFormat.UNDERLINE;
            case "STRIKE":
                return H2DHighlight.TextFormat.STRIKE;
            case "NORMAL":
                return H2DHighlight.TextFormat.NORMAL;
            case "RESET":
                return H2DHighlight.TextFormat.RESET;
        }
        return null;
    }
}
