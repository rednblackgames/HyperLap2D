package games.rednblack.editor.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import games.rednblack.editor.view.ui.dialog.LoadingBarDialog;
import games.rednblack.puremvc.Facade;

/**
 * Reports {@link TexturePacker}'s progress to the editor's loading dialog.
 * <p>
 * The packer reports through a nested-portion model: whoever drives it wraps its own loops in
 * {@code start(portion)}/{@code end()} pairs, and every callback arrives already scaled to a
 * fraction of the whole job. The repack nests three levels deep — the run, each resolution, each
 * pack — before the packer subdivides the innermost slice per image and per page.
 * <p>
 * Those callbacks come in on the packing thread, several per image, so updates are posted to the
 * render thread and thinned on the way: a repack of a big project would otherwise queue thousands
 * of notifications for a bar that is 250px wide.
 */
public class PackProgressListener extends TexturePacker.ProgressListener {

    /** Smallest change worth reporting, which caps a whole repack at ~200 updates. */
    private static final float MIN_STEP = 0.005f;

    private float lastReported = -1;
    private String lastMessage;

    @Override
    public void progress(float progress) {
        String message = getMessage();
        boolean messageChanged = message != null && !message.equals(lastMessage);
        boolean stepped = progress - lastReported >= MIN_STEP || (progress >= 1f && lastReported < 1f);
        if (!messageChanged && !stepped) return;

        lastReported = progress;
        lastMessage = message;

        Gdx.app.postRunnable(() -> {
            Facade facade = Facade.getInstance();
            if (messageChanged)
                facade.sendNotification(LoadingBarDialog.SET_MESSAGE, message);
            facade.sendNotification(LoadingBarDialog.SET_PROGRESS, progress);
        });
    }
}
