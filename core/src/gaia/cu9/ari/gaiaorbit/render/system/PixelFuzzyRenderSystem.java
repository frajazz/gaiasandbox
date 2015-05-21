package gaia.cu9.ari.gaiaorbit.render.system;

import com.badlogic.gdx.Gdx;
import com.bitfire.postprocessing.PostProcessor;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;
import gaia.cu9.ari.gaiaorbit.util.postprocessing.effects.Fuzzy;
import gaia.cu9.ari.gaiaorbit.util.postprocessing.effects.GravitationalDistortion;

import java.util.ArrayList;
import java.util.List;

public class PixelFuzzyRenderSystem extends PixelPostProcessRenderSystem implements IObserver {

    List<GravitationalDistortion> distortionList;

    public PixelFuzzyRenderSystem(RenderGroup rg, int priority, float[] alphas) {
        super(rg, priority, alphas);
        EventManager.instance.subscribe(this, Events.GRAVITATIONAL_LENSING_PARAMS);
    }

    protected PostProcessor getPostProcessor(int w, int h) {
        String key = getKey(w, h);
        if (!ppmap.containsKey(key)) {
            PostProcessor pp = new PostProcessor(w, h, true, true, true);

            // Fuzzy
            Fuzzy fuzzy = new Fuzzy(w, h, 3f);
            pp.addEffect(fuzzy);

            // Distortion
            GravitationalDistortion distortion = new GravitationalDistortion(w, h);
            distortion.setMassPosition(-100, -100);
            pp.addEffect(distortion);
            if (distortionList == null) {
                distortionList = new ArrayList<GravitationalDistortion>(4);
            }
            distortionList.add(distortion);
            distortion.setEnabled(true);

            // Enable post processor
            pp.setEnabled(true);

            ppmap.put(key, pp);
        }
        return ppmap.get(key);
    }

    @Override
    public void notify(Events event, final Object... data) {
        super.notify(event, data);
        switch (event) {
        case GRAVITATIONAL_LENSING_PARAMS:
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    for (GravitationalDistortion distortion : distortionList) {
                        distortion.setMassPosition((float) data[0], (float) data[1]);
                    }
                }
            });
            break;
        }
    }

}
