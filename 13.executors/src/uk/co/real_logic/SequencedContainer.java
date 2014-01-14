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

/**
 * A container for items processed in sequence
 */
public interface SequencedContainer
{
    /**
     * The number of items added to this container since creation.
     *
     * @return the number of items added.
     */
    long getAddedCount();

    /**
     * The number of items removed from this container since creation.
     *
     * @return the number of items removed.
     */
    long getRemovedCount();

    /**
     * The maximum capacity of this container to hold items.
     *
     * @return the capacity of the container.
     */
    int getCapacity();

    /**
     * Get the remaining capacity for elements in the container given the current size.
     *
     * @return remaining capacity of the container
     */
    int getRemainingCapacity();

    /**
     * Is the container currently empty.
     *
     * @return true if Empty otherwise false.
     */
    boolean isEmpty();
}
