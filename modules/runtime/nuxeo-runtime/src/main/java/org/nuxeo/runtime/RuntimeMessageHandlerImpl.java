/*
 * (C) Copyright 2017 Nuxeo (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Kevin Leturc <kleturc@nuxeo.com>
 *     Anahide Tchertchian
 */
package org.nuxeo.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.nuxeo.runtime.RuntimeMessage.ComponentManagerStep;
import org.nuxeo.runtime.model.ComponentManager;

/**
 * Handles runtime messages by taking care of component manager lifecycle in order to work correctly with hot reload.
 * This is interesting to not store several time the same message in case of hot reload.
 *
 * @since 9.10
 */
public class RuntimeMessageHandlerImpl implements RuntimeMessageHandler, ComponentManager.Listener {

    protected final List<RuntimeMessage> messages = new ArrayList<>();

    protected ComponentManagerStep currentStep = ComponentManagerStep.INITIALIZING;

    @Override
    @Deprecated
    public void addWarning(String message) {
        addMessage(new RuntimeMessage(currentStep, Level.WARNING, message));
    }

    @Override
    public List<String> getWarnings() {
        return getMessageStrings(msg -> Level.WARNING.equals(msg.getLevel()));
    }

    @Override
    @Deprecated
    public void addError(String message) {
        addMessage(new RuntimeMessage(currentStep, Level.SEVERE, message));
    }

    @Override
    public List<String> getErrors() {
        return getMessageStrings(msg -> Level.SEVERE.equals(msg.getLevel()));
    }

    @Override
    public void beforeActivation(ComponentManager mgr) {
        changeStep(ComponentManagerStep.ACTIVATING);
    }

    @Override
    public void beforeStart(ComponentManager mgr, boolean isResume) {
        changeStep(ComponentManagerStep.STARTING);
    }

    @Override
    public void afterStart(ComponentManager mgr, boolean isResume) {
        changeStep(ComponentManagerStep.RUNNING);
    }

    @Override
    public void beforeStop(ComponentManager mgr, boolean isStandby) {
        changeStep(ComponentManagerStep.STOPPING);
    }

    @Override
    public void beforeDeactivation(ComponentManager mgr) {
        changeStep(ComponentManagerStep.DEACTIVATING);
    }

    protected void changeStep(ComponentManagerStep step) {
        this.currentStep = step;
    }

    @Override
    public void addMessage(RuntimeMessage message) {
        messages.add(message);
    }

    @Override
    public void addMessage(Level level, String message, String source) {
        addMessage(new RuntimeMessage(currentStep, level, message, source));
    }

    protected Predicate<RuntimeMessage> getFinalPredicate(Predicate<RuntimeMessage> givenPredicate) {
        return givenPredicate == null ? m -> true : givenPredicate;
    }

    @Override
    public List<RuntimeMessage> getMessages(Predicate<RuntimeMessage> predicate) {
        final Predicate<RuntimeMessage> p = predicate == null ? m -> true : predicate;
        return messages.stream()
                       .filter(msg -> p.test(msg))
                       .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    @Override
    public List<String> getMessageStrings(Predicate<RuntimeMessage> predicate) {
        return getMessages(predicate).stream()
                                     .map(RuntimeMessage::getMessage)
                                     .collect(Collectors.collectingAndThen(Collectors.toList(),
                                             Collections::unmodifiableList));
    }

    @Override
    public void clear(Predicate<RuntimeMessage> predicate) {
        final Predicate<RuntimeMessage> p = predicate == null ? m -> true : predicate;
        messages.removeIf(msg -> p.test(msg));
    }

}
