package gaia.cu9.ari.gaiaorbit.desktop.render;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.render.IPostProcessor;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.bitfire.postprocessing.PostProcessor;
import com.bitfire.postprocessing.effects.Bloom;
import com.bitfire.postprocessing.effects.Fxaa;
import com.bitfire.postprocessing.effects.LensFlare2;
import com.bitfire.postprocessing.effects.MotionBlur;
import com.bitfire.postprocessing.effects.Nfaa;
import com.bitfire.utils.ShaderLoader;

public class DesktopPostProcessor implements IPostProcessor, IObserver {

    private PostProcessBean[] pps;

    float bloomFboScale = 0.5f;
    float lensFboScale = 0.25f;

    public DesktopPostProcessor() {
        ShaderLoader.BasePath = "shaders/";

        pps = new PostProcessBean[RenderType.values().length];

        pps[RenderType.screen.index] = newPostProcessor(getWidth(RenderType.screen), getHeight(RenderType.screen));
        if (Constants.desktop) {
            pps[RenderType.screenshot.index] = newPostProcessor(getWidth(RenderType.screenshot), getHeight(RenderType.screenshot));
            pps[RenderType.frame.index] = newPostProcessor(getWidth(RenderType.frame), getHeight(RenderType.frame));
        }

        // Output AA info.
        if (GlobalConf.postprocess.POSTPROCESS_ANTIALIAS == -1) {
            Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.selected", "FXAA"));
        } else if (GlobalConf.postprocess.POSTPROCESS_ANTIALIAS == -2) {
            Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.selected", "NFAA"));
        }

        EventManager.instance.subscribe(this, Events.PROPERTIES_WRITTEN, Events.BLOOM_CMD, Events.LENS_FLARE_CMD, Events.MOTION_BLUR_CMD);

    }

    private int getWidth(RenderType type) {
        switch (type) {
        case screen:
            return Gdx.graphics.getWidth();
        case screenshot:
            return GlobalConf.screenshot.SCREENSHOT_WIDTH;
        case frame:
            return GlobalConf.frame.RENDER_WIDTH;
        }
        return 0;
    }

    private int getHeight(RenderType type) {
        switch (type) {
        case screen:
            return Gdx.graphics.getHeight();
        case screenshot:
            return GlobalConf.screenshot.SCREENSHOT_HEIGHT;
        case frame:
            return GlobalConf.frame.RENDER_HEIGHT;
        }
        return 0;
    }

    private PostProcessBean newPostProcessor(int width, int height) {
        PostProcessBean ppb = new PostProcessBean();

        ppb.pp = new PostProcessor(width, height, true, false, true);

        // MOTION BLUR
        ppb.motionblur = new MotionBlur();
        ppb.motionblur.setBlurOpacity(GlobalConf.postprocess.POSTPROCESS_MOTION_BLUR);
        ppb.pp.addEffect(ppb.motionblur);

        // BLOOM
        ppb.bloom = new Bloom((int) (width * bloomFboScale), (int) (height * bloomFboScale));
        ppb.bloom.setBloomIntesity(GlobalConf.postprocess.POSTPROCESS_BLOOM_INTENSITY);
        ppb.bloom.setThreshold(0f);
        ppb.bloom.setEnabled(GlobalConf.postprocess.POSTPROCESS_BLOOM_INTENSITY > 0);
        ppb.pp.addEffect(ppb.bloom);

        // LENS FLARE
        ppb.lens = new LensFlare2((int) (width * lensFboScale), (int) (height * lensFboScale));
        ppb.lens.setGhosts(14);
        ppb.lens.setHaloWidth(0.55f);
        ppb.lens.setLensColorTexture(new Texture(Gdx.files.internal("img/lenscolor.png")));
        ppb.lens.setFlareIntesity(.39f);
        ppb.lens.setFlareSaturation(0.7f);
        ppb.lens.setBaseIntesity(1f);
        ppb.lens.setBias(-0.99f);
        ppb.lens.setBlurAmount(0.0f);
        ppb.lens.setBlurPasses(2);
        ppb.lens.setEnabled(GlobalConf.postprocess.POSTPROCESS_LENS_FLARE);
        ppb.pp.addEffect(ppb.lens);

        // ANTIALIAS
        if (GlobalConf.postprocess.POSTPROCESS_ANTIALIAS == -1) {
            ppb.antialiasing = new Fxaa(width, height);
            ((Fxaa) ppb.antialiasing).setSpanMax(2f);
        } else {
            ppb.antialiasing = new Nfaa(width, height);
        }
        ppb.antialiasing.setEnabled(GlobalConf.postprocess.POSTPROCESS_ANTIALIAS < 0);
        ppb.pp.addEffect(ppb.antialiasing);

        return ppb;
    }

