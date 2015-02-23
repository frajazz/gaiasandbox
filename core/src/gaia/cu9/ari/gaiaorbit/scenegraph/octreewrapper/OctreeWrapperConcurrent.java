package gaia.cu9.ari.gaiaorbit.scenegraph.octreewrapper;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.scenegraph.AbstractPositionEntity;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphConcurrent;
import gaia.cu9.ari.gaiaorbit.scenegraph.Transform;
import gaia.cu9.ari.gaiaorbit.util.concurrent.GaiaSandboxThreadFactory;
import gaia.cu9.ari.gaiaorbit.util.concurrent.UpdaterTask;
import gaia.cu9.ari.gaiaorbit.util.ds.RouletteList;
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
    private List<UpdaterTask<AbstractPositionEntity>> tasks;
    private RouletteList<AbstractPositionEntity> roulette;
    private int numThreads;

    public OctreeWrapperConcurrent(String parentName, OctreeNode<AbstractPositionEntity> root, int numThreads) {
	super(parentName, root);
	this.numThreads = numThreads;
    }

    @Override
    public void initialize() {
	super.initialize();

	roulette = new RouletteList<AbstractPositionEntity>(numThreads, root.nObjects / numThreads);

	pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads, new GaiaSandboxThreadFactory("octree-updater-"));
	tasks = new ArrayList<UpdaterTask<AbstractPositionEntity>>(pool.getCorePoolSize());

	// Create the tasks with the roulette collections references
	for (int i = 0; i < numThreads; i++) {
	    tasks.add(new UpdaterTask<AbstractPositionEntity>(roulette.getCollection(i)));
	}
    }

    @Override
    protected void processOctree(ITimeFrameProvider time, Transform parentTransform, ICamera camera) {
	if (children != null) {
	    // Clear roulette
	    roulette.clear();

	    // Add nodes to process to roulette
	    for (int i = 0; i < children.size(); i++) {
		roulette.add((AbstractPositionEntity) children.get(i));
	    }

	    EventManager.getInstance().post(Events.DEBUG3, "Octree threads: " + getRouletteDebug());

	    // Update task parameters
	    for (UpdaterTask<AbstractPositionEntity> task : tasks)
		task.setParameters(camera, time);

	    try {
		pool.invokeAll(tasks);
	    } catch (InterruptedException e) {
		Gdx.app.error(SceneGraphConcurrent.class.getName(), e.getLocalizedMessage());
	    }
	}
    }

    private String getRouletteDebug() {
	String s = "[";
	for (int i = 0; i < roulette.getNumCollections(); i++) {
	    s += roulette.getCollection(i).size();
	    if (i < roulette.getNumCollections() - 1)
		s += ", ";
	}
	s += "]";
	return s;
    }
}
