package gaia.cu9.ari.gaiaorbit.scenegraph;

import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

import java.util.List;

import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;

public class ConstellationBoundaries extends LineObject {
    float alpha = .8f;
    List<List<Vector3d>> boundaries;

    public ConstellationBoundaries() {
	super();
	cc = new float[] { .8f, .8f, 1f, alpha };
	this.name = "Constellation boundaries";
	this.parentName = SceneGraphNode.ROOT_NAME;
    }

    @Override
    public void render(ImmediateModeRenderer20 renderer, float alpha) {
	alpha *= this.alpha;
	// This is so that the shape renderer does not mess up the z-buffer
	for (List<Vector3d> points : boundaries) {

	    Vector3d previous = null;
	    for (Vector3d point : points) {
		if (previous != null) {
		    renderer.color(cc[0], cc[1], cc[2], alpha);
		    renderer.vertex((float) previous.x, (float) previous.y, (float) previous.z);
		    renderer.color(cc[0], cc[1], cc[2], alpha);
		    renderer.vertex((float) point.x, (float) point.y, (float) point.z);

		}
		previous = point;
	    }

	}
    }

    public void setBoundaries(List<List<Vector3d>> boundaries) {
	this.boundaries = boundaries;
    }

    @Override
    protected void addToRenderLists(ICamera camera) {
	// Add to toRender list
	addToRender(this, RenderGroup.LINE);
    }

    @Override
    public void updateLocalValues(ITimeFrameProvider time, ICamera camera) {
    }

}
