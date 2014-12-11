package gaia.cu9.ari.gaiaorbit.gui.swing.jsplash;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;

public class GuiUtility
{
    public GuiUtility() {
    }

    public static void centerOnScreen(Window w)
    {
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	Dimension windowSize = w.getPreferredSize();
	w.setLocation(screenSize.width / 2 - windowSize.width / 2,
		screenSize.height / 2 - windowSize.height / 2);
    }
}
