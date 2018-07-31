/*
 * Copyright 2007-2018 CAM Corporation.
 */

package jp.co.camnet.doma;

import org.seasar.doma.jdbc.query.Query;

import java.util.function.Consumer;

public interface DomaSqlParamterSetter<T extends DomaSqlParamterSetter> {

    public default T withQueryParamSetter(Consumer<Query> modifyQueryParamSetter) {
        return (T) this;
    }

    public void acceptQuery(Query query);
}
