package games.rednblack.editor.utils;

import com.badlogic.gdx.utils.Array;

import java.io.File;
import java.io.FileFilter;

public class RecursiveFileSuffixFilter implements FileFilter {

    private final Array<String> types = new Array<>();

    public RecursiveFileSuffixFilter(String suffix) {
        types.add(suffix);
    }

    public RecursiveFileSuffixFilter(String... types) {
        this.types.addAll(types);
    }

    public RecursiveFileSuffixFilter(Array<String> types) {
        this.types.addAll(types);
    }

    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) return true;
        for (String type : types) {
            if (f.getName().endsWith(type)) return true;
        }
        return false;
    }
}