package games.rednblack.editor.view.ui.dialog;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.kotcrab.vis.ui.util.highlight.Highlight;
import com.kotcrab.vis.ui.util.highlight.HighlightRule;
import com.kotcrab.vis.ui.util.highlight.Highlighter;
import com.kotcrab.vis.ui.widget.*;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.view.ui.Cursors;
import games.rednblack.h2d.common.view.ui.listener.CursorListener;
import games.rednblack.h2d.common.view.ui.listener.ScrollFocusListener;
import org.apache.commons.lang3.RegExUtils;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConsoleDialog extends VisDialog {

    private final HighlightTextArea textArea;
    private final FixedRule fixedRule;

    //RegEx to identify a valid color markup in format [RRGGBB] or [RRGGBBAA]
    private final String regex = "\\[([^\\]G-Zg-z]{6}|[^\\]G-Zg-z]{8})\\]";
    private final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);

    private final HashMap<String, Color> colorCache = new HashMap<>();
    private Color lastColor = Color.WHITE;

    public ConsoleDialog() {
        super("Console", "console");
        setModal(false);
        addCloseButton();
        this.getTitleTable().padTop(-15);

        fixedRule = new FixedRule();

        textArea = new HighlightTextArea("", "console") {
            @Override
            protected InputListener createInputListener() {
                return new TextAreaListener() {
                    @Override
                    public boolean keyDown(InputEvent event, int keycode) {
                        if (keycode == Input.Keys.V)
                            return true;
                        return super.keyDown(event, keycode);
                    }

                    @Override
                    public boolean keyTyped(InputEvent event, char character) {
                        return false;
                    }

                    @Override
                    public boolean keyUp(InputEvent event, int keycode) {
                        if (keycode == Input.Keys.V)
                            return true;
                        return super.keyUp(event, keycode);
                    }
                };
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
        getContentTable().add(scrollPane).padTop(20).grow().row();
    }

    @Override
    public void addCloseButton() {
        VisImageButton closeButton = new VisImageButton("close-window");
        this.getTitleTable().add(closeButton).padRight(0).padTop(40);
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

    @Override
    public void close() {
        super.close();
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
            colorCache.computeIfAbsent(colorHex, Color::valueOf);
            Color color = colorCache.get(colorHex);

            int start = matcher.start();
            int end = matcher.end();

            int ruleStart = lastIndex - markupAccumulator;
            int ruleEnd = start - markupAccumulator;
            if (ruleStart < ruleEnd)
                fixedRule.add(lastColor, ruleStart + previousLength, ruleEnd + previousLength);

            lastIndex = end;
            lastColor = color;
            markupAccumulator += end - start;
        }

        if (!s.equals("\n")) {
            int ruleStart = lastIndex - markupAccumulator;
            int ruleEnd = s.length() - markupAccumulator;
            if (ruleStart < ruleEnd)
                fixedRule.add(lastColor, ruleStart + previousLength, ruleEnd + previousLength);
        }

        String output = RegExUtils.removeAll(s, pattern);

        textArea.appendText(output);
        textArea.processHighlighter();
    }

    @Override
    public float getPrefWidth() {
        return Sandbox.getInstance().getUIStage().getWidth() * 0.7f;
    }

    @Override
    public float getPrefHeight() {
        return Sandbox.getInstance().getUIStage().getHeight() * 0.8f;
    }

    private static class FixedRule implements HighlightRule {
        Array<Highlight> highlights = new Array<>();

        @Override
        public void process(HighlightTextArea textArea, Array<Highlight> highlights) {
            highlights.addAll(this.highlights);
        }

        public void add(Color color, int start, int end) {
            if (highlights.size > 0) {
                Highlight highlight = highlights.get(highlights.size - 1);
                //Merge contiguous (or separated with blank newline) rules without create new `Highlight` object
                if (color.equals(highlight.getColor()) && (highlight.getEnd() + 1 == start || highlight.getEnd() == start)) {
                    //Using reflection because fields in `Highlight` class are private
                    try {
                        Field endField = ClassReflection.getDeclaredField(Highlight.class, "end");
                        endField.setAccessible(true);
                        endField.set(highlight, end);
                        return;
                    } catch (ReflectionException ignore) {
                    }
                }
            }
            highlights.add(new Highlight(color, start, end));
        }
    }
}
