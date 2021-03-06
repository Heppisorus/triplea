package games.strategy.engine.message.unifiedmessenger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import games.strategy.debug.ClientLogger;
import games.strategy.engine.message.MessageContext;
import games.strategy.engine.message.RemoteMethodCall;
import games.strategy.engine.message.RemoteMethodCallResults;
import games.strategy.net.INode;

/**
 * This is where the methods finally get called.
 * An endpoint contains the implementors for a given name that are local to this
 * node.
 * You can invoke the method and get the results for all the implementors.
 */
class EndPoint {
  // the next number we are going to give
  private final AtomicLong nextGivenNumber = new AtomicLong();
  // the next number we can run
  private long currentRunnableNumber = 0;
  private final Object numberMutex = new Object();
  private final Object implementorsMutex = new Object();
  private final String name;
  private final Class<?> remoteClass;
  private final List<Object> implementors = new ArrayList<>();
  private final boolean singleThreaded;

  public EndPoint(final String name, final Class<?> remoteClass, final boolean singleThreaded) {
    this.name = name;
    this.remoteClass = remoteClass;
    this.singleThreaded = singleThreaded;
  }

  public Object getFirstImplementor() {
    synchronized (implementorsMutex) {
      if (implementors.size() != 1) {
        throw new IllegalStateException("Invalid implementor count, " + implementors);
      }
      return implementors.get(0);
    }
  }

  public long takeANumber() {
    return nextGivenNumber.getAndIncrement();
  }

  private void waitTillCanBeRun(final long number) {
    synchronized (numberMutex) {
      while (number > currentRunnableNumber) {
        try {
          numberMutex.wait();
        } catch (final InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    }
  }

  private void releaseNumber() {
    synchronized (numberMutex) {
      currentRunnableNumber++;
      numberMutex.notifyAll();
    }
  }

  /**
   * @return is this the first implementor.
   */
  public boolean addImplementor(final Object implementor) {
    if (!remoteClass.isAssignableFrom(implementor.getClass())) {
      throw new IllegalArgumentException(remoteClass + " is not assignable from " + implementor.getClass());
    }
    synchronized (implementorsMutex) {
      final boolean isFirstImplementor = implementors.isEmpty();
      implementors.add(implementor);
      return isFirstImplementor;
    }
  }

  public boolean isSingleThreaded() {
    return singleThreaded;
  }

  public int getLocalImplementorCount() {
    synchronized (implementorsMutex) {
      return implementors.size();
    }
  }

  /**
   * @return we have no more implementors.
   */
  boolean removeImplementor(final Object implementor) {
    synchronized (implementorsMutex) {
      if (!implementors.remove(implementor)) {
        throw new IllegalStateException("Not removed, impl:" + implementor + " have " + implementors);
      }
      return implementors.isEmpty();
    }
  }

  public String getName() {
    return name;
  }

  public Class<?> getRemoteClass() {
    return remoteClass;
  }

  /*
   * @param number - like the number you get in a bank line, if we are single
   * threaded, then the method will not run until the number comes up. Acquire
   * with getNumber() @return a List of RemoteMethodCallResults
   */
  public List<RemoteMethodCallResults> invokeLocal(final RemoteMethodCall call, final long number,
      final INode messageOriginator) {
    try {
      if (singleThreaded) {
        waitTillCanBeRun(number);
      }
      return invokeMultiple(call, messageOriginator);
    } finally {
      releaseNumber();
    }
  }

  private List<RemoteMethodCallResults> invokeMultiple(final RemoteMethodCall call, final INode messageOriginator) {
    // copy the implementors
    final List<Object> implementorsCopy;
    synchronized (implementorsMutex) {
      implementorsCopy = new ArrayList<>(implementors);
    }
    final List<RemoteMethodCallResults> results = new ArrayList<>(implementorsCopy.size());
    for (final Object implementor : implementorsCopy) {
      results.add(invokeSingle(call, implementor, messageOriginator));
    }
    return results;
  }

  private RemoteMethodCallResults invokeSingle(final RemoteMethodCall call, final Object implementor,
      final INode messageOriginator) {
    call.resolve(remoteClass);
    final Method method;
    try {
      method = implementor.getClass().getMethod(call.getMethodName(), call.getArgTypes());
      method.setAccessible(true);
    } catch (final NoSuchMethodException e) {
      throw new IllegalStateException(e);
    }
    MessageContext.setSenderNodeForThread(messageOriginator);
    try {
      final Object methodRVal = method.invoke(implementor, call.getArgs());
      return new RemoteMethodCallResults(methodRVal);
    } catch (final InvocationTargetException e) {
      return new RemoteMethodCallResults(e.getTargetException());
    } catch (final IllegalAccessException e) {
      ClientLogger.logQuietly("error in call:" + call, e);
      return new RemoteMethodCallResults(e);
    } catch (final IllegalArgumentException e) {
      ClientLogger.logQuietly("error in call:" + call, e);
      return new RemoteMethodCallResults(e);
    } finally {
      MessageContext.setSenderNodeForThread(null);
    }
  }

  @Override
  public String toString() {
    return "Name:" + name + " singleThreaded:" + singleThreaded + " implementors:" + implementors;
  }
}
