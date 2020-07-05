/*
 * ******************************************************************************
 *  * Copyright 2015 See AUTHORS file.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *****************************************************************************
 */

package games.rednblack.h2d.common;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisDialog;

public class H2DDialog extends VisDialog {
    protected final Skin skin;

    public H2DDialog(String title) {
        super(title);
        skin = VisUI.getSkin();
    }

	@Override
	public void hide () {
		super.hide();
		onDismiss();
	}

	@Override
	public void hide (Action action) {
		super.hide(action);
		onDismiss();
	}

	@Override
	protected void close () {
		super.close();
		onDismiss();
	}

	protected void onDismiss() {

	}

}
