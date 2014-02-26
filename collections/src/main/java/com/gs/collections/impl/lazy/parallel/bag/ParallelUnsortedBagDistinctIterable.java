/*
 * Copyright 2014 Goldman Sachs.
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
 */

package com.gs.collections.impl.lazy.parallel.bag;

import java.util.concurrent.ExecutorService;

import com.gs.collections.api.LazyIterable;
import com.gs.collections.api.annotation.Beta;
import com.gs.collections.api.block.predicate.Predicate;
import com.gs.collections.api.block.procedure.Procedure;
import com.gs.collections.api.block.procedure.primitive.ObjectIntProcedure;
import com.gs.collections.api.set.ParallelUnsortedSetIterable;
import com.gs.collections.impl.lazy.parallel.set.AbstractParallelUnsortedSetIterable;
import com.gs.collections.impl.lazy.parallel.set.UnsortedSetBatch;
import com.gs.collections.impl.map.mutable.ConcurrentHashMap;

@Beta
class ParallelUnsortedBagDistinctIterable<T> extends AbstractParallelUnsortedSetIterable<T>
{
    private final AbstractParallelUnsortedBag<T> parallelBagIterable;

    ParallelUnsortedBagDistinctIterable(AbstractParallelUnsortedBag<T> parallelBagIterable)
    {
        this.parallelBagIterable = parallelBagIterable;
    }

    @Override
    public ExecutorService getExecutorService()
    {
        return this.parallelBagIterable.getExecutorService();
    }

    @Override
    public LazyIterable<UnsortedSetBatch<T>> split()
    {
        throw new UnsupportedOperationException();
    }

    public ParallelUnsortedSetIterable<T> asUnique()
    {
        return this;
    }

    public void forEach(final Procedure<? super T> procedure)
    {
        // TODO: Replace the map with a concurrent set once it's implemented
        final ConcurrentHashMap<T, Boolean> distinct = new ConcurrentHashMap<T, Boolean>();
        this.parallelBagIterable.forEachWithOccurrences(new ObjectIntProcedure<T>()
        {
            public void value(T each, int occurrences)
            {
                if (distinct.put(each, true))
                {
                    procedure.value(each);
                }
            }
        });
    }

    @Override
    public boolean anySatisfy(final Predicate<? super T> predicate)
    {
        // TODO: Replace the map with a concurrent set once it's implemented
        final ConcurrentHashMap<T, Boolean> distinct = new ConcurrentHashMap<T, Boolean>();
        return this.parallelBagIterable.anySatisfy(new Predicate<T>()
        {
            public boolean accept(T each)
            {
                return distinct.put(each, true) && predicate.accept(each);
            }
        });
    }
}