package org.interlis2.ilivalid_gwr;

import org.junit.Test;
import org.interlis2.ilivalid_gwr.impl.GwrDownload;
import org.junit.Assert;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class GwrDownloadTest {
    @Test 
    public void downloadFile() throws Exception {
        ch.interlis.ilirepository.IliManager iliManager=new ch.interlis.ilirepository.IliManager();
        GwrDownload gwr = new GwrDownload(iliManager);
        File gwrFile=gwr.getLocalGwrDump();
        Connection jdbcConnection=null;
        PreparedStatement stmt=null;
        try {
            jdbcConnection = DriverManager.getConnection("jdbc:sqlite:"+gwrFile, null, null);
            stmt=jdbcConnection.prepareStatement("SELECT count(*) FROM building");
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                Integer v=rs.getInt(1);
                Assert.assertNotNull(v);
            }
        }finally {
            if(stmt!=null) {
                stmt.close();
            }
            if(jdbcConnection!=null) {
                jdbcConnection.close();
            }
        }
        
    }
}
