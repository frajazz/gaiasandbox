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

    public static void initialize(File folder) {
	icons = new HashMap<String, Icon>();
	EventManager.getInstance().post(Events.POST_NOTIFICATION, I18n.bundle.get("notif.icon.initialising"));
	if (folder.exists() && folder.isDirectory() && folder.canRead()) {
	    String[] iconNames = folder.list();
	    for (String iconName : iconNames) {
		if (iconName.endsWith(".png")) {
		    try {
			URL iconURL = new File(folder, iconName).toURI().toURL();

			icons.put(iconName.substring(0, iconName.lastIndexOf('.')), new ImageIcon(iconURL));
		    } catch (Exception e) {
			EventManager.getInstance().post(Events.JAVA_EXCEPTION, new RuntimeException(I18n.bundle.format("error.icon.loading", iconName), e));
		    }
		}
	    }
	    EventManager.getInstance().post(Events.POST_NOTIFICATION, I18n.bundle.format("notif.icon.init", icons.size()));
	} else {
	    EventManager.getInstance().post(Events.JAVA_EXCEPTION, new RuntimeException(I18n.bundle.format("error.icon.folder", folder.getPath())));
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
