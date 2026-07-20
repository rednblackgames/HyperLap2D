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

package games.rednblack.editor.view.ui.properties;

import games.rednblack.editor.proxy.EntityDataProxy;

/**
 * Entity-property-mediator base that adapts the generic {@code Integer} observable
 * reference from {@link UIAbstractPropertiesMediator} to {@code int} for the
 * concrete panel subclasses. The duplicated {@code handleNotification},
 * {@code listNotificationInterests}, and {@code onItemDataUpdate} were removed
 * (they were copies of the parent's); the {@code int}-typed {@code setItem} and
 * {@code translateObservableDataToView} adapters stay so the 18 panel mediators
 * can keep their {@code int}-typed overrides.
 */
public abstract class UIAbstractEntityPropertiesMediator<V extends UIAbstractProperties> extends UIAbstractPropertiesMediator<Integer, V> {

    protected EntityDataProxy entityData;

    public UIAbstractEntityPropertiesMediator(String mediatorName, V viewComponent) {
        super(mediatorName, viewComponent);
        entityData = EntityDataProxy.get(facade);
    }

    /** Bridges the generic {@code Integer} setter to the {@code int} version used by panels. */
    @Override
    public void setItem(Integer item) {
        setItem((int) item);
    }

    public void setItem(int item) {
        observableReference = item;
        lockUpdates = true;
        translateObservableDataToView(observableReference);
        lockUpdates = false;
    }

    /** Bridges the generic {@code Integer} translate to the {@code int} version implemented by panels. */
    @Override
    protected void translateObservableDataToView(Integer item) {
        translateObservableDataToView((int) item);
    }

    protected abstract void translateObservableDataToView(int item);
}