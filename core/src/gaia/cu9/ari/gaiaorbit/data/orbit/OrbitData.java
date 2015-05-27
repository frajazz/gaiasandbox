package gaia.cu9.ari.gaiaorbit.data.orbit;

import com.badlogic.gdx.math.Vector3;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrbitData {
    // Values of x, y, z in world coordinates
    public List<Double> x, y, z;
    public List<Date> time;

    private Vector3d v0, v1;

    public OrbitData() {
        x = new ArrayList<Double>();
        y = new ArrayList<Double>();
        z = new ArrayList<Double>();
        time = new ArrayList<Date>();

        v0 = new Vector3d();
        v1 = new Vector3d();
    }

    /**
     * Loads the data point at the index in the vector in the Orbit reference system
     * @param v
     * @param index 
     */
    public void loadPoint(Vector3d v, int index) {
        v.set(x.get(index), y.get(index), z.get(index));
    }

    public int getNumPoints() {
        return x.size();
    }

    public double getX(int index) {
        return x.get(index);
    }

    public double getY(int index) {
        return y.get(index);
    }

    public double getZ(int index) {
        return z.get(index);
    }

    public Date getDate(int index) {
        return time.get(index);
    }

    /**
     * Loads the data point at the index in the vector in the world reference system
     * @param v
     * @param index
     */
    public void loadPointF(Vector3 v, int index) {
        v.set(x.get(index).floatValue(), y.get(index).floatValue(), z.get(index).floatValue());
    }

    /**
     * Returns a vector with the data point at the given time. It uses
     * linear interpolation.
     * @param date
     * @return
     */
    public boolean loadPoint(Vector3d v, Date date) {
        // Data is sorted
        int idx = binarySearch(time, date);

        if (idx < 0 || idx >= time.size()) {
            // No data for this time
            return false;
        }

        if (time.get(idx).equals(date)) {
            v.set(x.get(idx), y.get(idx), z.get(idx));
        } else {
            // Interpolate
            loadPoint(v0, idx);
            loadPoint(v1, idx + 1);
            Date t0 = time.get(idx);
            Date t1 = time.get(idx + 1);

            double scl = (double) (date.getTime() - t0.getTime()) / (t1.getTime() - t0.getTime());
            v.set(v1.sub(v0).scl(scl).add(v0));
        }
        return true;
    }

    private int binarySearch(List<Date> times, Date elem) {
        long time = elem.getTime();
        if (time >= times.get(0).getTime() && time <= times.get(times.size() - 1).getTime()) {
            return binarySearch(times, time, 0, times.size() - 1);
        } else {
            return -1;
        }
    }

    private int binarySearch(List<Date> times, long time, int i0, int i1) {
        if (i0 > i1) {
            return -1;
        } else if (i0 == i1) {
            if (times.get(i0).getTime() > time) {
                return i0 - 1;
            } else {
                return i0;
            }
        }

        int mid = (i0 + i1) / 2;
        if (times.get(mid).getTime() == time) {
            return mid;
        } else if (times.get(mid).getTime() < time) {
            return binarySearch(times, time, mid + 1, i1);
        } else {
            return binarySearch(times, time, i0, mid);
        }
    }

}
