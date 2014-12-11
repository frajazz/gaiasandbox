package gaia.cu9.ari.gaiaorbit.gui.swing.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JProgressBar;

public class MemoryBar extends JProgressBar implements ActionListener {

    private long totalMemory;
    private final Runtime runtime;
    private final long meg = 1000000;

    public MemoryBar() {
	super(0, 0);
	setStringPainted(true);
	runtime = Runtime.getRuntime();
	showMemoryStatus();
    }

    private void showMemoryStatus() {
	totalMemory = runtime.totalMemory() / meg;
	final long free = runtime.freeMemory() / meg;
	final long current = totalMemory - free;
	final String value = current + "M of " + totalMemory + "M";
	setString(value);
	setMaximum((int) totalMemory);
	setValue((int) current);
    }

    public void actionPerformed(ActionEvent e)
    {
	showMemoryStatus();
    }

}