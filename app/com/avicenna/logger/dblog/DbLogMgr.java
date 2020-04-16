package com.avicenna.logger.dblog;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import com.avicenna.logger.LogMgr;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.ebean.ExpressionList;
import io.ebean.Finder;
import io.ebean.Query;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import play.Logger;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class DbLogMgr extends LogMgr {

    private final ActorRef logActor;

    private final DbLogMdl.DbLogProvider dbLogPrv;

    @Inject DbLogMgr(@Named("dblog") ActorRef logActor, DbLogMdl.DbLogProvider dbLogPrv) {

        this.logActor = logActor;
        this.dbLogPrv = dbLogPrv;
    }

    @Override
    public void debug(String group, String reference, String message) {
        Logger.debug(group, reference + " - " + message);
        logActor.tell(new LogDto(new Date(), LogType.DEBUG, group, reference, message), ActorRef.noSender());
    }

    @Override
    public void info(String group, String reference, String message) {
        Logger.debug(group, reference + " - " + message);
        logActor.tell(new LogDto(new Date(), LogType.INFO, group, reference, message), ActorRef.noSender());
    }

    @Override
    public void warning(String group, String reference, String message) {
        Logger.debug(group, reference + " - " + message);
        logActor.tell(new LogDto(new Date(), LogType.WARNING, group, reference, message), ActorRef.noSender());
    }

    @Override
    public void error(String group, String reference, String message) {
        Logger.debug(group, reference + " - " + message);
        logActor.tell(new LogDto(new Date(), LogType.ERROR, group, reference, message), ActorRef.noSender());
    }

    @Override
    public void error(String group, String reference, Throwable throwable) {
        String errMsg = ExceptionUtils.getStackTrace(throwable);
        Logger.debug(group, reference + " - " + errMsg);
        logActor.tell(new LogDto(new Date(), LogType.ERROR, group, reference, errMsg), ActorRef.noSender());
    }

    @Override
    public void error(String group, String reference, String message, Throwable throwable) {
        Logger.debug(group, reference + " - " + message);
        String errMsg = ExceptionUtils.getStackTrace(throwable);
        logActor.tell(new LogDto(new Date(), LogType.ERROR, group, reference, message + System.lineSeparator() + errMsg), ActorRef.noSender());
    }

    @Override
    public List<LogDto> get(LogParam param) {

        Finder<Long, DbLogMdl> finder = dbLogPrv.get();

        Query<DbLogMdl> query = finder.query();
        query = query.setReadOnly(true);
        query = query.orderBy("createdAt desc");

        if(param.getLimit()==0) {
            query = query.setFirstRow(param.getOffset());
            query = query.setMaxRows(param.getLimit());
        }

        ExpressionList<DbLogMdl> where  = query.where();

        if(param.getLogTypes().size()==1) {
            where = where.eq("type", param.getLogTypes().get(0));
        } else if(param.getLogTypes().size() > 1) {
            where = where.in("type", param.getLogTypes());
        }

        if(param.getGroups().size()==1) {
            where = where.eq("group", param.getGroups().get(0));
        } else if(param.getLogTypes().size() > 1) {
            where = where.in("group", param.getGroups());
        }

        if(param.getReferences().size()==1) {
            where = where.eq("referenceKey", param.getReferences().get(0));
        } else if(param.getLogTypes().size() > 1) {
            where = where.in("referenceKey", param.getReferences());
        }

        List<DbLogMdl> dbs = ListUtils.emptyIfNull(where.findList());

        return dbs.stream().map(db -> {
                    return new LogDto(db.getCreatedAt(), db.getLogType(), db.getLogGroup(), db.getReferenceKey(), db.getMessageKey());
                }).collect(Collectors.toList());
    }

    public static class LogActor extends AbstractActor {

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .match(LogDto.class, e -> {
                        DbLogMdl db = new DbLogMdl();
                        db.setLogType(e.getType());
                        db.setLogGroup(e.getGroup());
                        db.setReferenceKey(e.getReference());
                        db.setMessageKey(e.getMessage());
                        db.insert();
                    })
                    .build();
        }
    }
}
