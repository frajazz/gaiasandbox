package gaia.cu9.ari.gaiaorbit.scenegraph.component;

public class RingComponent {
    public int divisions;
    public float innerRadius, outerRadius;

    public RingComponent() {

    }

    public void setInnerradius(Float innerRadius) {
	this.innerRadius = innerRadius;
    }

    public void setOuterradius(Float outerRadius) {
	this.outerRadius = outerRadius;
    }

    public void setDivisions(Long divisions) {
	this.divisions = divisions.intValue();
    }

}
