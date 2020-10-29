package games.rednblack.editor.renderer.data;

import java.util.ArrayList;

public class GraphVO {
    public int version = -1;
    public ArrayList<GraphNodeVO> nodes = new ArrayList<>();
    public ArrayList<GraphConnectionVO> connections = new ArrayList<>();
    public ArrayList<GraphGroupVO> groups = new ArrayList<>();
}
