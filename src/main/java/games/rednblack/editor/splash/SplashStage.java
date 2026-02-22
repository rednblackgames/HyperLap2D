package games.rednblack.editor.splash;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import games.rednblack.editor.HyperLap2DApp;
import games.rednblack.editor.utils.AppConfig;
import games.rednblack.editor.view.ui.widget.actors.basic.WhitePixel;

public class SplashStage extends Stage {
    private final Label progress;
    private boolean isLoading = true;

    public SplashStage(boolean isLoading) {
        WhitePixel.initializeShared();

        this.isLoading = isLoading;
        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("splash/splash.atlas"));
        BitmapFont robotFont = new BitmapFont(Gdx.files.internal("splash/roboto.fnt"));
        Label.LabelStyle whiteLabelStyle = new Label.LabelStyle(robotFont, new Color(1, 1, 1, 1f));

        Table root = new Table();
        root.setBackground(new TextureRegionDrawable(atlas.findRegion("splash_bg")));
        root.setFillParent(true);

        Table header = new Table();
        header.pad(15);
        Image logo = new Image(atlas.findRegion("splash_logo"));
        header.add(logo);
        header.add().expandX().padRight(50).right();
        root.add(header).growX().row();

        Table body = new Table();
        progress = new Label("Loading fonts", whiteLabelStyle);
        if (isLoading)
            body.add(progress).expand().padBottom(15).bottom();
        root.add(body).grow().row();

        Table footer = new Table();
        footer.pad(8);
        Drawable footerBg = WhitePixel.sharedInstance.drawable.tint(new Color(0, 0, 0, 0.5f));
        footer.setBackground(footerBg);
        Label companyName = new Label("Red & Black Games", whiteLabelStyle);
        footer.add(companyName).growX().left();
        Label version = new Label("v" + AppConfig.getInstance().versionString, whiteLabelStyle);
        footer.add(version).left().row();

        Label copyright = new Label("Copyright (c) 2026,  All rights reserved.", whiteLabelStyle);
        footer.add(copyright).padTop(2).growX();
        root.add(footer).growX();

        addActor(root);

        setProgressStatus("Initializing");
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        super.touchUp(screenX, screenY, pointer, button);
        if (!isLoading) {
            HyperLap2DApp.getInstance().splashWindow.closeWindow();
        }
        return true;
    }

    public void setProgressStatus(String status) {
        progress.setText(status);
    }

    public void loadedData() {
        addAction(Actions.sequence(Actions.delay(0.8f), Actions.run(() -> {
            HyperLap2DApp.getInstance().splashWindow.closeWindow();
        })));
    }
}
