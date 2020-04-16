package com.avicenna.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.ebean.annotation.Index;
import play.Logger;
import play.i18n.Lang;
import play.i18n.Messages;
import play.i18n.MessagesApi;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
public class LangUtil {

    private static int count = 1;

    private final MessagesApi messagesApi;
    private Messages messages;

    @Inject
    public LangUtil(MessagesApi messagesApi) {
        Logger.debug(this.getClass().getSimpleName() + " instantiated "+count+" time(s)");
        count++;

        this.messagesApi = messagesApi;

        Collection<Lang> candidates = Collections.singletonList(new Lang(Locale.US));
        this.messages = messagesApi.preferred(candidates);
    }

    public String at(String messageKey) {
        return messages.at(messageKey);
    }

    public String at(String messageKey, Object... args) {
        return messages.at(messageKey, args);
    }

    public String at(String messageKey, Exception e) {
        String messageValue = messages.at(messageKey);
        return messageValue + ". "+e.getMessage();
    }
}
