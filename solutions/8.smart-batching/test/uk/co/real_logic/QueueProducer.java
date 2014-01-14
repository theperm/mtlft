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
import java.util.concurrent.CyclicBarrier;

public class QueueProducer implements Runnable
{
    private final Queue<Integer> queue;
    private final int id;
    private final CyclicBarrier cyclicBarrier;
    private final int repetitions;

    public QueueProducer(final Queue<Integer> queue,
                         final int id,
                         final CyclicBarrier cyclicBarrier,
                         final int repetitions)
    {
        this.queue = queue;
        this.id = id;
        this.cyclicBarrier = cyclicBarrier;
        this.repetitions = repetitions;
    }

    public void run()
    {
        try
        {
            cyclicBarrier.await();
        }
        catch (Exception ignore)
        {
            return;
        }

        final Integer id = Integer.valueOf(this.id);
        int i = repetitions + 1;

        while (--i != 0)
        {
            while (!queue.offer(id))
            {
                Thread.yield();
            }
        }
    }
}
