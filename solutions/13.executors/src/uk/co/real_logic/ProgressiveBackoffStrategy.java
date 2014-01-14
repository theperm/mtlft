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

import java.util.concurrent.locks.LockSupport;

/**
 * {@link IdleStrategy} that applied a back off strategy of first spinning, then yielding, then parking for
 * minimum period possible before eventually sleeping in 1ms intervals when work is not available.
 *
 * This approaches allows for maximum throughput at the lowest latency when under load but is also a good
 * compromise by saving CPU resource when work is not available.
 */
public class ProgressiveBackoffStrategy
    implements IdleStrategy
{
    /**
     * {@inheritDoc}
     */
    public void idle(final int count)
    {
        if (count < 1000)
        {
            // busy spin
        }
        else if (count < 1100)
        {
            Thread.yield();
        }
        else if (count < 1200)
        {
            LockSupport.parkNanos(1);
        }
        else
        {
            try
            {
                Thread.sleep(1);
            }
            catch (InterruptedException ignore)
            {
            }
        }
    }
}
