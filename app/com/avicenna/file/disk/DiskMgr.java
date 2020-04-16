package com.avicenna.file.disk;

import com.avicenna.config.AppCfgMgr;
import com.avicenna.file.FileException;
import com.avicenna.file.FileIdxMdl;
import com.avicenna.file.FileMgr;
import com.avicenna.util.LangUtil;
import com.google.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import play.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class DiskMgr extends FileMgr {

    private static final int BUFFER_SIZE = 4096; // 4KB

    @Inject
    public DiskMgr(LangUtil langUtil, AppCfgMgr appCfgMgr, FileIdxMdl.FileIdxProv fileIdxProv) {
        super(langUtil, appCfgMgr, fileIdxProv);

        appCfgMgr.registerConfig(new AppCfgMgr.AppCfgDto("file.disk.path", System.getProperty("user.home")+File.separator+".dokter123", "Default Folder to Save File", "File"));

    }

    @Override
    protected FileAttribute uploadFile(String folderName, String referenceKey, String mimeType, String extension, File inputFile) throws FileException {

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            inputStream = new BufferedInputStream(new FileInputStream(inputFile));

            String uploadFolder = appCfgMgr.getString("file.disk.path");
            File outputFile = new File(uploadFolder+File.separator+folderName+File.separator+referenceKey);
            FileUtils.copyInputStreamToFile(inputStream, outputFile);

            return new FileAttribute(outputFile.getAbsolutePath(), "/fil/"+folderName+"/"+referenceKey, mimeType, extension);

        } catch (FileNotFoundException e) {

            Logger.error(this.getClass().getSimpleName(), e);
            throw new FileException(langUtil.at("file.upload.error.uploadfail"), e);

        } catch (IOException e) {

            Logger.error(this.getClass().getSimpleName(), e);
            throw new FileException(langUtil.at("file.upload.error.uploadfail"), e);

        } finally {

            if(outputStream!=null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    Logger.error(this.getClass().getSimpleName(), e);
                }
            }

            if(inputStream!=null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Logger.error(this.getClass().getSimpleName(), e);
                }
            }
        }
    }

    @Override
    protected File findFile(String foldername, String referenceKey) throws FileException {

        String uploadFolder = appCfgMgr.getString("file.disk.path");
        File file = new File(uploadFolder+File.separator+foldername+File.separator+referenceKey);

       if(!file.exists()) {
           throw new FileException(langUtil.at("file.upload.error.filenotfound"));
       }

       return file;
    }
}
