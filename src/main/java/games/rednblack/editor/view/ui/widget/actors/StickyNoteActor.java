package games.rednblack.editor.view.ui.widget.actors;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisTextArea;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.kotcrab.vis.ui.widget.color.ColorPicker;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.ModifyStickyNoteCommand;
import games.rednblack.editor.renderer.data.StickyNoteVO;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.h2d.common.view.ui.widget.H2DPopupMenu;
import games.rednblack.h2d.common.view.ui.widget.HyperLapColorPicker;

public class StickyNoteActor extends VisWindow {
    public String id;

    private final int MOVE = 1 << 5;

    private float worldX, worldY;
    private final Vector2 tmp = new Vector2();
    private final VisTextArea contentArea;
    private final HyperLap2DFacade facade = HyperLap2DFacade.getInstance();
    private final VisImage pinButton;

    private int resizeBorder = 8;

    public StickyNoteActor(String id) {
        super("", "sticky-note");
        pinButton = new VisImage("pin");
        pinButton.setX(-pinButton.getWidth() / 2f);
        pinButton.setY(-pinButton.getHeight() / 2f);
        this.getTitleTable().addActor(pinButton);

        setMoveListener();

        this.id = id;

        setKeepWithinParent(false);
        setKeepWithinStage(false);
        setResizable(true);
        setMovable(false);

        contentArea = StandardWidgetsFactory.createTextArea("sticky-note");
        contentArea.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return button == Input.Buttons.RIGHT || super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (button == Input.Buttons.RIGHT) {
                    showPopupMenu();
                }
            }
        });
        add(contentArea).padTop(5).padLeft(5).grow();
        setOrigin(Align.topLeft);
    }

    @Override
    public void close() {
        Action action = Actions.parallel(Actions.rotateBy(-35, .25f, Interpolation.swingIn),
                Actions.sequence(Actions.alpha(0, .25f), Actions.run(this::remove)));
        addAction(action);
    }

    public void show(Group parent) {
        parent.addActor(this);
        Action action = Actions.parallel(Actions.parallel(Actions.alpha(0), Actions.alpha(1, 0.125f)),
                Actions.sequence(Actions.rotateBy(20),
                        Actions.rotateBy(-35, .25f, Interpolation.smoother),
                        Actions.rotateBy(15, .25f, Interpolation.swingOut)));
        addAction(action);
    }

    /**
     * Override stage position to world position
     * @param x position in World space
     * @param y position in World space
     */
    @Override
    public void setPosition(float x, float y) {
        worldX = x;
        worldY = y;
    }

    /**
     * Override stage position to world position
     * @param x position in World space
     */
    @Override
    public void setX(float x) {
        worldX = x;
    }

    /**
     * Override stage position to world position
     * @param y position in World space
     */
    @Override
    public void setY(float y) {
        worldY = y;
    }

    /**
     * Override stage position to world position
     * @param x position in World space
     * @param y position in World space
     * @param width width size
     * @param height height size
     */
    @Override
    public void setBounds(float x, float y, float width, float height) {
        tmp.set(x, y);
        Sandbox.getInstance().screenToWorld(tmp);
        worldX = tmp.x;
        worldY = tmp.y;

        setSize(width, height);
    }

    @Override
    public void setSize(float width, float height) {
        super.setSize(width, height);
        setOrigin(Align.topLeft);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        tmp.set(worldX, worldY);
        Sandbox.getInstance().worldToScreen(tmp);
        float scale = Sandbox.getInstance().getZoomPercent() / 100f;
        setScale(Math.min(Math.max(scale, 0.3f), 1f));
        super.setX(tmp.x);
        super.setY(tmp.y - ((1 - getScaleY()) * getHeight())); //Correct y position when scale
        super.draw(batch, parentAlpha);
    }

    public String getContent() {
        return contentArea.getText();
    }

    public float getWorldX() {
        return worldX;
    }

    public float getWorldY() {
        return worldY;
    }

    public void setContent(String content) {
        contentArea.setText(content);
    }

    @Override
    public void setResizeBorder(int resizeBorder) {
        this.resizeBorder = resizeBorder;
        super.setResizeBorder(resizeBorder);
    }

    private void setMoveListener() {
        clearListeners();
        pinButton.addListener(new InputListener() {
            float startX, startY;
            private final Vector2 tmp = new Vector2();

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                event.stop();
                dragging = true;
                startX = x;
                startY = y;
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                dragging = false;
                StickyNoteVO payload = ModifyStickyNoteCommand.payload(StickyNoteActor.this);
                facade.sendNotification(MsgAPI.ACTION_MODIFY_STICKY_NOTE, payload);
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                event.stop();
                float windowX = getX(), windowY = getY();
                float amountX = x - startX, amountY = y - startY;

                windowX += amountX * getScaleX();
                windowY += amountY * getScaleY();

                setBounds(Math.round(windowX), Math.round(windowY + ((1 - getScaleY()) * getHeight())), getWidth(), getHeight());
            }
        });
        addListener(new InputListener() {
            float startX, startY, lastX, lastY;

            private void updateEdge (float x, float y) {
                float border = resizeBorder / 2f;
                float width = getWidth(), height = getHeight();
                float padTop = getPadTop(), padLeft = getPadLeft(), padBottom = getPadBottom(), padRight = getPadRight();
                float left = padLeft, right = width - padRight, bottom = padBottom;
                edge = 0;
                if (isResizable() && x >= left - border && x <= right + border && y >= bottom - border) {
                    if (x < left + border) edge |= Align.left;
                    if (x > right - border) edge |= Align.right;
                    if (y < bottom + border) edge |= Align.bottom;
                    if (edge != 0) border += 25;
                    if (x < left + border) edge |= Align.left;
                    if (x > right - border) edge |= Align.right;
                    if (y < bottom + border) edge |= Align.bottom;
                }
                if (isMovable() && edge == 0 && y <= height && y >= height - padTop && x >= left && x <= right) edge = MOVE;
            }

            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                if (button == Input.Buttons.LEFT) {
                    updateEdge(x, y);
                    dragging = edge != 0;
                    startX = x;
                    startY = y;
                    lastX = x - getWidth();
                    lastY = y - getHeight();
                }
                return edge != 0 || isModal();
            }

            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                if (button == Input.Buttons.LEFT) {
                    dragging = false;
                    StickyNoteVO payload = ModifyStickyNoteCommand.payload(StickyNoteActor.this);
                    facade.sendNotification(MsgAPI.ACTION_MODIFY_STICKY_NOTE, payload);
                } else if (button == Input.Buttons.RIGHT) {
                    showPopupMenu();
                }
            }

            public void touchDragged (InputEvent event, float x, float y, int pointer) {
                if (!dragging) return;
                float width = getWidth(), height = getHeight();
                float windowX = getX(), windowY = getY();

                float minWidth = getMinWidth();
                float minHeight = getMinHeight();

                if ((edge & MOVE) != 0) {
                    float amountX = x - startX, amountY = y - startY;
                    windowX += amountX * getScaleX();
                    windowY += amountY * getScaleY();
                }
                if ((edge & Align.left) != 0) {
                    float amountX = x - startX;
                    if (width - amountX < minWidth) amountX = -(minWidth - width);
                    width -= amountX;
                    windowX += amountX * getScaleX();
                }
                if ((edge & Align.bottom) != 0) {
                    float amountY = y - startY;
                    if (height - amountY < minHeight) amountY = -(minHeight - height);
                    height -= amountY;
                    windowY += amountY * getScaleY();
                }
                if ((edge & Align.right) != 0) {
                    float amountX = x - lastX - width;
                    if (width + amountX < minWidth) amountX = minWidth - width;
                    width += amountX;
                }
                if ((edge & Align.top) != 0) {
                    float amountY = y - lastY - height;
                    if (height + amountY < minHeight) amountY = minHeight - height;
                    height += amountY;
                }
                setBounds(Math.round(windowX), Math.round(windowY + ((1 - getScaleY()) * getHeight())), Math.round(width), Math.round(height));
            }

            public boolean mouseMoved (InputEvent event, float x, float y) {
                updateEdge(x, y);
                return isModal();
            }

            public boolean scrolled (InputEvent event, float x, float y, int amount) {
                return isModal();
            }

            public boolean keyDown (InputEvent event, int keycode) {
                return isModal();
            }

            public boolean keyUp (InputEvent event, int keycode) {
                return isModal();
            }

            public boolean keyTyped (InputEvent event, char character) {
                return isModal();
            }
        });
    }

    private void showPopupMenu() {
        H2DPopupMenu popupMenu = new H2DPopupMenu();
        MenuItem rename = new MenuItem("Remove note");
        rename.addListener(
                new ClickListener(Input.Buttons.LEFT) {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        facade.sendNotification(MsgAPI.ACTION_REMOVE_STICKY_NOTE, id);
                    }
                });
        popupMenu.addItem(rename);
        MenuItem changeColor = new MenuItem("Change color");
        changeColor.addListener(
                new ClickListener(Input.Buttons.LEFT) {
                    boolean init = false;
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        ColorPicker picker = new HyperLapColorPicker(new ColorPickerAdapter() {
                            @Override
                            public void finished(Color newColor) {
                                setColor(newColor);
                                StickyNoteVO payload = ModifyStickyNoteCommand.payload(StickyNoteActor.this);
                                facade.sendNotification(MsgAPI.ACTION_MODIFY_STICKY_NOTE, payload);
                            }

                            @Override
                            public void changed(Color newColor) {
                                if (init)
                                    setColor(newColor);
                            }
                        });
                        init = true;
                        picker.setColor(getColor());
                        Sandbox.getInstance().getUIStage().addActor(picker.fadeIn());
                    }
                });
        popupMenu.addItem(changeColor);

        Sandbox sandbox = Sandbox.getInstance();
        UIStage uiStage = sandbox.getUIStage();

        popupMenu.showMenu(Sandbox.getInstance().getUIStage(), sandbox.getInputX(), uiStage.getHeight() - sandbox.getInputY());
    }
}
