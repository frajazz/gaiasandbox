package gaia.cu9.ari.gaiaorbit.scenegraph;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Method;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import gaia.cu9.ari.gaiaorbit.data.galaxy.PointDataProvider;
import gaia.cu9.ari.gaiaorbit.render.I3DTextRenderable;
import gaia.cu9.ari.gaiaorbit.scenegraph.component.PointdataComponent;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.math.Matrix4d;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

public class MilkyWayReal extends Blob implements I3DTextRenderable {
    float[] labelColour = new float[] { 1f, 1f, 1f, 1f };
    String transformName;
    Matrix4 coordinateSystem;

    private List<Vector3> pointData;
    protected String provider;
    public PointdataComponent pc;

    public MilkyWayReal() {
        super();
        localTransform = new Matrix4();
        lowAngle = (float) Math.toRadians(60);
        highAngle = (float) Math.toRadians(75.51);
    }

    public void initialize() {
        /** Load data **/
        PointDataProvider provider = new PointDataProvider();
        try {
            pointData = provider.loadData(pc.source);
        } catch (Exception e) {
            Logger.error(e, getClass().getSimpleName());
        }

    }

    @Override
    public void doneLoading(AssetManager manager) {
        super.doneLoading(manager);

        // Set static coordinates to position
        coordinates.getEquatorialCartesianCoordinates(null, pos);

        // Initialize transform
        if (transformName != null) {
            Class<Coordinates> c = Coordinates.class;
            try {
                Method m = ClassReflection.getMethod(c, transformName);
                Matrix4d trf = (Matrix4d) m.invoke(null);
                coordinateSystem = new Matrix4(trf.valuesf());
            } catch (ReflectionException e) {
                Gdx.app.error(this.getClass().getName(), "Error getting/invoking method Coordinates." + transformName + "()");
            }
        } else {
            // Equatorial, nothing
        }

        // Transform all
        for (Vector3 point : pointData) {
            point.scl(size).mul(coordinateSystem);
        }

    }

    @Override
    protected void addToRenderLists(ICamera camera) {
        if (viewAngle <= highAngle) {

            if (renderText()) {
                addToRender(this, RenderGroup.LABEL);
            }
        }
    }

    @Override
    public void updateLocal(ITimeFrameProvider time, ICamera camera) {
        super.updateLocal(time, camera);
        // Directional light comes from up
        updateLocalTransform();

    }

    /**
     * Update the local transform with the transform and the rotations/scales necessary.
     * Override if your model contains more than just the position and size.
     */
    protected void updateLocalTransform() {
        // Scale + Rotate + Tilt + Translate 
        float[] trans = transform.getMatrix().getTranslationf();
        localTransform.idt().translate(trans[0], trans[1], trans[2]).scl(size);
        localTransform.mul(coordinateSystem);
    }

    @Override
    public void render(Object... params) {
        if (params[0] instanceof SpriteBatch) {
            render((SpriteBatch) params[0], (ShaderProgram) params[1], (BitmapFont) params[2], (ICamera) params[3]);
        }
    }

    /**
     * Label rendering.
     */
    @Override
    public void render(SpriteBatch batch, ShaderProgram shader, BitmapFont font, ICamera camera) {
        Vector3d pos = v3dpool.obtain();
        textPosition(pos);
        shader.setUniformf("a_viewAngle", 90f);
        shader.setUniformf("a_thOverFactor", 1f);
        render3DLabel(batch, shader, font, camera, text(), pos, textScale(), textSize(), textColour());
        v3dpool.free(pos);
    }

    public void setTransformName(String transformName) {
        this.transformName = transformName;
    }

    @Override
    public boolean renderText() {
        return true;
    }

    /**
     * Sets the absolute size of this entity
     * @param size
     */
    public void setSize(Double size) {
        this.size = (float) (size * Constants.KM_TO_U);
    }

    @Override
    public float[] textColour() {
        return labelColour;
    }

    @Override
    public float textSize() {
        return distToCamera * 3e-3f;
    }

    @Override
    public float textScale() {
        return 3f;
    }

    @Override
    public void textPosition(Vector3d out) {
        transform.getTranslation(out);
    }

    @Override
    public String text() {
        return name;
    }

    @Override
    public void textDepthBuffer() {
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthMask(false);
    }

    public void setLabelcolor(double[] labelcolor) {
        this.labelColour = GlobalResources.toFloatArray(labelcolor);

    }

    @Override
    public boolean isLabel() {
        return true;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setPointdata(PointdataComponent pc) {
        this.pc = pc;
    }

}
