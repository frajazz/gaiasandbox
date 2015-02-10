package gaia.cu9.ari.gaiaorbit.render;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.render.IPostProcessor.PostProcessBean;
import gaia.cu9.ari.gaiaorbit.render.system.AbstractRenderSystem;
import gaia.cu9.ari.gaiaorbit.render.system.IRenderSystem;
import gaia.cu9.ari.gaiaorbit.render.system.LineRenderSystem;
import gaia.cu9.ari.gaiaorbit.render.system.ModelBatchRenderSystem;
import gaia.cu9.ari.gaiaorbit.render.system.PixelBloomRenderSystem;
import gaia.cu9.ari.gaiaorbit.render.system.ShaderQuadRenderSystem;
import gaia.cu9.ari.gaiaorbit.render.system.SpriteBatchRenderSystem;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.NaturalCamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.override.AtmosphereGroundShaderProvider;
import gaia.cu9.ari.gaiaorbit.util.override.AtmosphereShaderProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.RenderableSorter;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Renders a scenegraph.
 * @author Toni Sagrista
 *
 */
public class SceneGraphRenderer extends AbstractRenderer implements IProcessRenderer, IObserver {

    public enum ComponentType {
	Stars("Stars"),
	Planets("Planets"),
	Moons("Moons"),
	Satellites("Satellites"),
	Asteroids("Asteroids"),
	Labels("Labels"),
	Equatorial("Equatorial grid", "grid-icon"),
	Ecliptic("Ecliptic grid", "grid-icon"),
	Galactic("Galactic grid", "grid-icon"),
	Orbits("Orbits"),
	Atmospheres("Atmospheres"),
	Constellations("Constellations"),
	Boundaries("Boundaries"),
	MilkyWay("Milky way"),
	Others("Others");

	private static Map<String, ComponentType> namesMap = new HashMap<String, ComponentType>();
	static {
	    for (ComponentType ct : ComponentType.values()) {
		namesMap.put(ct.id, ct);
	    }
	}

	public String id;
	private String name;
	public String style;

	private ComponentType(String id) {
	    this.id = id;
	}

	private ComponentType(String id, String icon) {
	    this(id);
	    this.style = icon;
	}

	public String getId() {
	    return id;
	}

	public String getName() {
	    if (name == null) {
		name = I18n.bundle.get("element." + name().toLowerCase());
		namesMap.put(name, this);
	    }
	    return name;
	}

	@Override
	public String toString() {
	    return getName();
	}

	public static ComponentType getFromName(String name) {
	    ComponentType ct = null;
	    try {
		ct = ComponentType.valueOf(name);
	    } catch (Exception e) {
		// Look for name
		ct = namesMap.get(name);
	    }
	    return ct;
	}
    }

    /** Contains the flags representing each type's visibility **/
    public static boolean[] visible;
    /** Contains the last update time of each of the flags **/
    public static long[] times;
    /** Alpha values for each type **/
    public static float[] alphas;

    private ShaderProgram starShader, fontShader;

    private RenderContext rc;

    /** Render lists for all render groups **/
    public static Map<RenderGroup, List<IRenderable>> render_lists;

    // Line renderer
    private ShapeRenderer shapeRenderer;

    // Two model batches, for front (models), back and atmospheres
    private SpriteBatch spriteBatch, fontBatch;

    private NavigableSet<IRenderSystem> renderProcesses;

    public SceneGraphRenderer() {
	super();
    }

