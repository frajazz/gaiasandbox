package gaia.cu9.ari.gaiaorbit.render.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.bitfire.postprocessing.PostProcessor;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.render.IRenderable;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.ProgramConf.StereoProfile;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class PixelPostProcessRenderSystem extends PixelRenderSystem implements IObserver {

    Map<String, PostProcessor> ppmap;
    Map<String, FrameBuffer> fbmap;
    int fbkey = 0;

    public PixelPostProcessRenderSystem(RenderGroup rg, int priority, float[] alphas) {
        super(rg, priority, alphas);

        // Initialize post processors
        ppmap = new HashMap<String, PostProcessor>();
        getPostProcessor(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        getPostProcessor(GlobalConf.screenshot.SCREENSHOT_WIDTH, GlobalConf.screen.SCREEN_HEIGHT);
        getPostProcessor(GlobalConf.frame.RENDER_WIDTH, GlobalConf.frame.RENDER_HEIGHT);

        // Initialize frame buffers
        fbmap = new HashMap<String, FrameBuffer>();
        getFrameBuffer(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        getFrameBuffer(GlobalConf.screenshot.SCREENSHOT_WIDTH, GlobalConf.screen.SCREEN_HEIGHT);
        getFrameBuffer(GlobalConf.frame.RENDER_WIDTH, GlobalConf.frame.RENDER_HEIGHT);

        EventManager.instance.subscribe(this, Events.SCREEN_RESIZE, Events.TOGGLE_STEREOSCOPIC);
    }

    @Override
    public void renderStud(List<IRenderable> renderables, ICamera camera) {
        /** Render to image or render to screen? **/
        if (rc.fb == null) {
            /** Render to screen, use bloom **/
            // Gather buffer and bloom post processor
            FrameBuffer our_fb = getFrameBuffer(rc.w, rc.h);
            PostProcessor pp = getPostProcessor(rc.w, rc.h);

            // Stop current post processing buffer if any
            if (rc.ppb != null) {
                rc.ppb.captureEnd();
            }

            pp.rebind();
            // Capture bloom
            pp.capture();

            super.renderStud(renderables, camera);

            // Render bloom to our frame buffer
            pp.render(our_fb);

            // Fetch stars_with_bloom texture
            Texture tex = our_fb.getColorBufferTexture();

            // Restart current post processing if any
            if (rc.ppb != null) {
                rc.ppb.captureNoClear();
            }

            /** DRAW TO CURRENT BUFFER **/

            Viewport vp = camera.getCurrent().getViewport();

            float outWidth = vp.getWorldWidth();
            float outHeight = vp.getWorldHeight();
            int width = rc.w;
            int height = rc.h;
            int screenX = vp.getScreenX();
            int screenY = vp.getScreenY();
            int sw = Gdx.graphics.getWidth();
            int sh = Gdx.graphics.getHeight();

            if (rc.ppb != null) {
                // Render to screen
                outWidth = Math.max(sw, outWidth);
                outHeight = Math.max(sh, outHeight);
            }
            if (GlobalConf.program.STEREOSCOPIC_MODE && rc.ppb != null && screenX > 0) {
                screenX = Gdx.graphics.getWidth() / 2;
            }
            if (GlobalConf.program.STEREOSCOPIC_MODE && (GlobalConf.program.STEREO_PROFILE == StereoProfile.HD_3DTV || rc.ppb != null)) {
                width *= 2;
            }

            GlobalResources.spriteBatch.begin();
            GlobalResources.spriteBatch.setColor(1f, 1f, 1f, 1f);
            GlobalResources.spriteBatch.draw(tex, screenX, screenY, 0, 0, outWidth, outHeight, 1, 1, 0, 0, 0, width, height, false, true);
            GlobalResources.spriteBatch.end();
            // Apply previous viewport
            if (GlobalConf.program.STEREOSCOPIC_MODE)
                vp.apply();
        } else {
            /** Render to image, use regular render method **/
            super.renderStud(renderables, camera);
        }

    }

    @Override
    public void notify(Events event, Object... data) {
        super.notify(event, data);
        switch (event) {
        case SCREEN_RESIZE:
            getFrameBuffer((Integer) data[0], (Integer) data[1]);
            break;
        case TOGGLE_STEREOSCOPIC:
            // Update size
            if (GlobalConf.program.STEREOSCOPIC_MODE) {
                getFrameBuffer(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight());
            }
            break;
        }
    }

    private FrameBuffer getFrameBuffer(int w, int h) {
        String key = getKey(w, h);
        if (!fbmap.containsKey(key)) {
            FrameBuffer fb = new FrameBuffer(Format.RGB888, w, h, true);
            fbmap.put(key, fb);
        }
        return fbmap.get(key);
    }

    protected abstract PostProcessor getPostProcessor(int w, int h);

    protected String getKey(int w, int h) {
        return w + "x" + h;
    }

}
