package gaia.cu9.ari.gaiaorbit.gui.swing.components;

import gaia.cu9.ari.gaiaorbit.gui.swing.IconManager;

import java.awt.GraphicsDevice;
import java.awt.GraphicsDevice.WindowTranslucency;
import java.awt.GraphicsEnvironment;
import java.awt.event.ComponentEvent;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.JFrame;

public class NotificationManager implements Runnable {
    private static JFrame parent;
    public static NotificationManager instance;

    private int TIMEOUT_MS = 3500;
    private int MAX_MESSAGES = 8;
    private int MARGIN = 15;
    private int SPACING = 5;
    private int posY = SPACING;
    private boolean running = false;
    private boolean translucent;
    private Queue<NotificationWindow> messages;

    public static void initialize(JFrame parent) {
	NotificationManager.parent = parent;
	instance = new NotificationManager();
    }

    public static void stop() {
	if (instance != null) {
	    instance.running = false;
	    instance.removeWindows();
	}
    }

    public static void parentResized(ComponentEvent e) {
	if (instance != null) {
	    instance.relocate();
	}
    }

    public static void setEnabled(boolean enabled) {
	if (enabled) {
	    if (instance == null) {
		initialize(parent);
	    }
	} else {
	    stop();
	    instance = null;
	}
    }

    public void relocate() {
	for (NotificationWindow nw : messages) {
	    nw.relocate();
	}
    }

    private NotificationManager() {
	super();
	this.running = true;
	messages = new LinkedList<NotificationWindow>();

	// Determine what the default GraphicsDevice can support.
	GraphicsEnvironment ge =
		GraphicsEnvironment.getLocalGraphicsEnvironment();
	GraphicsDevice gd = ge.getDefaultScreenDevice();

	translucent =
		gd.isWindowTranslucencySupported(WindowTranslucency.TRANSLUCENT);

	new Thread(this).start();
    }

    public NotificationManager(int tIMEOUT_MS, int mARGIN, int sPACING) {
	this();
	TIMEOUT_MS = tIMEOUT_MS;
	MARGIN = mARGIN;
	SPACING = sPACING;
    }

    public static void addMessage(String message) {
	if (instance != null)
	    instance.newMessage(message);
    }

    public void newMessage(String message) {
	if (messages.size() <= MAX_MESSAGES) {
	    NotificationWindow w = new NotificationWindow(message, IconManager.get("info-round"), parent, posY);
	    posY += MARGIN + w.getHeight();
	    messages.add(w);
	}
    }

    public void removeWindows() {
	if (messages != null) {
	    for (NotificationWindow nw : messages) {
		nw.setVisible(false);
		nw.dispose();
	    }
	    messages.clear();
	}
    }

    public void setTimeout(int timeoutMs) {
	this.TIMEOUT_MS = timeoutMs;
    }

    public void setMargin(int marginPx) {
	this.MARGIN = marginPx;
    }

    @Override
    public void run() {
	while (running) {
	    long now = new Date().getTime();
	    // Check notifications
	    if (!messages.isEmpty()) {
		NotificationWindow w = messages.peek();
		int removedSpace = 0;
		while (w != null && now - w.date.getTime() >= TIMEOUT_MS) {
		    // Remove
		    NotificationWindow nw = messages.poll();
		    posY -= (MARGIN + nw.getHeight());
		    removedSpace += MARGIN + nw.getHeight();
		    nw.setVisible(false);
		    nw.dispose();

		    w = messages.peek();
		}
		if (removedSpace > 0)
		    for (NotificationWindow nw : messages) {
			nw.moveDown(removedSpace);
		    }
	    }

	    try {
		Thread.sleep(500);
	    } catch (InterruptedException exc) {
	    }
	}
    }

}
