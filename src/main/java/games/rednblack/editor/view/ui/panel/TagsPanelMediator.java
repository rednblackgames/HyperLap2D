package games.rednblack.editor.view.ui.panel;

import com.badlogic.ashley.core.Entity;
import games.rednblack.editor.view.menu.WindowMenu;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.editor.view.ui.properties.panels.UIBasicItemProperties;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.mediator.Mediator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by azakhary on 8/1/2015.
 */
public class TagsPanelMediator extends Mediator<TagsPanel> {
    private static final String TAG = TagsPanelMediator.class.getCanonicalName();
    private static final String NAME = TAG;

    private final Set<Entity> observables = new HashSet<>();

    public TagsPanelMediator() {
        super(NAME, new TagsPanel());
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();
        viewComponent.setEmpty();
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                MsgAPI.ITEM_SELECTION_CHANGED,
                MsgAPI.EMPTY_SPACE_CLICKED,
                UIBasicItemProperties.TAGS_BUTTON_CLICKED,
                WindowMenu.TAGS_EDITOR_OPEN,
                TagsPanel.ITEM_ADD,
                TagsPanel.ITEM_REMOVED
        };
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);

        Sandbox sandbox = Sandbox.getInstance();
        UIStage uiStage = sandbox.getUIStage();

        switch (notification.getName()) {
            case WindowMenu.TAGS_EDITOR_OPEN:
            case UIBasicItemProperties.TAGS_BUTTON_CLICKED:
                viewComponent.show(uiStage);
                break;
            case MsgAPI.ITEM_SELECTION_CHANGED:
                Set<Entity> selection = notification.getBody();
                setObservable(selection);
                break;
            case MsgAPI.EMPTY_SPACE_CLICKED:
                setObservable(null);
                break;
            case TagsPanel.ITEM_REMOVED:
                viewComponent.updateView();
                String tagToRemove = notification.getBody();
                for (Entity observable : observables) {
                    MainItemComponent mainItemComponent = observable.getComponent(MainItemComponent.class);
                    mainItemComponent.tags.remove(tagToRemove);
                }
                break;
            case TagsPanel.ITEM_ADD:
                viewComponent.updateView();
                String tagToAdd = notification.getBody();
                for (Entity observable : observables) {
                    MainItemComponent mainItemComponent = observable.getComponent(MainItemComponent.class);
                    mainItemComponent.tags.add(tagToAdd);
                }
                break;
        }
    }

    private void setObservable(Set<Entity> items) {
        observables.clear();
        if (items != null)
            observables.addAll(items);
        updateView();
    }

    private void updateView() {
        if(observables.size() == 0) {
            viewComponent.setEmpty();
        } else {
            Iterator<Entity> iterator = observables.iterator();

            Entity entity = iterator.next();
            MainItemComponent mainItemComponent = ComponentRetriever.get(entity, MainItemComponent.class);
            if (mainItemComponent == null)
                return;
            Set<String> common = new LinkedHashSet<>();
            Set<String> toRetain = new LinkedHashSet<>();

            for (String tag : mainItemComponent.tags)
                common.add(tag);

            while (iterator.hasNext()) {
                entity = iterator.next();
                mainItemComponent = ComponentRetriever.get(entity, MainItemComponent.class);
                toRetain.clear();
                for (String tag : mainItemComponent.tags)
                    toRetain.add(tag);
                common.retainAll(toRetain);
            }

            viewComponent.setTags(common);
            viewComponent.updateView();
        }
    }
}
