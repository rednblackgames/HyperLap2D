package games.rednblack.editor.view.ui;

import games.rednblack.editor.HyperLap2DFacade;
import org.puremvc.java.patterns.mediator.Mediator;

public class UIWindowTitleMediator  extends Mediator<UIWindowTitle> {

    private static final String TAG = UIWindowTitleMediator.class.getCanonicalName();
    public static final String NAME = TAG;


    public UIWindowTitleMediator() {
        super(NAME, new UIWindowTitle());
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();
    }
}
