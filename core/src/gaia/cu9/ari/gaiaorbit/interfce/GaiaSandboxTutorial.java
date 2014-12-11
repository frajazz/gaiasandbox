package gaia.cu9.ari.gaiaorbit.interfce;

import gaia.cu9.ari.gaiaorbit.interfce.TutorialWindow.LayoutType;

import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;

/**
 * Manages the tutorial windows
 * @author Toni Sagrista
 * @Deprecated The tutorial is now a script
 */
public class GaiaSandboxTutorial implements EventListener {
    private static final float w = 500;

    IGui gui;
    List<TutorialWindow> windows;
    int index;
    TutorialWindow current;

    public GaiaSandboxTutorial(IGui gui, Skin skin) {
	super();
	this.gui = gui;
	windows = new LinkedList<TutorialWindow>();
	float fontWidth = skin.getFont("ui-11").getSpaceWidth();

	/** WELCOME WINDOW **/
	TutorialWindow window_welcome = new TutorialWindow("Tutorial", skin, "header", "default");
	window_welcome.initialize("Welcome", LayoutType.L_HORIZONTAL);
	CharSequence text = TextUtils.limitWidth("Welcome to the Gaia Sandbox application. This tutorial will guide you through some of the more imoportant" +
		" aspects of this software. It will give you some tips and tricks on how the software works" +
		" and how to operate it. Enjoy!", 120, fontWidth);

	// Add content
	window_welcome.addTutorialActor(new Image(new Texture(Gdx.files.internal("img/gaiasandboxlogo.png"))));
	window_welcome.addTutorialActor(new Label(text, skin, "default"));

	/** CONTROLS WINDOW **/
	TutorialWindow window_controls = new TutorialWindow("Tutorial", skin, "header", "default");
	window_controls.initialize("Controls", LayoutType.L_VERTICAL);

	// Add content
	window_controls.addTutorialActor(new Label("Keyboard", skin, "header2"));
	window_controls.addTutorialActor(new Label("- Numpad 0-8: Change camera mode\n"
		+ "      0 - Free camera\n"
		+ "      1 - Camera follows orbit\n"
		+ "      2 - Camera follows Gaia\n"
		+ "      3 - Camera fixed with respect to Gaia\n"
		+ "      4 - Camera rotates with Gaia\n"
		+ "      5 - Camera rotates and precesses with Gaia\n"
		+ "      6 - Camera inside Gaia\n"
		+ "      7 - Scene camera\n"
		+ "      8 - Focus camera\n"
		+ "- P: Toggle simulation play/pause\n"
		+ "- I: Toggle reference axes visibility\n"
		+ "- Shift + mouse: Camera roll", skin, "default"));
	window_controls.addTutorialActor(new Label("Mouse", skin, "header2"));
	window_controls.addTutorialActor(new Label("- Left click on object: Select object as focus\n"
		+ "- Left click + drag: Pitch and yaw (FREE mode) or rotate around foucs (FOCUS mode)\n"
		+ "- Middle click + drag or wheel: Forward/backward movement\n"
		+ "- Right click + drag: Move sideways (only in FREE mode)\n"
		+ "- Shift + left click + drag: Camera roll", skin, "default"));

	/** SIMULATION TIME WINDOW **/
	TutorialWindow window_time = new TutorialWindow("Tutorial", skin, "header", "default");
	window_time.initialize("Simulation time", LayoutType.L_HORIZONTAL);

	text = TextUtils.limitWidth("You can play and pause the simulation using the PLAY/PAUSE button in the OPTIONS window to the left.\n"
		+ "You can also change the pace, which is the simulation time to real time ratio, expressed in [h/sec]. If the pace is 2.1, then "
		+ "one second of real time translates to two hours of simulation time.\n\n"
		+ "Finally, the current simulation date is given in the bottom box of the Time group.", 120, fontWidth);

	// Add content
	window_time.addTutorialActor(new Label(text, skin, "default"));
	window_time.addTutorialActor(new Image(new Texture(Gdx.files.internal("img/tutorial-time.png"))));

	/** CAMERA WINDOW **/
	TutorialWindow window_camera = new TutorialWindow("Tutorial", skin, "header", "default");
	window_camera.initialize("Camera options", LayoutType.L_HORIZONTAL);
	text = TextUtils.limitWidth("In the camera options pane on the left you can select the type of camera. This can also be "
		+ "done by using the Numpad 0-8 keys.\n\n"
		+ "There is also a list of focus objects that can be selected from the interface. When an object is selected the "
		+ "camera automatically centers it in the view and you can rotate around it or zoom in and out.\n Objects can also "
		+ "be selected by clicking on them directly in the view.\n\n"
		+ "** Hint: Try focusing on Gaia and zoom in to inspect its movement and orbit.", 150, fontWidth);

	// Add content
	window_camera.addTutorialActor(new Image(new Texture(Gdx.files.internal("img/tutorial-camera.png"))));
	window_camera.addTutorialActor(new Label(text, skin, "default"));

	/** VISIBILITY WINDOW **/
	TutorialWindow window_toggles = new TutorialWindow("Tutorial", skin, "header", "default");
	window_toggles.initialize("Visibility toggles", LayoutType.L_HORIZONTAL);
	text = TextUtils.limitWidth("Most graphical elements can be turned off and on using the visibility toggles at the bottom of the "
		+ "OPTIONS window. For example you can remove the stars from the display by clicking on the 'stars' toggle. Some "
		+ "graphical elements are a bit costly to display, such as the 'star names' or the 'Milky Way'. If you "
		+ "experience poor framerates (below 30 FPS), try disabling some of the graphical elements and see "
		+ "if this fixes the issue.\n\n"
		+ "That is all for now, you may now close this window and start exploring by yourself!", 250, fontWidth);

	// Add content
	window_toggles.addTutorialActor(new Label(text, skin, "default"));

	windows.add(window_welcome);
	windows.add(window_controls);
	windows.add(window_time);
	windows.add(window_camera);
	windows.add(window_toggles);

	int n = windows.size();
	int i = 1;
	for (TutorialWindow tw : windows) {
	    tw.addListener(this);
	    tw.setTitle(tw.getTitle() + " (" + i + "/" + n + ")");
	    tw.addButtons(i > 1, i < n, true);
	    tw.pack();
	    tw.setWidth(w);
	    i++;
	}

	index = -1;

    }

