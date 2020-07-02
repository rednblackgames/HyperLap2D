package games.rednblack.h2d.common.vo;


import com.badlogic.gdx.utils.Array;

public class ExportMapperVO {
    public Array<ExportedAsset> mapper = new Array<>();
    public String projectVersion = "";

    public static class ExportedAsset {
        public int type;
        public String fileName;

        public ExportedAsset() {
            type = -1;
            fileName = "";
        }

        public ExportedAsset(int type, String name) {
            this.type = type;
            this.fileName = name;
        }
    }
}
