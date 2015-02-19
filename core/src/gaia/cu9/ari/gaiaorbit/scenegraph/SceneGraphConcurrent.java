package gaia.cu9.ari.gaiaorbit.scenegraph;

import gaia.cu9.ari.gaiaorbit.concurrent.UpdaterTask;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.util.I18n;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.badlogic.gdx.Gdx;

/**
 * Implementation of a 3D scene graph where the node updates takes place
 * concurrently in threads (as many as processors).
 * @author Toni Sagrista
 *
 */
public class SceneGraphConcurrent extends AbstractSceneGraph {

    /** The executor service containing the pool **/
    ThreadPoolExecutor pool;
    private List<UpdaterTask> tasks;
    int maxThreads;

    public SceneGraphConcurrent(int maxThreads) {
	super();
	this.maxThreads = maxThreads;

    }

    /** 
     * Builds the scene graph using the given nodes.
     * @param nodes
     */
    public void initialize(List<SceneGraphNode> nodes, ITimeFrameProvider time) {
	super.initialize(nodes, time);

	int threads = maxThreads <= 0 ? Runtime.getRuntime().availableProcessors() : maxThreads;

	pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(threads, new SceneGraphThreadFactory());
	objectsPerThread = new int[threads];
	tasks = new ArrayList<UpdaterTask>(pool.getCorePoolSize());

	// First naive implementation, we only separate the first-level stars.
	Iterator<SceneGraphNode> toUpdate = root.children.iterator();
	int nodesPerThread = root.numChildren / pool.getCorePoolSize();
	for (int i = 0; i < pool.getCorePoolSize(); i++) {
	    List<SceneGraphNode> partialList = new ArrayList<SceneGraphNode>(nodesPerThread);
	    int currentNumber = 0;
	    while (toUpdate.hasNext() && currentNumber <= nodesPerThread) {
		SceneGraphNode node = toUpdate.next();
		currentNumber += (node.getAggregatedChildren());
		partialList.add(node);
	    }

	    tasks.add(new UpdaterTask(partialList, time));
	    objectsPerThread[i] = currentNumber;
	}

	EventManager.getInstance().post(Events.POST_NOTIFICATION, this.getClass().getSimpleName(), I18n.bundle.format("notif.threadpool.init", threads));
    }

    public void update(ITimeFrameProvider time, ICamera camera) {
	super.update(time, camera);
	root.transform.position.set(camera.getInversePos());
	UpdaterTask.setCamera(camera);
	try {
	    pool.invokeAll(tasks);
	} catch (InterruptedException e) {
	    Gdx.app.error(SceneGraphConcurrent.class.getName(), e.getLocalizedMessage());
	}
    }

    public void dispose() {
	super.dispose();
	pool.shutdown(); // Disable new tasks from being submitted
	try {
	    // Wait a while for existing tasks to terminate
	    if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
		pool.shutdownNow(); // Cancel currently executing tasks
		// Wait a while for tasks to respond to being cancelled
		if (!pool.awaitTermination(60, TimeUnit.SECONDS))
		    System.err.println("Pool did not terminate");
	    }
	} catch (InterruptedException ie) {
	    // (Re-)Cancel if current thread also interrupted
	    pool.shutdownNow();
	    // Preserve interrupt status
	    Thread.currentThread().interrupt();
	}
    }

    /**
     * The default thread factory
     */
    static class SceneGraphThreadFactory implements ThreadFactory {
	private static final AtomicInteger poolNumber = new AtomicInteger(1);
	private final ThreadGroup group;
	private final AtomicInteger threadNumber = new AtomicInteger(1);
	private final String namePrefix;

	SceneGraphThreadFactory() {
	    SecurityManager s = System.getSecurityManager();
	    group = (s != null) ? s.getThreadGroup() :
		    Thread.currentThread().getThreadGroup();
	    namePrefix = "sg-updater-" +
		    poolNumber.getAndIncrement();
	}

	public Thread newThread(Runnable r) {
	    Thread t = new Thread(group, r,
		    namePrefix + threadNumber.getAndIncrement(),
		    0);
	    if (t.isDaemon())
		t.setDaemon(false);
	    if (t.getPriority() != Thread.NORM_PRIORITY)
		t.setPriority(Thread.NORM_PRIORITY);
	    return t;
	}
    }

    @Override
    public int getNThreads() {
	return pool.getCorePoolSize();
    }

}
