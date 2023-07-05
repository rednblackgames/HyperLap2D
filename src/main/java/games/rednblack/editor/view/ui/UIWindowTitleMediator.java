package games.rednblack.editor.view.ui;


import games.rednblack.puremvc.Mediator;

public class UIWindowTitleMediator  extends Mediator<UIWindowTitle> {

    private static final String TAG = UIWindowTitleMediator.class.getCanonicalName();
    public static final String NAME = TAG;


    public UIWindowTitleMediator() {
        super(NAME, new UIWindowTitle());
    }
}
