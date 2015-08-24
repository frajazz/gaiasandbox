package gaia.cu9.ari.gaiaorbit.scenegraph;

import gaia.cu9.ari.gaiaorbit.render.I3DTextRenderable;
import gaia.cu9.ari.gaiaorbit.util.DecalUtils;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.MyPools;
import gaia.cu9.ari.gaiaorbit.util.coord.IBodyCoordinates;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;

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
    public Vector2 posSph;

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
        posSph = new Vector2();
    }

    public AbstractPositionEntity(SceneGraphNode parent) {
        super(parent);
        // Positions
        pos = new Vector3d();
        posSph = new Vector2();
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

    /**
     * Returns the position of this entity in the camera reference system (i.e. to get the total position
     * you need to add the camera position) into the aux vector.
     * @param aux
     * @return
     */
    public Vector3d getPosition(Vector3d aux) {
        return transform.getTranslation(aux);
    }

    /**
     * Gets the predicted position of this entity in the next time step
     * in the camera reference system (i.e. to get the total position
     * you need to add the camera position) using the given time provider and the given camera.
     * @param aux The out vector where the result will be stored.
     * @param time The time frame provider.
     * @param camera The camera.
     * @return The aux vector for chaining.
     */
    public Vector3d getPredictedPosition(Vector3d aux, ITimeFrameProvider time, ICamera camera, boolean force) {
        if (time.getDt() == 0 && !force) {
            return getPosition(aux);
        } else {
            // Get copy of focus and update it to know where it will be in the next step
            AbstractPositionEntity fc = (AbstractPositionEntity) this;
            AbstractPositionEntity fccopy = fc.getLineCopy();
            fccopy.getRoot().transform.position.set(camera.getInversePos());
            fccopy.getRoot().update(time, null, camera);

            aux.set(fccopy.transform.getTranslation());

            // Return to poolvec
            SceneGraphNode ape = fccopy;
            do {
                ape.returnToPool();
                ape = ape.parent;
            } while (ape != null);

            return aux;
        }
    }

    /**
     * Returns the absolute position of this entity in the sandbox native coordinates 
     * (equatorial system).
     * @param aux
     * @return
     */
    public Vector3d getAbsolutePosition(Vector3d aux) {
        aux.set(pos);
        AbstractPositionEntity entity = this;
        while (entity.parent != null && entity.parent instanceof AbstractPositionEntity) {
            entity = (AbstractPositionEntity) entity.parent;
            aux.add(entity.pos);
        }
        return aux;
    }

    /**
     * Updates the local transform matrix.
     * @param time
     */
    @Override
    public void updateLocal(ITimeFrameProvider time, ICamera camera) {
        updateLocalValues(time, camera);

        this.transform.translate(pos);

        this.distToCamera = (float) transform.getTranslation(auxVector3d).len();
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
     * @param time
     * @param camera
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
        Pool<? extends AbstractPositionEntity> pool = MyPools.get(clazz);
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

    protected void render2DLabel(SpriteBatch batch, ShaderProgram shader, BitmapFont font, ICamera camera, String label, Vector3d pos) {
        Vector3 p = pos.setVector3(auxVector3f);

        camera.getCamera().project(p);
        p.x += 5;
        p.y -= 5;

        shader.setUniformf("scale", .5f);
        DecalUtils.drawFont2D(font, batch, label, p);
    }

    protected void render3DLabel(SpriteBatch batch, ShaderProgram shader, BitmapFont font, ICamera camera, String label, Vector3d pos, float scale, float size, float[] colour) {
        // The smoothing scale must be set according to the distance
        shader.setUniformf("scale", scale / camera.getFovFactor());

        double len = pos.len();
        Vector3d p = pos.clamp(0, len - size);

        // Enable or disable blending
        ((I3DTextRenderable) this).textDepthBuffer();

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
