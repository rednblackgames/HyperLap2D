package games.rednblack.editor.view.ui.dialog;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Align;
import com.google.common.io.ByteStreams;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.LinkLabel;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.SettingsManager;
import games.rednblack.editor.utils.AppConfig;
import games.rednblack.h2d.common.H2DDialog;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class AboutDialog extends H2DDialog {

    public AboutDialog() {
        super("About HyperLap2D");
        addCloseButton();

        SettingsManager settingsManager = HyperLap2DFacade.getInstance().retrieveProxy(SettingsManager.NAME);

        Date date = new Date(settingsManager.editorConfigVO.totalSpentTime);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        String totalTimeSpent = formatter.format(date);

        VisTable mainTable = new VisTable();
        VisTable leftTable = new VisTable();
        VisTable contentTable = new VisTable();
        contentTable.align(Align.left);
        VisScrollPane scrollPane = StandardWidgetsFactory.createScrollPane(contentTable);
        scrollPane.setFadeScrollBars(false);

        mainTable.add(leftTable).top().padLeft(10).left();
        mainTable.add(scrollPane).maxHeight(300).top().width(580).padLeft(28).expand().left();

        leftTable.add(new VisImage(VisUI.getSkin().getDrawable("splash_logo"))).pad(5).row();
        leftTable.add("HyperLap2D").padLeft(5).padRight(5).row();
        leftTable.add("Release " + AppConfig.getInstance().versionString).row();
        if (AppConfig.getInstance().build != null)
            leftTable.add("Build #" + AppConfig.getInstance().build).row();
        leftTable.add("Total Time").padTop(20).row();
        leftTable.add(totalTimeSpent).row();

        contentTable.add("Copyright \u00A9 2023 Red & Black Games").left().row();
        contentTable.add("").row();
        contentTable.add("Dedicated to game lovers. Create something awesome!").left().row();
        contentTable.add("").row();
        VisTable forkTable = new VisTable();
        forkTable.add("Original codebase fork from ");
        forkTable.add(new LinkLabel("Overlap2D", "https://github.com/UnderwaterApps/overlap2d"));
        contentTable.add(forkTable).left().row();
        contentTable.add("").row();
        contentTable.add("HyperLap2D is based on following libraries and open source tools:").left().padBottom(4).row();
        contentTable.add(new LinkLabel("- libGDX [https://github.com/libgdx/libgdx]", "https://github.com/libgdx/libgdx")).padLeft(6).left().row();
        contentTable.add(new LinkLabel("- Overlap2D [https://github.com/UnderwaterApps/overlap2d]", "https://github.com/UnderwaterApps/overlap2d")).padLeft(6).left().row();
        contentTable.add(new LinkLabel("- Artemis-odb [https://github.com/junkdog/artemis-odb]", "https://github.com/junkdog/artemis-odb")).padLeft(6).left().row();
        contentTable.add(new LinkLabel("- Box2DLights [https://github.com/libgdx/box2dlights]", "https://github.com/libgdx/box2dlights")).padLeft(6).left().row();
        contentTable.add(new LinkLabel("- PureMVC Framework [https://github.com/PureMVC/puremvc-java-standard-framework]", "https://github.com/PureMVC/puremvc-java-standard-framework")).padLeft(6).left().row();
        contentTable.add(new LinkLabel("- VisUI [https://github.com/kotcrab/vis-ui]", "https://github.com/kotcrab/vis-ui")).padLeft(6).left().row();
        contentTable.add(new LinkLabel("- Modular [https://github.com/mountainblade/modular]", "https://github.com/mountainblade/modular")).padLeft(6).left().row();
        contentTable.add(new LinkLabel("- Spine Runtimes [https://github.com/EsotericSoftware/spine-runtimes]", "https://github.com/EsotericSoftware/spine-runtimes")).padLeft(6).left().row();
        contentTable.add(new LinkLabel("- Physics Body Editor [https://www.aurelienribon.com]", "https://www.aurelienribon.com")).padLeft(6).left().row();
        contentTable.add(new LinkLabel("- Typing Label [https://github.com/rafaskb/typing-label]", "https://github.com/rafaskb/typing-label")).padLeft(6).left().row();
        contentTable.add(new LinkLabel("- Shape Drawer [https://github.com/earlygrey/shapedrawer]", "https://github.com/earlygrey/shapedrawer")).padLeft(6).left().row();
        contentTable.add(new LinkLabel("- Talos VFX [https://github.com/rockbite/talos]", "https://github.com/rockbite/talos")).padLeft(6).left().row();
        contentTable.add("\n").row();
        VisTable legalTable = new VisTable();
        legalTable.add(new LinkLabel("See Contributors list", "https://github.com/rednblackgames/HyperLap2D/blob/master/AUTHORS"));
        legalTable.add(" \u2022 ");
        legalTable.add(new LinkLabel("Open Source Licenses", getResourceFileFromJar("licenses.html").file().toURI().toString()));
        contentTable.add(legalTable).row();
        contentTable.add("\n").row();
        contentTable.add("Icon and Art design: Angelo Navarro").left().row();

        getContentTable().add(mainTable).padTop(5);
    }

    private FileHandle getResourceFileFromJar(String fileName) {
        SettingsManager settingsManager = HyperLap2DFacade.getInstance().retrieveProxy(SettingsManager.NAME);
        File tempFile = new File(settingsManager.cacheDir + File.separator + fileName);

        try {
            if (!tempFile.exists()) {
                FileUtils.write(tempFile, "", "utf-8");
            }

            InputStream in = getClass().getResourceAsStream("/"+fileName);
            FileOutputStream out = new FileOutputStream(tempFile);
            if (in != null) {
                ByteStreams.copy(in, out);
                in.close();
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new FileHandle(tempFile);
    }
}
