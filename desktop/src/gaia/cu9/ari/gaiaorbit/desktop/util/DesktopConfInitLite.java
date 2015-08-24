package gaia.cu9.ari.gaiaorbit.desktop.util;

import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.util.ConfInit;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.DataConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.PerformanceConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.PostprocessConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.ProgramConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.ProgramConf.StereoProfile;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.RuntimeConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.SceneConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.VersionConf;

public class DesktopConfInitLite extends ConfInit {

    @Override
    public void initGlobalConf() throws Exception {
        VersionConf vc = new VersionConf();
        vc.initialize("0.706b");

        PerformanceConf pc = new PerformanceConf();
        pc.initialize(false, 1);

        PostprocessConf ppc = new PostprocessConf();
        ppc.initialize(4, 0, 0, false);

        RuntimeConf rc = new RuntimeConf();
        rc.initialize(false, false, true, false, true, false, 20, true);

        DataConf dc = new DataConf();
        dc.initialize(true, 20f);

        ProgramConf prc = new ProgramConf();
        prc.initialize(false, true, "dark", "en-GB", false, StereoProfile.CROSSEYE);

        ComponentType[] cts = ComponentType.values();
        boolean[] VISIBILITY = new boolean[cts.length];
        VISIBILITY[ComponentType.Stars.ordinal()] = true;
        VISIBILITY[ComponentType.Atmospheres.ordinal()] = false;
        VISIBILITY[ComponentType.Planets.ordinal()] = true;
        VISIBILITY[ComponentType.Moons.ordinal()] = false;
        VISIBILITY[ComponentType.Orbits.ordinal()] = false;
        VISIBILITY[ComponentType.Satellites.ordinal()] = true;
        VISIBILITY[ComponentType.MilkyWay.ordinal()] = true;
        VISIBILITY[ComponentType.Asteroids.ordinal()] = false;
        VISIBILITY[ComponentType.Galaxies.ordinal()] = false;
        VISIBILITY[ComponentType.Labels.ordinal()] = true;
        VISIBILITY[ComponentType.Others.ordinal()] = true;

        SceneConf sc = new SceneConf();
        sc.initialize(2000, 2f, 0f, 50, 2.1f, 1866f, 2286f, 13, true, 7.0f, VISIBILITY, 2, 0, 0f, 2e-8f, 0f, 0.05f, 1f);

        GlobalConf.initialize(vc, prc, sc, dc, rc, ppc, pc);
    }

}
