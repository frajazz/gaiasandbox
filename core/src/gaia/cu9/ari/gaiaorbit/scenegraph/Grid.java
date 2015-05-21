package gaia.cu9.ari.gaiaorbit.scenegraph;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import gaia.cu9.ari.gaiaorbit.render.IAnnotationsRenderable;
import gaia.cu9.ari.gaiaorbit.render.IModelRenderable;
import gaia.cu9.ari.gaiaorbit.scenegraph.component.ModelComponent;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.ModelCache;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.g3d.MeshPartBuilder2;
import gaia.cu9.ari.gaiaorbit.util.g3d.ModelBuilder2;
import gaia.cu9.ari.gaiaorbit.util.math.Matrix4d;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Grid extends AbstractPositionEntity implements IModelRenderable, IAnnotationsRenderable {
    private static final float ANNOTATIONS_ALPHA = 0.8f;

    private static final int divisionsU = 36;
    private static final int divisionsV = 18;

    private BitmapFont font;
    private String transformName;
    public ModelComponent mc;
    private Vector3 auxf;
    private Vector3d auxd;

    public Grid() {
        super();
        localTransform = new Matrix4();
        auxf = new Vector3();
        auxd = new Vector3d();
    }

    @Override
    public void initialize() {
        mc = new ModelComponent();
    }

    @Override
    public void doneLoading(AssetManager manager) {
        Material material = new Material(new BlendingAttribute(cc[3]),
                new ColorAttribute(ColorAttribute.Diffuse, cc[0], cc[1], cc[2], cc[3]));
        // Load model
        ModelBuilder2 modelBuilder = ModelCache.cache.mb;
        modelBuilder.begin();
        //create part
        MeshPartBuilder2 bPartBuilder = modelBuilder.part("sph", GL20.GL_LINES, Usage.Position, material);
        bPartBuilder.sphere(1, 1, 1, divisionsU, divisionsV);

        Model model = (modelBuilder.end());
        // Initialize transform
        localTransform.scl(size);
        if (transformName != null) {
            Class<Coordinates> c = Coordinates.class;
            try {
                Method m = c.getMethod(transformName);
                Matrix4d trf = (Matrix4d) m.invoke(null);
                Matrix4 aux = new Matrix4(trf.valuesf());
                localTransform.mul(aux);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                Gdx.app.error(Grid.class.getName(), "Error getting/invoking method Coordinates." + transformName + "()");
            }
        } else {
            // Equatorial, nothing
        }
        mc.instance = new ModelInstance(model, this.localTransform);

        font = GlobalResources.skin.getFont("ui-11");

    }

    @Override
    protected void addToRenderLists(ICamera camera) {
        // Render group never changes
        // Add to toRender list
        addToRender(this, RenderGroup.MODEL_B);
        addToRender(this, RenderGroup.MODEL_B_ANNOT);
    }

    @Override
    public void updateLocalValues(ITimeFrameProvider time, ICamera camera) {
    }

    @Override
    public void render(Object... params) {
        if (params[0] instanceof ModelBatch) {
            render((ModelBatch) params[0], (Float) params[1]);
        } else if (params[0] instanceof SpriteBatch) {
            render((SpriteBatch) params[0], (ICamera) params[1], (Float) params[2]);
        }
    }

    /**
     * Model rendering.
     */
    @Override
    public void render(ModelBatch modelBatch, float alpha) {
        Gdx.gl.glLineWidth(1f);
        mc.setTransparencyColor(alpha * cc[3] * opacity);
        modelBatch.render(mc.instance, mc.env);
    }

    /**
     * Annotation rendering
     */
    @Override
    public void render(SpriteBatch spriteBatch, ICamera camera, float alpha) {

        // Horizon
        float stepAngle = 360 / divisionsU;
        alpha *= ANNOTATIONS_ALPHA;
        for (int angle = 0; angle < 360; angle += stepAngle) {
            auxf.set(Coordinates.sphericalToCartesian(Math.toRadians(angle), 0, 1f, auxd).valuesf()).mul(localTransform).nor();
            if (auxf.dot(camera.getCamera().direction.nor()) > 0) {
                auxf.add(camera.getCamera().position);
                camera.getCamera().project(auxf);
                float pl = .7f;
                font.setColor(Math.min(1, cc[0] + pl), Math.min(1, cc[1] + pl), Math.min(1, cc[2] + pl), alpha);
                font.draw(spriteBatch, Integer.toString(angle), auxf.x, auxf.y);
            }

        }
        // North-south line
        stepAngle = 180 / divisionsV;
        for (int angle = -90; angle <= 90; angle += stepAngle) {
            if (angle != 0) {
                auxf.set(Coordinates.sphericalToCartesian(0, Math.toRadians(angle), 1f, auxd).valuesf()).mul(localTransform).nor();
                if (auxf.dot(camera.getCamera().direction.nor()) > 0) {
                    auxf.add(camera.getCamera().position);
                    camera.getCamera().project(auxf);
                    float pl = .7f;
                    font.setColor(Math.min(1, cc[0] + pl), Math.min(1, cc[1] + pl), Math.min(1, cc[2] + pl), alpha);
                    font.draw(spriteBatch, Integer.toString(angle), auxf.x, auxf.y);
                }
                auxf.set(Coordinates.sphericalToCartesian(0, Math.toRadians(-angle), -1f, auxd).valuesf()).mul(localTransform).nor();
                if (auxf.dot(camera.getCamera().direction.nor()) > 0) {
                    auxf.add(camera.getCamera().position);
                    camera.getCamera().project(auxf);
                    float pl = .7f;
                    font.setColor(Math.min(1, cc[0] + pl), Math.min(1, cc[1] + pl), Math.min(1, cc[2] + pl), alpha);
                    font.draw(spriteBatch, Integer.toString(angle), auxf.x, auxf.y);
                }
            }
        }

    }

    public void setTransformName(String transformName) {
        this.transformName = transformName;
    }

    @Override
    public boolean hasAtmosphere() {
        return false;
    }

}
