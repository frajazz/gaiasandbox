package gaia.cu9.ari.gaiaorbit.scenegraph;

import java.util.Random;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.render.ILineRenderable;
import gaia.cu9.ari.gaiaorbit.render.IPointRenderable;
import gaia.cu9.ari.gaiaorbit.render.IRenderable;
import gaia.cu9.ari.gaiaorbit.render.SceneGraphRenderer;
import gaia.cu9.ari.gaiaorbit.render.system.LineRenderSystem;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.color.ColourUtils;
import gaia.cu9.ari.gaiaorbit.util.concurrent.ThreadIndexer;
import gaia.cu9.ari.gaiaorbit.util.coord.AstroUtils;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

/**
 * A point particle which may represent a star, a galaxy, etc.
 * @author Toni Sagrista
 *
 */
public class Particle extends CelestialBody implements IPointRenderable, ILineRenderable {

    private static final float DISC_FACTOR = 1.5f;
    private static final float LABEL_FACTOR = Constants.webgl ? 3f : 1f;

    private static Random rnd = new Random();

    @Override
    public double THRESHOLD_NONE() {
        return (float) GlobalConf.scene.STAR_THRESHOLD_NONE;
    }

    @Override
    public double THRESHOLD_POINT() {
        return (float) GlobalConf.scene.STAR_THRESHOLD_POINT;
    }

    @Override
    public double THRESHOLD_QUAD() {
        return (float) GlobalConf.scene.STAR_THRESHOLD_QUAD;
    }

    /** Proper motion in cartesian coordinates [U/yr] **/
    public Vector3 pm;

    /**
     * Source of this star:
     * <ul><li>
     * -1: Unknown 
     * </li><li>
     * 1: Gaia
     * </li><li>
     * 2: Hipparcos (HYG)
     * </li><li>
     * 3: Tycho
     * </li></ul>
     */
    public byte catalogSource = -1;

    double computedSize;
    double radius;
    boolean randomName = false;
    boolean hasPm = false;
    boolean visiblect = false;

    /**
     * Object server properties
     */

    /** The id of the octant it belongs to, if any **/
    public long octantId;
    /** Its page **/
    public OctreeNode<? extends SceneGraphNode> octant;

    /** Particle type
     * 90 - real star
     * 92 - virtual particle
     */
    public int type = 90;
    public int nparticles = 1;

    public Particle() {
        this.parentName = ROOT_NAME;
    }

    /**
     * Creates a new star.
     * @param pos Cartesian position, in equatorial coordinates and in internal units.
     * @param appmag Apparent magnitude.
     * @param absmag Absolute magnitude.
     * @param colorbv The B-V color index.
     * @param name The label or name.
     * @param starid The star unique id.
     */
    public Particle(Vector3d pos, float appmag, float absmag, float colorbv, String name, long starid) {
        this();
        this.pos = pos;
        this.name = name;
        this.appmag = appmag;
        this.absmag = absmag;
        this.colorbv = colorbv;
        this.id = starid;

        if (this.name == null) {
            randomName = true;
            this.name = "star_" + rnd.nextInt(10000000);
        }
        this.pm = new Vector3();
    }

    public Particle(Vector3d pos, float appmag, float absmag, float colorbv, String name, float ra, float dec, long starid) {
        this(pos, appmag, absmag, colorbv, name, starid);
        this.posSph = new Vector2(ra, dec);

    }

    public Particle(Vector3d pos, Vector3 pm, float appmag, float absmag, float colorbv, String name, float ra, float dec, long starid) {
        this(pos, appmag, absmag, colorbv, name, starid);
        this.posSph = new Vector2(ra, dec);
        this.pm.set(pm);
        this.hasPm = this.pm.len2() != 0;

    }

    @Override
    public void initialize() {
        setDerivedAttributes();
        ct = ComponentType.Galaxies;
        // Relation between our star size and actual star size (normalized for the Sun, 1391600 Km of diameter
        radius = size * Constants.STAR_SIZE_FACTOR;
    }

    private void setDerivedAttributes() {
        this.flux = (float) Math.pow(10, -absmag / 2.5f);
        setRGB(colorbv);

        // Calculate size
        size = (float) Math.min((Math.pow(flux, 0.5f) * Constants.PC_TO_U * 0.16f), 1e8f) / DISC_FACTOR;
    }

    @Override
    public void update(ITimeFrameProvider time, final Transform parentTransform, ICamera camera) {
        update(time, parentTransform, camera, 1f);
    }

