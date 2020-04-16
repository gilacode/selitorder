package com.avicenna.monitor;

import com.avicenna.logger.LogMgr;
import com.avicenna.util.DateTimeUtil;
import com.google.inject.Inject;
import org.apache.commons.text.StringEscapeUtils;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Date;
import java.util.concurrent.CompletionStage;

public class MonAction extends Action<MonAction> {

    private final DateTimeUtil dateTimeUtil;

    private final LogMgr dbLog;

    @Inject MonAction(DateTimeUtil dateTimeUtil, LogMgr dbLog) {

        this.dateTimeUtil = dateTimeUtil;

        this.dbLog = dbLog;
    }

    public CompletionStage<Result> call(Http.Context ctx) {

        long duration = System.currentTimeMillis();

        CompletionStage<Result> result = delegate.call(ctx);

        duration = System.currentTimeMillis() - duration;

        if(duration > 2000) {

            String body = "";

            if(ctx.request().hasBody()
                    && ctx.request().body()!=null
                    && ctx.request().body().asJson()!=null) {

                body = ctx.request().body().asJson().textValue();

            }

            String message = generateJson(ctx.request().path(),body, duration);

            dbLog.info("Controller", String.valueOf(System.currentTimeMillis()), message);

        }
        return result;
    }

    private String generateJson(String origin, String body, long duration) {

        body = body==null?"": StringEscapeUtils.escapeJson(body);

        origin = origin==null?"":StringEscapeUtils.escapeJson(origin);

        String json = "{ \"timestamp\" : \""+ this.dateTimeUtil.getDate(new Date()) +"\", " +
                "\"origin\" : \""+origin+"\", " +
                "\"body\" : \""+body+"\", " +
                "\"duration\" : "+duration+" }";

        return json;
    }

}
