package gaia.cu9.ari.gaiaorbit.scenegraph;

import gaia.cu9.ari.gaiaorbit.util.math.Matrix4d;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

import com.badlogic.gdx.math.Vector3;

/** 
 * Represents a geometric transformation. It can be either a matrix or a position. Since we have
 * such a vast amount of stars, it is convenient to be able to represent translations
 * with a position vector rather than with an almost empty transform matrix.
 * @author Toni Sagrista
 *
 */
public class Transform {

    public Matrix4d transform;
    public Vector3d position;

    public Transform() {
	super();
    }

    /**
     * Sets this transform to represent the same as the other transform.
     * @param other
     */
    public void set(Transform parent) {
	if (parent != null) {
	    if (position != null) {
		if (parent.position != null) {
		    position.set(parent.position);
		} else if (parent.transform != null) {
		    // Vector > matrix
		    parent.transform.getTranslation(position);
		}
	    } else if (transform != null) {
		if (parent.position != null) {
		    // Matrix > vector
		    transform.setTranslation(parent.position);
		} else if (parent.transform != null) {
		    // Matrix > matrix
		    transform.set(parent.transform);
		}
	    }
	}
    }

    public void translate(Vector3d position) {
	if (this.transform != null) {
	    this.transform.translate(position);
	} else if (this.position != null) {
	    this.position.add(position);
	}
    }

    public void setToTranslation(Transform parent, Vector3d localPosition) {
	set(parent);
	translate(localPosition);
    }

    public Matrix4d getMatrix() {
	if (transform != null) {
	    return transform;
	} else if (position != null) {
	    return new Matrix4d().translate(position);
	}
	return null;
    }

    public Vector3d getTranslation(Vector3d aux) {
	if (position != null) {
	    return aux.set(position);
	} else if (transform != null) {
	    return transform.getTranslation(aux);
	} else {
	    return aux;
	}
    }

    public Vector3 getTranslationf(Vector3 aux) {
	if (position != null) {
	    return aux.set(position.valuesf());
	} else if (transform != null) {
	    return transform.getTranslationf(aux);
	} else {
	    return aux;
	}
    }

    /** Adds the translation of this object to the aux vector, and 
     * returns it for chaining.
     * @param aux
     */
    public Vector3d addTranslationTo(Vector3d aux) {
	if (position != null) {
	    return aux.add(position);
	} else if (transform != null) {
	    return transform.addTranslationTo(aux);
	} else {
	    return aux;
	}
    }

    public double[] getTranslation() {
	if (position != null) {
	    return position.values();
	} else if (transform != null) {
	    return transform.getTranslation();
	} else {
	    return null;
	}
    }

    public float[] getTranslationf() {
	if (position != null) {
	    return position.valuesf();
	} else if (transform != null) {
	    return transform.getTranslationf();
	} else {
	    return null;
	}
    }

    @Override
    public String toString() {
	if (position != null) {
	    return position.toString();
	} else if (transform != null) {
	    return transform.toString();
	} else {
	    return super.toString();
	}
    }

}