    /**
     * Re-implementation of update method of {@link CelestialBody} and {@link SceneGraphNode}.
     */
    @Override
    public void update(ITimeFrameProvider time, final Transform parentTransform, ICamera camera, float opacity) {
        if (appmag <= GlobalConf.runtime.LIMIT_MAG_RUNTIME) {
            this.opacity = opacity;
            transform.position.set(parentTransform.position).add(pos);
            if (hasPm) {
                Vector3 pmv = new Vector3(pm).scl((float) Constants.S_TO_Y).scl((float) AstroUtils.getMsSinceJ2015(time.getTime()) / 1000f);
                transform.position.add(pmv);
            }
            distToCamera = (float) transform.position.len();

            if (!copy) {
                visiblect = SceneGraphRenderer.visible[ct.ordinal()];
                addToRender(this, RenderGroup.POINT);

//                if (camera.isVisible(time, this, GlobalConf.scene.COMPUTE_GAIA_SCAN) || camera.isFocus(this)) {
                    viewAngle = ((float) radius / distToCamera) / camera.getFovFactor();
                    viewAngleApparent = viewAngle * GlobalConf.scene.STAR_BRIGHTNESS;

                    addToRenderLists(camera);
//                }
            }

            // Compute nested
            if (children != null) {
                int size = children.size();
                for (int i = 0; i < size; i++) {
                    SceneGraphNode child = children.get(i);
                    child.update(time, parentTransform, camera, opacity);
                }
            }
        }
    }

    @Override
    protected void addToRenderLists(ICamera camera) {
        if (camera.getCurrent() instanceof FovCamera) {
            // Only shader for FovCamera
            addToRender(this, RenderGroup.SHADER);
        } else {

            if (viewAngleApparent >= THRESHOLD_POINT() * camera.getFovFactor()) {
                addToRender(this, RenderGroup.SHADER);
                if (this.hasPm)
                    addToRender(this, RenderGroup.LINE);
            }
        }
        if (renderText()) {
            addToRender(this, RenderGroup.LABEL);
        }

    }

    protected boolean addToRender(IRenderable renderable, RenderGroup rg) {
        if (visiblect || SceneGraphRenderer.alphas[ct.ordinal()] > 0) {
            SceneGraphRenderer.render_lists.get(rg).add(renderable, ThreadIndexer.i());
            return true;
        }
        return false;
    }

    public void render(Object... params) {
        Object first = params[0];
        if (first instanceof ImmediateModeRenderer) {
            // POINT
            render((ImmediateModeRenderer) first, (Float) params[1], (Boolean) params[2]);
        } else if (first instanceof LineRenderSystem) {
            // LINE (proper motion)
            render((LineRenderSystem) first, (ICamera) params[1], (Float) params[2]);
        } else {
            super.render(params);
        }
    }

    @Override
    public void render(ModelBatch modelBatch, float alpha) {
        // Void
    }

    /**
     * Sets the color
     * @param bv B-V color index
     */
    private void setRGB(float bv) {
        cc = ColourUtils.BVtoRGB(bv);
        setColor2Data();
    }

    @Override
    public float getInnerRad() {
        return 0.02f * DISC_FACTOR;
    }

    @Override
    public float getRadius() {
        return (float) radius;
    }

    public boolean isStar() {
        return true;
    }

    @Override
    public float labelSizeConcrete() {
        return (float) computedSize * LABEL_FACTOR;
    }

    @Override
    protected float labelFactor() {
        return 2e-1f;
    }

    @Override
    protected float labelMax() {
        return 0.01f;
    }

    public float getFuzzyRenderSize(ICamera camera) {
        computedSize = this.size;
        if (viewAngle > Constants.THRESHOLD_DOWN / camera.getFovFactor()) {
            double dist = distToCamera;
            if (viewAngle > Constants.THRESHOLD_UP / camera.getFovFactor()) {
                dist = (float) radius / Constants.THRESHOLD_UP;
            }
            computedSize = this.size * (dist / this.radius) * Constants.THRESHOLD_DOWN;
        }
        computedSize *= GlobalConf.scene.STAR_BRIGHTNESS;

        return (float) computedSize;
    }

    @Override
    public void doneLoading(AssetManager manager) {
    }

    @Override
    public void updateLocalValues(ITimeFrameProvider time, ICamera camera) {
    }

    @Override
    public int getStarCount() {
        return 1;
    }

    @Override
    public Object getStars() {
        return this;
    }

    @Override
    public <T extends SceneGraphNode> T getSimpleCopy() {
        Particle copy = (Particle) super.getSimpleCopy();
        copy.pm = this.pm;
        copy.hasPm = this.hasPm;
        return (T) copy;
    }

    /**
     * Line renderer. Renders proper motion
     * @param renderer
     * @param camera
     * @param alpha
     */
    @Override
    public void render(LineRenderSystem renderer, ICamera camera, float alpha) {
        Vector3 campos = v3fpool.obtain();
        Vector3 p1 = transform.position.setVector3(v3fpool.obtain());
        Vector3 ppm = v3fpool.obtain().set(pm).scl(1e3f);
        Vector3 p2 = v3fpool.obtain().set(p1).add(ppm);
        camera.getPos().setVector3(campos);

        renderer.addLine(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z, 1.0f, 0.0f, 0.0f, alpha * 0.5f);

        v3fpool.free(campos);
        v3fpool.free(p1);
        v3fpool.free(p2);
        v3fpool.free(ppm);
    }

}
