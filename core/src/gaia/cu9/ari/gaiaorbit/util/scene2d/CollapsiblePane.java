package gaia.cu9.ari.gaiaorbit.util.scene2d;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.util.I18n;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.Align;

/** 
 * A collapsible pane with a detach-to-window button.
 * @author Toni Sagrista
 *
 */
public class CollapsiblePane extends VerticalGroup {

    CollapsibleWindow dialogWindow;
    ImageButton expandIcon, detachIcon;
    float lastx = -1, lasty = -1;

    /**
     * Creates a collapsible pane.
     * @param stage The main stage.
     * @param labelText The text of the label.
     * @param content The content actor.
     * @param skin The skin to use.
     * @param labelStyle The style of the label.
     * @param expandButtonStyle The style of the expand icon.
     * @param detachButtonStyle The style of the detach icon.
     * @param topIcons List of top icons that will be added between the label and the expand/detach icons.
     */
    public CollapsiblePane(final Stage stage, final String labelText, final Actor content, final Skin skin, String labelStyle, String expandButtonStyle, String detachButtonStyle, Actor... topIcons) {
        super();

        Label mainLabel = new Label(labelText, skin, labelStyle);

        // Expand icon
        expandIcon = new OwnImageButton(skin, expandButtonStyle);
        expandIcon.setName("expand-collapse");
        expandIcon.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    if (expandIcon.isChecked() && dialogWindow == null) {
                        addActor(content);
                    } else {
                        removeActor(content);
                    }
                    EventManager.instance.post(Events.RECALCULATE_OPTIONS_SIZE);
                    return true;
                }
                return false;
            }
        });
        Label expandIconTooltip = new Label(I18n.bundle.get("gui.tooltip.expandcollapse.group"), skin, "tooltip");
        expandIcon.addListener(new Tooltip<Label>(expandIconTooltip, stage));

        // Detach icon
        detachIcon = new OwnImageButton(skin, detachButtonStyle);
        detachIcon.setName("expand-collapse");
        detachIcon.setChecked(false);
        detachIcon.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    dialogWindow = createWindow(labelText, content, skin, stage, lastx, lasty);

                    // Display
                    if (!stage.getActors().contains(dialogWindow, true))
                        stage.addActor(dialogWindow);

                    expandIcon.setChecked(false);
                    expandIcon.setDisabled(true);
                    detachIcon.setDisabled(true);
                    return true;
                }
                return false;
            }
        });
        Label detachIconTooltip = new Label(I18n.bundle.get("gui.tooltip.detach.group"), skin, "tooltip");
        detachIcon.addListener(new Tooltip<Label>(detachIconTooltip, stage));

        HorizontalGroup headerGroup = new HorizontalGroup();
        headerGroup.space(10).align(Align.center);
        headerGroup.addActor(mainLabel);

        if (topIcons != null && topIcons.length > 0) {
            for (Actor topIcon : topIcons) {
                headerGroup.addActor(topIcon);
            }
        }

        headerGroup.addActor(expandIcon);
        headerGroup.addActor(detachIcon);

        addActor(headerGroup);

        expandIcon.setChecked(true);
    }

    /**
     * Creates a collapsible pane.
     * @param stage The main stage.
     * @param labelText The text of the label.
     * @param content The content actor.
     * @param skin The skin to use.
     * @param topIcons List of top icons that will be added between the label and the expand/detach icons.
     */
    public CollapsiblePane(Stage stage, String labelText, final Actor content, Skin skin, Actor... topIcons) {
        this(stage, labelText, content, skin, "header", "expand-collapse", "detach", topIcons);
    }

    private CollapsibleWindow createWindow(String labelText, final Actor content, Skin skin, Stage stage, float x, float y) {
        final CollapsibleWindow window = new CollapsibleWindow(labelText, skin);
        window.align(Align.center);

        window.add(content).row();

        /** Close button **/
        TextButton close = new OwnTextButton(I18n.bundle.get("gui.close"), skin, "default");
        close.setName("close");
        close.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    lastx = window.getX();
                    lasty = window.getY();
                    window.remove();
                    dialogWindow = null;
                    expandIcon.setDisabled(false);
                    detachIcon.setDisabled(false);
                    return true;
                }

                return false;
            }

        });
        Container<Button> closeContainer = new Container<Button>(close);
        close.setSize(70, 20);
        closeContainer.align(Align.right);

        window.add(closeContainer).pad(5, 0, 0, 0).bottom().right();
        window.getTitleTable().align(Align.left);
        window.pack();

        x = x < 0 ? stage.getWidth() / 2f - this.getWidth() / 2f : x;
        y = y < 0 ? stage.getHeight() / 2f - this.getHeight() / 2f : y;
        window.setPosition(x, y);

        return window;
    }

}
