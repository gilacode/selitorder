package com.avicenna.file;

import com.avicenna.BaseCtrl;
import com.avicenna.nav.NavMgr;
import com.avicenna.security.SecMgr;
import com.avicenna.template.TmplMgr;
import com.avicenna.util.MimeTypeUtil;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import play.mvc.Result;

public class FileCtrl extends BaseCtrl {

    private final MimeTypeUtil mimeTypeUtil;

    private final FileMgr fileMgr;

    @Inject
    FileCtrl(MimeTypeUtil mimeTypeUtil, FileMgr fileMgr, NavMgr navMgr, SecMgr secMgr, TmplMgr tmplMgr) {

        super(tmplMgr, secMgr, navMgr);

        this.mimeTypeUtil = mimeTypeUtil;

        this.fileMgr = fileMgr;
    }

    public Result getImage(String folderName, String referenceKey) {
        try {

            FileMgr.FileDto fileDto = fileMgr.find(folderName, referenceKey, true);
            if(fileDto==null || fileDto.getFile()==null) {
                return notFound();
            }

            String fileName = fileDto.getReferenceKey()+"."+fileDto.getFileAttribute().getFileExt();
            String mimeType = fileDto.getFileAttribute().getMimeType();
            if(StringUtils.isBlank(mimeType)) {
                mimeType = mimeTypeUtil.getMimeType(fileDto.getFileAttribute().getFileExt());
            }
            return ok(fileDto.getFile()).as(mimeType);

        } catch (FileException e) {
            return notFound();
        }
    }
}
