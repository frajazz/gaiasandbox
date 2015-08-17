package gaia.cu9.ari.gaiaorbit.interfce.components;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.interfce.DateDialog;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.format.DateFormatFactory;
import gaia.cu9.ari.gaiaorbit.util.format.DateFormatFactory.DateType;
import gaia.cu9.ari.gaiaorbit.util.format.IDateFormat;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnImageButton;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnLabel;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnTextField;
import gaia.cu9.ari.gaiaorbit.util.time.GlobalClock;

import java.util.Date;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Tooltip;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.Align;

public class TimeComponent extends GuiComponent implements IObserver {

    /** Date format **/
    private IDateFormat df;

    protected OwnLabel date;
    protected Button plus, minus;
    protected TextField inputPace;
    protected ImageButton dateEdit;
    protected DateDialog dateDialog;

    public TimeComponent(Skin skin, Stage stage) {
        super(skin, stage);

        df = DateFormatFactory.getFormatter(I18n.locale, DateType.DATE);
        EventManager.instance.subscribe(this, Events.TIME_CHANGE_INFO, Events.TIME_CHANGE_CMD, Events.PACE_CHANGED_INFO);
    }

    @Override
    public void initialize() {
        // Time
        date = new OwnLabel("", skin);
        date.setName("input time");

        dateEdit = new OwnImageButton(skin, "edit");
        dateEdit.addListener(new EventListener() {

            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    // Left button click
                    if (dateDialog == null) {
                        dateDialog = new DateDialog(stage, skin);
                    }
                    dateDialog.updateTime(GlobalClock.clock.time);
                    dateDialog.display();
                }
                return false;
            }

        });
        dateEdit.addListener(new Tooltip(txt("gui.tooltip.dateedit"), skin));

        // Pace
        Label paceLabel = new Label(txt("gui.pace"), skin);
        plus = new ImageButton(skin.getDrawable("tree-plus"));
        plus.setName("plus");
        plus.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    // Plus pressed
                    EventManager.instance.post(Events.PACE_DOUBLE_CMD);

                    return true;
                }
                return false;
            }
        });
        minus = new ImageButton(skin.getDrawable("tree-minus"));
        minus.setName("minus");
        minus.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    // Minus pressed
                    EventManager.instance.post(Events.PACE_DIVIDE_CMD);
                    return true;
                }
                return false;
            }
        });
        inputPace = new OwnTextField(Double.toString(GlobalClock.clock.pace), skin);
        inputPace.setName("input pace");
        inputPace.setMaxLength(15);
        inputPace.setWidth(60f);
        inputPace.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof InputEvent) {
                    InputEvent ie = (InputEvent) event;
                    if (ie.getType() == Type.keyTyped) {
                        try {
                            double pace = Double.parseDouble(inputPace.getText());
                            EventManager.instance.post(Events.PACE_CHANGE_CMD, pace, true);
                        } catch (Exception e) {
                            return false;
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        VerticalGroup timeGroup = new VerticalGroup().align(Align.left).space(3).padTop(3);

        HorizontalGroup dateGroup = new HorizontalGroup();
        dateGroup.space(4);
        dateGroup.addActor(date);
        dateGroup.addActor(dateEdit);
        timeGroup.addActor(dateGroup);

        HorizontalGroup paceGroup = new HorizontalGroup();
        paceGroup.space(1);
        paceGroup.addActor(paceLabel);
        paceGroup.addActor(minus);
        paceGroup.addActor(inputPace);
        paceGroup.addActor(plus);

        timeGroup.addActor(paceGroup);

        timeGroup.pack();

        component = timeGroup;
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case TIME_CHANGE_INFO:
        case TIME_CHANGE_CMD:
            // Update input time
            Date time = (Date) data[0];
            date.setText(df.format(time));
            break;
        case PACE_CHANGED_INFO:
            if (data.length == 1)
                this.inputPace.setText(Double.toString((double) data[0]));
            break;
        }

    }

}