    public void displayNextWindow() {
	if (index < windows.size() - 1) {
	    if (current != null) {
		current.remove();
	    }
	    current = windows.get(++index);
	    // Center
	    current.setPosition((Gdx.graphics.getWidth() - current.getWidth()) / 2, (Gdx.graphics.getHeight() - current.getHeight()) / 2);
	    gui.getGuiStage().addActor(current);
	}
    }

    public void displayPreviousWindow() {
	if (index > 0) {
	    if (current != null) {
		current.remove();
	    }
	    current = windows.get(--index);
	    current.setPosition((Gdx.graphics.getWidth() - current.getWidth()) / 2, (Gdx.graphics.getHeight() - current.getHeight()) / 2);
	    gui.getGuiStage().addActor(current);
	}
    }

    public void close() {
	for (TutorialWindow w : windows) {
	    w.remove();
	}
	index = -1;
    }

    @Override
    public boolean handle(Event event) {
	if (event instanceof ChangeEvent) {
	    ChangeEvent ce = (ChangeEvent) event;
	    Actor actor = ce.getTarget();
	    String name = actor.getName();
	    if (actor instanceof TextButton) {
		if (name.equals("next")) {
		    // Next window
		    displayNextWindow();
		} else if (name.equals("close")) {
		    // Close tutorial
		    close();
		} else if (name.equals("prev")) {
		    // Previous window
		    displayPreviousWindow();
		}
	    } else {
		return false;
	    }
	}
	return false;
    }

    public void display() {
	displayNextWindow();
    }

}
