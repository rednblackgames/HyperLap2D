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

package games.rednblack.editor.view.ui.followers;

import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.h2d.extension.spine.SpineItemType;
import games.rednblack.h2d.extension.talos.TalosItemType;

/**
 * Created by azakhary on 5/21/2015.
 */
public class FollowerFactory {

    public static BasicFollower createFollower(int entity) {
        switch (EntityUtils.getType(entity)) {
            case EntityFactory.IMAGE_TYPE:
                return new ImageFollower(entity);
            case EntityFactory.LIGHT_TYPE:
                return new LightFollower(entity);
            case TalosItemType.TALOS_TYPE:
            case EntityFactory.PARTICLE_TYPE:
                return new ParticleFollower(entity);
            case SpineItemType.SPINE_TYPE:
                return new SpineFollower(entity);
        }

        return new NormalSelectionFollower(entity);
    }
}
