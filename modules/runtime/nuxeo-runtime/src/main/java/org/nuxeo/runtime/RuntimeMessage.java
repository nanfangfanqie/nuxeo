/*
 * (C) Copyright 2017-2020 Nuxeo (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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

import java.util.logging.Level;

/**
 * @since 11.2
 */
public class RuntimeMessage {

    protected final ComponentManagerStep step;

    protected final Level level;

    protected final String message;

    protected final String source;

    public RuntimeMessage(ComponentManagerStep step, Level level, String message) {
        this(step, level, message, null);
    }

    public RuntimeMessage(ComponentManagerStep step, Level level, String message, String source) {
        super();
        this.step = step;
        this.level = level;
        this.message = message;
        this.source = source;
    }

    public ComponentManagerStep getStep() {
        return step;
    }

    public Level getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public String getSource() {
        return source;
    }

    protected enum ComponentManagerStep {

        /**
         * Pseudo state when listener has not been called yet.
         *
         * @since 11.2
         */
        INITIALIZING,

        ACTIVATING,

        STARTING,

        RUNNING,

        STOPPING,

        DEACTIVATING

    }

}
