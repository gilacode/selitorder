package com.avicenna.template;

import com.google.inject.Inject;
import jsmessages.JsMessages;
import jsmessages.JsMessagesFactory;
import jsmessages.japi.Helper;
import play.libs.Scala;
import play.mvc.Controller;
import play.mvc.Result;

public class TmplCtrl extends Controller {

    protected final JsMessages jsMessages;

    @Inject TmplCtrl(JsMessagesFactory jsMessagesFactory) {

        jsMessages = jsMessagesFactory.all();
    }

    public Result jsMessages() {
        return ok(jsMessages.apply(Scala.Option("window.Messages"), Helper.messagesFromCurrentHttpContext()));
    }

}
