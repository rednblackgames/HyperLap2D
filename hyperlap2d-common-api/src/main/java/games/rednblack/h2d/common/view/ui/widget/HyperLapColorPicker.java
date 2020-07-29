package games.rednblack.h2d.common.view.ui.widget;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.color.ColorPicker;
import com.kotcrab.vis.ui.widget.color.ColorPickerListener;

public class HyperLapColorPicker extends ColorPicker {

    public HyperLapColorPicker (String title) {
        this("default", title, null);
    }

    public HyperLapColorPicker (String title, ColorPickerListener listener) {
        this("default", title, listener);
    }

    public HyperLapColorPicker (ColorPickerListener listener) {
        this("default", null, listener);
    }

    public HyperLapColorPicker (String styleName, String title, ColorPickerListener listener) {
        super(styleName, title, listener);
    }

    @Override
    public void addCloseButton() {
        VisImageButton closeButton = new VisImageButton("close-window");
        this.getTitleTable().add(closeButton).padRight(0);
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
}
