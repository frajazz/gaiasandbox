package gaia.cu9.ari.gaiaorbit.data.octreegen;

import gaia.cu9.ari.gaiaorbit.data.FileLocator;
import gaia.cu9.ari.gaiaorbit.data.stars.HYGBinaryLoader;
import gaia.cu9.ari.gaiaorbit.data.stars.ICatalogLoader;
import gaia.cu9.ari.gaiaorbit.data.stars.OctreeCatalogLoader;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.scenegraph.Particle;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.scenegraph.octreewrapper.AbstractOctreeWrapper;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

import java.io.*;
import java.util.List;
import java.util.Properties;

public class OctreeGeneratorTest implements IObserver {

    public static enum Operation {
        LOAD_OCTREE, GENERATE_OCTREE
    }

    public static Operation operation = Operation.GENERATE_OCTREE;

    public static void main(String[] args) {
        try {
            GlobalConf.initialize(new FileInputStream(new File("../android/assets/conf/global.properties")), new FileInputStream(new File("../android/assets/data/dummyversion")));

            I18n.initialize("../android/assets/i18n/gsbundle_en_GB");

            // Add notif watch
            EventManager.instance.subscribe(new OctreeGeneratorTest(), Events.POST_NOTIFICATION, Events.JAVA_EXCEPTION);

            switch (operation) {
            case GENERATE_OCTREE:
                generateOctree();
                break;
            case LOAD_OCTREE:
                FileLocator.initialize();
                loadOctree();
                break;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void generateOctree() throws IOException {
        OctreeGenerator og = new OctreeGenerator(BrightestStars.class);

        HYGBinaryLoader starLoader = new HYGBinaryLoader();

        List<Particle> list = (List<Particle>) starLoader.loadCatalog(new FileInputStream(new File("../android/assets/data/hygxyz.bin")));
        OctreeNode<Particle> octree = og.generateOctree(list);

        // Put all new particles in list
        list.clear();
        octree.addParticlesTo(list);

        System.out.println(octree.toString());

        String temp = System.getProperty("java.io.tmpdir");

        /** WRITE METADATA **/
        File metadata = new File(temp, "metadata_" + System.currentTimeMillis() + ".bin");
        if (metadata.exists()) {
            metadata.delete();
        }
        metadata.createNewFile();

        System.out.println("Writing metadata (" + octree.numNodes() + " nodes): " + metadata.getAbsolutePath());

        MetadataBinaryIO metadataWriter = new MetadataBinaryIO();
        metadataWriter.writeMetadata(octree, new FileOutputStream(metadata));

        /** WRITE PARTICLES **/
        File particles = new File(temp, "particles_" + System.currentTimeMillis() + ".bin");
        if (particles.exists()) {
            particles.delete();
        }
        particles.createNewFile();

        System.out.println("Writing particles (" + list.size() + " particles): " + particles.getAbsolutePath());

        ParticleDataBinaryIO particleWriter = new ParticleDataBinaryIO();
        particleWriter.writeParticles(list, new FileOutputStream(particles));
    }

    private static void loadOctree() throws FileNotFoundException {
        Properties p = new Properties();
        p.put("metadata", "data/hyg_metadata.bin");
        p.put("particles", "data/hyg_particles.bin");
        ICatalogLoader loader = new OctreeCatalogLoader();
        loader.initialize(p);
        List<? extends SceneGraphNode> l = loader.loadCatalog();
        AbstractOctreeWrapper ow = null;
        for (SceneGraphNode n : l) {
            if (n instanceof AbstractOctreeWrapper) {
                ow = (AbstractOctreeWrapper) n;
                break;
            }
        }
        System.out.println(ow.root.toString());

    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case POST_NOTIFICATION:
            String message = "";
            boolean perm = false;
            for (int i = 0; i < data.length; i++) {
                if (i == data.length - 1 && data[i] instanceof Boolean) {
                    perm = (Boolean) data[i];
                } else {
                    message += (String) data[i];
                    if (i < data.length - 1 && !(i == data.length - 2 && data[data.length - 1] instanceof Boolean)) {
                        message += " - ";
                    }
                }
            }
            System.out.println(message);
            break;
        case JAVA_EXCEPTION:
            Exception e = (Exception) data[0];
            e.printStackTrace(System.err);
            break;
        }

    }

}
