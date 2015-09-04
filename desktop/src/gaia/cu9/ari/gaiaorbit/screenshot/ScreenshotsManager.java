package gaia.cu9.ari.gaiaorbit.screenshot;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.interfce.IGui;
import gaia.cu9.ari.gaiaorbit.interfce.RenderGui;
import gaia.cu9.ari.gaiaorbit.render.IMainRenderer;
import gaia.cu9.ari.gaiaorbit.render.IPostProcessor.PostProcessBean;
import gaia.cu9.ari.gaiaorbit.render.IPostProcessor.RenderType;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

public class ScreenshotsManager implements IObserver {
    public static ScreenshotsManager system;

    public static void initialize() {
        system = new ScreenshotsManager();
    }

    /** Command to take screenshot **/
    private class ScreenshotCmd {
        public static final String FILENAME = "screenshot";
        public String folder;
        public int width, height;
        public boolean active = false;

        public ScreenshotCmd() {
            super();
        }

        public void takeScreenshot(int width, int height, String folder) {
            this.folder = folder;
            this.width = width;
            this.height = height;
            this.active = true;
        }

    }

    public IFileImageRenderer frameRenderer, screenshotRenderer;
    private ScreenshotCmd screenshot;
    private IGui renderGui;

    public ScreenshotsManager() {
        super();
        frameRenderer = new BufferedFileImageRenderer(GlobalConf.runtime.OUTPUT_FRAME_BUFFER_SIZE);
        screenshotRenderer = new BasicFileImageRenderer();
        screenshot = new ScreenshotCmd();

        EventManager.instance.subscribe(this, Events.RENDER_FRAME, Events.RENDER_SCREENSHOT, Events.FLUSH_FRAMES, Events.SCREENSHOT_CMD, Events.UPDATE_GUI, Events.DISPOSE);
    }

    public void renderFrame(IMainRenderer mr) {
        if (GlobalConf.frame.RENDER_OUTPUT) {
            switch (GlobalConf.frame.FRAME_MODE) {
            case simple:
                frameRenderer.saveScreenshot(GlobalConf.frame.RENDER_FOLDER, GlobalConf.frame.RENDER_FILE_NAME, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
                break;
            case redraw:
                renderToImage(mr, mr.getCameraManager(), mr.getPostProcessor().getPostProcessBean(RenderType.frame), GlobalConf.frame.RENDER_WIDTH, GlobalConf.frame.RENDER_HEIGHT, GlobalConf.frame.RENDER_FOLDER, GlobalConf.frame.RENDER_FILE_NAME, frameRenderer);
                break;
            }
        }
    }

    public void renderScreenshot(IMainRenderer mr) {
        if (screenshot.active) {
            String file = null;
            switch (GlobalConf.screenshot.SCREENSHOT_MODE) {
            case simple:
                file = ImageRenderer.renderToImageGl20(screenshot.folder, ScreenshotCmd.FILENAME, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                break;
            case redraw:
                file = renderToImage(mr, mr.getCameraManager(), mr.getPostProcessor().getPostProcessBean(RenderType.screenshot), screenshot.width, screenshot.height, screenshot.folder, ScreenshotCmd.FILENAME, screenshotRenderer);
                break;
            }
            if (file != null) {
                screenshot.active = false;
                EventManager.instance.post(Events.SCREENSHOT_INFO, file);
            }

        }
    }

    /**
     * Renders the current scene to an image and returns the file name where it
     * has been written to
     * 
     * @param camera
     * @param width
     *            The width of the image.
     * @param height
     *            The height of the image.
     * @param folder
     *            The folder to save the image to.
     * @param filename
     *            The file name prefix.
     * @param renderer
     *            the {@link IFileImageRenderer} to use.
     * @return
     */
    public String renderToImage(IMainRenderer mr, ICamera camera, PostProcessBean ppb, int width, int height, String folder, String filename, IFileImageRenderer renderer) {
        FrameBuffer frameBuffer = mr.getFrameBuffer(width, height);
        // TODO That's a dirty trick, we should find a better way (i.e. making
        // buildEnabledEffectsList() method public)
        boolean postprocessing = ppb.pp.captureNoClear();
        ppb.pp.captureEnd();
        if (!postprocessing) {
            // If post processing is not active, we must start the buffer now.
            // Otherwise, it is used in the render method to write the results
            // of the pp.
            frameBuffer.begin();
        }

        // this is the main render function
        mr.preRenderScene();
        // sgr.render(camera, width, height, postprocessing ? m_fbo : null,
        // ppb);
        mr.renderSgr(camera, width, height, frameBuffer, ppb);

        if (postprocessing) {
            // If post processing is active, we have to start now again because
            // the renderScene() has closed it.
            frameBuffer.begin();
        }
        if (GlobalConf.frame.RENDER_SCREENSHOT_TIME) {
            // Timestamp
            renderGui().resize(width, height);
            renderGui().render();
        }

        String res = renderer.saveScreenshot(folder, filename, width, height, false);

        frameBuffer.end();
        return res;
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case RENDER_FRAME:
            IMainRenderer mr = (IMainRenderer) data[0];
            renderFrame(mr);
            break;
        case RENDER_SCREENSHOT:
            mr = (IMainRenderer) data[0];
            renderScreenshot(mr);
            break;
        case FLUSH_FRAMES:
            frameRenderer.flush();
            break;
        case SCREENSHOT_CMD:
            screenshot.takeScreenshot((int) data[0], (int) data[1], (String) data[2]);
            break;
        case UPDATE_GUI:
            renderGui().update((Float) data[0]);
            break;
        case DISPOSE:
            renderGui().dispose();
            break;
        }

    }

    private IGui renderGui() {
        // Lazy initialised
        if (renderGui == null) {
            renderGui = new RenderGui();
            renderGui.initialize(null);
            renderGui.doneLoading(null);
        }
        return renderGui;
    }
}
