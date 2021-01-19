package games.rednblack.editor.view.ui.dialog;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.util.highlight.Highlight;
import com.kotcrab.vis.ui.util.highlight.HighlightRule;
import com.kotcrab.vis.ui.util.highlight.Highlighter;
import com.kotcrab.vis.ui.widget.*;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.H2DDialog;
import games.rednblack.h2d.common.view.ui.Cursors;
import games.rednblack.h2d.common.view.ui.listener.CursorListener;
import games.rednblack.h2d.common.view.ui.listener.ScrollFocusListener;
import org.apache.commons.lang3.RegExUtils;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConsoleDialog extends H2DDialog {

    private final HighlightTextArea textArea;
    private final FixedRule fixedRule;

    //RegEx to identify a valid color markup in format [RRGGBB] or [RRGGBBAA]
    private final String regex = "\\[([^\\]G-Zg-z]{6}|[^\\]G-Zg-z]{8}|NORMAL|UNDERLINE|STRIKE)\\]";
    private final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);

    private final HashMap<String, Color> colorCache = new HashMap<>();
    private Color lastColor = Color.WHITE;
    private ConsoleHighlight.TextFormat lastTextFormat = ConsoleHighlight.TextFormat.NORMAL;

    public ConsoleDialog() {
        super("Console", "console");
        setModal(false);
        setResizable(true);
        addCloseButton();

        fixedRule = new FixedRule();

        textArea = new ConsoleTextArea("");

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
            ConsoleHighlight.TextFormat textFormat = matchTextFormat(colorHex);
            Color color = lastColor;
            if (textFormat == null) {
                textFormat = ConsoleHighlight.TextFormat.NORMAL;
                colorCache.computeIfAbsent(colorHex, Color::valueOf);
                color = colorCache.get(colorHex);
            }

            int start = matcher.start();
            int end = matcher.end();

            int ruleStart = lastIndex - markupAccumulator;
            int ruleEnd = start - markupAccumulator;
            if (ruleStart < ruleEnd)
                fixedRule.add(lastColor, ruleStart + previousLength, ruleEnd + previousLength, lastTextFormat);

            lastIndex = end;
            lastColor = color;
            lastTextFormat = textFormat;
            markupAccumulator += end - start;
        }

        if (!s.equals("\n")) {
            int ruleStart = lastIndex - markupAccumulator;
            int ruleEnd = s.length() - markupAccumulator;
            if (ruleStart < ruleEnd)
                fixedRule.add(lastColor, ruleStart + previousLength, ruleEnd + previousLength, lastTextFormat);
        }

        String output = RegExUtils.removeAll(s, pattern);

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
        Array<ConsoleHighlight> highlights = new Array<>();

        @Override
        public void process(HighlightTextArea textArea, Array<Highlight> highlights) {
            highlights.addAll(this.highlights);
        }

        public void add(Color color, int start, int end, ConsoleHighlight.TextFormat textFormat) {
            if (highlights.size > 0) {
                ConsoleHighlight highlight = highlights.get(highlights.size - 1);
                //Merge contiguous (or separated with blank newline) rules without create new `Highlight` object
                if (color.equals(highlight.getColor()) && textFormat.equals(highlight.getTextFormat())
                        && (highlight.getEnd() + 1 == start || highlight.getEnd() == start)) {
                    highlight.setEnd(end);
                    return;
                }
            }
            highlights.add(new ConsoleHighlight(color, start, end, textFormat));
        }
    }

    private ConsoleHighlight.TextFormat matchTextFormat(String str) {
        switch (str) {
            case "UNDERLINE":
                return ConsoleHighlight.TextFormat.UNDERLINE;
            case "STRIKE":
                return ConsoleHighlight.TextFormat.STRIKE;
            case "NORMAL":
                return ConsoleHighlight.TextFormat.NORMAL;
        }
        return null;
    }
}
