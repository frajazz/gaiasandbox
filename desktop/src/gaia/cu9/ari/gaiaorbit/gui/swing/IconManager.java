package gaia.cu9.ari.gaiaorbit.gui.swing;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.render.SceneGraphRenderer.ComponentType;
import gaia.cu9.ari.gaiaorbit.util.I18n;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class IconManager {

    public static Map<String, Icon> icons;

    public static void initialise(File folder) {
	if (folder.exists() && folder.isDirectory() && folder.canRead()) {
	    icons = new HashMap<String, Icon>();
	    EventManager.instance.post(Events.POST_NOTIFICATION, I18n.bundle.get("notif.icon.initialising"));

	    initialiseDirectory(folder, "");

	    EventManager.instance.post(Events.POST_NOTIFICATION, I18n.bundle.format("notif.icon.init", icons.size()));
	}
    }

    private static void initialiseDirectory(File dir, String prefix) {
	String[] names = dir.list();
	for (String name : names) {
	    File f = new File(dir, name);
	    if (f.isDirectory()) {
		initialiseDirectory(f, prefix + f.getName() + "/");
	    } else {
		if (name.endsWith(".png")) {
		    try {
			URL iconURL = new File(dir, name).toURI().toURL();

			icons.put(prefix + name.substring(0, name.lastIndexOf('.')), new ImageIcon(iconURL));
		    } catch (Exception e) {
			EventManager.instance.post(Events.JAVA_EXCEPTION, new RuntimeException(I18n.bundle.format("error.icon.loading", name), e));
		    }
		}
	    }
	}
    }

    public static synchronized Icon get(String name) {
	return icons.get(name);
    }

    public static synchronized Icon get(ComponentType ct) {
	String name = ct.name().toLowerCase();
	if (icons.containsKey(name)) {
	    return icons.get(name);
	} else if (icons.containsKey(name.substring(0, name.length() - 1))) {
	    return icons.get(name.substring(0, name.length() - 1));
	} else {
	    switch (ct) {
	    case Ecliptic:
	    case Equatorial:
	    case Galactic:
		return icons.get("grid");
	    default:
		return icons.get("model");
	    }
	}
    }
}
