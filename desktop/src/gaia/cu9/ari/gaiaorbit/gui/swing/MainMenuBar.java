package gaia.cu9.ari.gaiaorbit.gui.swing;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.gui.swing.components.AboutWindow;
import gaia.cu9.ari.gaiaorbit.gui.swing.components.NotificationManager;
import gaia.cu9.ari.gaiaorbit.gui.swing.components.SystemInfoWindow;
import gaia.cu9.ari.gaiaorbit.gui.swing.display.CapabilitiesTest;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;

import java.awt.Container;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.alee.extended.image.WebImage;
import com.alee.laf.optionpane.WebOptionPane;
import com.alee.laf.text.WebTextField;

public class MainMenuBar extends JMenuBar implements IObserver {

    JMenu menu;
    Gui gui;

    public MainMenuBar(Gui thegui) {
	super();
	this.gui = thegui;
	JMenuItem menuItem;

	/** FILE MENU **/

	menu = new JMenu("File");
	menu.setMnemonic(KeyEvent.VK_F);
	menu.getAccessibleContext().setAccessibleDescription(
		"File menu");
	add(menu);

	menuItem = new JMenuItem("Save state...",
		KeyEvent.VK_S);
	menuItem.setIcon(IconManager.get("save"));
	menuItem.setAccelerator(KeyStroke.getKeyStroke(
		KeyEvent.VK_S, ActionEvent.CTRL_MASK));
	menuItem.getAccessibleContext().setAccessibleDescription(
		"Save the state of the application");
	menu.add(menuItem);

	menuItem = new JMenuItem("Load state...",
		KeyEvent.VK_L);
	menuItem.setIcon(IconManager.get("load"));
	menuItem.setAccelerator(KeyStroke.getKeyStroke(
		KeyEvent.VK_L, ActionEvent.CTRL_MASK));
	menuItem.getAccessibleContext().setAccessibleDescription(
		"Load an application state");
	menu.add(menuItem);

	menu.addSeparator();

	menuItem = new JMenuItem("Quit",
		KeyEvent.VK_Q);
	menuItem.setIcon(IconManager.get("exit"));
	menuItem.setAccelerator(KeyStroke.getKeyStroke(
		KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
	menuItem.getAccessibleContext().setAccessibleDescription(
		"Quit application");

	menuItem.addActionListener(new ExitApp(menu.getParent()));
	menu.add(menuItem);

	/** TIME MENU **/
	menu = new JMenu("Time");
	menu.setMnemonic(KeyEvent.VK_T);
	menu.getAccessibleContext().setAccessibleDescription(
		"Time menu");
	add(menu);

	menuItem = new JMenuItem("Play/pause",
		KeyEvent.VK_P);
	menuItem.setIcon(IconManager.get("play"));
	menuItem.setAccelerator(KeyStroke.getKeyStroke(
		KeyEvent.VK_P, ActionEvent.CTRL_MASK));
	menuItem.getAccessibleContext().setAccessibleDescription(
		"Play/pause the time");
	menuItem.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		EventManager.getInstance().post(Events.TOGGLE_TIME_CMD);
	    }
	});
	menu.add(menuItem);

