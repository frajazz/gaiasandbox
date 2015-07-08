package gaia.cu9.ari.gaiaorbit.render;

import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.bitfire.postprocessing.PostProcessor;
import com.bitfire.postprocessing.effects.Antialiasing;
import com.bitfire.postprocessing.effects.Bloom;
import com.bitfire.postprocessing.effects.LensFlare2;
import com.bitfire.postprocessing.effects.MotionBlur;

public interface IPostProcessor {
    public class PostProcessBean {
        public PostProcessor pp;
        public Bloom bloom;
        public Antialiasing antialiasing;
        public LensFlare2 lens;
        public MotionBlur motionblur;

        public boolean capture() {
            return pp.capture();
        }

        public boolean captureNoClear() {
            return pp.captureNoClear();
        }

        public void render() {
            pp.render();
        }

        public FrameBuffer captureEnd() {
            return pp.captureEnd();
        }

        public void render(FrameBuffer dest) {
            pp.render(dest);
        }

    }

    public PostProcessBean getPostProcessBean();

    public void resize(int width, int height);
}
