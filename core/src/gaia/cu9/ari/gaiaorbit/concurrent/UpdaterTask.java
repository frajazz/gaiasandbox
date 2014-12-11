package gaia.cu9.ari.gaiaorbit.concurrent;

import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.ITimeFrameProvider;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;

import java.util.List;
import java.util.concurrent.Callable;

public class UpdaterTask implements Callable<Void> {

    static ICamera camera;

    List<SceneGraphNode> nodes;
    ITimeFrameProvider time;

    public UpdaterTask(List<SceneGraphNode> nodes, ITimeFrameProvider time) {
	this.nodes = nodes;
	this.time = time;
    }

    @Override
    public Void call() {
	for (SceneGraphNode node : nodes) {
	    node.update(time, node.parent.transform, camera);
	}
	return null;
    }

    public static void setCamera(ICamera camera) {
	UpdaterTask.camera = camera;
    }

}
