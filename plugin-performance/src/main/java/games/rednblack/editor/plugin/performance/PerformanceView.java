package games.rednblack.editor.plugin.performance;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.Separator;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.editor.plugin.performance.advisor.Advisor;
import games.rednblack.editor.plugin.performance.data.Fmt;
import games.rednblack.editor.plugin.performance.data.Metric;
import games.rednblack.editor.plugin.performance.data.PerformanceModel;
import games.rednblack.editor.plugin.performance.data.Scope;
import games.rednblack.editor.plugin.performance.widget.FrameGraph;
import games.rednblack.editor.plugin.performance.widget.HintStrip;
import games.rednblack.editor.plugin.performance.widget.Labels;
import games.rednblack.editor.plugin.performance.widget.Palette;
import games.rednblack.editor.plugin.performance.widget.ScopeSelector;
import games.rednblack.editor.plugin.performance.widget.StatTile;
import games.rednblack.editor.plugin.performance.widget.SystemBreakdown;
import games.rednblack.editor.renderer.utils.profiling.FrameProfiler;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

import java.util.Map;

/**
 * Everything the profiler shows, in one table.
 * <p>
 * It only draws: the frames are sampled by the mediator, once per editor frame, because this lives in a
 * window of its own that redraws at its own rate. Reading the model here instead would sample whenever
 * this window happened to be painted, and the chart would be of the profiler rather than of the editor.
 */
public class PerformanceView extends VisTable {

    /** 0 follows the display; the rest are the rates a game is actually written for. */
    private static final int[] BUDGET_RATES = {0, 120, 60, 30};

    private final PerformanceModel model;
    private final Advisor advisor;
    private final Map<String, Object> preferences;
    private final StringBuilder text = new StringBuilder(64);

    private final VisTable liveTable = new VisTable();

    private final ScopeSelector scopeSelector = new ScopeSelector();
    private final StatTile frameTile = new StatTile("Frame", "ms");
    private final StatTile rateTile = new StatTile("Rate", "fps");
    private final StatTile drawTile = new StatTile("Draw calls", "");
    private final StatTile memoryTile = new StatTile("Heap", "MB");
    private final VisLabel sceneInfo = Labels.fitted("small");
    private final VisLabel loopInfo = Labels.fitted("small");
    private final FrameGraph graph;
    private final SystemBreakdown breakdown;
    private final HintStrip hints;

    private VisSelectBox<String> budgetBox;
    private VisCheckBox glDetailBox;
    private VisImageButton pauseButton;
    private VisImageButton resetButton;
    private VisImageButton.VisImageButtonStyle pauseStyle;
    private VisImageButton.VisImageButtonStyle playStyle;

    private boolean built;
    private boolean sceneReady;
    private float windowMillis;

    public PerformanceView(TextureAtlas atlas, PerformanceModel model, Advisor advisor, Map<String, Object> preferences) {
        this.model = model;
        this.advisor = advisor;
        this.preferences = preferences;

        graph = new FrameGraph(model);
        breakdown = new SystemBreakdown(model);
        hints = new HintStrip(advisor);

        pad(10f);
        buildHeaderWidgets(atlas);
        restorePreferences();

        if (model.isAttached()) showLive();
        else showWaiting();
    }

    /** What this window last cost to draw, for the line that says so. */
    public void setWindowMillis(float millis) {
        windowMillis = millis;
    }

    /** A scene has been opened while the window was up. */
    public void sceneReady() {
        if (sceneReady) return;
        showLive();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!sceneReady) return;

