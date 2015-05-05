package gaia.cu9.ari.gaiaorbit.scenegraph;

import gaia.cu9.ari.gaiaorbit.render.ILabelRenderable;
import gaia.cu9.ari.gaiaorbit.util.DecalUtils;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.coord.IBodyCoordinates;
import gaia.cu9.ari.gaiaorbit.util.math.Vector2d;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

/**
 * A base abstract graphical entity with the basics.
 * @author Toni Sagrista
 *
 */
public abstract class AbstractPositionEntity extends SceneGraphNode {
    /**
     * Overlap factor applied to angle to get the upper boundary when rendering with shader and model. 
     */
    public static final float SHADER_MODEL_OVERLAP_FACTOR = 5;
    /**
     * Position of this entity in the local reference system.
     * The units are {@link gaia.cu9.ari.gaiaorbit.util.Constants#U_TO_KM} by default. 
     */
    public Vector3d pos;

    /** 
     * Coordinates provider. Helps updating the position at each time step. 
     **/
    protected IBodyCoordinates coordinates;

    /**
     * Position in the equatorial system; ra, dec.
     */
    public Vector2d posSph;

    /**
     * Size factor in units
     * of {@link gaia.cu9.ari.gaiaorbit.util.Constants#U_TO_KM}.
     */
    public float size;

    /**
     * The distance to the camera from the focus center.
     */
    public float distToCamera;

    /**
     * The viewing angle in radians.
     */
    public float viewAngle, viewAngleApparent;

    /**
     * Base color
     */
    public float[] cc;

    /**
     * Is this just a copy?
     */
    protected boolean copy = false;

    protected AbstractPositionEntity() {
        super();
        // Positions
        pos = new Vector3d();
        posSph = new Vector2d();
    }

    public AbstractPositionEntity(SceneGraphNode parent) {
        super(parent);
        // Positions
        pos = new Vector3d();
        posSph = new Vector2d();
    }

    public AbstractPositionEntity(String name) {
        super(name);
    }

    @Override
    public void doneLoading(AssetManager manager) {
        super.doneLoading(manager);

        if (coordinates != null)
            coordinates.doneLoading(sg);
    }

    public Vector3d getPosition(Vector3d aux) {
        return transform.getTranslation(aux);
    }

    /**
     * Updates the local transform matrix.
     * @param time
     */
    @Override
    public void updateLocal(ITimeFrameProvider time, ICamera camera) {
        updateLocalValues(time, camera);

        this.transform.translate(pos);

        this.distToCamera = (float) transform.getTranslation(auxVector3d.get()).len();
        this.viewAngle = (float) Math.atan(size / distToCamera);
        this.viewAngleApparent = this.viewAngle;
        if (!copy) {
            addToRenderLists(camera);
        }
    }

    /**
     * Adds this entity to the necessary render lists after the
     * distance to the camera and the view angle have been determined.
     */
    protected abstract void addToRenderLists(ICamera camera);

    /**
     * This function updates all the local values before the localTransform is
     * updated. Position, rotations and scale must be updated in here.
     * @param dt
     * @param time
     */
    public abstract void updateLocalValues(ITimeFrameProvider time, ICamera camera);

    public float getRadius() {
        return size / 2;
    }

    /**
     * Sets the absolute size of this entity
     * @param size
     */
    public void setSize(Double size) {
        this.size = size.floatValue();
    }

    public void setSize(Long size) {
        this.size = (float) size;
    }

    public void setColor(double[] color) {
        this.cc = GlobalResources.toFloatArray(color);
    }

    public Vector3d computeFuturePosition() {
        return null;
    }

    /**
     * Gets a copy of this object but does not copy its parent or children.
     * @return
     */
    @Override
    public <T extends SceneGraphNode> T getSimpleCopy() {
        Class<? extends AbstractPositionEntity> clazz = this.getClass();
        Pool<? extends AbstractPositionEntity> pool = Pools.get(clazz);
        try {
            AbstractPositionEntity instance = pool.obtain();
            instance.copy = true;
            instance.name = this.name;
            instance.pos.set(this.pos);
            instance.size = this.size;
            instance.distToCamera = this.distToCamera;
            instance.viewAngle = this.viewAngle;
            instance.transform.set(this.transform);
            instance.ct = this.ct;
            instance.coordinates = this.coordinates;
            if (this.localTransform != null)
                instance.localTransform.set(this.localTransform);

            return (T) instance;
        } catch (Exception e) {
            Logger.error(e);
        }
        return null;
    }

    protected AbstractPositionEntity getComputedAncestor() {
        if (!this.computed) {
            return this.parent != null && this.parent instanceof AbstractPositionEntity ? ((AbstractPositionEntity) this.parent).getComputedAncestor() : null;
        } else {
            return this;
        }
    }

    public float getDistToCamera() {
        return distToCamera;
    }

    protected void renderLabel(SpriteBatch batch, ShaderProgram shader, BitmapFont font, ICamera camera, String label, Vector3d pos, float scale, float size, float[] colour) {
        // The smoothing scale must be set according to the distance
        shader.setUniformf("scale", scale / camera.getFovFactor());

        double len = pos.len();
        Vector3d p = pos.clamp(0, len - size);

        // Enable or disable blending
        ((ILabelRenderable) this).labelDepthBuffer();

        font.setColor(colour[0], colour[1], colour[2], colour[3]);
        DecalUtils.drawFont3D(font, batch, label, (float) p.x, (float) p.y, (float) p.z, size, camera.getCamera(), true);
    }

    public void setCoordinates(IBodyCoordinates coord) {
        coordinates = coord;
    }

    @Override
    public Vector3d getPosition() {
        return pos;
    }

}
