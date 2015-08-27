package gaia.cu9.ari.gaiaorbit.desktop.gui.swing;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.badlogic.gdx.files.FileHandle;

public class IconManager {

    public static Map<String, Icon> icons;

    public static void initialise(FileHandle folder) {
        if (folder.exists() && folder.isDirectory()) {
            icons = new HashMap<String, Icon>();
            Logger.info(I18n.bundle.get("notif.icon.initialising"));

            initialiseDirectory(folder, "");

            Logger.info(I18n.bundle.format("notif.icon.init", icons.size()));
        }
    }

    private static void initialiseDirectory(FileHandle dir, String prefix) {
        FileHandle[] files = dir.list();
        for (FileHandle f : files) {
            String name = f.name();
            if (f.isDirectory()) {
                initialiseDirectory(f, prefix + f.name() + "/");
            } else {
                if (name.endsWith(".png")) {
                    try {
                        URL iconURL = f.file().toURI().toURL();

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
