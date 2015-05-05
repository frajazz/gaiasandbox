package gaia.cu9.ari.gaiaorbit.gui.swing.components;

/**
 * The utillib library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002 Alexander Maryanovsky.
 * All rights reserved.
 *
 * The utillib library is free software; you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * The utillib library is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with utillib library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JLabel;

/**
 * An extension of JLabel which looks like a link and responds appropriately
 * when clicked. Note that this class will only work with Swing 1.1.1 and later.
 * Note that because of the way this class is implemented, getText() will not
 * return correct values, user <code>getNormalText</code> instead.
 */

public class LinkLabel extends JLabel {

    class OpenUrlAction implements ActionListener {
        URI uri;

        public OpenUrlAction(URI uri) {
            this.uri = uri;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            open(uri);
        }
    }

    private static void open(URI uri) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(uri);
            } catch (IOException e) { /* TODO: error handling */
            }
        } else { /* TODO: error handling */
        }
    }

    /**
     * The normal text set by the user.
     */

    private String text;

    /**
     * Creates a new LinkLabel with the given text and link.
     */
    public LinkLabel(String displayText, String link) {
        super(displayText);
        URI uri = null;
        try {
            uri = new URI(link);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        enableEvents(MouseEvent.MOUSE_EVENT_MASK);
        this.addActionListener(new OpenUrlAction(uri));
    }

    public LinkLabel(String text) {
        this(text, text);
    }

    /**
     * Sets the text of the label.
     */

    public void setText(String text) {
        super.setText("<html><font color=\"#0000CF\"><u>" + text + "</u></font></html>"); //$NON-NLS-1$ //$NON-NLS-2$
        this.text = text;
    }

    /**
     * Returns the text set by the user.
     */

    public String getNormalText() {
        return text;
    }

    /**
     * Processes mouse events and responds to clicks.
     */

    protected void processMouseEvent(MouseEvent evt) {
        super.processMouseEvent(evt);
        if (evt.getID() == MouseEvent.MOUSE_CLICKED)
            fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, getNormalText()));
    }

    /**
     * Adds an ActionListener to the list of listeners receiving notifications
     * when the label is clicked.
     */

    public void addActionListener(ActionListener listener) {
        listenerList.add(ActionListener.class, listener);
    }

    /**
     * Removes the given ActionListener from the list of listeners receiving
     * notifications when the label is clicked.
     */

    public void removeActionListener(ActionListener listener) {
        listenerList.remove(ActionListener.class, listener);
    }

    /**
     * Fires an ActionEvent to all interested listeners.
     */

    protected void fireActionPerformed(ActionEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == ActionListener.class) {
                ActionListener listener = (ActionListener) listeners[i + 1];
                listener.actionPerformed(evt);
            }
        }
    }

}
