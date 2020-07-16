/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.accumulo.core.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Create a simple thread pool using common parameters.
 */
public class SimpleThreadPool extends ThreadPoolExecutor {

  // the number of seconds before we allow a thread to terminate with non-use.
  public static final long DEFAULT_TIMEOUT_MILLISECS = 180000L;

  public SimpleThreadPool(int coreAndMax, final String name) {
    this(coreAndMax, DEFAULT_TIMEOUT_MILLISECS, name);
  }

  public SimpleThreadPool(int coreAndMax, long threadTimeOut, final String name) {
    this(coreAndMax, threadTimeOut, name, new LinkedBlockingQueue<>());
  }

  public SimpleThreadPool(int coreAndMax, final String name, BlockingQueue<Runnable> queue) {
    this(coreAndMax, DEFAULT_TIMEOUT_MILLISECS, name, queue);
  }

  public SimpleThreadPool(int coreAndMax, long threadTimeOut, final String name,
      BlockingQueue<Runnable> queue) {
    super(coreAndMax, coreAndMax, threadTimeOut, TimeUnit.MILLISECONDS, queue,
        new NamingThreadFactory(name));
    if (threadTimeOut > 0) {
      allowCoreThreadTimeOut(true);
    }
  }

  /**
   * Wrap this with a trivial object whose {@link AutoCloseable#close()} method calls
   * {@link #shutdownNow()}.
   */
  public CloseableSimpleThreadPool asCloseable() {
    return new CloseableSimpleThreadPool(this);
  }

  public static class CloseableSimpleThreadPool implements AutoCloseable {
    private final SimpleThreadPool stp;

    public CloseableSimpleThreadPool(SimpleThreadPool simpleThreadPool) {
      this.stp = simpleThreadPool;
    }

    @Override
    public void close() {
      stp.shutdownNow();
    }
  }

}
