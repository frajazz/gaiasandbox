package gaia.cu9.ari.gaiaorbit.desktop.concurrent;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.scenegraph.AbstractSceneGraph;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.scenegraph.octreewrapper.OctreeWrapperConcurrent;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of a 3D scene graph where the node updates takes place
 * concurrently in threads (as many as processors). This implementation takes
 * into account that one of the top-level nodes is an Octree whose contents also 
 * need to be parallelized at the top level.
 * @author Toni Sagrista
 *
 */
public class SceneGraphConcurrentOctree extends AbstractSceneGraph {

    private ThreadPoolExecutor pool;
    private List<UpdaterTask<SceneGraphNode>> tasks;
    private OctreeWrapperConcurrent octree;
    private List<SceneGraphNode> roulette;
    int numThreads;

    public SceneGraphConcurrentOctree(int numThreads) {
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
        tasks = new ArrayList<UpdaterTask<SceneGraphNode>>(pool.getCorePoolSize());
        roulette = new ArrayList<SceneGraphNode>(150000);

        Iterator<SceneGraphNode> it = nodes.iterator();
        while (it.hasNext()) {
            SceneGraphNode node = it.next();
            if (node instanceof OctreeWrapperConcurrent) {
                octree = (OctreeWrapperConcurrent) node;
                it.remove();
                octree.setRoulette(roulette);
                break;
            }
        }

        // Create the tasks with the roulette collections references
        for (int i = 0; i < numThreads; i++) {
            tasks.add(new UpdaterTask<SceneGraphNode>(roulette, i, numThreads));
        }

        Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.threadpool.init", numThreads));
    }

    public void update(ITimeFrameProvider time, ICamera camera) {
        super.update(time, camera);
        root.transform.position.set(camera.getInversePos());

        // Add top-level nodes to roulette
        roulette.addAll(root.children);
        roulette.remove(octree);

        // Update octree - Add nodes to process to roulette
        octree.update(time, root.transform, camera, 1f);

        // Update params
        int size = tasks.size();
        for (int i = 0; i < size; i++) {
            UpdaterTask<SceneGraphNode> task = tasks.get(i);
            task.setParameters(camera, time);
        }

        try {
            pool.invokeAll(tasks);
        } catch (InterruptedException e) {
            Logger.error(e);
        }

        // Update focus, just in case
        CelestialBody focus = camera.getFocus();
        if (focus != null) {
            SceneGraphNode star = focus.getFirstStarAncestor();
            OctreeNode<SceneGraphNode> parent = octree.parenthood.get(star);
            if (parent != null && !parent.isObserved()) {
                star.update(time, star.parent.transform, camera);
            }
        }

        // Debug thread number
        EventManager.instance.post(Events.DEBUG2, "SG threads: " + getRouletteDebug());

        // Clear roulette
        roulette.clear();
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
