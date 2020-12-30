package games.rednblack.editor.view.ui.dialog;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.*;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.view.ui.Cursors;
import games.rednblack.h2d.common.view.ui.listener.CursorListener;
import games.rednblack.h2d.common.view.ui.listener.ScrollFocusListener;

public class ConsoleDialog extends VisDialog {

    private final HighlightTextArea textArea;

    public ConsoleDialog() {
        super("Console", "console");
        setModal(false);
        addCloseButton();
        this.getTitleTable().padTop(-15);

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
            s = s.replace("\t", "   ");
        textArea.appendText(s);
    }

    @Override
    public float getPrefWidth() {
        return Sandbox.getInstance().getUIStage().getWidth() * 0.7f;
    }

    @Override
    public float getPrefHeight() {
        return Sandbox.getInstance().getUIStage().getHeight() * 0.8f;
    }
}
