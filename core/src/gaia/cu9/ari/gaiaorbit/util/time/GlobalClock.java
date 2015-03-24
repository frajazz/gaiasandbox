package gaia.cu9.ari.gaiaorbit.util.time;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Keeps pace of the simulation time vs real time and holds the global clock
 * @author Toni Sagrista
 *
 */
public class GlobalClock implements IObserver, ITimeFrameProvider {
    private static final double MS_TO_HOUR = 1 / 3600000d;

    // Represents the current time
    public GregorianCalendar cal;
    /**The current time of the clock **/
    public Date time, lastTime;
    /** The hour difference from the last frame **/
    public double hdiff;

    // Represents the pace in simulation hours/real seconds
    public double pace = 2;
    // Seconds since last event POST
    private float lastUpdate = 1;
    /** The fixed frame rate when not in real time. Set negative to use real time **/
    public float fps = -1;

    /** The singleton pattern **/
    public static GlobalClock clock;

    /**
     * Initializes the singleton pattern
     * @param pace The pace of the clock in [simulation hours/real seconds]
     */
    public static void initialize(double pace) {
	clock = new GlobalClock(pace, new Date());
    }

    public static void initialize(double pace, Date date) {
	clock = new GlobalClock(pace, date);
    }

    public static boolean initialized() {
	return clock != null;
    }

    /**
     * Creates a new GlobalClock
     * @param pace The pace of the clock in [simulation hours/real seconds]
     * @param date The date with which to initialize the clock
     */
    public GlobalClock(double pace, Date date) {
	super();
	// Now
	cal = new GregorianCalendar();
	this.pace = pace;
	hdiff = 0d;
	time = date;
	lastTime = new Date(time.getTime());
	cal.setTime(time);
	EventManager.instance.subscribe(this, Events.PACE_CHANGE_CMD, Events.PACE_DIVIDE_CMD, Events.PACE_DOUBLE_CMD, Events.TIME_CHANGE_CMD);
    }

    double msacum = 0d;

    public void update(double dt) {
	if (dt != 0) {
	    // In case we are in constant rate mode
	    if (fps > 0) {
		dt = 1 / fps;
	    }

	    int days = 0;
	    int hours = 0;
	    int mins = 0;
	    int secs = 0;
	    int millisecs = 0;

	    int sign = (int) Math.signum(pace);
	    double h = Math.abs(dt * pace);
	    hdiff = h * sign;

	    days = (int) (h / 24);
	    h = (h / 24 - days) * 24;
	    hours = (int) h;

	    double m = (h - Math.floor(h)) * 60;
	    mins = (int) m;

	    double s = (m - Math.floor(m)) * 60;
	    secs = (int) s;

	    double ms = (s - Math.floor(s)) * 1000;
	    millisecs = (int) Math.round(ms);
	    if (days == 0 && hours == 0 && mins == 0 && secs == 0 && millisecs == 0) {
		msacum += ms;
		millisecs = (int) Math.round(msacum);
		if (millisecs > 0) {
		    msacum = 0;
		}
	    }

	    cal.add(Calendar.DAY_OF_YEAR, days * sign);
	    cal.add(Calendar.HOUR, hours * sign);
	    cal.add(Calendar.MINUTE, mins * sign);
	    cal.add(Calendar.SECOND, secs * sign);
	    cal.add(Calendar.MILLISECOND, millisecs * sign);

	    lastTime.setTime(time.getTime());
	    time.setTime(cal.getTimeInMillis());

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
	    this.cal.setTimeInMillis(newt);
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
