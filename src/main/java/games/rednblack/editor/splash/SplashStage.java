package games.rednblack.editor.splash;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import games.rednblack.editor.HyperLap2DApp;
import games.rednblack.editor.utils.AppConfig;

public class SplashStage extends Stage {
    private TextureAtlas atlas;
    private Image logoText;
    private Label progress;

    public SplashStage() {
        atlas = new TextureAtlas(Gdx.files.internal("splash/splash.atlas"));
        BitmapFont robotFont = new BitmapFont(Gdx.files.internal("splash/myriad.fnt"));
        Label.LabelStyle labelStyle = new Label.LabelStyle(robotFont, new Color(0, 0, 0, 1f));

        Image bg = new Image(atlas.findRegion("bg_color"));
        bg.setX(0);
        bg.setY(0);
        bg.setWidth(getWidth());
        bg.setHeight(getHeight());
        addActor(bg);

        Image imageBg = new Image(atlas.findRegion("splash_bg"));
        imageBg.setX(0);
        imageBg.setY(getHeight() - imageBg.getHeight());
        addActor(imageBg);

        Image logo = new Image(atlas.findRegion("splash_logo"));
        logo.setX(25);
        logo.setY(getHeight() - logo.getHeight() - 20);
        addActor(logo);

        logoText = new Image(atlas.findRegion("splash_logo_text"));
        logoText.setX(200);
        logoText.setY(logo.getY() + 30);
        addActor(logoText);

        progress = new Label("Loading fonts", labelStyle);
        progress.setX(logoText.getX() + ((logoText.getWidth() - progress.getWidth() )/ 2));
        progress.setY(logo.getY() + 5);
        addActor(progress);

        Label companyName = new Label("Red & Black Games", labelStyle);
        companyName.setX(13);
        companyName.setY(55 - companyName.getHeight() - 7);
        addActor(companyName);

        Label copyright = new Label("Copyright (c) 2020. All rights reserved.", labelStyle);
        copyright.setX(13);
        copyright.setY(companyName.getY() - 20);
        addActor(copyright);

        Label version = new Label(AppConfig.getInstance().version, labelStyle);
        version.setX(getWidth() - 13 - version.getWidth());
        version.setY(companyName.getY());
        addActor(version);

        setProgressStatus("Initializing");
    }

    public void setProgressStatus(String status) {
        progress.setText(status);
        progress.setX(logoText.getX() + ((logoText.getWidth() - progress.getWidth()) / 2));
    }

    public void loadedData() {
        addAction(Actions.sequence(Actions.delay(0.8f), Actions.run(() -> {
            HyperLap2DApp.getInstance().splashWindow.closeWindow();
        })));
    }
}
