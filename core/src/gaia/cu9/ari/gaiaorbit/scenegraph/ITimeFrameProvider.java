package gaia.cu9.ari.gaiaorbit.scenegraph;

import java.util.Date;

/**
 * Basic interface for entities that provide an time frame in
 * the scene.
 * @author Toni Sagrista
 *
 */
public interface ITimeFrameProvider {

    /**
     * Gets the difference from the last time frame in hours.
     * @return
     */
    public double getDt();

    /**
     * Gets the current time.
     * @return
     */
    public Date getTime();

    /**
     * Updates this time frame with the system time difference.
     * @param dt System time difference in seconds.
     */
    public void update(double dt);

    /**
     * Gets the current pace.
     * @return
     */
    public double getPace();

}
