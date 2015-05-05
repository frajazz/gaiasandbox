package gaia.cu9.ari.gaiaorbit.gui.swing.jsplash;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

public final class JSplashLabel
        extends JLabel
{
    private static final long serialVersionUID = -774117609679903588L;
    private String m_text = null;

    private Font m_font = null;

    private Color m_color = null;

    public JSplashLabel(URL url, String s, Font f, Color c)
    {
        ImageIcon icon = new ImageIcon(url);
        if (icon.getImageLoadStatus() != 8) {
            System.err.println("Cannot load splash screen: " + url);
            setText("Cannot load splash screen: " + url);
        } else {
            setIcon(icon);
            m_text = s;
            m_font = f;
            m_color = c;

            if (m_font != null) {
                setFont(m_font);
            }
        }
    }

    public JSplashLabel(URL url, String s)
    {
        ImageIcon icon = new ImageIcon(url);
        if (icon.getImageLoadStatus() != 8) {
            System.err.println("Cannot load splash screen: " + url);
            setText("Cannot load splash screen: " + url);
        } else {
            setIcon(icon);
            m_text = s;
            if (m_font != null) {
                setFont(m_font);
            }
        }
    }

    public void paint(Graphics g)
    {
        super.paint(g);

        if (m_text != null) {
            if (m_color != null) {
                g.setColor(m_color);
            }

            FontMetrics fm = g.getFontMetrics();
            int width = fm.stringWidth(m_text) + 20;
            int height = fm.getHeight();

            g.drawString(m_text, getWidth() - width, getHeight() - height);
        }
    }
}

/* Location:           /home/tsagrista/Workspaces/workspace/GaiaOrbit-desktop-branch1/libs/JSplashScreen.jar
 * Qualified Name:     com.thehowtotutorial.splashscreen.JSplashLabel
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.7.0.1
 */
