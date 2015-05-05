/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package gaia.cu9.ari.gaiaorbit.util.scene2d;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/** A {@code CollapsableWindow} can be expanded/collapsed with a single click on the title bar.
 * 
 * @author Xoppa */
public class CollapsibleWindow extends Window {
    private boolean collapsed;
    private float collapseHeight = 20f;
    private float expandHeight;
    private Vector2 vec2;
    Actor me;
    Skin skin;

    String expandIcon = "window-expand";
    String collapseIcon = "window-collapse";

    public CollapsibleWindow(String title, Skin skin) {
        super(title, skin);
        this.me = this;
        this.skin = skin;

        vec2 = new Vector2();
        addListener(new ClickListener() {
            private float startx, starty;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                startx = x + getX();
                starty = y + getY();
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                float endx = x + getX();
                float endy = y + getY();
                vec2.set(endx - startx, endy - starty);
                // pixels of margin
                if (vec2.len() < 3) {
                    if (getHeight() - y <= getPadTop() && y < getHeight() && x > 0 && x < getWidth())
                        toggleCollapsed();
                }
                super.touchUp(event, x, y, pointer, button);
            }

        });

    }

    protected void drawBackground(Batch batch, float parentAlpha, float x, float y) {
        float width = getWidth(), height = getHeight();
        float padTop = getPadTop();

        super.drawBackground(batch, parentAlpha, x, y);

        x += width - 8 - getPadRight();
        y += height - getPadTop() / 2;
        y -= (padTop - 8) / 2;

        Drawable icon = collapsed ? skin.getDrawable(expandIcon) : skin.getDrawable(collapseIcon);

        icon.draw(batch, x, y, 8, 8);

    }

    public void expand() {
        if (!collapsed)
            return;
        setHeight(expandHeight);
        setY(getY() - expandHeight + collapseHeight);
        collapsed = false;
    }

    public void collapse() {
        if (collapsed)
            return;
        expandHeight = getHeight();
        setHeight(collapseHeight);
        setY(getY() + expandHeight - collapseHeight);
        collapsed = true;
        if (getStage() != null)
            getStage().setScrollFocus(null);
    }

    public void toggleCollapsed() {
        if (collapsed)
            expand();
        else
            collapse();
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    @Override
    public void pack() {
        collapsed = false;
        super.pack();
    }
}