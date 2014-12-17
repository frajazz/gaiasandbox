package gaia.cu9.ari.gaiaorbit.interfce;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

/**
 * Contains the key mappings and the actions. This should be persisted somehow in the future.
 * @author Toni Sagrista
 *
 */
public class KeyMappings {
    public Map<TreeSet<Integer>, ProgramAction> mappings;

    public static KeyMappings instance;

    public static void initialize() {
	if (instance == null) {
	    instance = new KeyMappings();
	}
    }

    /**
     * Creates a key mappings instance.
     */
    public KeyMappings() {
	mappings = new HashMap<TreeSet<Integer>, ProgramAction>();

	// For now this will do
	initDefault();
    }

    public void addMapping(ProgramAction action, int... keyCodes) {
	TreeSet<Integer> keys = new TreeSet<Integer>();
	for (int key : keyCodes) {
	    keys.add(key);
	}
	mappings.put(keys, action);
    }

    /**
     * Initializes the default keyboard mappings. In the future these
     * should be read from a configuration file.
     */
    public void initDefault() {

	// ESCAPE -> Exit
	addMapping(new ProgramAction("Exit app", new Runnable() {
	    @Override
	    public void run() {
		if (GlobalConf.OPENGL_GUI) {
		    Gdx.app.exit();
		} else {
		    EventManager.getInstance().post(Events.FULLSCREEN_CMD, false);
		}
	    }
	}), Keys.ESCAPE);

	// O -> Toggle orbits
	addMapping(new ProgramAction("Toggle orbits", new Runnable() {
	    @Override
	    public void run() {
		EventManager.getInstance().post(Events.TOGGLE_VISIBILITY_CMD, "Orbits", false);
	    }
	}), Keys.O);

	// P -> Toggle orbits
	addMapping(new ProgramAction("Toggle planets", new Runnable() {
	    @Override
	    public void run() {
		EventManager.getInstance().post(Events.TOGGLE_VISIBILITY_CMD, "Planets", false);
	    }
	}), Keys.P);

	// M -> Toggle moons
	addMapping(new ProgramAction("Toggle moons", new Runnable() {
	    @Override
	    public void run() {
		EventManager.getInstance().post(Events.TOGGLE_VISIBILITY_CMD, "Moons", false);
	    }
	}), Keys.M);

	// S -> Toggle stars
	addMapping(new ProgramAction("Toggle stars", new Runnable() {
	    @Override
	    public void run() {
		EventManager.getInstance().post(Events.TOGGLE_VISIBILITY_CMD, "Stars", false);
	    }
	}), Keys.S);

	// T -> Toggle satellites
	addMapping(new ProgramAction("Toggle satellites", new Runnable() {
	    @Override
	    public void run() {
		EventManager.getInstance().post(Events.TOGGLE_VISIBILITY_CMD, "Satellites", false);
	    }
	}), Keys.T);

	// L -> Toggle labels
	addMapping(new ProgramAction("Toggle labels", new Runnable() {
	    @Override
	    public void run() {
		EventManager.getInstance().post(Events.TOGGLE_VISIBILITY_CMD, "Labels", false);
	    }
	}), Keys.L);

	// C -> Toggle constellations
	addMapping(new ProgramAction("Toggle constellations", new Runnable() {
	    @Override
	    public void run() {
		EventManager.getInstance().post(Events.TOGGLE_VISIBILITY_CMD, "Constellations", false);
	    }
	}), Keys.C);

	// B -> Toggle boundaries
	addMapping(new ProgramAction("Toggle constellation boundaries", new Runnable() {
	    @Override
	    public void run() {
		EventManager.getInstance().post(Events.TOGGLE_VISIBILITY_CMD, "Boundaries", false);
	    }
	}), Keys.B);

	// Q -> Toggle equatorial
	addMapping(new ProgramAction("Toggle equatorial grid", new Runnable() {
	    @Override
	    public void run() {
		EventManager.getInstance().post(Events.TOGGLE_VISIBILITY_CMD, "Equatorial grid", false);
	    }
	}), Keys.Q);

	// E -> Toggle ecliptic
	addMapping(new ProgramAction("Toggle ecliptic grid", new Runnable() {
	    @Override
	    public void run() {
		EventManager.getInstance().post(Events.TOGGLE_VISIBILITY_CMD, "Ecliptic grid", false);
	    }
	}), Keys.E);

	// G -> Toggle galactic
	addMapping(new ProgramAction("Toggle galactic grid", new Runnable() {
	    @Override
	    public void run() {
		EventManager.getInstance().post(Events.TOGGLE_VISIBILITY_CMD, "Galactic grid", false);
	    }
	}), Keys.G);

	// Left bracket -> divide speed
	addMapping(new ProgramAction("Divide time pace", new Runnable() {
	    @Override
	    public void run() {
		EventManager.getInstance().post(Events.PACE_DIVIDE_CMD);
	    }
	}), Keys.LEFT_BRACKET);

	// Right bracket -> double speed
	addMapping(new ProgramAction("Double time pace", new Runnable() {
	    @Override
	    public void run() {
		EventManager.getInstance().post(Events.PACE_DOUBLE_CMD);
	    }
	}), Keys.RIGHT_BRACKET);

	// SPACE -> toggle time
	addMapping(new ProgramAction("Pause/resume time", new Runnable() {
	    @Override
	    public void run() {
		EventManager.getInstance().post(Events.SIMU_TIME_TOGGLED, (Object) null);
	    }
	}), Keys.SPACE);

	// Plus -> increase limit magnitude
	addMapping(new ProgramAction("Increase limit mag", new Runnable() {
	    @Override
	    public void run() {
		EventManager.getInstance().post(Events.LIMIT_MAG_CMD, GlobalConf.instance.LIMIT_MAG_RUNTIME + 0.1f);
	    }
	}), Keys.PLUS);

	// Minus -> decrease limit magnitude
	addMapping(new ProgramAction("Decrease limit mag", new Runnable() {
	    @Override
	    public void run() {
		EventManager.getInstance().post(Events.LIMIT_MAG_CMD, GlobalConf.instance.LIMIT_MAG_RUNTIME - 0.1f);
	    }
	}), Keys.MINUS);

	// Star -> reset limit mag
	addMapping(new ProgramAction("Reset limit mag", new Runnable() {
	    @Override
	    public void run() {
		EventManager.getInstance().post(Events.LIMIT_MAG_CMD, GlobalConf.instance.LIMIT_MAG_LOAD);
	    }
	}), Keys.STAR);

	// F11 -> fullscreen
	addMapping(new ProgramAction("Toggle fullscreen", new Runnable() {
	    @Override
	    public void run() {
		EventManager.getInstance().post(Events.FULLSCREEN_CMD);
	    }
	}), Keys.F11);

	// F5 -> take screenshot
	addMapping(new ProgramAction("Take screenshot", new Runnable() {
	    @Override
	    public void run() {
		EventManager.getInstance().post(Events.SCREENSHOT_CMD, GlobalConf.instance.SCREENSHOT_WIDTH, GlobalConf.instance.SCREENSHOT_HEIGHT, GlobalConf.instance.SCREENSHOT_FOLDER);
	    }
	}), Keys.F5);

	// U -> toggle UI collapse/expand
	addMapping(new ProgramAction("UI toggle", new Runnable() {
	    @Override
	    public void run() {
		EventManager.getInstance().post(Events.GUI_FOLD_CMD);
	    }
	}), Keys.U);

	// Camera modes (NUMERIC KEYPAD)
	for (int i = 144; i <= 153; i++) {
	    // Camera mode
	    int m = i - 144;
	    final CameraMode mode = CameraMode.getMode(m);
	    if (mode != null) {
		addMapping(new ProgramAction(mode.name(), new Runnable() {
		    @Override
		    public void run() {
			EventManager.getInstance().post(Events.CAMERA_MODE_CMD, mode);
		    }
		}), i);
	    }
	}

	// CTRL + D -> Toggle debug information
	addMapping(new ProgramAction("Toggle debug information", new Runnable() {
	    @Override
	    public void run() {
		EventManager.getInstance().post(Events.SHOW_DEBUG_CMD);
	    }
	}), Keys.CONTROL_LEFT, Keys.D);

	// CTRL + F -> Search dialog
	addMapping(new ProgramAction("Search dialogue", new Runnable() {
	    @Override
	    public void run() {
		EventManager.getInstance().post(Events.SHOW_SEARCH_ACTION);
	    }
	}), Keys.CONTROL_LEFT, Keys.F);

    }

    /**
     * A simple program action.
     * @author Toni Sagrista
     *
     */
    public class ProgramAction implements Runnable {
	public final String actionName;
	private final Runnable action;

	public ProgramAction(String actionName, Runnable action) {
	    this.actionName = actionName;
	    this.action = action;
	}

	@Override
	public void run() {
	    action.run();
	}

    }
}
