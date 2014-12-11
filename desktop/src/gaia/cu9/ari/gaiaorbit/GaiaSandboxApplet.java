package gaia.cu9.ari.gaiaorbit;

import com.badlogic.gdx.backends.lwjgl.LwjglApplet;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class GaiaSandboxApplet extends LwjglApplet {
    private static final long serialVersionUID = 1L;

    public GaiaSandboxApplet()
    {
	super(new GaiaSandbox(true), getConfig());
	// Configuration

    }

    private static LwjglApplicationConfiguration getConfig() {
	LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
	cfg.title = "GaiaOrbit";
	cfg.samples = 0;
	cfg.depth = 16;
	cfg.vSyncEnabled = true;
	return cfg;
    }
}
