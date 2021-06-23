package games.rednblack.editor.plugin.tiled.manager;

import com.badlogic.ashley.core.Entity;

import games.rednblack.editor.plugin.tiled.TiledPlugin;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.h2d.common.command.ReplaceRegionCommandBuilder;

public class AutoGridTileManager {

    private final ReplaceRegionCommandBuilder replaceRegionCommandBuilder = new ReplaceRegionCommandBuilder();

    private TiledPlugin tiledPlugin;
    
    public AutoGridTileManager(TiledPlugin tiledPlugin) {
    	this.tiledPlugin = tiledPlugin;
    }

    public void autoFill() {
    	for (Entity entity : tiledPlugin.getAPI().getProjectEntities()) {
    		MainItemComponent mainItemComponent = ComponentRetriever.get(entity, MainItemComponent.class);
    		if (!mainItemComponent.tags.contains(TiledPlugin.AUTO_TILE_TAG)) {
    			continue;
    		}
    		int col = mainItemComponent.customVariables.getIntegerVariable(TiledPlugin.COLUMN);
    		int row = mainItemComponent.customVariables.getIntegerVariable(TiledPlugin.ROW);

    		int c = 0;
    		Entity ul = tiledPlugin.getPluginEntityWithParams(row + 1, col - 1);
    		if (ul != null) {
    			c++;
    		}
    		Entity u = tiledPlugin.getPluginEntityWithParams(row + 1, col);
    		if (u != null) {
    			c++;
    		}
    		Entity ur = tiledPlugin.getPluginEntityWithParams(row + 1, col + 1);
    		if (ur != null) {
    			c++;
    		}
    		Entity r = tiledPlugin.getPluginEntityWithParams(row, col + 1);
    		if (r != null) {
    			c++;
    		}
    		Entity dr = tiledPlugin.getPluginEntityWithParams(row - 1, col + 1);
    		if (dr != null) {
    			c++;
    		}
    		Entity d = tiledPlugin.getPluginEntityWithParams(row - 1, col);
    		if (d != null) {
    			c++;
    		}
    		Entity dl = tiledPlugin.getPluginEntityWithParams(row - 1, col - 1);
    		if (dl != null) {
    			c++;
    		}
    		Entity l = tiledPlugin.getPluginEntityWithParams(row, col - 1);
    		if (l != null) {
    			c++;
    		}
    		
    		int index = getIndex(c, ul, u, ur, r, dr, d, dl, l);

            String region = mainItemComponent.customVariables.getStringVariable(TiledPlugin.REGION) + index;
            replaceRegionCommandBuilder.begin(entity);
            replaceRegionCommandBuilder.setRegion(tiledPlugin.getAPI().getSceneLoader().getRm().getTextureRegion(region));
            replaceRegionCommandBuilder.setRegionName(region);
            replaceRegionCommandBuilder.execute(tiledPlugin.facade);
    	}
    }
    
    private int getIndex(int c, Entity ul, Entity u, Entity ur, Entity r, Entity dr, Entity d, Entity dl, Entity l) {
		// default
		int index = 15;
		switch (c) {
    		case 8:
    			index = 5;
    			break;
    		case 7:
    			if (dr == null) {
    				index = 16;
    			} else if (dl == null) {
    				index = 24;
    			} else if (ul == null) {
    				index = 26;
    			} else if (ur == null) {
    				index = 18;
    			} else if (u == null) {
    				index = 4;
    			} else if (r == null) {
    				index = 9;
    			} else if (d == null) {
    				index = 6;
    			} else if (l == null) {
    				index = 1;
    			}
    			break;
    		case 6:
    			// 1, 4, 6, 9
    			if (u == null) {
    				index = 4;
    			} else if (r == null) {
    				index = 9;
    			} else if (d == null) {
    				index = 6;
    			} else if (l == null) {
    				index = 1;
    			}
    			break;
    		case 5:
    			// 0, 1, 2, 4, 6, 8, 9, 10
    			if (ul == null && u == null && l == null) {
    				index = 0;
    			} else if (u == null && ur == null && r == null) {
    				index = 8;
    			} else if (l == null && dl == null && d == null) {
    				index = 2;
    			} else if (r == null && dr == null && d == null) {
    				index = 10;
    			} else if (u == null) {
    				index = 4;
    			} else if (r == null) {
    				index = 9;
    			} else if (d == null) {
    				index = 6;
    			} else if (l == null) {
    				index = 1;
    			}
    			break;
    		case 4:
    			if (u != null && r != null && d != null && l != null) {
    				// ul
    				index = 21;
    			} else if (u == null && ul == null && l == null) {
    				index = 0;
    			} else if (u == null && ur == null && r == null) {
    				index = 8;
    			} else if (r == null && dr == null && d == null) {
    				index = 10;
    			} else if (l == null && dl == null && d == null) {
    				index = 2;
    			} else if (u == null && ur == null && d == null && dr == null) {
    				index = 7;
    			} else if (u == null && ul == null && d == null && dl == null) {
    				index = 7;
    			} else if (l == null && r == null && dr == null && dl == null) {
    				index = 13;
    			} else if (l == null && r == null && ur == null && ul == null) {
    				index = 13;
    			}
    			break;
    		case 3:
    			// 0, 2, 8, 10
    			if (dl == null && l == null && ul == null && u == null && ur == null) {
    				// ul
    				index = 0;
    			} else if (ul == null && u == null && ur == null && r == null && dr == null) {
    				// ur
    				index = 8;
    			} else if (ur == null && r == null && dr == null && d == null && dl == null) {
    				// dr
    				index = 10;
    			} else if (dr == null && d == null && dl == null && l == null && ul == null) {
    				// dl
    				index = 2;
    			} else if (ur != null && r != null && dr != null) {
    				index = 17;
    			} else if (dl != null && d != null && dr != null) {
    				index = 20;
    			} else if (ul != null && l != null && dl != null) {
    				index = 25;
    			} else if (ul != null && u != null && ur != null) {
    				index = 22;
    			}
    			break;
    		case 2:
    			// 7, 13
    			if (l != null && r != null) {
    				index = 7;
    			} else if (u != null && d != null) {
    				index = 13;
    			}
    			break;
    		case 1:
    			// 3, 11, 12, 14
    			if (r != null) {
    				index = 3;
    			} else if (l != null) {
    				index = 11;
    			} else if (d != null) {
    				index = 12;
    			} else if (u != null) {
    				index = 14;
    			}
    			break;
    		case 0:
    			index = 15;
    			break;
		}
		
		return index;
    }

}
