package gaia.cu9.ari.gaiaorbit.data.stars;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;

import gaia.cu9.ari.gaiaorbit.data.ISceneGraphLoader;
import gaia.cu9.ari.gaiaorbit.data.octreegen.MetadataBinaryIO;
import gaia.cu9.ari.gaiaorbit.data.octreegen.ParticleDataBinaryIO;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.scenegraph.Star;
import gaia.cu9.ari.gaiaorbit.scenegraph.octreewrapper.AbstractOctreeWrapper;
import gaia.cu9.ari.gaiaorbit.scenegraph.octreewrapper.OctreeWrapper;
import gaia.cu9.ari.gaiaorbit.scenegraph.octreewrapper.OctreeWrapperConcurrent;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.tree.LoadStatus;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

public class OctreeCatalogLoader implements ISceneGraphLoader {

    String metadata, particles;

    @Override
    public List<? extends SceneGraphNode> loadData() throws FileNotFoundException {
        Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.limitmag", GlobalConf.data.LIMIT_MAG_LOAD));

        MetadataBinaryIO metadataReader = new MetadataBinaryIO();
        OctreeNode<SceneGraphNode> root = (OctreeNode<SceneGraphNode>) metadataReader.readMetadata(Gdx.files.internal(metadata).read());

        Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.nodeloader", root.numNodes(), metadata));

        ParticleDataBinaryIO particleReader = new ParticleDataBinaryIO();
        List<CelestialBody> particleList = particleReader.readParticles(Gdx.files.internal(particles).read());

        Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.nodeloader", particleList.size(), particles));

        /**
        * CREATE OCTREE WRAPPER WITH ROOT NODE
        */
        AbstractOctreeWrapper octreeWrapper = null;
        if (GlobalConf.performance.MULTITHREADING) {
            octreeWrapper = new OctreeWrapperConcurrent("Universe", root);
        } else {
            octreeWrapper = new OctreeWrapper("Universe", root);
        }
        List<SceneGraphNode> result = new ArrayList<SceneGraphNode>(1);
        result.add(octreeWrapper);

        /** 
         * ADD STARS
         */
        // Update model
        for (SceneGraphNode sgn : particleList) {
            Star s = (Star) sgn;
            OctreeNode<SceneGraphNode> octant = metadataReader.nodesMap.get(s.octantId).getFirst();
            octant.add(s);
            s.octant = octant;
            // Update status
            octant.setStatus(LoadStatus.LOADED);

            // Add objects to octree wrapper node
            octreeWrapper.add(s, octant);
        }

        Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.catalog.init", particleList.size()));

        return result;
    }

    @Override
    public void initialize(String[] files) throws RuntimeException {
        if (files == null || files.length < 2) {
            throw new RuntimeException("Error loading octree files: " + files.length);
        }
        particles = files[0];
        metadata = files[1];
    }

}
