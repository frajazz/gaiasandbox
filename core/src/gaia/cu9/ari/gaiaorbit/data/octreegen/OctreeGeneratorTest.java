package gaia.cu9.ari.gaiaorbit.data.octreegen;

import gaia.cu9.ari.gaiaorbit.data.stars.HYGBinaryLoader;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

public class OctreeGeneratorTest implements IObserver {

    public static void main(String[] args) {
	try {
	    GlobalConf.initialize(new FileInputStream(new File("../android/assets/conf/global.properties")), new FileInputStream(new File("../android/assets/data/dummyversion")));

	    I18n.initialize("../android/assets/i18n/gsbundle_en_GB");

	    // Add notif watch
	    EventManager.getInstance().subscribe(new OctreeGeneratorTest(), Events.POST_NOTIFICATION);

	    OctreeGenerator og = new OctreeGenerator(BrightestStars.class);

	    HYGBinaryLoader starLoader = new HYGBinaryLoader();

	    List<CelestialBody> list = starLoader.loadStars(new FileInputStream(new File("../android/assets/data/hygxyz.bin")));
	    OctreeNode<CelestialBody> octree = og.generateOctree(list);

	    System.out.println(octree.toString());

	} catch (FileNotFoundException e) {
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
	}

    }

}
