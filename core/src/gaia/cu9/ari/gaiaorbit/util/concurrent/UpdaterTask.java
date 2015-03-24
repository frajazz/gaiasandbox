package gaia.cu9.ari.gaiaorbit.util.concurrent;

import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

import java.util.List;
import java.util.concurrent.Callable;

public class UpdaterTask<T extends SceneGraphNode> implements Callable<Void> {

    ICamera camera;
    List<T> nodes;
    ITimeFrameProvider time;
    int start, step;

    public UpdaterTask(List<T> nodes, int start, int step) {
	this.nodes = nodes;
	this.start = start;
	this.step = step;
    }

    public UpdaterTask(List<T> nodes) {
	this(nodes, 0, 1);
    }

    @Override
    public Void call() throws Exception {
	int size = nodes.size();
	for (int i = start; i < size; i += step) {
	    SceneGraphNode node = nodes.get(i);
	    node.update(time, node.parent.transform, camera);
	}
	return null;
    }

    public void setNodesToProcess(List<T> nodes) {
	this.nodes = nodes;
    }

    public void addAll(List<T> list) {
	this.nodes.addAll(list);
    }

    /**
     * This must be called to prepare the updater task for execution
     * @param camera
     * @param time
     */
    public void setParameters(ICamera camera, ITimeFrameProvider time) {
	this.camera = camera;
	this.time = time;
    }

}