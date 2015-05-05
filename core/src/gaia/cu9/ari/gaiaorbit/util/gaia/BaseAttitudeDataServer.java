/*
 * GaiaTools
 * Copyright (C) 2006 Gaia Data Processing and Analysis Consortium
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package gaia.cu9.ari.gaiaorbit.util.gaia;

/**
 * Common base class for all attitude data servers. This holds all common fields
 * e.g. the time origin for relative time scales (in ns) and implements
 * {@link #getAttitude(GaiaTime)} in terms of {@link #getAttitude(long)} which
 * is the same for all servers.
 * 
 * The time context and its possible switch is implemented in a thread-safe manner.
 * Derived classes should hence be likewise thread-safe.
 * 
 * @author Uwe Lammers
 * @version $Id: BaseAttitudeDataServer.java 254926 2012-10-01 15:10:38Z
 *          ulammers $
 * @param <A>
 *            type of Attitude that the server is serving
 */
public abstract class BaseAttitudeDataServer<A extends Attitude> {

    /**
     * Some scanning laws have constants or tables for interpolation that need
     * to be computed before the first use and recomputed after changing certain
     * reference values. This flag indicates that the constants or tables
     * (whatever applicable) are up to date.
     */
    protected boolean initialized = false;

    /**
     * switch to decide if attitude uncertainties and correlations should be calculated
     */
    protected boolean withUncertaintiesCorrelations = true;

    /**
     * @return Returns the initialised.
     */
    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    /**
     * @see gaia.cu1.tools.satellite.attitude.AttitudeDataServer#getAttitude(gaia.cu1.tools.time.GaiaTime)
     */
    public A getAttitude(long time) {
        return getAttitudeNative(time);
    }

    /** 
     * Evaluate the attitude in the native time system of the server
     */
    abstract protected A getAttitudeNative(long time);

}
