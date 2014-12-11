package gaia.cu9.ari.gaiaorbit.render.system;

import gaia.cu9.ari.gaiaorbit.render.IRenderable;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;

import java.util.Comparator;
import java.util.List;

import com.badlogic.gdx.graphics.glutils.FrameBuffer;

public abstract class AbstractRenderSystem implements IRenderSystem {

    private RenderGroup group;
    protected int priority;
    protected float[] alphas;
    /** Comparator of renderables, in case of need **/
    protected Comparator<IRenderable> comp;
    protected FrameBuffer fb;

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
    public void render(List<IRenderable> renderables, ICamera camera, FrameBuffer fb) {
	if (!renderables.isEmpty()) {
	    this.fb = fb;
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
