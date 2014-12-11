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

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/** A {@code CollapsableWindow} can be expanded/collapsed with a single click on the title bar.
 * 
 * @author Xoppa */
public class CollapsableWindow extends Window {
    private boolean collapsed;
    private float collapseHeight = 20f;
    private float expandHeight;

    public CollapsableWindow(String title, Skin skin) {
	super(title, skin);
	addListener(new ClickListener() {
	    private boolean dragged = false;

	    @Override
	    public void touchDragged(InputEvent event, float x, float y, int pointer) {
		dragged = true;
		super.touchDragged(event, x, y, pointer);
	    }

	    @Override
	    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
		if (!dragged) {
		    if (getTapCount() == 1 && getHeight() - y <= getPadTop() && y < getHeight() && x > 0 && x < getWidth())
			toggleCollapsed();
		} else {
		    dragged = false;
		}
		super.touchUp(event, x, y, pointer, button);
	    }

	});
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