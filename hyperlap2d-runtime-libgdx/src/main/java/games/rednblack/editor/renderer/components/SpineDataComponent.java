package games.rednblack.editor.renderer.components;

public class SpineDataComponent implements BaseComponent {
    public String animationName = "";
    public String currentAnimationName = "";

    @Override
    public void reset() {
        animationName = "";
        currentAnimationName = "";
    }
}
