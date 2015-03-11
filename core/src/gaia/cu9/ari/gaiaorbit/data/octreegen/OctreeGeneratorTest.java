package gaia.cu9.ari.gaiaorbit.data.octreegen;

import gaia.cu9.ari.gaiaorbit.data.stars.HYGBinaryLoader;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.scenegraph.Star;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class OctreeGeneratorTest implements IObserver {

    public static void main(String[] args) {
	try {
	    GlobalConf.initialize(new FileInputStream(new File("../android/assets/conf/global.properties")), new FileInputStream(new File("../android/assets/data/dummyversion")));

	    I18n.initialize("../android/assets/i18n/gsbundle_en_GB");

	    // Add notif watch
	    EventManager.getInstance().subscribe(new OctreeGeneratorTest(), Events.POST_NOTIFICATION, Events.JAVA_EXCEPTION);

	    OctreeGenerator og = new OctreeGenerator(BrightestStars.class);

	    HYGBinaryLoader starLoader = new HYGBinaryLoader();

	    List<Star> list = (List<Star>) starLoader.loadCatalog(new FileInputStream(new File("../android/assets/data/hygxyz.bin")));
	    OctreeNode<Star> octree = og.generateOctree(list);

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

	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
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
