package com.jetbrains.teamsys.dnq.database;

import com.jetbrains.teamsys.database.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;
import jetbrains.springframework.configuration.runtime.ServiceLocator;

/**
 * Wrapper for persistent iterable. Handles iterator.next and delegates it to transient session.
 *
 * @author Vadim.Gurov
 */
class PersistentEntityIterableWrapper implements EntityIterable {

  private static final Log log = LogFactory.getLog(PersistentEntityIterableWrapper.class);

  private EntityIterable wrappedIterable;

  PersistentEntityIterableWrapper(@NotNull EntityIterable wrappedIterable) {
    if (wrappedIterable instanceof PersistentEntityIterableWrapper) {
      throw new IllegalArgumentException("Can't wrap transient entity iterable with another transient entity iterable.");
    }

    this.wrappedIterable = wrappedIterable;
  }

  public void dispose() {
    wrappedIterable.dispose();
  }

  public long size() {
    return wrappedIterable.size();
  }

  public long count() {
    return wrappedIterable.count();
  }

  public int indexOf(@NotNull Entity entity) {
    return wrappedIterable.indexOf(entity);
  }

  @NotNull
  public EntityIterableHandle getHandle() {
    return wrappedIterable.getHandle();
  }

  @NotNull
  public EntityIterable intersect(@NotNull EntityIterable right) {
    return new PersistentEntityIterableWrapper(wrappedIterable.intersect(right.getSource()));
  }

  @NotNull
  public EntityIterable union(@NotNull EntityIterable right) {
    return new PersistentEntityIterableWrapper(wrappedIterable.union(right.getSource()));
  }

  @NotNull
  public EntityIterable minus(@NotNull EntityIterable right) {
    return new PersistentEntityIterableWrapper(wrappedIterable.minus(right.getSource()));
  }

  @NotNull
  public EntityIterable concat(@NotNull EntityIterable right) {
    return new PersistentEntityIterableWrapper(wrappedIterable.concat(right.getSource()));
  }

  public EntityIterable skip(int number) {
    return new PersistentEntityIterableWrapper(wrappedIterable.skip(number));
  }

  @NotNull
  public EntityIterable getSource() {
    return wrappedIterable;
  }

  public EntityIterator iterator() {
    if (log.isTraceEnabled()) {
      log.trace("New iterator requested for persistent iterable wrapper " + this);
    }

    return new PersistentEntityIteratorWrapper(wrappedIterable.iterator(),
            (TransientStoreSession) ((TransientEntityStore) ServiceLocator.getBean("transientEntityStore")).getThreadSession());
  }
}
