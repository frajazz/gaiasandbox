package gaia.cu9.ari.gaiaorbit.render.system;

import gaia.cu9.ari.gaiaorbit.render.IRenderable;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;
import gaia.cu9.ari.gaiaorbit.util.comp.ModelComparator;

import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.graphics.g3d.ModelBatch;

/**
 * Renders with a given model batch.
 * @author Toni Sagrista
 *
 */
public class ModelBatchRenderSystem extends AbstractRenderSystem {
    private ModelBatch batch;
    private boolean addByte;

    /**
     * Creates a new model batch render component.
     * @param rg The render group.
     * @param priority The priority.
     * @param alphas The alphas list.
     * @param batch The model batch.
     * @param addByte Should we add a byte to the call? (atmosphere rendering).
     */
    public ModelBatchRenderSystem(RenderGroup rg, int priority, float[] alphas, ModelBatch batch, boolean addByte) {
	super(rg, priority, alphas);
	this.batch = batch;
	this.addByte = addByte;
	comp = new ModelComparator<IRenderable>();
    }

    @Override
    public void renderStud(List<IRenderable> renderables, ICamera camera) {
	Collections.sort(renderables, comp);
	if (mustRender()) {
	    batch.begin(camera.getCamera());
	    int size = renderables.size();
	    for (int i = 0; i < size; i++) {
		IRenderable s = renderables.get(i);
		if (!addByte) {
		    s.render(batch, getAlpha(s));
		} else {
		    s.render(batch, getAlpha(s), (byte) 1);
		}
	    }
	    batch.end();
	}
    }

    protected boolean mustRender() {
	return true;
    }

    protected float getAlpha(IRenderable s) {
	return alphas[s.getComponentType().ordinal()];
    }

}
