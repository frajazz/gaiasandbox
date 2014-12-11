package gaia.cu9.ari.gaiaorbit.interfce;

import gaia.cu9.ari.gaiaorbit.util.GlobalConf;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;

/**
 * 
 * @author Toni Sagrista
 * @deprecated This should not be used anymore. Use {@link gaia.cu9.ari.gaiaorbit.gui.swing.HelpDialog} instead.
 */
public class AboutWindow extends TutorialWindow {

    IGui gui;

    public AboutWindow(IGui gui, Skin skin) {
	super("About this application", skin, "header", "default");
	this.gui = gui;

	this.addListener(new EventListener() {
	    @Override
	    public boolean handle(Event event) {
		if (event instanceof ChangeEvent) {
		    event.getTarget().getParent().getParent().remove();
		}
		return false;
	    }
	});
	float fontWidth = skin.getFont("ui-11").getSpaceWidth();
	this.initialize("Gaia Sandbox - " + GlobalConf.instance.VERSION.version, LayoutType.L_VERTICAL);
	CharSequence text = TextUtils.limitWidth("The Gaia Sandbox (" + GlobalConf.instance.VERSION.version + ") has"
		+ " been developed in the Astronomisches Rechen-Institut"
		+ " - ZAH - Heidelberg Universität.", 250, fontWidth);

	Table propList = new Table();
	propList.pad(5);
	propList.add(new Label("Webpage", skin)).left();
	Link web = new Link(GlobalConf.instance.WEBPAGE, skin, "default-blue", GlobalConf.instance.WEBPAGE);
	propList.add(web).left().pad(5);
	propList.row();
	propList.add(new Label("Java version", skin)).left();
	propList.add(new Label(System.getProperty("java.version"), skin)).left().pad(5);
	propList.row();
	propList.add(new Label("Author", skin)).left();
	VerticalGroup authorGroup = new VerticalGroup();
	authorGroup.align(Align.left);
	authorGroup.space(5);
	HorizontalGroup authorMail = new HorizontalGroup();
	authorMail.space(5);
	authorMail.addActor(new Label("Toni Sagrista Selles - ", skin));
	authorMail.addActor(new Link("tsagrista@ari.uni-heidelberg.de", skin, "default-blue", "mailto:tsagrista@ari.uni-heidelberg.de?subject=Gaia%20Sandbox%20application"));

	authorGroup.addActor(authorMail);
	authorGroup.addActor(new Link("www.tonisagrista.com", skin, "default-blue", "http://www.tonisagrista.com"));
	propList.add(authorGroup).left().pad(5);
	propList.row();
	propList.add(new Label("Contributors", skin)).left();
	HorizontalGroup contribMail = new HorizontalGroup();
	contribMail.space(5);
	contribMail.addActor(new Label("Dr. Stefan Jordan - ", skin));
	contribMail.addActor(new Link("jordan@ari.uni-heidelberg.de", skin, "default-blue", "mailto:jordan@ari.uni-heidelberg.de?subject=Gaia%20Sandbox%20application"));
	propList.add(contribMail).left().pad(5);
	propList.row();
	propList.add(new Label("Gaia Sandbox version", skin)).left();
	propList.add(new Label(GlobalConf.instance.VERSION.version, skin)).left().pad(5);
	propList.row();
	propList.add(new Label("Build", skin)).left();
	propList.add(new Label(GlobalConf.instance.VERSION.build, skin)).left().pad(5);
	propList.row();
	propList.add(new Label("Build time", skin)).left();
	propList.add(new Label(GlobalConf.instance.VERSION.buildtime, skin)).left().pad(5);
	propList.row();
	propList.add(new Label("Builder", skin)).left();
	propList.add(new Label(GlobalConf.instance.VERSION.builder, skin)).left().pad(5);
	propList.row();
	propList.add(new Label("Build system", skin)).left();
	propList.add(new Label(TextUtils.limitWidth(GlobalConf.instance.VERSION.system, 120, fontWidth), skin)).left().pad(5);

	Label licenseText = new Label(TextUtils.limitWidth("This software is published under the LGPL (Lesser General Public License) license.", 160, fontWidth), skin);
	Image licenseImg = new Image(new Texture(Gdx.files.internal("img/license.png")));
	HorizontalGroup licenseHg = new HorizontalGroup();
	licenseHg.space(10);
	licenseHg.addActor(licenseImg);
	licenseHg.addActor(licenseText);

	this.addTutorialActor(new Label(text, skin, "default"));
	this.addTutorialActor(propList);
	this.addTutorialActor(licenseHg);
	this.addButtons(false, false, true);
	this.pack();
	this.setWidth(550);
	this.setPosition((Gdx.graphics.getWidth() - this.getWidth()) / 2, (Gdx.graphics.getHeight() - this.getHeight()) / 2);

    }

    public void display() {
	gui.getGuiStage().addActor(this);
    }
}
