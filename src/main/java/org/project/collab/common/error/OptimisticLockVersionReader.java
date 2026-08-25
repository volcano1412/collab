package org.project.collab.common.error;

import java.util.Optional;

public interface OptimisticLockVersionReader {
  Optional<Long> read(Class<?> entityType, Object identifier);
}