        updateTiles();
        updateSceneInfo();
        breakdown.update();
        hints.update();
    }

    public void savePreferences() {
        preferences.put("scope", scopeSelector.getScope().name());
        preferences.put("budgetFps", model.budgetFps());
        preferences.put("glDetail", glDetailBox.isChecked());
    }

    private void showLive() {
        sceneReady = true;
        if (!built) buildLiveView();

        clearChildren();
        add(liveTable).grow();
    }

    private void showWaiting() {
        sceneReady = false;

        clearChildren();
        VisLabel message = new VisLabel("No project open", "small");
        message.setColor(Palette.TEXT_MUTED);
        add(message).center().expand();
    }

    private void buildHeaderWidgets(TextureAtlas atlas) {
        Drawable pauseIcon = icon(atlas, "icon-perf-pause");
        Drawable playIcon = icon(atlas, "icon-perf-play");
        Drawable resetIcon = icon(atlas, "icon-perf-reset");

        pauseButton = new VisImageButton(pauseIcon, "Freeze the window");
        pauseStyle = new VisImageButton.VisImageButtonStyle(pauseButton.getStyle());
        playStyle = new VisImageButton.VisImageButtonStyle(pauseButton.getStyle());
        playStyle.imageUp = playIcon;

        pauseButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                model.setPaused(!model.isPaused());
                pauseButton.setStyle(model.isPaused() ? playStyle : pauseStyle);
            }
        });

        resetButton = new VisImageButton(resetIcon, "Clear the window");
        resetButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                model.reset();
            }
        });

        buildBudgetBox();

        glDetailBox = new VisCheckBox(" GL detail (costly)");
        glDetailBox.setChecked(true);
        glDetailBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                model.setGLDetail(glDetailBox.isChecked());
            }
        });

        scopeSelector.setListener(graph::setScope);
    }

    private void buildBudgetBox() {
        String[] items = new String[BUDGET_RATES.length];
        items[0] = "Display " + model.displayRefreshRate() + " Hz";
        for (int i = 1; i < BUDGET_RATES.length; i++) {
            items[i] = BUDGET_RATES[i] + " fps";
        }

        //built here rather than through the factory: the factory's box carries the editor's cursor
        //listener, and a cursor created while this window is current is destroyed when it closes
        budgetBox = new VisSelectBox<String>() {
            @Override
            protected void onShow(Actor selectBoxList, boolean below) {
                selectBoxList.setOrigin(below ? Align.top : Align.bottom);
                selectBoxList.setScaleY(0);
                selectBoxList.clearActions();
                selectBoxList.addAction(Actions.scaleTo(1, 1, 0.15f, Interpolation.swingOut));
            }

            @Override
            protected void onHide(Actor selectBoxList) {
                selectBoxList.clearActions();
                selectBoxList.addAction(Actions.sequence(Actions.scaleTo(1, 0, 0.15f, Interpolation.swingIn),
                        Actions.removeActor()));
            }
        };
        budgetBox.setItems(items);
        budgetBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                model.setBudgetFps(BUDGET_RATES[budgetBox.getSelectedIndex()]);
            }
        });
    }

    private void buildLiveView() {
        built = true;

        VisLabel budgetLabel = new VisLabel("BUDGET", "small");
        budgetLabel.setColor(Palette.TEXT_MUTED);

        VisTable header = new VisTable();
        header.add(scopeSelector).left();
        header.add().growX();
        header.add(glDetailBox).right().padRight(12f);
        header.add(budgetLabel).right().padRight(6f);
        //wide enough for the longest item it ever holds: "Display 165 Hz" and its arrow
        header.add(budgetBox).width(150f).height(24f).padRight(10f);
        header.add(pauseButton).size(28f).padRight(3f);
        header.add(resetButton).size(28f);

        VisTable tiles = new VisTable();
        tiles.defaults().uniformX().growX().padRight(6f);
        tiles.add(frameTile);
        tiles.add(rateTile);
        tiles.add(drawTile);
        tiles.add(memoryTile).padRight(0f);

        VisScrollPane systemScroll = StandardWidgetsFactory.createScrollPane(breakdown);
        systemScroll.setFadeScrollBars(false);
        systemScroll.setScrollingDisabled(true, false);
        breakdown.setScrollPane(systemScroll);

        sceneInfo.setColor(Palette.TEXT_MUTED);
        loopInfo.setColor(Palette.TEXT_MUTED);

        liveTable.clearChildren();
        liveTable.add(header).growX().padBottom(8f).row();
        liveTable.add(tiles).growX().padBottom(10f).row();
        liveTable.add(section("Frame time")).growX().padBottom(4f).row();
        liveTable.add(graph).growX().height(160f).padBottom(6f).row();
        liveTable.add(sceneInfo).left().growX().padBottom(2f).row();
        liveTable.add(loopInfo).left().growX().padBottom(10f).row();
        liveTable.add(section("Systems")).growX().padBottom(4f).row();
        liveTable.add(systemScroll).grow().minHeight(120f).padBottom(10f).row();
        liveTable.add(section("What stands out")).growX().padBottom(4f).row();
        liveTable.add(hints).growX().top().row();
    }

    private void updateTiles() {
        Scope scope = scopeSelector.getScope();
        Color scopeColor = scope == Scope.EDITOR ? Palette.EDITOR : Palette.RUNTIME;

        Metric millis = model.series(PerformanceModel.SERIES_MS, scope);
        Fmt.clear(text);
        Fmt.number(text, millis.average(), 2);
        frameTile.setValue(text);
        frameTile.setValueColor(Palette.state(millis.average(), model.budgetMillis()));
        Fmt.clear(text);
        text.append("ms · 1% low ");
        Fmt.number(text, millis.worstPercent(), 1);
        frameTile.setUnit(text);
        frameTile.setSeries(millis, scopeColor);

        float fps = model.fps().last();
        Fmt.clear(text);
        Fmt.number(text, fps, 0);
        rateTile.setValue(text);
        rateTile.setValueColor(Palette.state(1000f / Math.max(1f, fps), model.budgetMillis() * 1.1f));
        Fmt.clear(text);
        text.append("fps · average ");
        Fmt.number(text, model.fps().average(), 0);
        rateTile.setUnit(text);
        rateTile.setSeries(model.fps(), Palette.OK);

        Metric draws = model.series(PerformanceModel.SERIES_DRAW_CALLS, scope);
        Fmt.clear(text);
        Fmt.number(text, draws.average(), 0);
        drawTile.setValue(text);
        drawTile.setValueColor(Palette.state(draws.average(), scope == Scope.RUNTIME ? 50f : 120f));
        Fmt.clear(text);
        text.append("calls");
        if (model.isGLDetail()) {
            //the GL call count is what the driver is actually asked to do, and under a profiler it is also
            //what the cost of profiling is proportional to: one error query per call
            text.append(" · ");
            Fmt.grouped(text, (long) model.series(PerformanceModel.SERIES_GL_CALLS, scope).average());
            text.append(" GL · ");
            Fmt.number(text, model.series(PerformanceModel.SERIES_TEXTURE_BINDINGS, scope).average(), 0);
            text.append(" binds · ");
            Fmt.number(text, model.series(PerformanceModel.SERIES_SHADER_SWITCHES, scope).average(), 0);
            text.append(" shaders");
        }
        drawTile.setUnit(text);
        drawTile.setSeries(draws, scopeColor);

        Fmt.clear(text);
        Fmt.number(text, model.heapUsedMb().last(), 0);
        memoryTile.setValue(text);
        memoryTile.setValueColor(Palette.state(model.allocationRate(), 4f));
        Fmt.clear(text);
        text.append("MB of ");
        text.append(model.heapCommittedMb());
        text.append(" · ");
        Fmt.number(text, model.allocationRate(), 1);
        text.append(" MB/s");
        memoryTile.setUnit(text);
        memoryTile.setSeries(model.heapUsedMb(), Palette.EDITOR);
    }

    private void updateSceneInfo() {
        Fmt.clear(text);
        text.append(model.entityCount()).append(" entities");
        text.append("   ").append(model.bodyCount()).append(" bodies");
        text.append("   ").append(model.contactCount()).append(" contacts");
        text.append("   ").append(model.lightCount()).append(" lights");
        if (model.isGLDetail()) {
            text.append("   ");
            Fmt.grouped(text, (long) model.series(PerformanceModel.SERIES_VERTICES, Scope.RUNTIME).average());
            text.append(" vertices");
        }

        sceneInfo.setText(text);

        //where a frame goes when it is not in a render: the part a frame time on its own can never show
        float editorTurn = FrameProfiler.loopPhaseMillis(FrameProfiler.LOOP_MAIN);
        float editorRender = model.series(PerformanceModel.SERIES_MS, Scope.BOTH).average();

        Fmt.clear(text);
        text.append("loop ");
        Fmt.number(text, model.loopMillis().average(), 1);
        text.append(" ms   swap ");
        Fmt.number(text, Math.max(0f, editorTurn - editorRender), 1);
        text.append("   this window ");
        Fmt.number(text, FrameProfiler.loopPhaseMillis(FrameProfiler.LOOP_WINDOWS), 1);
        text.append("   context ");
        Fmt.number(text, FrameProfiler.loopPhaseMillis(FrameProfiler.LOOP_CONTEXT), 1);
        text.append("   events ");
        Fmt.number(text, FrameProfiler.loopPhaseMillis(FrameProfiler.LOOP_EVENTS), 1);
        text.append("   posted ");
        Fmt.number(text, FrameProfiler.loopPhaseMillis(FrameProfiler.LOOP_RUNNABLES), 1);
        text.append("   wait ");
        Fmt.number(text, FrameProfiler.loopPhaseMillis(FrameProfiler.LOOP_WAIT), 1);
        loopInfo.setText(text);
    }

    private void restorePreferences() {
        Object scope = preferences.get("scope");
        if (scope != null) {
            for (Scope option : Scope.values()) {
                if (option.name().equals(scope.toString())) {
                    scopeSelector.setScope(option);
                    graph.setScope(option);
                    break;
                }
            }
        }

        Object glDetail = preferences.get("glDetail");
        if (glDetail != null) {
            boolean wanted = glDetail instanceof Boolean
                    ? (Boolean) glDetail
                    : Boolean.parseBoolean(glDetail.toString());
            glDetailBox.setChecked(wanted);
            model.setGLDetail(wanted);
        }

        Object budget = preferences.get("budgetFps");
        if (!(budget instanceof Number)) return;

        int fps = ((Number) budget).intValue();
        for (int i = 0; i < BUDGET_RATES.length; i++) {
            if (BUDGET_RATES[i] == fps) {
                budgetBox.setSelectedIndex(i);
                model.setBudgetFps(fps);
                return;
            }
        }
    }

    private VisTable section(String title) {
        VisLabel label = new VisLabel(title.toUpperCase(), "small");
        label.setColor(Palette.TEXT_MUTED);

        VisTable table = new VisTable();
        table.add(label).left().padRight(8f);
        table.add(new Separator("menu")).growX().padTop(2f);
        return table;
    }

    private static Drawable icon(TextureAtlas atlas, String region) {
        if (atlas == null || region == null) return null;
        TextureAtlas.AtlasRegion found = atlas.findRegion(region);
        return found == null ? null : new TextureRegionDrawable(found);
    }
}
