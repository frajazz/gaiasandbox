package gaia.cu9.ari.gaiaorbit.scenegraph;

import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

/**
 * Implementation of a 3D scene graph.
 * @author Toni Sagrista
 *
 */
public class SceneGraph extends AbstractSceneGraph {

    public SceneGraph() {
        super();
    }

    public void update(ITimeFrameProvider time, ICamera camera) {
        super.update(time, camera);

        root.transform.position.set(camera.getInversePos());
        root.update(time, null, camera);
        objectsPerThread[0] = root.numChildren;
    }

    public void dispose() {
        super.dispose();
    }

}
