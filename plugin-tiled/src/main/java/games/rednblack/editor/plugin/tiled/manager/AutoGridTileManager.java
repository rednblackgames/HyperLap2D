package games.rednblack.editor.plugin.tiled.manager;

import games.rednblack.editor.plugin.tiled.TiledPlugin;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.h2d.common.command.ReplaceRegionCommandBuilder;

public class AutoGridTileManager {

	private static final int UL = 1;
	private static final int U = 2;
	private static final int UR = 4;
	private static final int R = 8;
	private static final int DR = 16;
	private static final int D = 32;
	private static final int DL = 64;
	private static final int L = 128;

    private final ReplaceRegionCommandBuilder replaceRegionCommandBuilder = new ReplaceRegionCommandBuilder();

    private TiledPlugin tiledPlugin;
    
    public AutoGridTileManager(TiledPlugin tiledPlugin) {
    	this.tiledPlugin = tiledPlugin;
    }

    public void autoFill() {
    	for (int entity : tiledPlugin.getAPI().getProjectEntities()) {
    		MainItemComponent mainItemComponent = ComponentRetriever.get(entity, MainItemComponent.class, tiledPlugin.getAPI().getEngine());
    		if (!mainItemComponent.tags.contains(TiledPlugin.AUTO_TILE_TAG)) {
    			continue;
    		}
    		int col = Integer.parseInt(mainItemComponent.customVariables.get(TiledPlugin.COLUMN));
    		int row = Integer.parseInt(mainItemComponent.customVariables.get(TiledPlugin.ROW));

    		int c = 0;
    		int val = 0;
    		int ul = tiledPlugin.getPluginEntityWithParams(row + 1, col - 1);
    		if (ul != -1) {
    			c++;
    			val += UL;
    		}
    		int u = tiledPlugin.getPluginEntityWithParams(row + 1, col);
    		if (u != -1) {
    			c++;
    			val += U;
    		}
    		int ur = tiledPlugin.getPluginEntityWithParams(row + 1, col + 1);
    		if (ur != -1) {
    			c++;
    			val += UR;
    		}
    		int r = tiledPlugin.getPluginEntityWithParams(row, col + 1);
    		if (r != -1) {
    			c++;
    			val += R;
    		}
    		int dr = tiledPlugin.getPluginEntityWithParams(row - 1, col + 1);
    		if (dr != -1) {
    			c++;
    			val += DR;
    		}
    		int d = tiledPlugin.getPluginEntityWithParams(row - 1, col);
    		if (d != -1) {
    			c++;
    			val += D;
    		}
    		int dl = tiledPlugin.getPluginEntityWithParams(row - 1, col - 1);
    		if (dl != -1) {
    			c++;
    			val += DL;
    		}
    		int l = tiledPlugin.getPluginEntityWithParams(row, col - 1);
    		if (l != -1) {
    			c++;
    			val += L;
    		}
    		
    		int index = getIndex(c, val);

            String region = mainItemComponent.customVariables.get(TiledPlugin.REGION) + index;
            replaceRegionCommandBuilder.begin(entity);
            replaceRegionCommandBuilder.setRegion(tiledPlugin.getAPI().getSceneLoader().getRm().getTextureRegion(region));
            replaceRegionCommandBuilder.setRegionName(region);
            replaceRegionCommandBuilder.execute(tiledPlugin.facade);
    	}
    }

    /**
     * Test whether the tiles in the given direction ({@link #UL} to {@link #L}) are all occupied.
     * 
     * @param tiles The tiles ({@link #UL} to {@link #L}).
     * @param val The value for the surroundings.
     * 
     * @return true, if the tiles are all occupied by another tile, false otherwise.
     */
    private boolean isOccupied(int val, int ... tiles) {
    	for (int t : tiles) {
    		if ((t & val) == 0) {
    			return false;
    		}
    	}
    	return true;
    }

    /**
     * Test whether the tiles in the given direction ({@link #UL} to {@link #L}) are all empty.
     * 
     * @param tiles The tiles ({@link #UL} to {@link #L}).
     * @param val The value for the surroundings.
     * 
     * @return true, if the tiles are all empty, false otherwise.
     */
    private boolean isEmpty(int val, int ... tiles) {
    	for (int t : tiles) {
    		if ((t & val) > 0) {
    			return false;
    		}
    	}
    	return true;
    }
    
