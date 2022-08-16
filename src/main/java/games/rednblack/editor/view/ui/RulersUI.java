package games.rednblack.editor.view.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.kotcrab.vis.ui.widget.VisLabel;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.utils.Guide;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.ui.widget.actors.basic.WhitePixel;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.HashMap;

/**
 * Created by azakhary on 7/18/2015.
 */
public class RulersUI extends Actor {

    private static final String CLASS_NAME = "games.rednblack.editor.view.ui.RulersUI";
    public static final String ACTION_GUIDES_MODIFIED = CLASS_NAME + ".ACTION_GUIDES_MODIFIED";
    public static final String RIGHT_CLICK_RULER = CLASS_NAME + ".RIGHT_CLICK_RULER";

    private static final int rulerBoxSize = 14;

    private static final int topOffset = 64;
    private static final int leftOffset = 40;

    private static final int separatorsCount = 20;

    private static final Color BG_COLOR = new Color(48f / 255f, 48f / 255f, 48f / 255f, 1f);
    private static final Color LINE_COLOR = new Color(85f / 255f, 85f / 255f, 85f / 255f, 1f);
    private static final Color GUIDE_COLOR = new Color(255f / 255f, 94f / 255f, 0f / 255f, 0.5f);
    private static final Color OVER_GUIDE_COLOR = new Color(255f / 255f, 173f / 255f, 125f / 255f, 1f);
    private static final Color TEXT_COLOR = new Color(194f / 255f, 194f / 255f, 194f / 255f, 1f);

    //Allows the ChangeRulerXPositionCommand to change the guide's position from the function UpdateGuideManully
    private static Guide editableDraggingGuide = null;

   private ShapeDrawer shapeDrawer;

    private final Rectangle horizontalRect, verticalRect;

    private boolean isShowingPixels = false;

    private float viewMeasurableWidth;
    private float viewMeasurableHeight;

    private float gridMeasuringSize;
    private float gridMeasuringSizeInWorld;
    private float gridMeasureToDisplayScale;

    private final Array<VisLabel> labels = new Array<>();

    private Array<Guide> guides = new Array<>();
    private Guide mouseOverGuide = null;

    private final VisLabel guidePosLbl;

    private Guide draggingGuide = null;
    private boolean lockLines;

    private final Vector2 tmp1 = new Vector2();
    private final Vector2 tmp2 = new Vector2();
    private final Circle tmpCircle = new Circle();
    private final Color tmpColor = new Color();

    private final HashMap<Integer, String> labelTextCache = new HashMap<>();

    public RulersUI() {
        horizontalRect = new Rectangle();
        verticalRect = new Rectangle();

        guidePosLbl = new VisLabel();

        addListeners();
    }

