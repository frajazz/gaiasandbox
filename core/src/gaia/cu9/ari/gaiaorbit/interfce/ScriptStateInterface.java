package gaia.cu9.ari.gaiaorbit.interfce;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnTextButton;
import sandbox.script.JythonFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;

public class ScriptStateInterface extends Table implements IObserver {

    private Image img;
    private TextButton cancelScript;

    public ScriptStateInterface(Skin skin) {
        super(skin);
        img = new Image(new Texture(Gdx.files.internal("img/keyboard.png")));
        this.add(img).left().row();
        img.setVisible(!GlobalConf.runtime.INPUT_ENABLED);

        int num = JythonFactory.getInstance().getNumRunningScripts();
        cancelScript = new OwnTextButton(I18n.bundle.format("gui.script.stop", num), skin);
        this.add(cancelScript).left();
        cancelScript.setVisible(num > 0);
        cancelScript.addListener(new EventListener() {

            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.CANCEL_SCRIPT_CMD);
                }
                return false;
            }
        });

        EventManager.instance.subscribe(this, Events.INPUT_ENABLED_CMD, Events.NUM_RUNNING_SCRIPTS);
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case INPUT_ENABLED_CMD:
            img.setVisible(!(boolean) data[0]);
            break;
        case NUM_RUNNING_SCRIPTS:
            int num = (Integer) data[0];
            cancelScript.setVisible(num > 0);
            cancelScript.setText(I18n.bundle.format("gui.script.stop", num));
            break;
        default:
            break;
        }
    }

}
