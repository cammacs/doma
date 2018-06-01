package org.seasar.doma.jdbc.command.cam;

public interface LoggerExtension {
    public default void accept(SqlMonitor sqlMonitor) {
        if (sqlMonitor == null) {
            // nothing to do
        } else if (sqlMonitor.isSuccess()) {
            logSuccess(sqlMonitor);
        } else if (sqlMonitor.isError()) {
            logError(sqlMonitor);
        }
    }

    void monitor(SqlMonitor monitor);

    void logSuccess(SqlMonitor monitor);

    void logError(SqlMonitor monitor);
}
