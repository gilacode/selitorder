package com.avicenna.file;

import com.avicenna.config.AppCfgMgr;
import com.avicenna.util.LangUtil;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.inject.Inject;
import org.apache.commons.collections4.ListUtils;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class FileMgr {

    protected final LangUtil langUtil;

    protected final AppCfgMgr appCfgMgr;
    protected final FileIdxMdl.FileIdxProv fileIdxProv;

    @Inject
    public FileMgr(LangUtil langUtil, AppCfgMgr appCfgMgr, FileIdxMdl.FileIdxProv fileIdxProv) {

        this.langUtil = langUtil;

        this.appCfgMgr = appCfgMgr;
        this.fileIdxProv = fileIdxProv;

        appCfgMgr.registerConfig(new AppCfgMgr.AppCfgDto("file.size.max", "100", "Max File Size in MB", "File"));
    }

    public FileDto upload(String folderName, String referenceKey, String mimeType, String extension, File file) throws FileException {

        FileIdxMdl fileIdx = fileIdxProv.get().query().where()
                .eq("folderName", folderName)
                .eq("referenceKey", referenceKey)
                .findOne();

        if(fileIdx==null) {
            fileIdx = new FileIdxMdl();
            fileIdx.setFolderName(folderName);
            fileIdx.setReferenceKey(referenceKey);
        }

        fileIdx.setMimeType(mimeType);
        fileIdx.setFileExt(extension);

        FileAttribute fileAttribute = uploadFile(folderName, referenceKey, mimeType, extension, file);
        fileIdx.setFilePath(fileAttribute.getFilePath());
        fileIdx.setFileUrl(fileAttribute.getFileUrl());

        fileIdx.save();

        return new FileDto(fileIdx);
    }

    public FileDto find(String folderName, String referenceKey, boolean includeFile) throws FileException {

        FileIdxMdl fileIdx = fileIdxProv.get().query().where()
                .eq("folderName", folderName)
                .eq("referenceKey", referenceKey)
                .findOne();

        if(fileIdx!=null) {
            FileDto fileDto = new FileDto(fileIdx);
            if(includeFile) {
                File file = findFile(folderName, referenceKey);
                if(file == null) {
                    throw new FileException(langUtil.at("file.upload.error.filenotfound"));
                }
                fileDto.setFile(file);
                return fileDto;
            }
        }

        return null;
    }

    public List<FileDto> get(String folderName) {

        List<FileIdxMdl> fileIdxs = ListUtils.emptyIfNull(
                fileIdxProv.get().query().where()
                .eq("folderName", folderName)
                .findList());

        return fileIdxs.stream()
                .map(f -> new FileDto(f))
                .collect(Collectors.toList());
    }

    protected abstract FileAttribute uploadFile(String foldername, String referenceKey, String mimeType, String extension, File inputFile) throws FileException;
    protected abstract File findFile(String foldername, String referenceKey) throws FileException;

    public static class FileAttribute {

        private final String filePath;
        private final String fileUrl;
        private final String mimeType;
        private final String fileExt;

        @JsonCreator
        public FileAttribute(String filePath, String fileUrl, String mimeType, String fileExt) {
            this.filePath = filePath;
            this.fileUrl = fileUrl;
            this.mimeType = mimeType;
            this.fileExt = fileExt;
        }

        public String getFilePath() {
            return filePath;
        }

        public String getFileUrl() {
            return fileUrl;
        }

        public String getMimeType() {
            return mimeType;
        }

        public String getFileExt() {
            return fileExt;
        }
    }

    public static class FileDto {

        private final String folderName;
        private final String referenceKey;
        private File file;
        private FileAttribute fileAttribute;

        @JsonCreator
        public FileDto(String folderName, String referenceKey, File file) {
            this.folderName = folderName;
            this.referenceKey = referenceKey;
            this.file = file;
        }

        public FileDto(String folderName, String referenceKey) {
            this.folderName = folderName;
            this.referenceKey = referenceKey;
        }

        public FileDto(FileIdxMdl db) {
            this.folderName = db.getFolderName();
            this.referenceKey = db.getReferenceKey();
            this.fileAttribute = new FileAttribute(db.getFilePath(), db.getFileUrl(), db.getMimeType(), db.getFileExt());
        }

        public String getFolderName() {
            return folderName;
        }

        public String getReferenceKey() {
            return referenceKey;
        }

        public File getFile() {
            return file;
        }

        public void setFile(File file) {
            this.file = file;
        }

        public FileAttribute getFileAttribute() {
            return fileAttribute;
        }

        public void setFileAttribute(FileAttribute fileAttribute) {
            this.fileAttribute = fileAttribute;
        }
    }
}
