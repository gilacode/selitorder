package com.avicenna;

import com.avicenna.html.IndexMain;
import com.avicenna.nav.NavMgr;
import com.avicenna.security.SecException;
import com.avicenna.security.SecMgr;
import com.avicenna.template.TmplDto;
import com.avicenna.template.TmplMgr;
import com.google.inject.Inject;
import play.mvc.Result;

public class IndexCtrl extends BaseCtrl {

    @Inject IndexCtrl(TmplMgr tmplMgr, SecMgr secMgr, NavMgr navMgr) {
        super(tmplMgr, secMgr, navMgr);
    }

    public Result index() throws SecException {
        IndexDto dto = new IndexDto();
        TmplDto tmpl = tmplMgr.getTmpl("Index");
        return ok(IndexMain.render(dto, tmpl, currentUser()));
    }
}
