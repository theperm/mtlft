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

import java.util.Queue;

/**
 * A {@link java.util.Queue} that also supports the ability to track additions and removals as a {@link SequencedContainer}
 *
 * @param <E> type of the element to be held in this collection.
 */
public interface SequencedContainerQueue<E>
    extends Queue<E>, SequencedContainer
{
    /**
     * Invoke a {@link ElementHandler} callback on each elements to drain the collection of elements until it is empty.
     *
     * If possible, implementations should use smart batching to best handle burst traffic.
     *
     * @param elementHandler to callback for processing elements
     * @return the number of elements drained
     */
    int drain(ElementHandler<E> elementHandler);
}