    @Override
    public PostProcessBean getPostProcessBean(RenderType type) {
        return pps[type.index];
    }

    @Override
    public void resize(final int width, final int height) {
        if (pps[RenderType.screen.index].antialiasing != null) {
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    replace(RenderType.screen.index, width, height);
                }
            });
        }

    }

    @Override
    public void notify(Events event, final Object... data) {
        switch (event) {
        case PROPERTIES_WRITTEN:
            if (changed(pps[RenderType.screenshot.index].pp, GlobalConf.screenshot.SCREENSHOT_WIDTH, GlobalConf.screenshot.SCREENSHOT_HEIGHT)) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        replace(RenderType.screenshot.index, GlobalConf.screenshot.SCREENSHOT_WIDTH, GlobalConf.screenshot.SCREENSHOT_HEIGHT);
                    }
                });
            }

            if (changed(pps[RenderType.frame.index].pp, GlobalConf.frame.RENDER_WIDTH, GlobalConf.frame.RENDER_HEIGHT)) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        replace(RenderType.frame.index, GlobalConf.frame.RENDER_WIDTH, GlobalConf.frame.RENDER_HEIGHT);
                    }
                });
            }
            break;
        case BLOOM_CMD:
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    float intensity = (float) data[0];
                    for (int i = 0; i < RenderType.values().length; i++) {
                        PostProcessBean ppb = pps[i];
                        ppb.bloom.setBloomIntesity(intensity);
                        ppb.bloom.setEnabled(intensity > 0);
                    }
                }
            });
            break;
        case LENS_FLARE_CMD:
            boolean active = (Boolean) data[0];
            for (int i = 0; i < RenderType.values().length; i++) {
                PostProcessBean ppb = pps[i];
                ppb.lens.setEnabled(active);
            }
            break;
        case CAMERA_MOTION_UPDATED:
            //    rts = RenderType.values();
            //    float strength = (float) MathUtilsd.lint(((double) data[1] * Constants.KM_TO_U * Constants.U_TO_PC), 0, 100, 0, 0.05);
            //    for (int i = 0; i < rts.length; i++) {
            //pps[i].zoomer.setBlurStrength(strength);
            //    }
            break;
        case MOTION_BLUR_CMD:
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    float opacity = (float) data[0];
                    for (int i = 0; i < RenderType.values().length; i++) {
                        PostProcessBean ppb = pps[i];
                        ppb.motionblur.setBlurOpacity(opacity);
                        ppb.motionblur.setEnabled(opacity > 0);
                    }
                }
            });
            break;
        }

    }

    /**
     * Reloads the postprocessor at the given index with the given width and height.
     * @param index
     * @param width
     * @param height
     */
    private void replace(int index, final int width, final int height) {
        //pps[index].pp.dispose(false);
        pps[index] = newPostProcessor(width, height);

    }

    private boolean changed(PostProcessor postProcess, int width, int height) {
        return postProcess.getCombinedBuffer().width != width || postProcess.getCombinedBuffer().height != height;
    }

}
