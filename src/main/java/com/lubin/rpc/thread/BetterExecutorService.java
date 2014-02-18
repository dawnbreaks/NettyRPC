package com.lubin.rpc.thread;


import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * a Better executor service and a better thread pool....
 */
public class BetterExecutorService extends AbstractExecutorService {
  private final Lock lock = new ReentrantLock();
  private final BlockingQueue<Runnable> workQueue;
  private final ExecutorService executor;
  private final String poolName;
  private final long maxTimeSliceMillis;
  private final BlockingQueue<Drainer> drainerList;

  private final Logger logger = LoggerFactory.getLogger(BetterExecutorService.class);

  /**
   * 
   * @param workQueue
   * @param executor
   * @param maxDrainers
   * @param maxTimeSlice - the maximum time slice of a drainer can run
   * @param maxTimeSliceUnit - the unit of the maxTimeSlice argument
   */
  public BetterExecutorService(
    BlockingQueue<Runnable> workQueue, 
    ExecutorService executor,
    String poolName,
    int maxDrainers,
    long maxTimeSlice,
    TimeUnit maxTimeSliceUnit
  ) {
    this.workQueue = workQueue;
    this.executor = executor;
    this.poolName = poolName;
    this.maxTimeSliceMillis = maxTimeSliceUnit.toMillis(maxTimeSlice);
    drainerList = new ArrayBlockingQueue<Drainer>(maxDrainers);
    
    for (int i = 0; i < maxDrainers; i++) {
      drainerList.add(new Drainer(String.format("%s-%03d", poolName, i)));
    }
  }

  public BetterExecutorService(
    BlockingQueue<Runnable> workQueue, 
    ExecutorService executor,
    int maxDrainers,
    long maxTimeSlice,
    TimeUnit maxTimeSliceUnit
  ) {
    this(workQueue, executor, "Drainer", maxDrainers, maxTimeSlice, maxTimeSliceUnit);
  }

  public BetterExecutorService(
    BlockingQueue<Runnable> workQueue, 
    ExecutorService executor,
    String poolName,
    int maxDrainers
  ) {
    this(workQueue, executor, poolName, maxDrainers, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
  }

  public BetterExecutorService(
    BlockingQueue<Runnable> workQueue, 
    ExecutorService executor, 
    int maxDrainers
  ) {
    this(workQueue, executor, "Drainer", maxDrainers, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
  }

  public BetterExecutorService(
    ExecutorService executor,
    long maxTimeSlice,
    TimeUnit maxTimeSliceUnit
  ) {
    this(
      new LinkedBlockingQueue<Runnable>(), 
      executor, 
      "Drainer",
      1, 
      maxTimeSlice,
      maxTimeSliceUnit);
  }
  
  public BetterExecutorService(ExecutorService executor) {
    this(new LinkedBlockingQueue<Runnable>(), executor, 1);
  }

  @Override
  public void shutdown() {
    throw new UnsupportedOperationException();
  }

  @Override
  public synchronized List<Runnable> shutdownNow() {
    throw new UnsupportedOperationException();    
  }

  @Override
  public boolean isShutdown() {
    throw new UnsupportedOperationException();    
  }

  @Override
  public boolean isTerminated() {
    throw new UnsupportedOperationException();    
  }

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit)
    throws InterruptedException {
    throw new UnsupportedOperationException();    
  }

  @Override
  public void execute(Runnable task) {
    workQueue.offer(task);
    lock.lock();

    try {
      if (!drainerList.isEmpty()) {
        executor.execute(drainerList.poll());
      }
    } finally {
      lock.unlock();
    }
  }

  private class Drainer implements Runnable {
    private final String threadName;

    private Drainer(String threadName) {
      this.threadName = threadName;
    }

    public void run() {
      Thread t = Thread.currentThread();
      String oldName = t.getName();
      t.setName(threadName);

      try {
        internalRun();
      } finally {
        t.setName(oldName);
      }

    }

    private void internalRun() {
      long startTime = System.currentTimeMillis();

      while (System.currentTimeMillis() - startTime < maxTimeSliceMillis) {
        Runnable task = null;

        lock.lock();
        
        try {
          task = workQueue.poll();

          if (task == null) {
            drainerList.add(this);

            return;
          }
        } finally {
          lock.unlock();
        }

        try {
          task.run();
        } catch (RuntimeException e) {
          logger.warn("Ignoring Task Failure", e);
        }
      }

      lock.lock();

      try {
        if (workQueue.isEmpty()) {
          drainerList.add(this);
        } else {
          executor.execute(this);
        }
      } finally {
        lock.unlock();
      }
    }
  }
}