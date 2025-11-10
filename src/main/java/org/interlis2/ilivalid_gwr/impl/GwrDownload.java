package org.interlis2.ilivalid_gwr.impl;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipException;

import ch.interlis.ili2c.Ili2cException;
import ch.interlis.iox.IoxException;

public class GwrDownload {
    private static final String ENV_ILIVALID_GWR_DUMP = "ILIVALID_GWR_DUMP";
    private static final String ENV_ILIVALID_GWR_DUMP_HARDFILE = "ILIVALID_GWR_DUMP_HARDFILE";
    private static final String ILIVALID_GWR_DUMP_DEFAULT = "https://public.madd.bfs.admin.ch/ch.zip";
    private final long MAX_TTL=43200000L;
    private ch.interlis.ilirepository.IliManager iliManager=null;
    public GwrDownload(ch.interlis.ilirepository.IliManager iliManager) {
        this.iliManager=iliManager;
    }
    public File getLocalGwrDump() throws IoxException {
        String fileName=System.getenv(ENV_ILIVALID_GWR_DUMP_HARDFILE);
        if(fileName!=null) {
            return new File(fileName);
        }
        fileName=System.getenv(ENV_ILIVALID_GWR_DUMP);
        if(fileName==null) {
            fileName=ILIVALID_GWR_DUMP_DEFAULT;
        }
        File ret=null;
        try {
            ret = ch.interlis.ilirepository.IliManager.getLocalCopyOfReposFile(iliManager,fileName,MAX_TTL);
        } catch (Ili2cException e) {
            throw new IoxException("failed to get GWR dump file",e);
        }
        java.util.zip.ZipFile zipFile=null;
        try {
            try {
                zipFile = new java.util.zip.ZipFile(ret);
            } catch (ZipException e1) {
                // not a zip file; assume it is directly the sqlite file
                zipFile=null;
            }
            if(zipFile!=null) {
                java.util.zip.ZipEntry entry=zipFile.getEntry("data.sqlite");
                if(entry!=null) {
                    File sqliteFile=new File(ret.getPath()+"-data.sqlite");
                    if(sqliteFile.lastModified()<ret.lastModified()) {
                        // extract sqlite file 
                        java.io.InputStream input=zipFile.getInputStream(entry);
                        copyStream(sqliteFile,input);
                    }
                    ret=sqliteFile;
                }
            }
        } catch (IOException e) {
            throw new IoxException("failed to get GWR dump file",e);
        }finally {
            if(zipFile!=null) {
                try {
                    zipFile.close();
                } catch (IOException e) {
                }
            }
        }
        return ret;
    }
    private static void copyStream(java.io.File outFile, java.io.InputStream in) throws IOException {
        java.io.BufferedOutputStream out=new java.io.BufferedOutputStream(new java.io.FileOutputStream(outFile));
        byte[] bt = new byte[1024];
        int i;
        while((i=in.read(bt)) != -1)
                    {
                        out.write(bt,0,i);
                    }
        out.close();
    }

}
