package gaia.cu9.ari.gaiaorbit.util.coord.vsop87;

import com.badlogic.gdx.Gdx;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;

import java.util.HashMap;
import java.util.Map;

public class VSOP87 {
    public static VSOP87 instance;
    static {
        instance = new VSOP87();
    }

    private Map<String, iVSOP87> elements;
    private Map<String, Boolean> tried;

    public VSOP87() {
        elements = new HashMap<String, iVSOP87>();
        tried = new HashMap<String, Boolean>();
    }

    public iVSOP87 getVOSP87(String cb) {
        if (!tried.containsKey(cb) || !tried.get(cb)) {
            // Initialize
            String pkg = "gaia.cu9.ari.gaiaorbit.util.coord.vsop87.";
            String name = GlobalResources.trueCapitalise(cb) + "VSOP87";
            Class<?> clazz = null;
            try {
                clazz = Class.forName(pkg + name);
            } catch (ClassNotFoundException e) {
                clazz = DummyVSOP87.class;
            }
            try {
                elements.put(cb, (iVSOP87) clazz.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                Gdx.app.error("VSOP87", e.getLocalizedMessage());
            }
            tried.put(cb, true);
        }
        return elements.get(cb);
    }
}
