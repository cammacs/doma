package org.seasar.doma.jdbc.command.cam;

import org.seasar.doma.jdbc.JdbcLogger;
import org.seasar.doma.jdbc.query.Query;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SqlMonitor {
    // 未実行
    // ステートメント発行
    //

    protected Date beginTime;
    protected Date endTime;
    protected boolean executed = false;
    protected boolean success = false;
    protected boolean monitoring = false;

    protected final Query query;

    protected final Statement statemnt;
    protected final LoggerExtension logger;
    protected List<Long> monitoringIntervals = new ArrayList<>();
    protected final Thread monitoringThread;

    public SqlMonitor(Statement statemnt, Query query) {
        beginTime = null;
        endTime = null;
        this.query = query;
        JdbcLogger logger0 = query.getConfig().getJdbcLogger();
        if ((logger0 instanceof LoggerExtension)) {
            this.logger = (LoggerExtension) logger0;
        } else {
            this.logger = null;
        }
        this.statemnt = statemnt;

        monitoringThread = new Thread(() -> {
            while (endTime == null && monitoring) {
                try {
                    Thread.sleep(1000 * 60);//1分毎に監視
                    monitor0();
                } catch (InterruptedException e) {
                    monitoring = false;
                    //モニタリングエラー
                } catch (Exception e) {
                    monitoring = false;
                }
            }
        });
    }

    public void begin(boolean monitoring) {
        beginTime = new Date();
        executed = true;
        if (monitoring) {
            monitor();
        }
    }

    public void success() {
        success = true;
        endTime = new Date();
    }

    public void error() {
        success = false;
        endTime = new Date();
    }

    public long duration() {
        if (beginTime == null) {
            return -1;
        }
        if (endTime == null) {
            return new Date().getTime() - beginTime.getTime();
        }
        return endTime.getTime() - beginTime.getTime();
    }

    public boolean isSuccess() {
        return executed && success;
    }

    public boolean isError() {
        return executed && !success && endTime != null;
    }

    public Query getQuery() {
        return query;
    }

    public Statement getStatement() {
        return statemnt;
    }

    public List<Long> getMonitoringIntervals() {
        return monitoringIntervals;
    }

    protected void monitor() {
        monitoring = true;
        JdbcLogger logger0 = query.getConfig().getJdbcLogger();
        if (logger0 instanceof LoggerExtension) {
            Thread thread = monitoringThread;
            thread.setDaemon(true);
            thread.setName("MonitoringThreadOf-" + Thread.currentThread().getName());
            thread.start();
        }
        return;
    }

    protected void monitor0() {
        if (logger != null) {
            long time = countFromPrevious();
            if (time > 1000 * 60 * 10) {//TODO 外部化
                logger.monitor(this);
                monitoringIntervals.add(new Date().getTime());
            }
        }
    }

    public void stopMonitoring() {
        monitoring = false;
        if (monitoringThread != null) {
            monitoringThread.interrupt();
        }
    }

    public void finish() {
        try {
            if (logger != null) {
                logger.accept(this);
            }
        } finally {
            stopMonitoring();
        }
        return;
    }

    public long countFromBegin() {
        long now = new Date().getTime();
        return now - beginTime.getTime();
    }

    public long countFromPrevious() {
        long prev;
        long now = new Date().getTime();
        if (monitoringIntervals == null || monitoringIntervals.isEmpty()) {
            return countFromBegin();
        } else {
            int i = monitoringIntervals.size() - 1;
            prev = monitoringIntervals.get(i);
        }

        return now - prev;
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public Date getEndTime() {
        return endTime;
    }
}