    @Override
    public void initialize(AssetManager manager) {
	ShaderProgram.pedantic = false;
	starShader = new ShaderProgram(Gdx.files.internal("shader/star.vertex.glsl"), Gdx.files.internal("shader/star.rays.fragment.glsl"));
	if (!starShader.isCompiled()) {
	    Gdx.app.error(this.getClass().getName(), "Star shader compilation failed:\n" + starShader.getLog());
	}

	fontShader = new ShaderProgram(Gdx.files.internal("shader/simple.vertex.glsl"), Gdx.files.internal("shader/font.fragment.glsl"));
	if (!fontShader.isCompiled()) {
	    Gdx.app.error(this.getClass().getName(), "Font shader compilation failed:\n" + fontShader.getLog());
	}

	RenderGroup[] renderGroups = RenderGroup.values();
	render_lists = new HashMap<RenderGroup, List<IRenderable>>(renderGroups.length);
	for (RenderGroup rg : renderGroups) {
	    render_lists.put(rg, Collections.synchronizedList(new ArrayList<IRenderable>()));
	}

	// Shape renderers
	shapeRenderer = new ShapeRenderer();

	ShaderProvider spnormal = new AtmosphereGroundShaderProvider(Gdx.files.internal("shader/normal.vertex.glsl"), Gdx.files.internal("shader/normal.fragment.glsl"));
	ShaderProvider sp = new AtmosphereGroundShaderProvider(Gdx.files.internal("shader/default.vertex.glsl"), Gdx.files.internal("shader/default.fragment.glsl"));
	ShaderProvider spatm = new AtmosphereShaderProvider(Gdx.files.internal("shader/atm.vertex.glsl"), Gdx.files.internal("shader/atm.fragment.glsl"));
	ShaderProvider spsurface = new DefaultShaderProvider(Gdx.files.internal("shader/default.vertex.glsl"), Gdx.files.internal("shader/starsurface.fragment.glsl"));

	RenderableSorter noSorter = new RenderableSorter() {
	    @Override
	    public void sort(Camera camera, Array<Renderable> renderables) {
		// Does nothing
	    }
	};

	ModelBatch modelBatchB = new ModelBatch(sp, noSorter);
	ModelBatch modelBatchF = new ModelBatch(spnormal, noSorter);
	ModelBatch modelBatchAtm = new ModelBatch(spatm, noSorter);
	ModelBatch modelBatchS = new ModelBatch(spsurface, noSorter);

	// Sprites
	spriteBatch = GlobalResources.spriteBatch;
	spriteBatch.enableBlending();

	// Font batch
	fontBatch = new SpriteBatch(1000, fontShader);
	fontBatch.enableBlending();

	// Render context
	rc = new RenderContext();

	ComponentType[] comps = ComponentType.values();

	// Set reference
	visible = GlobalConf.instance.VISIBILITY;

	times = new long[comps.length];
	alphas = new float[comps.length];
	for (int i = 0; i < comps.length; i++) {
	    alphas[i] = 1f;
	}

	/** 
	 * 
	 * =======  INITIALIZE RENDER COMPONENTS  =======
	 * 
	 **/

	renderProcesses = new TreeSet<IRenderSystem>();

	Runnable blendNoDepthRunnable = new Runnable() {
	    @Override
	    public void run() {
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDepthMask(false);
	    }
	};
	Runnable blendDepthRunnable = new Runnable() {
	    @Override
	    public void run() {
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDepthMask(true);
	    }
	};

	int priority = 1;

	// POINTS
	AbstractRenderSystem pixelProc = new PixelBloomRenderSystem(RenderGroup.POINT, priority++, alphas);
	pixelProc.setPreRunnable(blendNoDepthRunnable);

	// MODEL BACK
	AbstractRenderSystem modelBackProc = new ModelBatchRenderSystem(RenderGroup.MODEL_B, priority++, alphas, modelBatchB, false);
	modelBackProc.setPreRunnable(blendNoDepthRunnable);
	modelBackProc.setPostRunnable(new Runnable() {
	    @Override
	    public void run() {
		// This always goes at the back, clear depth buffer
		Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
	    }
	});

	// ANNOTATIONS
	AbstractRenderSystem annotationsProc = new SpriteBatchRenderSystem(RenderGroup.MODEL_B_ANNOT, priority++, alphas, spriteBatch);
	annotationsProc.setPreRunnable(blendNoDepthRunnable);
	annotationsProc.setPostRunnable(new Runnable() {
	    @Override
	    public void run() {
		// This always goes at the back, clear depth buffer
		Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
	    }
	});

	// SHADER STARS
	AbstractRenderSystem shaderBackProc = new ShaderQuadRenderSystem(RenderGroup.SHADER, priority++, alphas, starShader, true);
	shaderBackProc.setPreRunnable(blendNoDepthRunnable);

	// LINES
	AbstractRenderSystem lineProc = new LineRenderSystem(RenderGroup.LINE, priority++, alphas, shapeRenderer);
	lineProc.setPreRunnable(blendDepthRunnable);

	// MODEL FRONT
	AbstractRenderSystem modelFrontProc = new ModelBatchRenderSystem(RenderGroup.MODEL_F, priority++, alphas, modelBatchF, false);
	modelFrontProc.setPreRunnable(blendDepthRunnable);

	// MODEL STARS
	AbstractRenderSystem modelStarsProc = new ModelBatchRenderSystem(RenderGroup.MODEL_S, priority++, alphas, modelBatchS, false);
	modelStarsProc.setPreRunnable(blendDepthRunnable);

	// LABELS
	AbstractRenderSystem labelsProc = new SpriteBatchRenderSystem(RenderGroup.LABEL, priority++, alphas, fontBatch, fontShader);
	labelsProc.setPreRunnable(blendDepthRunnable);

	// MODEL ATMOSPHERE
	AbstractRenderSystem modelAtmProc = new ModelBatchRenderSystem(RenderGroup.MODEL_F_ATM, priority++, alphas, modelBatchAtm, true) {
	    @Override
	    protected float getAlpha(IRenderable s) {
		return alphas[ComponentType.Atmospheres.ordinal()] * (float) Math.pow(alphas[s.getComponentType().ordinal()], 2);
	    }

	    @Override
	    protected boolean mustRender() {
		return alphas[ComponentType.Atmospheres.ordinal()] * alphas[ComponentType.Planets.ordinal()] > 0;
	    }
	};
	modelAtmProc.setPreRunnable(blendDepthRunnable);

	// SHADER SSO
	AbstractRenderSystem shaderFrontProc = new ShaderQuadRenderSystem(RenderGroup.SHADER_F, priority++, alphas, starShader, false);
	shaderFrontProc.setPreRunnable(blendDepthRunnable);

	// Add components to set
	renderProcesses.add(modelBackProc);
	renderProcesses.add(annotationsProc);
	renderProcesses.add(pixelProc);
	//renderProcesses.add(shaderBackProc);
	renderProcesses.add(modelAtmProc);
	renderProcesses.add(modelFrontProc);
	renderProcesses.add(modelStarsProc);
	renderProcesses.add(lineProc);
	renderProcesses.add(labelsProc);
	renderProcesses.add(shaderFrontProc);

	EventManager.getInstance().subscribe(this, Events.TOGGLE_VISIBILITY_CMD);

    }