    private void addListeners() {
        addListener(new ClickListener() {

            private boolean isTouchingDownRuler;
            private boolean isTouchDownRulerVertical;
            private final Vector2 tmp = new Vector2();
            private final Circle tmpCircle = new Circle();

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                super.touchDown(event, x, y, pointer, button);

                Circle touchCircle = tmpCircle;
                touchCircle.radius = 5;
                touchCircle.setPosition(x, y);

                isTouchingDownRuler = false;
                if (verticalRect.contains(touchCircle.x, touchCircle.y)) {
                    isTouchDownRulerVertical = true;
                    isTouchingDownRuler = true;
                }
                if (horizontalRect.contains(touchCircle.x, touchCircle.y)) {
                    isTouchDownRulerVertical = false;
                    isTouchingDownRuler = true;
                }

                // check for collision with guides.
                Guide collisionGuide = guideCollision(x, y);
                if (collisionGuide != null) {
                    draggingGuide = collisionGuide;
                }

                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                super.touchDragged(event, x, y, pointer);

                if (lockLines) return;

                Vector2 downPost = tmp.set(getTouchDownX(), getTouchDownY());
                if (isTouchingDownRuler && draggingGuide == null && downPost.dst(x, y) > 3) {
                    draggingGuide = new Guide(isTouchDownRulerVertical);
                    guides.add(draggingGuide);
                }

                //Changes the dragging guide's position to the world position
                if (draggingGuide != null) {
                    Vector2 worldCoords = hereToWorld(tmp.set(x, y));
                    if (draggingGuide.isVertical) {
                        draggingGuide.pos = worldCoords.x;
                        if (!isShowingPixels)
                            snap(draggingGuide);
                    } else {
                        draggingGuide.pos = worldCoords.y;
                        if (!isShowingPixels) {
                            snap(draggingGuide);
                        }
                    }
                }

            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                if (getTapCount() >= 2) {
                    // double click toggles the mode
                    isShowingPixels = !isShowingPixels;
                }

                if (draggingGuide != null) {
                    if ((draggingGuide.isVertical && x < verticalRect.x + verticalRect.getWidth()) ||
                            (!draggingGuide.isVertical && y > horizontalRect.y)) {
                        guides.removeValue(draggingGuide, true);
                    } else {
                        if (button == Input.Buttons.RIGHT) {
                            editableDraggingGuide = draggingGuide;
                            HyperLap2DFacade.getInstance().sendNotification(RIGHT_CLICK_RULER);
                        }
                    }

                    HyperLap2DFacade.getInstance().sendNotification(ACTION_GUIDES_MODIFIED);
                }
                draggingGuide = null;
            }

            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {
                mouseOverGuide = guideCollision(x, y);
                return super.mouseMoved(event, x, y);
            }
        });
    }

    @Override
    public void act(float delta) {
        if (!isVisible()) return;

        super.act(delta);

        horizontalRect.set(leftOffset, getStage().getHeight() - rulerBoxSize - topOffset, getStage().getWidth() - leftOffset, rulerBoxSize);
        verticalRect.set(leftOffset, 0, rulerBoxSize, getStage().getHeight() - topOffset);

        //calculating sizes
        viewMeasurableWidth = Sandbox.getInstance().getViewport().getWorldWidth() * Sandbox.getInstance().getCamera().zoom;
        viewMeasurableHeight = Sandbox.getInstance().getViewport().getWorldHeight() * Sandbox.getInstance().getCamera().zoom;

        if (isShowingPixels) {
            viewMeasurableWidth = viewMeasurableWidth * Sandbox.getInstance().getPixelPerWU();
            viewMeasurableHeight = viewMeasurableHeight * Sandbox.getInstance().getPixelPerWU();
        }

        gridMeasureToDisplayScale = getStage().getWidth() / viewMeasurableWidth;

        gridMeasuringSize = viewMeasurableWidth / separatorsCount;
        if (gridMeasuringSize <= 0.5) {
            gridMeasuringSize = roundToFirstDecimal(gridMeasuringSize);
        } else if (gridMeasuringSize <= 10) {
            gridMeasuringSize = Math.round(gridMeasuringSize);
        } else if (gridMeasuringSize > 10 && gridMeasuringSize <= 20) {
            gridMeasuringSize = Math.round(gridMeasuringSize / 5) * 5;
        } else {
            gridMeasuringSize = Math.round(gridMeasuringSize / 10) * 10;
        }

        gridMeasuringSizeInWorld = gridMeasuringSize;
        if (isShowingPixels) {
            gridMeasuringSizeInWorld = gridMeasuringSize / Sandbox.getInstance().getPixelPerWU();
        }

    }

    private Vector2 worldToHere(Vector2 tmp) {
        tmp = Sandbox.getInstance().worldToScreen(tmp);

        return tmp;
    }

    private Vector2 hereToWorld(Vector2 tmp) {
        tmp = Sandbox.getInstance().screenToWorld(tmp);

        return tmp;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (shapeDrawer == null) {
            shapeDrawer = new ShapeDrawer(batch, WhitePixel.sharedInstance.textureRegion){
                /* OPTIONAL: Ensuring a certain smoothness. */
                @Override
                protected int estimateSidesRequired(float radiusX, float radiusY) {
                    return 200;
                }
            };
        }

        try {
            drawShapes(parentAlpha);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            drawBatch(batch, parentAlpha);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void drawShapes(float parentAlpha) {
        drawBg(parentAlpha);
        drawLines(parentAlpha);
    }

    public void drawBg(float parentAlpha) {
        tmpColor.set(BG_COLOR);
        tmpColor.a *= parentAlpha;

        shapeDrawer.setColor(tmpColor);
        shapeDrawer.filledRectangle(horizontalRect.x, horizontalRect.y, horizontalRect.width, horizontalRect.height);
        shapeDrawer.filledRectangle(verticalRect.x, verticalRect.y, verticalRect.width, verticalRect.height);
    }

    public void drawLines(float parentAlpha) {
        tmpColor.set(LINE_COLOR);
        tmpColor.a *= parentAlpha;

        shapeDrawer.setColor(tmpColor);

        // Static Lines for Aesthetics
        shapeDrawer.line(horizontalRect.x + rulerBoxSize, horizontalRect.y, horizontalRect.x + horizontalRect.width, horizontalRect.y, 1f);
        shapeDrawer.line(verticalRect.x + verticalRect.width + 1, verticalRect.y, verticalRect.x + verticalRect.width + 1, verticalRect.y + verticalRect.height - rulerBoxSize, 1f);

        //Functional lines to show grid
        Vector2 startPoint = tmp1.set(horizontalRect.x + rulerBoxSize, verticalRect.y);
        Vector2 worldStartPoint = hereToWorld(startPoint);
        worldStartPoint.x -= worldStartPoint.x % gridMeasuringSizeInWorld;
        worldStartPoint.y -= worldStartPoint.y % gridMeasuringSizeInWorld;
        Vector2 worldStartPointCpy = tmp2.set(worldStartPoint);
        Vector2 gridCurrPoint = worldToHere(worldStartPoint);

        String postFix = "";
        if (isShowingPixels) {
            postFix = "px";
            worldStartPointCpy.x *= Sandbox.getInstance().getPixelPerWU();
            worldStartPointCpy.y *= Sandbox.getInstance().getPixelPerWU();
        }

        float gridSize = gridMeasuringSize * gridMeasureToDisplayScale;
        int iterator = 0;
        while (gridCurrPoint.x < horizontalRect.x + horizontalRect.getWidth()) {
            shapeDrawer.line(gridCurrPoint.x, horizontalRect.y, gridCurrPoint.x, horizontalRect.y + rulerBoxSize, 1f);
            shapeDrawer.line(gridCurrPoint.x + gridSize / 2, horizontalRect.y, gridCurrPoint.x + gridSize / 2, horizontalRect.y + rulerBoxSize / 2f, 1f);

            VisLabel label = Pools.obtain(VisLabel.class);
            label.setPosition(gridCurrPoint.x + 2, horizontalRect.y + 7);
            label.setColor(TEXT_COLOR);
            label.getText().clear();
            if (gridMeasuringSize < 1) {
                label.getText().append(roundToFirstDecimal(Math.abs(worldStartPointCpy.x + iterator * gridMeasuringSize)));
            } else {
                label.getText().append((int) Math.abs(worldStartPointCpy.x + iterator * gridMeasuringSize));
            }
            label.getText().append(postFix);
            label.setWrap(false);
            labels.add(label);

            gridCurrPoint.x += gridSize;
            iterator++;
        }
        iterator = 0;
        while (gridCurrPoint.y < verticalRect.y + verticalRect.getHeight()) {
            shapeDrawer.line(verticalRect.x + verticalRect.getWidth(), gridCurrPoint.y, verticalRect.x + verticalRect.getWidth() - rulerBoxSize, gridCurrPoint.y, 1f);
            shapeDrawer.line(verticalRect.x + verticalRect.getWidth(), gridCurrPoint.y + gridSize / 2, verticalRect.x + verticalRect.getWidth() - rulerBoxSize / 2f, gridCurrPoint.y + gridSize / 2, 1f);

            VisLabel label = Pools.obtain(VisLabel.class);
            label.setColor(TEXT_COLOR);
            int textNumber = (int) Math.abs(worldStartPointCpy.y + iterator * gridMeasuringSize);
            labelTextCache.putIfAbsent(textNumber, "");
            String lblText = labelTextCache.get(textNumber);
            if (lblText.equals("")) {
                lblText = verticalize(textNumber + "");
                labelTextCache.put(textNumber, lblText);
            }

            label.setText(lblText);
            label.setWrap(true);
            label.setPosition(verticalRect.x + 3, gridCurrPoint.y - label.getPrefHeight() / 2);
            labels.add(label);

            gridCurrPoint.y += gridSize;
            iterator++;
        }

        drawGuides(parentAlpha);
    }

    public void drawGuides(float parentAlpha) {
        for (int i = 0; i < guides.size; i++) {
            Guide guide = guides.get(i);

            if (mouseOverGuide == guide) {
                tmpColor.set(OVER_GUIDE_COLOR);
            } else {
                tmpColor.set(GUIDE_COLOR);
            }

            tmpColor.a *= parentAlpha;
            shapeDrawer.setColor(tmpColor);

            if (guide.isVertical) {
                Vector2 localCoords = worldToHere(tmp1.set(guide.pos, 0));
                if (localCoords.x > verticalRect.x + verticalRect.width) {
                    shapeDrawer.line(localCoords.x, 0, localCoords.x, horizontalRect.y, 1f);
                }
            } else {
                Vector2 localCoords = worldToHere(tmp1.set(0, guide.pos));
                if (localCoords.y < horizontalRect.y) {
                    shapeDrawer.line(verticalRect.x + verticalRect.getWidth(), localCoords.y, getStage().getWidth(), localCoords.y, 1f);
                }
            }
        }
    }

    public void drawBatch(Batch batch, float parentAlpha) {
        for (int i = 0; i < labels.size; i++) {
            labels.get(i).draw(batch, parentAlpha);
            Pools.free(labels.get(i));
        }
        labels.clear();

        if (draggingGuide != null) {
            float pos = draggingGuide.pos;
            String axis = "Y";
            String postfix = "";
            if (draggingGuide.isVertical) axis = "X";
            if (isShowingPixels) {
                pos = draggingGuide.pos * Sandbox.getInstance().getPixelPerWU();
                postfix = "px";
            }

            //Rounds the guide's position to the nearest 100th, if in World Unit mode
            String positionAsString = "" + pos;
            if (!isShowingPixels) {
                pos = (float) Math.round(pos * 100) / 100;
                positionAsString = String.format("%.2f", pos);
            } else
                pos = (float) (Math.round(pos * 100) / 100);

            guidePosLbl.setText(axis + ": " + positionAsString + postfix);
            guidePosLbl.setPosition(Sandbox.getInstance().getInputX(15), getStage().getHeight() - Sandbox.getInstance().getInputY(15));
            guidePosLbl.draw(batch, parentAlpha);
        }
    }

    private final StringBuilder labelVerticalBuilder = new StringBuilder();

    private String verticalize(String text) {
        if (labelVerticalBuilder.length() != text.length() * 2) {
            labelVerticalBuilder.setLength(text.length() * 2);
        }
        for (int i = 0, j = 0; i < text.length(); i++, j += 2) {
            labelVerticalBuilder.setCharAt(j, text.charAt(i));
            labelVerticalBuilder.setCharAt(j + 1, '\n');
        }

        return labelVerticalBuilder.toString();
    }

    private float roundToFirstDecimal(float value) {
        return Math.round(value * 10f) / 10f;
    }

    @Override
    public Actor hit(float x, float y, boolean touchable) {
        if (verticalRect.contains(x, y) || horizontalRect.contains(x, y)) {
            return this;
        }

        mouseOverGuide = guideCollision(x, y);
        if (mouseOverGuide != null) {
            return this;
        }

        return null;
    }

    public Guide guideCollision(float x, float y) {
        Vector2 point = tmp1.set(x, y);
        point = hereToWorld(point);

        Circle touchCircle = tmpCircle;
        touchCircle.radius = 3f / Sandbox.getInstance().getPixelPerWU();
        touchCircle.setPosition(point.x, point.y);


        for (int i = 0; i < guides.size; i++) {
            if (guides.get(i).isVertical) {
                // this is really weird that I have to substract half of radius.... I am totally lost.
                if (touchCircle.contains(guides.get(i).pos - touchCircle.radius / 2f, touchCircle.y)) {
                    return guides.get(i);
                }
            } else {
                if (touchCircle.contains(touchCircle.x, guides.get(i).pos - touchCircle.radius / 2f)) {
                    return guides.get(i);
                }
            }
        }

        return null;
    }

    //Snaps to nearest quarter if less than 0.04 World Units away
    private void snap(Guide guide) {
        float snapDistance = 0.04f;
        float absoluteValPos = Math.abs(guide.pos);
        float nearestQuarter = Math.round(absoluteValPos * 4) / 4f;
        if (Math.abs(absoluteValPos - nearestQuarter) < snapDistance) {
            absoluteValPos = nearestQuarter;
        }
        if (guide.pos < 0)
            absoluteValPos *= -1;
        guide.pos = absoluteValPos;
    }

    public static Guide getPreviousGuide() {
        return editableDraggingGuide;
    }

    //Allows the ChangeRulerXPositionCommand to change the guide's position
    public static void updateGuideManually(float destination) {
        editableDraggingGuide.pos = destination;

        HyperLap2DFacade.getInstance().sendNotification(ACTION_GUIDES_MODIFIED);
    }

    public Array<Guide> getGuides() {
        return guides;
    }

    public void setGuides(Array<Guide> guides) {
        this.guides = guides;
    }

    public void setLockLines(boolean lockLines) {
        this.lockLines = lockLines;
    }
}