    // 24
    private int getIndex(int c, int val) {
		switch (c) {
    		case 8:
    			return 6;
    		case 7:
    			if (isEmpty(val, DR)) {
      				return 26;
      			} else if (isEmpty(val, UR)) {
     				return 27;
     			} else if (isEmpty(val, DL)) {
     				return 31;
     			} else if (isEmpty(val, UL)) {
     				return 32;
     			}
    		case 6:
    			if (isOccupied(val, L, DL, D, R, U, UL) && isEmpty(val, DR, UR)) {
      				return 29;
      			} else if (isOccupied(val, L, U, UR, R, DR, D) && isEmpty(val, UL, DL)) {
     				return 34;
     			} else if (isOccupied(val, L, UL, U, UR, R, D) && isEmpty(val, DR, DL)) {
     				return 41;
     			} else if (isOccupied(val, L, U, R, DR, D, DL) && isEmpty(val, UR, UL)) {
     				return 42;
     			} else if (isOccupied(val, L, UL, U, R, DR, D) && isEmpty(val, DL, UR)) {
     				return 45;
     			} else if (isOccupied(val, D, DL, L, U, UR, R) && isEmpty(val, UL, DR)) {
     				return 46;
     			}
    		case 5:
    			if (isOccupied(val, U, UR, R, DR, D) && isEmpty(val, L)) {
    				return 1;
    			} else if (isOccupied(val, R, DR, D, DL, L) && isEmpty(val, U)) {
    				return 5;
    			} else if (isOccupied(val, L, UL, U, UR, R) && isEmpty(val, D)) {
    				return 7;
    			} else if (isOccupied(val, U, D, DL, L, UL) && isEmpty(val, R)) {
    				return 11;
    			} else if (isOccupied(val, L, U, R, DR, D) && isEmpty(val, DL, UL, UR)) {
     				return 47;
     			} else if (isOccupied(val, L, U, UR, R, D) && isEmpty(val, UL, DR, DL)) {
     				return 48;
     			} else if (isOccupied(val, L, U, R, D, DL) && isEmpty(val, UL, UR, DR)) {
     				return 52;
     			} else if (isOccupied(val, L, UL, U, R, D) && isEmpty(val, DL, UR, DR)) {
     				return 53;
     			}
    		case 4:
    			if (isOccupied(val, U, UR, R, D) && isEmpty(val, DR, L)) {
     				return 21;
     			} else if (isOccupied(val, U, R, DR, D) && isEmpty(val, UR, L)) {
     				return 22;
     			} else if (isOccupied(val, L, DL, D, R) && isEmpty(val, DR, U)) {
     				return 25;
     			} else if (isOccupied(val, L, UL, U, R) && isEmpty(val, UR, D)) {
     				return 28;
     			} else if (isOccupied(val, L, D, DR, R) && isEmpty(val, U, DL)) {
     				return 30;
     			} else if (isOccupied(val, L, U, UR, R) && isEmpty(val, UL, D)) {
     				return 33;
     			} else if (isOccupied(val, L, UL, U, D) && isEmpty(val, R, DL)) {
     				return 36;
     			} else if (isOccupied(val, U, D, DL, L) && isEmpty(val, UL, R)) {
     				return 37;
     			} else if (isOccupied(val, L, U, R, D) && isEmpty(val, DR, DL, UR, UL)) {
     				return 44;
     			}
    		case 3:
    			if (isOccupied(val, R, DR, D) && isEmpty(val, L, U)) {
    				return 0;
    			} else if (isOccupied(val, U, UR, R) && isEmpty(val, L, D)) {
    				return 2;
    			} else if (isOccupied(val, L, DL, D) && isEmpty(val, U, R)) {
    				return 10;
    			} else if (isOccupied(val, L, UL, U) && isEmpty(val, R, D)) {
    				return 12;
    			} else if (isOccupied(val, U, R, D) && isEmpty(val, L, UR, DR)) {
     				return 24;
     			} else if (isOccupied(val, U, D, L) && isEmpty(val, UL, R, DL)) {
     				return 39;
     			} else if (isOccupied(val, L, D, R) && isEmpty(val, DL, DR, U)) {
     				return 40;
     			} else if (isOccupied(val, L, U, R) && isEmpty(val, UL, UR, D)) {
     				return 43;
     			}
    		case 2:
    			if (isOccupied(val, L, R) && isEmpty(val, U, D)) {
     				return 8;
     			} else if (isOccupied(val, U, D) && isEmpty(val, L, R)) {
    				return 16;
    			} else if (isOccupied(val, R, D) && isEmpty(val, DR, U, L)) {
    				return 20;
    			} else if (isOccupied(val, U, R) && isEmpty(val, UR, D, L)) {
     				return 23;
     			} else if (isOccupied(val, L, U) && isEmpty(val, UL, R, D)) {
     				return 38;
     			} else if (isOccupied(val, L, D) && isEmpty(val, DL, U, R)) {
     				return 35;
     			}
    		case 1:
    			if (isOccupied(val, R) && isEmpty(val, U, D, L)) {
    				return 3;
    			} else if (isOccupied(val, L) && isEmpty(val, U, R, D)) {
    				return 13;
    			} else if (isOccupied(val, D) && isEmpty(val, L, U, R)) {
    				return 15;
    			} else if (isOccupied(val, U) && isEmpty(val, L, D, R)) {
    				return 17;
    			}
    		case 0:
    			return 18;
		}
		// default
		return 54;
    }

}
