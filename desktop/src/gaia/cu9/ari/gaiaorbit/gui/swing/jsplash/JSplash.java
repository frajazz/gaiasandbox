//Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
//Jad home page: http://www.geocities.com/kpdus/jad.html
//Decompiler options: packimports(3) 
//Source File Name:   JSplash.java
package gaia.cu9.ari.gaiaorbit.gui.swing.jsplash;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JWindow;

//Referenced classes of package com.thehowtotutorial.splashscreen:
//         JSplashLabel, GuiUtility

public final class JSplash extends JWindow
{

    public JSplash(URL url, boolean progress, boolean messages, boolean percent, String versionString, Font versionStringFont, Color versionStringColor,
	    Color progressbarColor)
    {
	super();
	init(url, progress, messages, percent, versionString, versionStringFont, versionStringColor, progressbarColor);
    }

    public JSplash(JFrame parent, URL url, boolean progress, boolean messages, boolean percent, String versionString, Font versionStringFont, Color versionStringColor,
	    Color progressbarColor)
    {
	super(parent);
	init(url, progress, messages, percent, versionString, versionStringFont, versionStringColor, progressbarColor);
    }

    private void init(URL url, boolean progress, boolean messages, boolean percent, String versionString, Font versionStringFont, Color versionStringColor,
	    Color progressbarColor) {
	m_progress = new JProgressBar();
	m_progressBar = false;
	m_progressBarMessages = false;
	m_progressBarPercent = false;

	m_progressBar = progress;
	m_progressBarMessages = messages;
	m_progressBarPercent = percent;

	m_progress.setForeground(progressbarColor);
	getContentPane().setLayout(new BorderLayout());
	((JComponent) getContentPane()).setBorder(BorderFactory.createLineBorder(Color.BLACK));

	JSplashLabel label = new JSplashLabel(url, versionString, versionStringFont, versionStringColor);
	if (m_progressBar)
	{
	    if (m_progressBarMessages || m_progressBarPercent)
		m_progress.setStringPainted(true);
	    else
		m_progress.setStringPainted(false);
	    if (m_progressBarMessages && !m_progressBarPercent)
		m_progress.setString("");
	    m_progress.setMaximum(100);
	    m_progress.setMinimum(0);
	    m_progress.setValue(0);
	}
	getContentPane().add(label, "Center");
	if (m_progressBar)
	    getContentPane().add(m_progress, "South");
	pack();
	GuiUtility.centerOnScreen(this);
	setVisible(false);
    }

    public void splashOn()
    {
	setVisible(true);
    }

    public void splashOff()
    {
	setVisible(false);
	dispose();
    }

    public void setProgress(int value)
    {
	if (m_progressBar && value >= 0 && value <= 100)
	    m_progress.setValue(value);
    }

    public void setProgress(int value, String msg)
    {
	setProgress(value);
	if (m_progressBarMessages && !m_progressBarPercent && msg != null)
	    m_progress.setString(msg);
    }

    public final JProgressBar getProgressBar()
    {
	return m_progress;
    }

    private static final long serialVersionUID = 0x45cc3db7d4c497e9L;
    private JProgressBar m_progress;
    private boolean m_progressBar;
    private boolean m_progressBarMessages;
    private boolean m_progressBarPercent;
}
