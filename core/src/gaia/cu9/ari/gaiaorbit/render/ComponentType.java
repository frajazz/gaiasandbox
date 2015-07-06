package gaia.cu9.ari.gaiaorbit.render;

import gaia.cu9.ari.gaiaorbit.util.I18n;

import java.util.HashMap;
import java.util.Map;

public enum ComponentType {
    Stars("Stars"), Planets("Planets"), Moons("Moons"), Satellites("Satellites"), Asteroids("Asteroids"), Labels("Labels"), Equatorial("Equatorial grid", "grid-icon"), Ecliptic("Ecliptic grid", "grid-icon"), Galactic("Galactic grid", "grid-icon"), Orbits("Orbits"), Atmospheres("Atmospheres"), Constellations("Constellations"), Boundaries("Boundaries"), MilkyWay("Milky way"), Galaxies("Galaxies"), Others("Others");

    private static Map<String, ComponentType> namesMap = new HashMap<String, ComponentType>();

    static {
        for (ComponentType ct : ComponentType.values()) {
            namesMap.put(ct.id, ct);
        }
    }

    public String id;
    private String name;
    public String style;

    private ComponentType(String id) {
        this.id = id;
    }

    private ComponentType(String id, String icon) {
        this(id);
        this.style = icon;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        if (name == null) {
            name = I18n.bundle.get("element." + name().toLowerCase());
            namesMap.put(name, this);
        }
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

    public static ComponentType getFromName(String name) {
        ComponentType ct = null;
        try {
            ct = ComponentType.valueOf(name);
        } catch (Exception e) {
            // Look for name
            ct = namesMap.get(name);
        }
        return ct;
    }
}
