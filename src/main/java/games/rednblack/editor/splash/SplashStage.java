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
    private boolean isLoading = true;

    public SplashStage(boolean isLoading) {
        this.isLoading = isLoading;
        atlas = new TextureAtlas(Gdx.files.internal("splash/splash.atlas"));
        BitmapFont robotFont = new BitmapFont(Gdx.files.internal("splash/roboto.fnt"));
        Label.LabelStyle whiteLabelStyle = new Label.LabelStyle(robotFont, new Color(1, 1, 1, 1f));
        Label.LabelStyle blackLabelStyle = new Label.LabelStyle(robotFont, new Color(0, 0, 0, 1f));

        Image bg = new Image(atlas.findRegion("bg_color"));
        bg.setColor(0, 0, 0, 1);
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

        progress = new Label("Loading fonts", blackLabelStyle);
        progress.setX(logoText.getX() + ((logoText.getWidth() - progress.getWidth() )/ 2));
        progress.setY(logo.getY() + 5);
        if (isLoading)
            addActor(progress);

        Label companyName = new Label("Red & Black Games", whiteLabelStyle);
        companyName.setX(13);
        companyName.setY(55 - companyName.getHeight() - 7);
        addActor(companyName);

        Label copyright = new Label("Copyright (c) 2023,  All rights reserved.", whiteLabelStyle);
        copyright.setX(13);
        copyright.setY(companyName.getY() - 20);
        addActor(copyright);

        Label version = new Label("v" + AppConfig.getInstance().versionString, whiteLabelStyle);
        version.setX(getWidth() - 13 - version.getWidth());
        version.setY(companyName.getY());
        addActor(version);

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
        progress.setX(logoText.getX() + ((logoText.getWidth() - progress.getPrefWidth()) / 2));
    }

    public void loadedData() {
        addAction(Actions.sequence(Actions.delay(0.8f), Actions.run(() -> {
            HyperLap2DApp.getInstance().splashWindow.closeWindow();
        })));
    }
}
