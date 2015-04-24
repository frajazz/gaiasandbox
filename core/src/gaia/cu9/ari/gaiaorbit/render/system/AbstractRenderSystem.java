package gaia.cu9.ari.gaiaorbit.render.system;

import gaia.cu9.ari.gaiaorbit.render.IRenderable;
import gaia.cu9.ari.gaiaorbit.render.RenderContext;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;

import java.util.Comparator;
import java.util.List;

public abstract class AbstractRenderSystem implements IRenderSystem {
    /** When this is true, new point information is available, so new data is streamed to the GPU **/
    public static boolean POINT_UPDATE_FLAG = true;

    private RenderGroup group;
    protected int priority;
    protected float[] alphas;
    /** Comparator of renderables, in case of need **/
    protected Comparator<IRenderable> comp;
    protected RenderContext rc;

    private Runnable preRunnables, postRunnables;

    protected AbstractRenderSystem(RenderGroup rg, int priority, float[] alphas) {
	super();
	this.group = rg;
	this.priority = priority;
	this.alphas = alphas;
    }

    @Override
    public RenderGroup getRenderGroup() {
	return group;
    }

    @Override
    public int getPriority() {
	return priority;
    }

    @Override
    public void render(List<IRenderable> renderables, ICamera camera, RenderContext rc) {
	if (!renderables.isEmpty()) {
	    this.rc = rc;
	    run(preRunnables);
	    renderStud(renderables, camera);
	    run(postRunnables);
	}
    }

    public abstract void renderStud(List<IRenderable> renderables, ICamera camera);

    public void setPreRunnable(Runnable r) {
	preRunnables = r;
    }

    public void setPostRunnable(Runnable r) {
	postRunnables = r;
    }

    private void run(Runnable runnable) {
	if (runnable != null) {
	    runnable.run();
	}
    }

    @Override
    public int compareTo(IRenderSystem o) {
	return Integer.compare(priority, o.getPriority());
    }

}
