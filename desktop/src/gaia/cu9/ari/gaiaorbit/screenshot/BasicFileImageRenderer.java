package gaia.cu9.ari.gaiaorbit.screenshot;

/**
 * Renders image files synchronously.
 * @author Toni Sagrista
 *
 */
public class BasicFileImageRenderer implements IFileImageRenderer {

    @Override
    public String saveScreenshot(String absoluteLocation, String baseFileName, int w, int h, boolean immediate) {
        return ImageRenderer.renderToImageGl20(absoluteLocation, baseFileName, w, h);
    }

    @Override
    public void flush() {
        // Nothing to do
    }

}
