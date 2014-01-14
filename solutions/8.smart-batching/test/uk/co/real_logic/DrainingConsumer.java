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

public class DrainingConsumer implements Runnable
{
    private final Drainable<Integer> queue;
    private final ElementHandler<Integer> elementHandler;
    private volatile boolean running = true;

    public DrainingConsumer(final Drainable<Integer> queue, final ElementHandler<Integer> elementHandler)
    {
        this.queue = queue;
        this.elementHandler = elementHandler;
    }

    public void run()
    {
        while (running)
        {
            // Do some pretend work so we get a burst that has to be handled.
            Thread.yield();

            if (0 == queue.drain(elementHandler))
            {
                Thread.yield();
            }
        }
    }

    public void halt()
    {
        running = false;
    }
}
