package gaia.cu9.ari.gaiaorbit.interfce.components;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.Align;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnLabel;

public class VisualEffectsComponent extends GuiComponent {

    protected Slider starBrightness, bloomEffect, ambientLight, motionBlur;
    protected OwnLabel brightness, bloom, ambient, motion;
    protected CheckBox lensFlare;

    public VisualEffectsComponent(Skin skin, Stage stage) {
        super(skin, stage);
    }

    public void initialize() {
        /** Star brightness **/
        Label brightnessLabel = new Label(txt("gui.starbrightness"), skin, "default");
        brightness = new OwnLabel(Integer.toString((int) (MathUtilsd.lint(GlobalConf.scene.STAR_BRIGHTNESS, Constants.MIN_STAR_BRIGHT, Constants.MAX_STAR_BRIGHT, Constants.MIN_SLIDER, Constants.MAX_SLIDER))), skin);
        starBrightness = new Slider(Constants.MIN_SLIDER, Constants.MAX_SLIDER, 1, false, skin);
        starBrightness.setName("star brightness");
        starBrightness.setValue(MathUtilsd.lint(GlobalConf.scene.STAR_BRIGHTNESS, Constants.MIN_STAR_BRIGHT, Constants.MAX_STAR_BRIGHT, Constants.MIN_SLIDER, Constants.MAX_SLIDER));
        starBrightness.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.STAR_BRIGHTNESS_CMD, MathUtilsd.lint(starBrightness.getValue(), Constants.MIN_SLIDER, Constants.MAX_SLIDER, Constants.MIN_STAR_BRIGHT, Constants.MAX_STAR_BRIGHT));
                    brightness.setText(Integer.toString((int) starBrightness.getValue()));
                    return true;
                }
                return false;
            }
        });
        HorizontalGroup brightnessGroup = new HorizontalGroup();
        brightnessGroup.space(3);
        brightnessGroup.addActor(starBrightness);
        brightnessGroup.addActor(brightness);

        /** Ambient light **/
        Label ambientLightLabel = new Label(txt("gui.light.ambient"), skin, "default");
        ambient = new OwnLabel(Integer.toString((int) (GlobalConf.scene.AMBIENT_LIGHT * 100)), skin);
        ambientLight = new Slider(Constants.MIN_SLIDER, Constants.MAX_SLIDER, 1, false, skin);
        ambientLight.setName("ambient light");
        ambientLight.setValue(GlobalConf.scene.AMBIENT_LIGHT * 100);
        ambientLight.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.AMBIENT_LIGHT_CMD, ambientLight.getValue() / 100f);
                    ambient.setText(Integer.toString((int) ambientLight.getValue()));
                    return true;
                }
                return false;
            }
        });
        HorizontalGroup ambientGroup = new HorizontalGroup();
        ambientGroup.space(3);
        ambientGroup.addActor(ambientLight);
        ambientGroup.addActor(ambient);

        /** Bloom **/
        Label bloomLabel = new Label(txt("gui.bloom"), skin, "default");
        bloom = new OwnLabel(Integer.toString((int) (GlobalConf.postprocess.POSTPROCESS_BLOOM_INTENSITY * 10)), skin);
        bloomEffect = new Slider(Constants.MIN_SLIDER, Constants.MAX_SLIDER, 1, false, skin);
        bloomEffect.setName("bloom effect");
        bloomEffect.setValue(GlobalConf.postprocess.POSTPROCESS_BLOOM_INTENSITY * 10f);
        bloomEffect.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.BLOOM_CMD, bloomEffect.getValue() / 10f);
                    bloom.setText(Integer.toString((int) bloomEffect.getValue()));
                    return true;
                }
                return false;
            }
        });

        HorizontalGroup bloomGroup = new HorizontalGroup();
        bloomGroup.space(3);
        bloomGroup.addActor(bloomEffect);
        bloomGroup.addActor(bloom);

        /** Motion blur **/
        Label motionBlurLabel = new Label(txt("gui.motionblur"), skin, "default");
        motion = new OwnLabel(Integer.toString((int) (GlobalConf.postprocess.POSTPROCESS_MOTION_BLUR * 100)), skin);
        motionBlur = new Slider(Constants.MIN_SLIDER, Constants.MAX_SLIDER, 1, false, skin);
        motionBlur.setName("motion blur");
        motionBlur.setValue(GlobalConf.postprocess.POSTPROCESS_MOTION_BLUR * 100f);
        motionBlur.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.MOTION_BLUR_CMD, motionBlur.getValue() / 100f);
                    motion.setText(Integer.toString((int) motionBlur.getValue()));
                    return true;
                }
                return false;
            }
        });

        HorizontalGroup motionGroup = new HorizontalGroup();
        motionGroup.space(3);
        motionGroup.addActor(motionBlur);
        motionGroup.addActor(motion);

        /** Lens flare **/
        lensFlare = new CheckBox(txt("gui.lensflare"), skin);
        lensFlare.setName("lens flare");
        lensFlare.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.LENS_FLARE_CMD, lensFlare.isChecked());
                    return true;
                }
                return false;
            }
        });
        lensFlare.setChecked(GlobalConf.postprocess.POSTPROCESS_LENS_FLARE);

        VerticalGroup lightingGroup = new VerticalGroup().align(Align.left);
        lightingGroup.addActor(brightnessLabel);
        lightingGroup.addActor(brightnessGroup);
        lightingGroup.addActor(ambientLightLabel);
        lightingGroup.addActor(ambientGroup);
        lightingGroup.addActor(bloomLabel);
        lightingGroup.addActor(bloomGroup);
        lightingGroup.addActor(motionBlurLabel);
        lightingGroup.addActor(motionGroup);
        lightingGroup.addActor(lensFlare);

        component = lightingGroup;
    }

}
