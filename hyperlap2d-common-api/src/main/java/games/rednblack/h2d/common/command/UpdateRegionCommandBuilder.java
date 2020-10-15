package games.rednblack.h2d.common.command;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import games.rednblack.h2d.common.MsgAPI;
import org.puremvc.java.interfaces.IFacade;

public class UpdateRegionCommandBuilder {

    private Object[] payload;

    public void begin(Entity forEntity) {
        payload = new Object[3];
        payload[0] = forEntity;
    }

    public void setRegionName(String regionName) {
        payload[1] = regionName;
    }

    public void setRegion(TextureRegion region) {
        payload[2] = region;
    }

    public void execute(IFacade facade) {
        facade.sendNotification(MsgAPI.ACTION_UPDATE_REGION_DATA, payload);
    }
}
