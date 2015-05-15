package gaia.cu9.ari.gaiaorbit.scenegraph;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.render.SceneGraphRenderer.ComponentType;
import gaia.cu9.ari.gaiaorbit.scenegraph.component.ModelComponent;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.ModelCache;
import gaia.cu9.ari.gaiaorbit.util.Pair;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.TimeUtils;

import java.util.Map;
import java.util.TreeMap;

public class Star extends Particle {

    /** Has the model used to represent the star **/
    private static ModelComponent mc;
    private static Matrix4 modelTransform;

    public static void initModel() {
        if (mc == null) {
            Texture tex = new Texture(Gdx.files.internal(GlobalConf.TEXTURES_FOLDER + "star.jpg"));
            Texture lut = new Texture(Gdx.files.internal(GlobalConf.TEXTURES_FOLDER + "lut.jpg"));
            tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);

            Map<String, Object> params = new TreeMap<String, Object>();
            params.put("quality", 120l);
            params.put("diameter", 1d);
            params.put("flip", false);

            Pair<Model, Map<String, Material>> pair = ModelCache.cache.getModel("sphere", params, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
            Model model = pair.getFirst();
            Material mat = pair.getSecond().get("base");
            mat.clear();
            mat.set(new TextureAttribute(TextureAttribute.Diffuse, tex));
            mat.set(new TextureAttribute(TextureAttribute.Normal, lut));
            // Only to activate view vector (camera position)
            mat.set(new TextureAttribute(TextureAttribute.Specular, lut));
            mat.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
            modelTransform = new Matrix4();
            mc = new ModelComponent(false);
            mc.env = new Environment();
            mc.env.set(new ColorAttribute(ColorAttribute.AmbientLight, 1f, 1f, 1f, 1f));
            mc.env.set(new FloatAttribute(FloatAttribute.Shininess, 0f));
            mc.instance = new ModelInstance(model, modelTransform);
        }
    }

    double modelDistance;

    public Star() {
        this.parentName = ROOT_NAME;
    }

    public Star(Vector3d pos, float appmag, float absmag, float colorbv, String name, long starid) {
        super(pos, appmag, absmag, colorbv, name, starid);
    }

    public Star(Vector3d pos, float appmag, float absmag, float colorbv, String name, double ra, double dec, long starid) {
        super(pos, appmag, absmag, colorbv, name, ra, dec, starid);
    }

    @Override
    public void initialize() {
        super.initialize();
        modelDistance = 172.4643429 * radius;
        ct = ComponentType.Stars;
    }

    @Override
    protected void addToRenderLists(ICamera camera) {
        if (viewAngleApparent >= THRESHOLD_ANGLE_NONE() * camera.getFovFactor()) {
            if (camera.getCurrent() instanceof FovCamera) {
                // Only shader for FovCamera
                addToRender(this, RenderGroup.SHADER);
            } else {
                if (viewAngleApparent >= THRESHOLD_ANGLE_POINT() * camera.getFovFactor()) {
                    addToRender(this, RenderGroup.SHADER);
                    if (distToCamera < modelDistance) {
                        camera.checkClosest(this);
                        addToRender(this, RenderGroup.MODEL_S);
                    }
                    // Check Sol position for gravity distortion

//                    if (this.name.equalsIgnoreCase("sol")) {
//                        // We have the closest shader star
//                        Vector3 aux = auxVector3f.get();
//                        camera.getCamera().project(aux.set(transform.getTranslationf()));
//                        float x = aux.x;
//                        float y = aux.y;
//
//                        EventManager.instance.post(Events.GRAVITATIONAL_LENSING_PARAMS, x, y);
//                    }
                }
            }
            if (renderText()) {
                addToRender(this, RenderGroup.LABEL);
            }
        }

    }

    @Override
    public void render(ModelBatch modelBatch, float alpha) {
        mc.setTransparency(alpha);
        ((ColorAttribute) mc.env.get(ColorAttribute.AmbientLight)).color.set(cc[0], cc[1], cc[2], 1f);
        ((FloatAttribute) mc.env.get(FloatAttribute.Shininess)).value = TimeUtils.getRunningTimeSecs();
        // Local transform
        mc.instance.transform.set(transform.getMatrix().valuesf()).scl(getRadius() * 2);
        modelBatch.render(mc.instance, mc.env);
    }

    @Override
    public void doneLoading(AssetManager manager) {
        initModel();
    }
}