	menuItem = new JMenuItem("x1 Real time",
		KeyEvent.VK_R);
	menuItem.setIcon(IconManager.get("time"));
	menuItem.setAccelerator(KeyStroke.getKeyStroke(
		KeyEvent.VK_R, ActionEvent.CTRL_MASK));
	menuItem.getAccessibleContext().setAccessibleDescription(
		"Set the time pace to real time");
	menuItem.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		EventManager.getInstance().post(Events.PACE_CHANGE_CMD, 0.00028f);
	    }
	});
	menu.add(menuItem);

	menuItem = new JMenuItem("x2 Real time",
		KeyEvent.VK_T);
	menuItem.setIcon(IconManager.get("fast"));
	menuItem.setAccelerator(KeyStroke.getKeyStroke(
		KeyEvent.VK_T, ActionEvent.CTRL_MASK));
	menuItem.getAccessibleContext().setAccessibleDescription(
		"Set the time pace twice the real time");
	menuItem.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		EventManager.getInstance().post(Events.PACE_CHANGE_CMD, 0.00028f * 2f);
	    }
	});
	menu.add(menuItem);

	menu.addSeparator();

	/** OPTIONS MENU **/
	menu = new JMenu("Options");
	menu.setMnemonic(KeyEvent.VK_O);
	menu.getAccessibleContext().setAccessibleDescription(
		"Options menu");
	add(menu);

	menuItem = new JMenuItem("Find object...",
		KeyEvent.VK_F);
	menuItem.setIcon(IconManager.get("search"));
	menuItem.setAccelerator(KeyStroke.getKeyStroke(
		KeyEvent.VK_F, ActionEvent.CTRL_MASK));
	menuItem.getAccessibleContext().setAccessibleDescription(
		"Find object by name");
	menuItem.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		final JDialog dialog = new JDialog((JFrame) SwingUtilities.getRoot(menu), "Search...", true);
		final WebTextField searchInput = new WebTextField(20);
		searchInput.setMargin(0, 0, 0, 2);
		searchInput.setInputPrompt("Search...");
		searchInput.setInputPromptFont(searchInput.getFont().deriveFont(Font.ITALIC));
		searchInput.setTrailingComponent(new WebImage(IconManager.get("search")));
		searchInput.getDocument().addDocumentListener(new DocumentListener() {
		    public void changedUpdate(DocumentEvent e) {
			warn();
		    }

		    public void removeUpdate(DocumentEvent e) {
			warn();
		    }

		    public void insertUpdate(DocumentEvent e) {
			warn();
		    }

		    public void warn() {
			String text = searchInput.getText();
			if (gui.stringNode.containsKey(text.toLowerCase())) {
			    SceneGraphNode node = gui.stringNode.get(text.toLowerCase());
			    if (node instanceof CelestialBody) {
				EventManager.getInstance().post(Events.FOCUS_CHANGE_CMD, node, false);
				gui.selectNodeInTree(node);
			    }
			}
		    }
		});
		searchInput.addKeyListener(new KeyListener() {

		    @Override
		    public void keyTyped(KeyEvent e) {
			// TODO Auto-generated method stub

		    }

		    @Override
		    public void keyPressed(KeyEvent e) {
			// TODO Auto-generated method stub

		    }

		    @Override
		    public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			    dialog.dispose();
			}
		    }
		});
		dialog.add(searchInput);
		dialog.pack();
		dialog.setLocationByPlatform(true);
		dialog.setLocationRelativeTo((JFrame) SwingUtilities.getRoot(menu));

		dialog.setVisible(true);

	    }
	});
	menu.add(menuItem);

	menu.addSeparator();

	final JCheckBoxMenuItem cbMenuItem = new JCheckBoxMenuItem("Notification messages");
	//	cbMenuItem.setIcon(IconManager.get("light"));
	cbMenuItem.setAccelerator(KeyStroke.getKeyStroke(
		KeyEvent.VK_N, ActionEvent.CTRL_MASK));
	cbMenuItem.getAccessibleContext().setAccessibleDescription(
		"Enable/disable notification messages");
	cbMenuItem.setSelected(true);
	cbMenuItem.addChangeListener(new ChangeListener() {
	    @Override
	    public void stateChanged(ChangeEvent e) {
		NotificationManager.setEnabled(cbMenuItem.isSelected());
	    }
	});
	menu.add(cbMenuItem);

	menuItem = new JMenuItem("Preferences...",
		KeyEvent.VK_O);
	menuItem.setIcon(IconManager.get("preferences"));
	menuItem.setAccelerator(KeyStroke.getKeyStroke(
		KeyEvent.VK_O, ActionEvent.CTRL_MASK));
	menuItem.getAccessibleContext().setAccessibleDescription(
		"Open the preferences dialog");
	menu.add(menuItem);

	/** GRAPHICS MENU **/
	menu = new JMenu("Graphics");
	menu.setMnemonic(KeyEvent.VK_G);
	menu.getAccessibleContext().setAccessibleDescription(
		"Graphics menu");
	add(menu);

	menuItem = new JMenuItem("Fullscreen",
		KeyEvent.VK_F11);
	menuItem.setIcon(IconManager.get("fullscreen"));
	menuItem.setAccelerator(KeyStroke.getKeyStroke(
		KeyEvent.VK_F11, 0));
	menuItem.getAccessibleContext().setAccessibleDescription(
		"Fullscreen mode");
	menuItem.setEnabled(false);
	menuItem.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		EventManager.getInstance().post(Events.FULLSCREEN_CMD);
	    }
	});
	menu.add(menuItem);

	menu.addSeparator();

	menuItem = new JMenuItem("Take screenshot",
		KeyEvent.VK_F5);
	menuItem.setIcon(IconManager.get("graphics"));
	menuItem.setAccelerator(KeyStroke.getKeyStroke(
		KeyEvent.VK_F5, 0));
	menuItem.getAccessibleContext().setAccessibleDescription(
		"Take a screenshot and save it in the screenshots folder (see preferences)");
	menuItem.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		EventManager.getInstance().post(Events.SCREENSHOT_CMD, GlobalConf.screenshot.SCREENSHOT_WIDTH, GlobalConf.screenshot.SCREENSHOT_HEIGHT, GlobalConf.screenshot.SCREENSHOT_FOLDER);
	    }
	});
	menu.add(menuItem);

	/** HELP MENU **/
	menu = new JMenu("Help");
	menu.setMnemonic(KeyEvent.VK_H);
	menu.getAccessibleContext().setAccessibleDescription(
		"Help menu");
	add(menu);

	menuItem = new JMenuItem("Help...");
	menuItem.setIcon(IconManager.get("help"));
	menuItem.getAccessibleContext().setAccessibleDescription(
		"Help information");
	menuItem.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {

	    }
	});
	menu.add(menuItem);

	menu.addSeparator();

	menuItem = new JMenuItem("System info...");
	menuItem.setIcon(IconManager.get("info"));
	menuItem.getAccessibleContext().setAccessibleDescription(
		"System information");
	menuItem.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		SystemInfoWindow siw = new SystemInfoWindow();
		siw.pack();
		siw.setVisible(true);
	    }
	});
	menu.add(menuItem);

	menuItem = new JMenuItem("Graphics device info...");
	menuItem.setIcon(IconManager.get("info"));
	menuItem.getAccessibleContext().setAccessibleDescription(
		"Graphics device capabilities information");
	menuItem.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		GraphicsEnvironment ge =
			GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] devices = ge.getScreenDevices();
		for (int i = 0; i < devices.length; i++) {
		    CapabilitiesTest tst = new CapabilitiesTest(devices[i]);
		    tst.pack();
		    tst.setVisible(true);
		}
	    }
	});
	menu.add(menuItem);

	menu.addSeparator();

	menuItem = new JMenuItem("About...");
	menuItem.setIcon(IconManager.get("home"));
	menuItem.getAccessibleContext().setAccessibleDescription(
		"About the program");
	menuItem.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		AboutWindow aw = new AboutWindow();
		aw.pack();
		aw.setVisible(true);
	    }
	});
	menu.add(menuItem);

	EventManager.getInstance().subscribe(this, Events.SCREENSHOT_INFO);
    }

    // Exit app
    static class ExitApp implements ActionListener
    {
	Container frame;

	public ExitApp(Container frame) {
	    this.frame = frame;
	}

	public void actionPerformed(ActionEvent e)
	{
	    do
		frame = frame.getParent();
	    while (!(frame instanceof JFrame));
	    ((JFrame) frame).dispose();
	}
    }

    @Override
    public void notify(Events event, Object... data) {
	switch (event) {
	case SCREENSHOT_INFO:
	    WebOptionPane.showMessageDialog((JFrame) SwingUtilities.getRoot(menu), "Screenshot saved to " + (String) data[0], "Screen captured", WebOptionPane.INFORMATION_MESSAGE);
	    break;
	}

    }

}
