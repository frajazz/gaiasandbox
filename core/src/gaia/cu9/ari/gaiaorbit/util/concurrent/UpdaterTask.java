package gaia.cu9.ari.gaiaorbit.util.concurrent;

import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Callable;

public class UpdaterTask<T extends SceneGraphNode> implements Callable<Void> {

    ICamera camera;
    Collection<T> nodes;
    ITimeFrameProvider time;

    public UpdaterTask(Collection<T> nodes) {
	this.nodes = nodes;
    }

    @Override
    public Void call() {
	Iterator<T> it = nodes.iterator();
	while (it.hasNext()) {
	    SceneGraphNode node = it.next();
	    node.update(time, node.parent.transform, camera);
	}
	return null;
    }

    public void setNodesToProcess(Collection<T> nodes) {
	this.nodes = nodes;
    }

    public void addAll(Collection<T> list) {
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
