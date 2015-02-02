package gaia.cu9.ari.gaiaorbit.interfce;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.scene2d.CollapsableWindow;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnLabel;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnTextButton;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnTextField;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;

public class DateDialog extends CollapsableWindow {
    private final Window me;
    private final IGui gui;

    private final TextField day, month, year, hour, min, sec;
    private final TextButton setNow;

    public DateDialog(IGui gui, Skin skin) {
	super("Pick a date", skin);
	this.me = this;
	this.gui = gui;

	/** SET NOW **/
	setNow = new OwnTextButton("Set current time", skin);
	setNow.addListener(new EventListener() {
	    @Override
	    public boolean handle(Event event) {
		if (event instanceof ChangeEvent) {
		    updateTime(new Date());
		    return true;
		}
		return false;
	    }
	});
	add(setNow).center().colspan(2).padTop(5);
	row();

	/** DAY GROUP **/
	HorizontalGroup dayGroup = new HorizontalGroup();
	day = new OwnTextField("", skin);
	day.setMaxLength(2);
	day.setWidth(40);
	month = new OwnTextField("", skin);
	month.setMaxLength(2);
	month.setWidth(40);
	year = new OwnTextField("", skin);
	year.setMaxLength(4);
	year.setWidth(40);

	dayGroup.addActor(day);
	dayGroup.addActor(new OwnLabel("/", skin));
	dayGroup.addActor(month);
	dayGroup.addActor(new OwnLabel("/", skin));
	dayGroup.addActor(year);

	add(new OwnLabel(I18n.bundle.get("gui.time.date") + " (dd/MM/yyyy):", skin)).pad(5, 5, 0, 5).right();
	add(dayGroup).pad(5, 0, 0, 5);
	row();

	/** HOUR GROUP **/
	HorizontalGroup hourGroup = new HorizontalGroup();
	hour = new OwnTextField("", skin);
	hour.setMaxLength(2);
	hour.setWidth(40);
	min = new OwnTextField("", skin);
	min.setMaxLength(2);
	min.setWidth(40);
	sec = new OwnTextField("", skin);
	sec.setMaxLength(2);
	sec.setWidth(40);

	hourGroup.addActor(hour);
	hourGroup.addActor(new OwnLabel(":", skin));
	hourGroup.addActor(min);
	hourGroup.addActor(new OwnLabel(":", skin));
	hourGroup.addActor(sec);

	add(new OwnLabel(I18n.bundle.get("gui.time.time") + " (hh:mm:ss):", skin)).pad(5, 5, 0, 5).right();
	add(hourGroup).pad(5, 0, 0, 5);
	row();

	/** BUTTONS **/
	HorizontalGroup buttonGroup = new HorizontalGroup();
	TextButton ok = new OwnTextButton(I18n.bundle.get("gui.ok"), skin, "default");
	ok.setName("close");
	ok.addListener(new EventListener() {
	    @Override
	    public boolean handle(Event event) {
		if (event instanceof ChangeEvent) {

		    boolean cool = checkField(day, 1, 31) &&
			    checkField(month, 1, 12) &&
			    checkField(year, -20000, 20000) &&
			    checkField(hour, 0, 23) &&
			    checkField(min, 0, 59) &&
			    checkField(sec, 0, 59);

		    if (cool) {
			// Set the date
			GregorianCalendar cal = new GregorianCalendar();
			cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day.getText()));
			cal.set(Calendar.MONTH, Integer.parseInt(month.getText()) - 1);
			cal.set(Calendar.YEAR, Integer.parseInt(year.getText()));

			cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour.getText()));
			cal.set(Calendar.MINUTE, Integer.parseInt(min.getText()));
			cal.set(Calendar.SECOND, Integer.parseInt(sec.getText()));

			// Send time change command
			EventManager.getInstance().post(Events.TIME_CHANGE_CMD, cal.getTime());

			me.remove();
		    }

		    return true;
		}

		return false;
	    }

	});
	TextButton cancel = new OwnTextButton(I18n.bundle.get("gui.cancel"), skin, "default");
	cancel.setName("close");
	cancel.addListener(new EventListener() {
	    @Override
	    public boolean handle(Event event) {
		if (event instanceof ChangeEvent) {
		    me.remove();
		    return true;
		}

		return false;
	    }

	});
	buttonGroup.addActor(ok);
	ok.setSize(70, 20);
	buttonGroup.addActor(cancel);
	cancel.setSize(70, 20);
	buttonGroup.align(Align.right).space(10);

	add(buttonGroup).colspan(2).pad(5, 0, 0, 0).bottom().right();
	setTitleAlignment(Align.left);

	pack();

	this.setPosition(gui.getGuiStage().getWidth() / 2f - this.getWidth() / 2f, gui.getGuiStage().getHeight() / 2f - this.getHeight() / 2f);
    }

    /**
     * Returns true if all is good
     * @param f
     * @param min
     * @param max
     * @return
     */
    public boolean checkField(TextField f, int min, int max) {
	try {
	    int val = Integer.parseInt(f.getText());
	    if (val < min || val > max) {
		f.setColor(1, 0, 0, 1);
		return false;
	    }
	} catch (Exception e) {
	    f.setColor(1, 0, 0, 1);
	    return false;
	}
	return true;
    }

    /** Updates the time **/
    public void updateTime(Date date) {
	GregorianCalendar cal = new GregorianCalendar();
	cal.setTime(date);

	int day = cal.get(Calendar.DAY_OF_MONTH);
	int month = cal.get(Calendar.MONTH) + 1;
	int year = cal.get(Calendar.YEAR);

	int hour = cal.get(Calendar.HOUR_OF_DAY);
	int min = cal.get(Calendar.MINUTE);
	int sec = cal.get(Calendar.SECOND);

	this.day.setText(String.valueOf(day));
	this.month.setText(String.valueOf(month));
	this.year.setText(String.valueOf(year));
	this.hour.setText(String.valueOf(hour));
	this.min.setText(String.valueOf(min));
	this.sec.setText(String.valueOf(sec));
    }

    public void display() {
	if (!gui.getGuiStage().getActors().contains(me, true))
	    gui.getGuiStage().addActor(this);
    }

}
