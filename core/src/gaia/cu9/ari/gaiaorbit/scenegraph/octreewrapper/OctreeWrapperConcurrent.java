package gaia.cu9.ari.gaiaorbit.scenegraph.octreewrapper;

import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphConcurrent;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.scenegraph.Transform;
import gaia.cu9.ari.gaiaorbit.util.concurrent.GaiaSandboxThreadFactory;
import gaia.cu9.ari.gaiaorbit.util.concurrent.UpdaterTask;
import gaia.cu9.ari.gaiaorbit.util.ds.Multilist;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.badlogic.gdx.Gdx;

/**
 * Static octree wrapper that can be inserted into the scene graph. 
 * This implementation splits the update process in a number of concurrent threads.
 * The octree processing still happens in the main thread.
 * @author Toni Sagrista
 *
 */
public class OctreeWrapperConcurrent extends AbstractOctreeWrapper {

    /** The executor service containing the pool **/
    ThreadPoolExecutor pool;
    private List<UpdaterTask<SceneGraphNode>> tasks;
    private int numThreads;

    public OctreeWrapperConcurrent(String parentName, OctreeNode<SceneGraphNode> root, int numThreads) {
	super(parentName, root);
	this.numThreads = numThreads;
	roulette = new Multilist<SceneGraphNode>(numThreads, root.nObjects / numThreads);
    }

    @Override
    public void initialize() {
	super.initialize();

	pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads, new GaiaSandboxThreadFactory("octree-updater-"));
	tasks = new ArrayList<UpdaterTask<SceneGraphNode>>(pool.getCorePoolSize());

	// Create the tasks with the roulette collections references
	for (int i = 0; i < numThreads; i++) {
	    tasks.add(new UpdaterTask<SceneGraphNode>(roulette, i, numThreads));
	}
    }

    @Override
    protected void updateOctreeObjects(ITimeFrameProvider time, Transform parentTransform, ICamera camera) {
	// Update task parameters
	for (UpdaterTask<SceneGraphNode> task : tasks)
	    task.setParameters(camera, time);

	try {
	    pool.invokeAll(tasks);
	} catch (InterruptedException e) {
	    Gdx.app.error(SceneGraphConcurrent.class.getName(), e.getLocalizedMessage());
	}
    }

    @Override
    protected String getRouletteDebug() {
	{
	    String s = "[";
	    for (int i = 0; i < numThreads; i++) {
		s += (roulette.size() / numThreads);
		if (i < numThreads - 1)
		    s += ", ";
	    }
	    s += "]";
	    return s;
	}
    }

}
