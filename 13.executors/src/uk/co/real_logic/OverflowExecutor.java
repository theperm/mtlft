/*
 * Copyright 2012 Real Logic Ltd.
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

import java.util.concurrent.Executor;

/**
 * Executor that can overflow execution of commands to the calling thread when the queue is full.
 */
public interface OverflowExecutor extends Executor, SequencedContainer
{
    /**
     * Shutdown the Executor immediately without processing more commands and clear the internal queue.
     */
    void shutdownNow() throws InterruptedException;

    /**
     * Are any threads in the pool still active?  By active they are ready to process a command or are currently processing a command.
     *
     * @return true if threads in the pool have exited their run methods.
     */
    boolean isActive();

    /**
     * Get a count of the number of commands that have been executing on a calling thread.
     *
     * @return the number of commands that have been executed on a calling thread
     */
    long getOverflowCounter();
}
