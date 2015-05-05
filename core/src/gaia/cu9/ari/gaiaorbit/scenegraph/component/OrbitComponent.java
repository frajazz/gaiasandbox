package gaia.cu9.ari.gaiaorbit.scenegraph.component;

public class OrbitComponent {

    /** Source file **/
    public String source;
    /** Orbital period in days **/
    public double period;
    /** Base epoch **/
    public double epoch;
    /** Semi major axis of the ellipse, a.**/
    public double semimajoraxis;
    /** Eccentricity of the ellipse. **/
    public double e;
    /** Inclination, angle between the reference plane (ecliptic) and the orbital plane. **/
    public double i;
    /** Longitude of the ascending node in degrees. **/
    public double ascendingnode;
    /** Argument of perihelion in degrees. **/
    public double argofpericenter;
    /** Mean anomaly at epoch, in degrees. **/
    public double meananomaly;

    public void setSource(String source) {
        this.source = source;
    }

    public void setPeriod(Double period) {
        this.period = period;
    }

    public void setEpoch(Long epoch) {
        this.epoch = epoch;
    }

    public void setEpoch(Double epoch) {
        this.epoch = epoch;
    }

    public void setSemimajoraxis(Double semimajoraxis) {
        this.semimajoraxis = semimajoraxis;
    }

    public void setEccentricity(Double e) {
        this.e = e;
    }

    public void setInclination(Double i) {
        this.i = i;
    }

    public void setAscendingnode(Double ascendingnode) {
        this.ascendingnode = ascendingnode;
    }

    public void setArgofpericenter(Double argofpericenter) {
        this.argofpericenter = argofpericenter;
    }

    public void setMeananomaly(Double meanAnomaly) {
        this.meananomaly = meanAnomaly;
    }

}
