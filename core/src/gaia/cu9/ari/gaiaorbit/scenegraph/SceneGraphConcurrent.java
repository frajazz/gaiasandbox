package gaia.cu9.ari.gaiaorbit.scenegraph;

import com.badlogic.gdx.Gdx;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.concurrent.ThreadPoolManager;
import gaia.cu9.ari.gaiaorbit.util.concurrent.UpdaterTask;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of a 3D scene graph where the node updates takes place
 * concurrently in threads (as many as processors).
 * @author Toni Sagrista
 *
 */
public class SceneGraphConcurrent extends AbstractSceneGraph {

    private ThreadPoolExecutor pool;
    private List<UpdaterTask<SceneGraphNode>> tasks;
    int numThreads;

    public SceneGraphConcurrent(int numThreads) {
        super();
        this.numThreads = numThreads;

    }

    /** 
     * Builds the scene graph using the given nodes.
     * @param nodes
     */
    public void initialize(List<SceneGraphNode> nodes, ITimeFrameProvider time) {
        super.initialize(nodes, time);

        pool = ThreadPoolManager.pool;
        objectsPerThread = new int[numThreads];
        tasks = new ArrayList<UpdaterTask<SceneGraphNode>>(pool.getCorePoolSize());

        // First naive implementation, we only separate the first-level stars.
        Iterator<SceneGraphNode> toUpdate = root.children.iterator();
        int nodesPerThread = root.numChildren / numThreads;

        for (int i = 0; i < numThreads; i++) {
            List<SceneGraphNode> partialList = new ArrayList<SceneGraphNode>(nodesPerThread);
            int currentNumber = 0;
            while (toUpdate.hasNext() && currentNumber <= nodesPerThread) {
                SceneGraphNode node = toUpdate.next();
                currentNumber += (node.getAggregatedChildren());
                partialList.add(node);
            }

            tasks.add(new UpdaterTask<SceneGraphNode>(partialList));
            objectsPerThread[i] = currentNumber;
        }

        Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.threadpool.init", numThreads));
    }

    public void update(ITimeFrameProvider time, ICamera camera) {
        super.update(time, camera);
        root.transform.position.set(camera.getInversePos());

        // Update params
        int size = tasks.size();
        for (int i = 0; i < size; i++) {
            UpdaterTask<SceneGraphNode> task = tasks.get(i);
            task.setParameters(camera, time);
        }

        try {
            pool.invokeAll(tasks);
        } catch (InterruptedException e) {
            Gdx.app.error(SceneGraphConcurrent.class.getName(), e.getLocalizedMessage());
        }

        // Debug thread number
        EventManager.instance.post(Events.DEBUG2, "SG threads: " + Arrays.toString(objectsPerThread));
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

}
