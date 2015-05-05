package gaia.cu9.ari.gaiaorbit.scenegraph.component;

public class RingComponent {
    public int divisions;
    public float innerRadius, outerRadius;

    public RingComponent() {

    }

    public void setInnerradius(Double innerRadius) {
        this.innerRadius = innerRadius.floatValue();
    }

    public void setOuterradius(Double outerRadius) {
        this.outerRadius = outerRadius.floatValue();
    }

    public void setDivisions(Long divisions) {
        this.divisions = divisions.intValue();
    }

}
