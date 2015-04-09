package gaia.cu9.ari.gaiaorbit.data.stars;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.Particle;
import gaia.cu9.ari.gaiaorbit.scenegraph.Star;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.units.Position;
import gaia.cu9.ari.gaiaorbit.util.units.Position.PositionType;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.StarTableFactory;

public class STILCatalogLoader extends AbstractCatalogLoader implements ICatalogLoader {

    @Override
    public void initialize(Properties p) {
	super.initialize(p);
    }

    @Override
    public List<? extends CelestialBody> loadCatalog() throws FileNotFoundException {
	long starid = 0;

	List<CelestialBody> result = new ArrayList<CelestialBody>();
	StarTableFactory factory = new StarTableFactory();
	EventManager.instance.post(Events.POST_NOTIFICATION, this.getClass().getSimpleName(), I18n.bundle.format("notif.limitmag", GlobalConf.data.LIMIT_MAG_LOAD));

	try {
	    Map<String, ColumnInfo> ucds = new HashMap<String, ColumnInfo>();
	    Map<String, Integer> ucdsi = new HashMap<String, Integer>();
	    StarTable table = factory.makeStarTable(file);
	    int count = table.getColumnCount();
	    ColumnInfo[] colInfo = new ColumnInfo[count];
	    for (int i = 0; i < count; i++) {
		colInfo[i] = table.getColumnInfo(i);
		ucds.put(colInfo[i].getUCD(), colInfo[i]);
		ucdsi.put(colInfo[i].getUCD(), i);
	    }

	    /** POSITION **/

	    ColumnInfo ac = null, bc = null, cc = null;
	    int ai, bi, ci;
	    PositionType type = null;

	    // Check positions
	    if (ucds.containsKey("pos.eq.ra")) {
		// RA_DEC_DIST or RA_DEC_PLX
		ac = ucds.get("pos.eq.ra");
		bc = ucds.get("pos.eq.dec");
		cc = ucds.containsKey("pos.parallax.trig") ? ucds.get("pos.parallax.trig") : ucds.get("pos.distance");
		type = ucds.containsKey("pos.parallax.trig") ? PositionType.RA_DEC_PLX : PositionType.RA_DEC_DIST;
	    }

	    if (type == null && ucds.containsKey("pos.galactic.lon")) {
		// GLON_GLAT_DIST or GLON_GLAT_PLX
		ac = ucds.get("pos.galactic.lon");
		bc = ucds.get("pos.galactic.lat");
		cc = ucds.containsKey("pos.parallax.trig") ? ucds.get("pos.parallax.trig") : ucds.get("pos.distance");
		type = ucds.containsKey("pos.parallax.trig") ? PositionType.GLON_GLAT_PLX : PositionType.GLON_GLAT_DIST;
	    }

	    if (type == null && ucds.containsKey("pos.eq.x")) {
		// Equatorial XYZ
		ac = ucds.get("pos.eq.x");
		bc = ucds.get("pos.eq.y");
		cc = ucds.get("pos.eq.z");
		type = PositionType.XYZ_EQUATORIAL;
	    }

	    if (type == null && ucds.containsKey("pos.galactic.x")) {
		// Galactic XYZ
		ac = ucds.get("pos.galactic.x");
		bc = ucds.get("pos.galactic.y");
		cc = ucds.get("pos.galactic.z");
		type = PositionType.XYZ_GALACTIC;
	    }

	    if (type == null) {
		throw new RuntimeException("Could not find suitable position candidate columns");
	    } else {
		ai = ucdsi.get(ac.getUCD());
		bi = ucdsi.get(bc.getUCD());
		ci = ucdsi.get(cc.getUCD());
	    }

	    /** APP MAGNITUDE **/
	    ColumnInfo magc = null;
	    int magi;
	    if (ucds.containsKey("phot.mag;em.opt.V")) {
		magc = ucds.get("phot.mag;em.opt.V");
	    } else if (ucds.containsKey("phot.mag;em.opt.B")) {
		magc = ucds.get("phot.mag;em.opt.B");
	    } else if (ucds.containsKey("phot.mag;em.opt.I")) {
		magc = ucds.get("phot.mag;em.opt.I");
	    } else if (ucds.containsKey("phot.mag;em.opt.R")) {
		magc = ucds.get("phot.mag;em.opt.R");
	    } else {
		throw new RuntimeException("Could not find suitable magnitude candidate column");
	    }
	    magi = ucdsi.get(magc.getUCD());

	    /** ABS MAGNITUDE **/
	    ColumnInfo abmagc = null;
	    int abmagi;
	    if (ucds.containsKey("phys.magAbs;em.opt.V")) {
		abmagc = ucds.get("phys.magAbs;em.opt.V");
	    } else if (ucds.containsKey("phys.magAbs;em.opt.B")) {
		abmagc = ucds.get("phys.magAbs;em.opt.B");
	    } else if (ucds.containsKey("phys.magAbs;em.opt.I")) {
		abmagc = ucds.get("phys.magAbs;em.opt.I");
	    } else if (ucds.containsKey("phys.magAbs;em.opt.R")) {
		abmagc = ucds.get("phys.magAbs;em.opt.R");
	    } else {
		abmagc = magc;
	    }
	    abmagi = ucdsi.get(abmagc.getUCD());

	    /** COLOR **/
	    ColumnInfo colc = null;
	    int coli = -1;
	    if (ucds.containsKey("phot.color;em.opt.B;em.opt.V")) {
		// B-V
		colc = ucds.get("phot.color;em.opt.B;em.opt.V");
	    }
	    if (colc != null) {
		coli = ucdsi.get(colc.getUCD());
	    }

	    /** NAME **/
	    ColumnInfo idstrc = null;
	    ColumnInfo idc = null;
	    int idstri = 0, idi = 0;
	    if (ucds.containsKey("meta.id")) {
		idstrc = ucds.get("meta.id");
		idstri = ucdsi.get("meta.id");
	    }
	    if (ucds.containsKey("meta.id;meta.main")) {
		idc = ucds.get("meta.id;meta.main");
		idi = ucdsi.get("meta.id;meta.main");
	    }

	    long rowcount = table.getRowCount();
	    for (long i = 0; i < rowcount; i++) {
		Object[] row = table.getRow(i);
		double a = ((Number) row[ai]).doubleValue();
		double b = ((Number) row[bi]).doubleValue();
		double c = ((Number) row[ci]).doubleValue();
		Position p = new Position(a, ac.getUnitString(), b, bc.getUnitString(), c, cc.getUnitString(), type);
		double dist = p.gsposition.len();
		p.gsposition.scl(Constants.PC_TO_U);

		float mag = ((Number) row[magi]).floatValue();
		float absmag = ((Number) row[abmagi]).floatValue();

		float color = coli > 0 ? ((Number) row[coli]).floatValue() : 0.656f;

		starid++;

		String idstr = (idstrc == null || !idstrc.getContentClass().isAssignableFrom(String.class)) ? "star_" + starid : (String) row[idstri];
		Long id = (idc == null || !idc.getContentClass().isAssignableFrom(Number.class)) ? starid : ((Number) row[idi]).longValue();

		CelestialBody s = null;
		if (dist > 2.2e5) {
		    // Galaxy
		    s = new Particle(p.gsposition, mag, absmag, color, idstr, id);
		} else {
		    s = new Star(p.gsposition, mag, absmag, color, idstr, id);
		}
		s.initialize();
		result.add(s);
	    }

	} catch (Exception e) {
	    EventManager.instance.post(Events.JAVA_EXCEPTION, e);
	}

	EventManager.instance.post(Events.POST_NOTIFICATION, this.getClass().getSimpleName(), I18n.bundle.format("notif.catalog.init", result.size()));
	return result;
    }
}
