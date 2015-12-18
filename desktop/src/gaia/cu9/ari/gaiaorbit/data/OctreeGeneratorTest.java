package gaia.cu9.ari.gaiaorbit.data;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import gaia.cu9.ari.gaiaorbit.data.octreegen.BrightestStars;
import gaia.cu9.ari.gaiaorbit.data.octreegen.IAggregationAlgorithm;
import gaia.cu9.ari.gaiaorbit.data.octreegen.MetadataBinaryIO;
import gaia.cu9.ari.gaiaorbit.data.octreegen.OctreeGenerator;
import gaia.cu9.ari.gaiaorbit.data.octreegen.ParticleDataBinaryIO;
import gaia.cu9.ari.gaiaorbit.data.stars.CatalogFilter;
import gaia.cu9.ari.gaiaorbit.data.stars.HYGBinaryLoader;
import gaia.cu9.ari.gaiaorbit.data.stars.OctreeCatalogLoader;
import gaia.cu9.ari.gaiaorbit.data.stars.TGASLoader;
import gaia.cu9.ari.gaiaorbit.desktop.format.DesktopDateFormatFactory;
import gaia.cu9.ari.gaiaorbit.desktop.format.DesktopNumberFormatFactory;
import gaia.cu9.ari.gaiaorbit.desktop.util.DesktopConfInit;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.Particle;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.scenegraph.Star;
import gaia.cu9.ari.gaiaorbit.scenegraph.octreewrapper.AbstractOctreeWrapper;
import gaia.cu9.ari.gaiaorbit.util.ConfInit;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.format.DateFormatFactory;
import gaia.cu9.ari.gaiaorbit.util.format.NumberFormatFactory;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

public class OctreeGeneratorTest implements IObserver {

    public static enum Operation {
        LOAD_OCTREE, GENERATE_OCTREE
    }

    public static Operation operation = Operation.GENERATE_OCTREE;

    public static void main(String[] args) {
        try {
            Gdx.files = new LwjglFiles();

            // Initialize number format
            NumberFormatFactory.initialize(new DesktopNumberFormatFactory());

            // Initialize date format
            DateFormatFactory.initialize(new DesktopDateFormatFactory());

            ConfInit.initialize(new DesktopConfInit(new FileInputStream(new File("../android/assets/conf/global.properties")), new FileInputStream(new File("../android/assets/data/dummyversion"))));

            I18n.initialize(new FileHandle("/home/tsagrista/git/gaiasandbox/android/assets/i18n/gsbundle"));

            // Add notif watch
            EventManager.instance.subscribe(new OctreeGeneratorTest(), Events.POST_NOTIFICATION, Events.JAVA_EXCEPTION);

            switch (operation) {
            case GENERATE_OCTREE:
                generateOctree();
                break;
            case LOAD_OCTREE:
                Gdx.files = new LwjglFiles();
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
        IAggregationAlgorithm<Particle> aggr;
        try {
            aggr = ClassReflection.newInstance(BrightestStars.class);
        } catch (ReflectionException e) {
            e.printStackTrace(System.err);
            return;
        }

        OctreeGenerator og = new OctreeGenerator(aggr);

        /** HIP **/
        HYGBinaryLoader hyg = new HYGBinaryLoader();
        hyg.initialize(new String[] { "data/hygxyz.bin" });
        hyg.addFilter(new CatalogFilter() {
            @Override
            public boolean filter(CelestialBody s) {
                return (s instanceof Particle) && ((Particle) s).appmag <= 7.5f;
            }
        });

        /** TYCHO **/
        //        STILCatalogLoader tycho = new STILCatalogLoader();
        //        tycho.initialize(new String[] { "/home/tsagrista/Workspaces/objectserver/data/tycho.vot.gz" });

        /** TGAS **/
        TGASLoader tgas = new TGASLoader();
        tgas.initialize(new String[] { "data/tgas_201507/Solution1507280011.txt" });

        List<Particle> list = (List<Particle>) hyg.loadData();
        Set<Integer> hips = new HashSet<Integer>();
        for (Particle p : list) {
            if (p instanceof Star) {
                hips.add(((Star) p).hip);
            }
        }

        List<Particle> list2 = tgas.loadData();
        for (Particle p : list2) {
            if (!(p instanceof Star) || (p instanceof Star && !hips.contains(((Star) p).hip))) {
                list.add(p);
            }
        }

        Logger.info("Generating octree with " + list.size() + " actual stars");

        OctreeNode<Particle> octree = og.generateOctree(list);

        // Put all new particles in list
        list.clear();
        octree.addParticlesTo(list);

        System.out.println(octree.toString(true));

        String temp = System.getProperty("java.io.tmpdir");

        long tstamp = System.currentTimeMillis();

        /** NUMBERS **/
        System.out.println("Octree generated with " + octree.numNodes() + " octants and " + list.size() + " particles");
        System.out.println(aggr.getDiscarded() + " particles have been discarded due to density");

        /** WRITE METADATA **/
        File metadata = new File(temp, "metadata_" + tstamp + ".bin");
        if (metadata.exists()) {
            metadata.delete();
        }
        metadata.createNewFile();

        System.out.println("Writing metadata (" + octree.numNodes() + " nodes): " + metadata.getAbsolutePath());

        MetadataBinaryIO metadataWriter = new MetadataBinaryIO();
        metadataWriter.writeMetadata(octree, new FileOutputStream(metadata));

        /** WRITE PARTICLES **/
        File particles = new File(temp, "particles_" + tstamp + ".bin");
        if (particles.exists()) {
            particles.delete();
        }
        particles.createNewFile();

        System.out.println("Writing particles (" + list.size() + " particles): " + particles.getAbsolutePath());

        ParticleDataBinaryIO particleWriter = new ParticleDataBinaryIO();
        particleWriter.writeParticles(list, new BufferedOutputStream(new FileOutputStream(particles)));
    }

    private static void loadOctree() throws FileNotFoundException {
        String[] files = new String[] { "data/hyg_particles.bin", "data/hyg_metadata.bin" };
        ISceneGraphLoader loader = new OctreeCatalogLoader();
        loader.initialize(files);
        List<? extends SceneGraphNode> l = loader.loadData();
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
