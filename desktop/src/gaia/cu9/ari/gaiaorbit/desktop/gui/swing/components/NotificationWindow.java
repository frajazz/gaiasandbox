package gaia.cu9.ari.gaiaorbit.desktop.gui.swing.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Date;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.border.EmptyBorder;

public class NotificationWindow extends JWindow {
    private static final int MARGIN = 10;
    public Date date;
    int posY;
    Component parent;
    NotificationWindow thisw;

    public NotificationWindow(String message, Icon icon, Component parent, int posY) {
        super();
        this.parent = parent;
        this.posY = posY;
        this.thisw = this;
        this.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel = new JPanel();
        panel.setBorder(new EmptyBorder(MARGIN, MARGIN, MARGIN, 0));
        panel.add(new JLabel(icon));
        this.add(panel, BorderLayout.WEST);

        panel = new JPanel();
        panel.setBorder(new EmptyBorder(MARGIN, MARGIN, MARGIN, MARGIN));
        panel.add(new JLabel(message));
        this.add(panel, BorderLayout.CENTER);
        this.setLocationRelativeTo(parent);

        this.setMinimumSize(new Dimension(215, 46));
        this.setOpacity(0.85f);
        this.pack();

        relocate();

        this.setVisible(true);
        this.date = new Date();

        this.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                thisw.setVisible(false);
                thisw.dispose();
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

        });

    }

    public void relocate() {
        int x = parent.getX() + parent.getWidth() - this.getWidth() - 15;
        int y = parent.getY() + parent.getHeight() - this.getHeight() - posY;
        this.setLocation(x, y);
    }

    public void moveDown(int pixels) {
        this.posY += pixels;
        this.setLocation(this.getLocation().x, this.getLocation().y + pixels);
    }

}
