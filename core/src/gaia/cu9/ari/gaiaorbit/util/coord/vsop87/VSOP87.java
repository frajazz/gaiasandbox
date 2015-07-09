package gaia.cu9.ari.gaiaorbit.util.coord.vsop87;

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
            elements.put(cb, getVSOP87Instance(GlobalResources.trueCapitalise(cb)));
            tried.put(cb, true);
        }
        return elements.get(cb);
    }

    private iVSOP87 getVSOP87Instance(String name) {
        if (name.contains("Earth")) {
            return new EarthVSOP87();
        } else if (name.contains("Jupiter")) {
            return new JupiterVSOP87();
        } else if (name.contains("Mars")) {
            return new MarsVSOP87();
        } else if (name.contains("Mercury")) {
            return new MercuryVSOP87();
        } else if (name.contains("Neptune")) {
            return new NeptuneVSOP87();
        } else if (name.contains("Saturn")) {
            return new SaturnVSOP87();
        } else if (name.contains("Uranus")) {
            return new UranusVSOP87();
        } else if (name.contains("Venus")) {
            return new VenusVSOP87();
        } else {
            return new DummyVSOP87();
        }
    }
}
