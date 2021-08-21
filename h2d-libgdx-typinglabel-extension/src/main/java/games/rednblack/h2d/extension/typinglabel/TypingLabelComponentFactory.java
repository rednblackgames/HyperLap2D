package games.rednblack.h2d.extension.typinglabel;

import com.artemis.ComponentMapper;
import com.artemis.EntityTransmuter;
import com.artemis.EntityTransmuterFactory;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import games.rednblack.editor.renderer.box2dLight.RayHandler;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.ParentNodeComponent;
import games.rednblack.editor.renderer.components.label.LabelComponent;
import games.rednblack.editor.renderer.data.LabelVO;
import games.rednblack.editor.renderer.data.MainItemVO;
import games.rednblack.editor.renderer.data.ProjectInfoVO;
import games.rednblack.editor.renderer.data.ResolutionEntryVO;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.renderer.factory.component.ComponentFactory;
import games.rednblack.editor.renderer.resources.IResourceRetriever;

public class TypingLabelComponentFactory extends ComponentFactory {

    protected ComponentMapper<LabelComponent> labelCM;
    protected ComponentMapper<TypingLabelComponent> typingLabelCM;

    private static int labelDefaultSize = 12;

    private EntityTransmuter transmuter;

    public TypingLabelComponentFactory() {
        super();
    }

    @Override
    public void injectDependencies(com.artemis.World engine, RayHandler rayHandler, World world, IResourceRetriever rm) {
        super.injectDependencies(engine, rayHandler, world, rm);

        transmuter = new EntityTransmuterFactory(engine)
                .add(ParentNodeComponent.class)
                .add(LabelComponent.class)
                .add(TypingLabelComponent.class)
                .build();
    }

    @Override
    public int createSpecialisedEntity(int root, MainItemVO vo) {
        int entity = createGeneralEntity(vo, EntityFactory.LABEL_TYPE);
        transmuter.transmute(entity);

        adjustNodeHierarchy(root, entity);

        initializeLabelComponent(labelCM.get(entity), (LabelVO) vo);
        checkTypingLabelComponent(entity, (LabelVO) vo);

        return entity;
    }

    protected void initializeDimensionsComponent(int entity, DimensionsComponent component, MainItemVO vo) {
        component.height = ((LabelVO) vo).height;
        component.width = ((LabelVO) vo).width;
    }

    protected void initializeLabelComponent(LabelComponent component, LabelVO vo) {
        component.setText(vo.text);
        component.setStyle(generateStyle(rm, vo.style, vo.size));
        component.fontName = vo.style;
        component.fontSize = vo.size;
        component.setAlignment(vo.align);
        component.setWrap(vo.wrap);
        component.typingEffect = vo.isTyping;

        ProjectInfoVO projectInfoVO = rm.getProjectVO();
        ResolutionEntryVO resolutionEntryVO = rm.getLoadedResolution();
        float multiplier = resolutionEntryVO.getMultiplier(rm.getProjectVO().originalResolution);

        component.setFontScale(multiplier / projectInfoVO.pixelToWorld);
    }

    protected void checkTypingLabelComponent(int entity, LabelVO vo) {
        if (!vo.isTyping) typingLabelCM.remove(entity);
    }


    public static LabelStyle generateStyle(IResourceRetriever rManager, String fontName, int size) {

        if (size == 0) {
            size = labelDefaultSize;
        }
        return new LabelStyle(rManager.getBitmapFont(fontName, size), null);
    }

}