    @Override
    public void render(ICamera camera, FrameBuffer fb, PostProcessBean ppb, float alpha) {
	render(camera, fb, ppb);
    }

    public void render(ICamera camera, FrameBuffer fb, PostProcessBean ppb) {

	boolean postproc = ppb.capture();
	if (postproc) {
	    rc.ppb = ppb;
	} else {
	    rc.ppb = null;
	}
	rc.fb = fb;

	if (camera.getNCameras() > 1) {

	    /** FIELD OF VIEW CAMERA **/

	    CameraMode aux = camera.getMode();

	    camera.updateMode(CameraMode.Gaia_FOV2, false);

	    renderScene(camera, rc);

	    camera.updateMode(CameraMode.Gaia_FOV1, false);

	    renderScene(camera, rc);

	    camera.updateMode(aux, false);

	} else {
	    /** NORMAL MODE **/

	    if (GlobalConf.instance.STEREOSCOPIC_MODE) {
		boolean movecam = camera.getMode() == CameraMode.Free_Camera || camera.getMode() == CameraMode.Focus;
		// Side by side rendering
		Viewport vp = camera.getViewport();
		int w = fb != null ? fb.getWidth() : vp.getScreenWidth();
		int h = fb != null ? fb.getHeight() : vp.getScreenHeight();

		PerspectiveCamera cam = camera.getCamera();
		Pool<Vector3> vectorPool = Pools.get(Vector3.class);
		// Vector of 1 meter length pointing to the side of the camera
		Vector3 side = vectorPool.obtain().set(cam.direction);
		float separation = (float) Constants.M_TO_U * GlobalConf.instance.STEREOSCOPIC_EYE_SEPARATION_M;
		if (camera.getMode() == CameraMode.Focus) {
		    // In focus mode we keep the separation dependant on the distance with a fixed angle
		    float distToFocus = ((NaturalCamera) camera.getCurrent()).focus.distToCamera - ((NaturalCamera) camera.getCurrent()).focus.getRadius();
		    separation = (float) Math.min((Math.tan(Math.toRadians(1.5)) * distToFocus), 1e16 * Constants.M_TO_U);
		}

		side.crs(cam.up).nor().scl(separation);
		Vector3 backup = vectorPool.obtain().set(cam.position);

		/** LEFT EYE **/
		if (Constants.mobile) {
		    // Mobile, left eye goes to left image
		    vp.setScreenBounds(0, 0, w / 2, h);
		} else {
		    // Desktop, left eye goes to right image
		    vp.setScreenBounds(w / 2, 0, w / 2, h);
		}

		vp.setWorldSize(w / 2, h);
		vp.apply(false);
		// Camera to left
		if (movecam) {
		    cam.position.sub(side);
		    cam.update();
		}
		renderScene(camera, rc);

		/** RIGHT EYE **/
		if (Constants.mobile) {
		    // Mobile, right eye goes to right image
		    vp.setScreenBounds(w / 2, 0, w / 2, h);
		} else {
		    // Desktop, right eye goes to left image
		    vp.setScreenBounds(0, 0, w / 2, h);
		}

		vp.setWorldSize(w / 2, h);
		vp.apply(false);
		// Camera to right
		if (movecam) {
		    cam.position.set(backup).add(side);
		    cam.update();
		}
		renderScene(camera, rc);

		// Restore cam.position and viewport size
		cam.position.set(backup);
		vp.setScreenBounds(0, 0, w, h);

		vectorPool.free(side);
		vectorPool.free(backup);

	    } else {
		renderScene(camera, rc);
	    }
	}
	ppb.render(fb);

	// Render camera
	if (fb != null) {
	    fb.begin();
	}
	camera.render();
	if (fb != null) {
	    fb.end();
	}
    }

