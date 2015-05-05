package gaia.cu9.ari.gaiaorbit.scenegraph.octreewrapper;

import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.scenegraph.Transform;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

import java.util.List;

/**
 * Static Octree wrapper that can be inserted into the scene graph. 
 * This implementation is prepared to work with the {@link gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphConcurrentOctree},
 * which is designed to take advantage of knowing an octree is present in the scenegraph. 
 * @author Toni Sagrista
 *
 */
public class OctreeWrapperConcurrent extends AbstractOctreeWrapper {

    public OctreeWrapperConcurrent() {
        super();
    }

    public OctreeWrapperConcurrent(String parentName, OctreeNode<SceneGraphNode> root) {
        super(parentName, root);
    }

    public void setRoulette(List<SceneGraphNode> roulette) {
        this.roulette = roulette;
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    public void update(ITimeFrameProvider time, final Transform parentTransform, ICamera camera, float opacity) {
        this.opacity = opacity;
        transform.set(parentTransform);

        // Update octants
        if (!copy) {
            // Compute observed octants and fill roulette list
            root.update(transform, camera, roulette, 1f);
            updateLocal(time, camera);

        } else {
            // Just update children
            for (SceneGraphNode node : children) {
                node.update(time, transform, camera);
            }
        }

    }

    @Override
    protected void updateOctreeObjects(ITimeFrameProvider time, Transform parentTransform, ICamera camera) {
    }

    @Override
    protected String getRouletteDebug() {
        return null;
    }

}
