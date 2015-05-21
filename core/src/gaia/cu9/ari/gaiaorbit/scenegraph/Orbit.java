package gaia.cu9.ari.gaiaorbit.scenegraph;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import gaia.cu9.ari.gaiaorbit.data.orbit.IOrbitDataProvider;
import gaia.cu9.ari.gaiaorbit.data.orbit.OrbitData;
import gaia.cu9.ari.gaiaorbit.data.orbit.OrbitDataLoader;
import gaia.cu9.ari.gaiaorbit.render.system.LineRenderSystem;
import gaia.cu9.ari.gaiaorbit.scenegraph.component.OrbitComponent;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.math.Matrix4d;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

import java.lang.reflect.Method;
import java.util.Date;

public class Orbit extends LineObject {

    /** Threshold angle **/
    protected static final float ANGLE_LIMIT = (float) Math.toRadians(1.5);
    /**
     * Special overlap factor
     */
    protected static final float SHADER_MODEL_OVERLAP_FACTOR = 20f;

    public OrbitData orbitData;
    protected Vector3d prev, curr;
    protected float alpha;
    public Matrix4d localTransformD, transformFunction;
    protected String provider;
    protected Class<? extends IOrbitDataProvider> providerClass;
    public OrbitComponent oc;

    public Orbit() {
        super();
        localTransformD = new Matrix4d();
        prev = new Vector3d();
        curr = new Vector3d();
    }

    @Override
    public void initialize() {
        try {
            providerClass = (Class<? extends IOrbitDataProvider>) Class.forName(provider);
            // Orbit data
            IOrbitDataProvider provider;
            try {
                provider = providerClass.newInstance();
                provider.load(oc.source, new OrbitDataLoader.OrbitDataLoaderParameter(name, providerClass, oc));
                orbitData = provider.getData();
            } catch (Exception e) {
                Gdx.app.error(getClass().getSimpleName(), e.getMessage());
            }
        } catch (ClassNotFoundException e) {
            Gdx.app.error(getClass().getSimpleName(), e.getMessage());
        }
    }

    @Override
    public void doneLoading(AssetManager manager) {
        alpha = cc[3];
        int last = orbitData.getNumPoints() - 1;
        Vector3d v = new Vector3d(orbitData.x.get(last), orbitData.y.get(last), orbitData.z.get(last));
        this.size = (float) v.len() * 5;
    }

    @Override
    public void updateLocal(ITimeFrameProvider time, ICamera camera) {
        super.updateLocal(time, camera);
        updateLocalTransform(time.getTime());
    }

    protected void updateLocalTransform(Date date) {
        if (oc.source != null || parent.orientation == null) {
            // Orbit is sampled, only get position
            localTransformD.set(transform.getMatrix());
            if (transformFunction != null)
                localTransformD.mul(transformFunction);

            localTransformD.rotate(0, 1, 0, oc.argofpericenter);
            localTransformD.rotate(0, 0, 1, oc.i);
            localTransformD.rotate(0, 1, 0, oc.ascendingnode);
        } else {
            // Orbit is defined by its parameters and not sampled
            // Set to parent orientation
            localTransformD.set(transform.getMatrix()).mul(parent.orientation);

            localTransformD.rotate(0, 1, 0, oc.argofpericenter);
            localTransformD.rotate(0, 0, 1, oc.i);
            localTransformD.rotate(0, 1, 0, oc.ascendingnode);
        }

    }

    @Override
    public void updateLocalValues(ITimeFrameProvider time, ICamera camera) {
    }

    @Override
    protected void addToRenderLists(ICamera camera) {
        float angleLimit = ANGLE_LIMIT * camera.getFovFactor();
        if (viewAngle > angleLimit) {
            if (viewAngle < angleLimit * SHADER_MODEL_OVERLAP_FACTOR) {
                float alpha = MathUtilsd.lint(viewAngle, angleLimit, angleLimit * SHADER_MODEL_OVERLAP_FACTOR, 0, cc[3]);
                this.alpha = alpha;
            } else {
                this.alpha = cc[3];
            }

            addToRender(this, RenderGroup.LINE);
        }

    }

    @Override
    public void render(LineRenderSystem renderer, ICamera camera, float alpha) {
        alpha *= this.alpha;

        // Make origin Gaia
        Vector3d parentPos = null;
        if (parent instanceof Gaia) {
            parentPos = ((Gaia) parent).unrotatedPos;
        }

        // This is so that the shape renderer does not mess up the z-buffer
        for (int i = 1; i < orbitData.getNumPoints(); i++) {
            orbitData.loadPoint(prev, i - 1);
            orbitData.loadPoint(curr, i);

            if (parentPos != null) {
                prev.sub(parentPos);
                curr.sub(parentPos);
            }

            prev.mul(localTransformD);
            curr.mul(localTransformD);

            renderer.addLine((float) prev.x, (float) prev.y, (float) prev.z, (float) curr.x, (float) curr.y, (float) curr.z, cc[0], cc[1], cc[2], alpha);

        }
    }

    /**
     * Sets the absolute size of this entity
     * @param size
     */
    public void setSize(Float size) {
        this.size = size * (float) Constants.KM_TO_U;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setOrbit(OrbitComponent oc) {
        this.oc = oc;
    }

    public void setTransformFunction(String transformFunction) {
        if (transformFunction != null && !transformFunction.isEmpty()) {
            try {
                Method m = Coordinates.class.getMethod(transformFunction);
                this.transformFunction = (Matrix4d) m.invoke(null);
            } catch (Exception e) {
                Logger.error(e);
            }

        }
    }

}