    public void renderScene(ICamera camera, RenderContext rc) {
	// Update time difference since last update
	long now = new Date().getTime();
	for (ComponentType ct : ComponentType.values()) {
	    alphas[ct.ordinal()] = calculateAlpha(ct, now);
	}

	EventManager.getInstance().post(Events.DEBUG1, "quad: " + (render_lists.get(RenderGroup.SHADER).size() + render_lists.get(RenderGroup.SHADER_F).size()) + ", point: " + render_lists.get(RenderGroup.POINT).size());

	Iterator<IRenderSystem> it = renderProcesses.iterator();
	while (it.hasNext()) {
	    IRenderSystem process = it.next();

	    process.render(render_lists.get(process.getRenderGroup()), camera, rc);
	}

    }

    /**
     * This must be called when all the rendering for the current frame has finished.
     */
    public void clearLists() {
	for (RenderGroup rg : RenderGroup.values()) {
	    render_lists.get(rg).clear();
	}
    }

    public String[] getRenderComponents() {
	ComponentType[] comps = ComponentType.values();
	String[] res = new String[comps.length];
	int i = 0;
	for (ComponentType comp : comps) {
	    res[i++] = comp.getName();
	}
	return res;
    }

    public boolean isOn(ComponentType comp) {
	return visible[comp.ordinal()];
    }

    @Override
    public void notify(Events event, final Object... data) {
	switch (event) {
	case TOGGLE_VISIBILITY_CMD:
	    int idx = ComponentType.getFromName((String) data[0]).ordinal();
	    if (data.length == 3) {
		// We have the boolean
		visible[idx] = (boolean) data[2];
		times[idx] = new Date().getTime();
	    } else {
		// Only toggle
		visible[idx] = !visible[idx];
		times[idx] = new Date().getTime();
	    }
	    break;

	}
    }

    private float calculateAlpha(ComponentType type, long now) {
	long diff = now - times[type.ordinal()];
	if (diff > GlobalConf.instance.OBJECT_FADE_MS) {
	    if (visible[type.ordinal()]) {
		alphas[type.ordinal()] = 1;
	    } else {
		alphas[type.ordinal()] = 0;
	    }
	    return alphas[type.ordinal()];
	} else {
	    return visible[type.ordinal()] ? MathUtilsd.lint(diff, 0, GlobalConf.instance.OBJECT_FADE_MS, 0, 1) : MathUtilsd.lint(diff, 0, GlobalConf.instance.OBJECT_FADE_MS, 1, 0);
	}
    }

}
