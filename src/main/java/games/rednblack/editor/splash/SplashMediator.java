package games.rednblack.editor.splash;

import com.puremvc.patterns.mediator.SimpleMediator;
import com.puremvc.patterns.observer.Notification;
import games.rednblack.editor.HyperLap2DApp;
import games.rednblack.editor.HyperLap2DFacade;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SplashMediator extends SimpleMediator<Object> {

    private static final String TAG = SplashMediator.class.getCanonicalName();
    private static final String NAME = TAG;

    public SplashMediator() {
        super(NAME, new Object());
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                SplashScreenAdapter.UPDATE_SPLASH,
                SplashScreenAdapter.CLOSE_SPLASH
        };
    }

    @Override
    public void handleNotification(Notification notification) {
        super.handleNotification(notification);
        System.out.println(notification.getBody().toString());

        if (HyperLap2DApp.getInstance().splashWindow != null && HyperLap2DApp.getInstance().mainWindow != null) {
            ((SplashScreenAdapter)(HyperLap2DApp.getInstance().splashWindow.getListener())).setProgressStatus(notification.getBody().toString());

            if (notification.getName().equals(SplashScreenAdapter.CLOSE_SPLASH)) {
                ((SplashScreenAdapter)(HyperLap2DApp.getInstance().splashWindow.getListener())).loadedData();
                HyperLap2DApp.getInstance().mainWindow.setVisible(true);
            }

            HyperLap2DApp.getInstance().splashWindow.focusWindow();
        }
    }
}
