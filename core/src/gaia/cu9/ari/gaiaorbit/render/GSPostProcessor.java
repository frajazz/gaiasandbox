package gaia.cu9.ari.gaiaorbit.render;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;

import com.badlogic.gdx.Gdx;
import com.bitfire.postprocessing.PostProcessor;
import com.bitfire.utils.ShaderLoader;

public class GSPostProcessor implements IPostProcessor, IObserver {

    private PostProcessBean pps;

    float bloomFboScale = 0.5f;
    float lensFboScale = 0.25f;

    public GSPostProcessor() {
        ShaderLoader.BasePath = "shaders/";

        pps = newPostProcessor(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        ;

        // Output AA info.
        if (GlobalConf.postprocess.POSTPROCESS_ANTIALIAS == -1) {
            Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.selected", "FXAA"));
        } else if (GlobalConf.postprocess.POSTPROCESS_ANTIALIAS == -2) {
            Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.selected", "NFAA"));
        }

        EventManager.instance.subscribe(this, Events.BLOOM_CMD, Events.LENS_FLARE_CMD, Events.MOTION_BLUR_CMD);

    }

    private PostProcessBean newPostProcessor(int width, int height) {
        PostProcessBean ppb = new PostProcessBean();

        ppb.pp = new PostProcessor(width, height, true, false, true);

        //        // MOTION BLUR
        //        ppb.motionblur = new MotionBlur();
        //        ppb.motionblur.setBlurOpacity(GlobalConf.postprocess.POSTPROCESS_MOTION_BLUR);
        //        ppb.pp.addEffect(ppb.motionblur);
        //
        //        // BLOOM
        //        ppb.bloom = new Bloom((int) (width * bloomFboScale), (int) (height * bloomFboScale));
        //        ppb.bloom.setBloomIntesity(GlobalConf.postprocess.POSTPROCESS_BLOOM_INTENSITY);
        //        ppb.bloom.setThreshold(0f);
        //        ppb.bloom.setEnabled(GlobalConf.postprocess.POSTPROCESS_BLOOM_INTENSITY > 0);
        //        ppb.pp.addEffect(ppb.bloom);
        //
        //        // LENS FLARE
        //        ppb.lens = new LensFlare2((int) (width * lensFboScale), (int) (height * lensFboScale));
        //        ppb.lens.setGhosts(14);
        //        ppb.lens.setHaloWidth(0.55f);
        //        ppb.lens.setLensColorTexture(new Texture(Gdx.files.internal("img/lenscolor.png")));
        //        ppb.lens.setFlareIntesity(.39f);
        //        ppb.lens.setFlareSaturation(0.7f);
        //        ppb.lens.setBaseIntesity(1f);
        //        ppb.lens.setBias(-0.99f);
        //        ppb.lens.setBlurAmount(0.0f);
        //        ppb.lens.setBlurPasses(2);
        //        ppb.lens.setEnabled(GlobalConf.postprocess.POSTPROCESS_LENS_FLARE);
        //        ppb.pp.addEffect(ppb.lens);
        //
        //        // ANTIALIAS
        //        if (GlobalConf.postprocess.POSTPROCESS_ANTIALIAS == -1) {
        //            ppb.antialiasing = new Fxaa(width, height);
        //            ((Fxaa) ppb.antialiasing).setSpanMax(2f);
        //        } else {
        //            ppb.antialiasing = new Nfaa(width, height);
        //        }
        //        ppb.antialiasing.setEnabled(GlobalConf.postprocess.POSTPROCESS_ANTIALIAS < 0);
        //        ppb.pp.addEffect(ppb.antialiasing);

        return ppb;
    }

    @Override
    public PostProcessBean getPostProcessBean() {
        return pps;
    }

    @Override
    public void resize(final int width, final int height) {
        if (pps.antialiasing != null) {
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    replace(width, height);
                }
            });
        }

    }

    @Override
    public void notify(Events event, final Object... data) {
        switch (event) {

        case BLOOM_CMD:
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    float intensity = (float) data[0];
                    pps.bloom.setBloomIntesity(intensity);
                    pps.bloom.setEnabled(intensity > 0);
                }
            });
            break;
        case LENS_FLARE_CMD:
            boolean active = (Boolean) data[0];
            pps.lens.setEnabled(active);
            break;
        case MOTION_BLUR_CMD:
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    float opacity = (float) data[0];
                    pps.motionblur.setBlurOpacity(opacity);
                    pps.motionblur.setEnabled(opacity > 0);
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
    private void replace(final int width, final int height) {
        pps = newPostProcessor(width, height);

    }

    private boolean changed(PostProcessor postProcess, int width, int height) {
        return postProcess.getCombinedBuffer().width != width || postProcess.getCombinedBuffer().height != height;
    }

}
