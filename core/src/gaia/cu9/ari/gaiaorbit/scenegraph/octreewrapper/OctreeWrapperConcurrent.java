package gaia.cu9.ari.gaiaorbit.scenegraph.octreewrapper;

import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphConcurrent;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.scenegraph.Transform;
import gaia.cu9.ari.gaiaorbit.util.concurrent.ThreadPoolManager;
import gaia.cu9.ari.gaiaorbit.util.concurrent.UpdaterTask;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import com.badlogic.gdx.Gdx;

/**
 * Static Octree wrapper that can be inserted into the scene graph. 
 * This implementation splits the update process in a number of concurrent threads.
 * The Octree processing still happens in the single main thread.
 * @author Toni Sagrista
 *
 */
public class OctreeWrapperConcurrent extends AbstractOctreeWrapper {

    private ThreadPoolExecutor pool;
    private List<UpdaterTask<SceneGraphNode>> tasks;
    private int numThreads;

    public OctreeWrapperConcurrent(String parentName, OctreeNode<SceneGraphNode> root, int numThreads) {
	super(parentName, root);
	this.numThreads = numThreads;
	roulette = new ArrayList<SceneGraphNode>(root.nObjects);
    }

    @Override
    public void initialize() {
	super.initialize();

	this.pool = ThreadPoolManager.pool;
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
	    int size = roulette.size() / numThreads;
	    String s = "[";
	    for (int i = 0; i < numThreads; i++) {
		s += (size);
		if (i < numThreads - 1)
		    s += ", ";
	    }
	    s += "]";
	    return s;
	}
    }

}
