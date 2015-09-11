package gaia.cu9.ari.gaiaorbit.util.time;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.util.Constants;

import java.util.Date;

/**
 * Keeps pace of the simulation time vs real time and holds the global clock
 * @author Toni Sagrista
 *
 */
public class GlobalClock implements IObserver, ITimeFrameProvider {
    private static final double MS_TO_HOUR = 1 / 3600000d;

    /**The current time of the clock **/
    public Date time, lastTime;
    /** The hour difference from the last frame **/
    public double hdiff;

    /** Represents the pace in simulation hours/real seconds **/
    public double pace = 2;
    // Seconds since last event POST
    private float lastUpdate = 1;
    /** The fixed frame rate when not in real time. Set negative to use real time **/
    public float fps = -1;

    /**
     * Creates a new GlobalClock
     * @param pace The pace of the clock in [simulation hours/real seconds]
     * @param date The date with which to initialise the clock
     */
    public GlobalClock(double pace, Date date) {
        super();
        // Now
        this.pace = pace;
        hdiff = 0d;
        time = date;
        lastTime = new Date(time.getTime());
        EventManager.instance.subscribe(this, Events.PACE_CHANGE_CMD, Events.PACE_DIVIDE_CMD, Events.PACE_DOUBLE_CMD, Events.TIME_CHANGE_CMD);
    }

    double msacum = 0d;

    public void update(double dt) {
        if (dt != 0) {
            // In case we are in constant rate mode
            if (fps > 0) {
                dt = 1 / fps;
            }

            int sign = (int) Math.signum(pace);
            double h = Math.abs(dt * pace);
            hdiff = h * sign;

            double ms = sign * h * Constants.H_TO_MS;

            long currentTime = time.getTime();
            lastTime.setTime(currentTime);
            time.setTime(currentTime + (long) ms);

            // Post event each 1/2 second
            lastUpdate += dt;
            if (lastUpdate > .5) {
                EventManager.instance.post(Events.TIME_CHANGE_INFO, time);
                lastUpdate = 0;
            }
        } else if (time.getTime() - lastTime.getTime() != 0) {
            hdiff = (time.getTime() - lastTime.getTime()) * MS_TO_HOUR;
            lastTime.setTime(time.getTime());
        } else {
            hdiff = 0d;
        }
    }

    @Override
    public Date getTime() {
        return time;
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case PACE_CHANGE_CMD:
            // Update pace
            this.pace = (Double) data[0];
            EventManager.instance.post(Events.PACE_CHANGED_INFO, this.pace);
            break;
        case PACE_DOUBLE_CMD:
            this.pace *= 2d;
            EventManager.instance.post(Events.PACE_CHANGED_INFO, this.pace);
            break;
        case PACE_DIVIDE_CMD:
            this.pace /= 2d;
            EventManager.instance.post(Events.PACE_CHANGED_INFO, this.pace);
            break;
        case TIME_CHANGE_CMD:
            // Update time
            long newt = ((Date) data[0]).getTime();
            this.time.setTime(newt);
            break;
        }

    }

    /**
     * Provides the time difference in hours
     */
    @Override
    public double getDt() {
        return hdiff;
    }

    @Override
    public double getPace() {
        return pace;
    }

}
