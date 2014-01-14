/*
 * Copyright 2013 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.real_logic;

/**
 * Strategy to be used by a busy spinning thread so it can idle when no work is available.
 */
public interface IdleStrategy
{
    /**
     * Callback to notify that no work is available and counter for how many times the callback
     * has been invoked since work was last available.
     *
     * @param count of invocations since work was last available.
     */
    void idle(final int count);
}
