/*
 * Copyright 2007-2018 CAM Corporation.
 */

package org.seasar.doma.jdbc.command.cam;

import org.seasar.doma.internal.expr.Value;

public interface AddParameterImplements {
    public default void addParameter(String name, Class<?> type, Object value) {
    }

    public default void addParameter(String name, Value value) {
        addParameter(name, value.getType(), value.getValue());
    }
}
