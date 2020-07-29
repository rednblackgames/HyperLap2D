package games.rednblack.h2d.common.view.ui.widget;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.file.FileChooser;

public class HyperLapFileChooser extends FileChooser {
    public HyperLapFileChooser(Mode mode) {
        super(mode);
    }

    public HyperLapFileChooser(FileHandle directory, Mode mode) {
        super(directory, mode);
    }

    public HyperLapFileChooser(String title, Mode mode) {
        super(title, mode);
    }

    public HyperLapFileChooser(String styleName, String title, Mode mode) {
        super(styleName, title, mode);
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
