/*
 * Copyright 2004-2010 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.doma.jdbc.command;

import org.seasar.doma.internal.jdbc.command.PreparedSqlParameterBinder;
import org.seasar.doma.internal.jdbc.sql.PreparedSql;
import org.seasar.doma.internal.jdbc.util.JdbcUtil;
import org.seasar.doma.jdbc.JdbcLogger;
import org.seasar.doma.jdbc.NoResultException;
import org.seasar.doma.jdbc.Sql;
import org.seasar.doma.jdbc.SqlExecutionException;
import org.seasar.doma.jdbc.command.cam.SqlMonitor;
import org.seasar.doma.jdbc.dialect.Dialect;
import org.seasar.doma.jdbc.query.SelectQuery;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Supplier;

import static org.seasar.doma.internal.util.AssertionUtil.assertNotNull;

/**
 * @author taedium
 * @param <RESULT>
 *            結果
 */
public class SelectCommand<RESULT> implements Command<RESULT> {

    protected final SelectQuery query;

    protected final PreparedSql sql;

    protected final ResultSetHandler<RESULT> resultSetHandler;

    public SelectCommand(SelectQuery query,
                         ResultSetHandler<RESULT> resultSetHandler) {
        assertNotNull(query, resultSetHandler);
        this.query = query;
        this.sql = query.getSql();
        this.resultSetHandler = resultSetHandler;
    }

    @Override
    public RESULT execute() {
        Supplier<RESULT> supplier = null;
        Connection connection = JdbcUtil.getConnection(query.getConfig()
                .getDataSource());
        try {
            PreparedStatement preparedStatement = JdbcUtil.prepareStatement(
                    connection, sql);
            SqlMonitor sqlMonitor = new SqlMonitor(preparedStatement, query);
            try {
                log();
                setupOptions(preparedStatement);
                bindParameters(preparedStatement);
                sqlMonitor.begin(true);
                supplier = executeQuery(preparedStatement);
                sqlMonitor.success();
            } catch (SQLException e) {
                sqlMonitor.error();
                Dialect dialect = query.getConfig().getDialect();
                throw new SqlExecutionException(query.getConfig()
                        .getExceptionSqlLogType(), sql, e,
                        dialect.getRootCause(e));
            } finally {
                sqlMonitor.finish();
                JdbcUtil.close(preparedStatement, query.getConfig()
                        .getJdbcLogger());
            }
        } finally {
            JdbcUtil.close(connection, query.getConfig().getJdbcLogger());
        }
        return supplier.get();
    }

    protected void log() {
        JdbcLogger logger = query.getConfig().getJdbcLogger();
        logger.logSql(query.getClassName(), query.getMethodName(), sql);
    }

    protected void setupOptions(PreparedStatement preparedStatement)
            throws SQLException {
        if (query.getFetchSize() > 0) {
            preparedStatement.setFetchSize(query.getFetchSize());
        }
        if (query.getMaxRows() > 0) {
            preparedStatement.setMaxRows(query.getMaxRows());
        }
        if (query.getQueryTimeout() > 0) {
            preparedStatement.setQueryTimeout(query.getQueryTimeout());
        }
    }

    protected void bindParameters(PreparedStatement preparedStatement)
            throws SQLException {
        PreparedSqlParameterBinder binder = new PreparedSqlParameterBinder(
                query);
        binder.bind(preparedStatement, sql.getParameters());
    }

    protected Supplier<RESULT> executeQuery(PreparedStatement preparedStatement)
            throws SQLException {
        ResultSet resultSet = preparedStatement.executeQuery();
        try {
            return handleResultSet(resultSet);
        } finally {
            JdbcUtil.close(resultSet, query.getConfig().getJdbcLogger());
        }
    }

    protected Supplier<RESULT> handleResultSet(ResultSet resultSet)
            throws SQLException {
        return resultSetHandler.handle(resultSet, query, (index, next) -> {
            if (index == -1 && !next && query.isResultEnsured()) {
                Sql<?> sql = query.getSql();
                throw new NoResultException(query.getConfig()
                        .getExceptionSqlLogType(), sql);
            }
        });
    }
}
