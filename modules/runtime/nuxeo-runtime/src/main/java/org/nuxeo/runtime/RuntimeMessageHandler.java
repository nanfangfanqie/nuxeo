/*
 * (C) Copyright 2017-2020 Nuxeo (http://nuxeo.com/) and others.
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

import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;

/**
 * Handles runtime message.
 *
 * @since 9.10
 */
public interface RuntimeMessageHandler {

    /**
     * Warning messages don't block server startup.
     *
     * @deprecated since 11.2, use {@link #addMessage(Level, String, String, String, String)} or
     *             {@link #addMessage(RuntimeMessage)} instead.
     */
    @Deprecated
    void addWarning(String message);

    /**
     * @return an unmodifiable {@link List} of warning messages
     */
    List<String> getWarnings();

    /**
     * Add new error.
     * <p />
     * Error messages block server startup in strict mode.
     *
     * @deprecated since 11.2, use {@link #addMessage(Level, String, String, String, String)} or
     *             {@link #addMessage(RuntimeMessage)} instead. O
     */
    @Deprecated
    void addError(String message);

    /**
     * @return an unmodifiable {@link List} of error messages
     */
    List<String> getErrors();

    void addMessage(RuntimeMessage message);

    /**
     * Adds the following message.
     * <p>
     * The source is a free string identifier allowing to distinguish between messages (and to clear them selectively if
     * needed). * @since 11.2
     */
    void addMessage(Level level, String message, String source);

    /**
     * Returns all messages, filtered by following predicate.
     * <p>
     * Predicate can be null, in which case no filtering will be done.
     *
     * @since 11.2
     */
    List<RuntimeMessage> getMessages(Predicate<RuntimeMessage> predicate);

    /**
     * Returns all messages strings, filtered by following predicate.
     * <p>
     * Predicate can be null, in which case no filtering will be done.
     *
     * @since 11.2
     */
    List<String> getMessageStrings(Predicate<RuntimeMessage> predicate);

    /**
     * Clears messages filtered by following predicate.
     * <p>
     * Predicate can be null, in which case all messages will be cleared.
     *
     * @since 11.2
     */
    void clear(Predicate<RuntimeMessage> predicate);

}
