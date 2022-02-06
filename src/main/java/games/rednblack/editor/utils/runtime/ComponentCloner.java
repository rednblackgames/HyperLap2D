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

package games.rednblack.editor.utils.runtime;

import com.artemis.Component;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by azakhary on 6/3/2015.
 */
public class ComponentCloner {

    public static <E extends Component> E get(E source, boolean ignoreTransient) {
        Class<?> eClass = source.getClass();
        E target = null;
        try {
            target = (E) ClassReflection.newInstance(eClass);
            Field[] sourceFields = source.getClass().getDeclaredFields();
            Field[] targetFields = target.getClass().getDeclaredFields();
            for(int i = 0; i < targetFields.length; i++) {
                int modifiers = targetFields[i].getModifiers();
                if(Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers) && !Modifier.isFinal(modifiers)) {
                    if (ignoreTransient && Modifier.isTransient(modifiers))
                        continue;

                    targetFields[i].set(target, sourceFields[i].get(source));
                }
            }
        } catch (IllegalAccessException | ReflectionException e) {
            e.printStackTrace();
        }

        return target;
    }
    public static <E extends Component> E get(E source) {
        return get(source, false);
    }


    public static  <E extends Component> void set(E target, E source) {
        try {
            Field[] sourceFields = source.getClass().getDeclaredFields();
            Field[] targetFields = target.getClass().getDeclaredFields();
            for(int i = 0; i < targetFields.length; i++) {
                if(Modifier.isPublic(targetFields[i].getModifiers())) {
                    targetFields[i].set(target, sourceFields[i].get(source));
                }
            }
        } catch (IllegalAccessException e) {
            //e.printStackTrace();
        }
    }

    public static Collection<Component> cloneAll(Collection<Component> components) {
        Collection<Component> clones = new ArrayList<>();
        for(Component component: components) {
            clones.add(get(component));
        }

        return clones;
    }
}
