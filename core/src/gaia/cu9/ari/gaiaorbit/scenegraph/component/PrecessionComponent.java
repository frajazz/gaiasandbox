package gaia.cu9.ari.gaiaorbit.scenegraph.component;

/**
 * Provides the information for the precession of this body.
 * @author Toni Sagrista
 *
 */
public class PrecessionComponent {
    /** Precession angle in deg **/
    public float precessionAngle;
    /** Precession velocity in deg/s **/
    protected float precessionVelocity;
    /** Current precession position around y **/
    public float precessionPosition;

    public PrecessionComponent() {

    }
}
