package org.interlis2.ilivalid_gwr;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.interlis2.ilivalid_gwr.impl.GwrDownload;

import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.basics.settings.Settings;
import ch.interlis.ili2c.Ili2cException;
import ch.interlis.ili2c.gui.UserSettings;
import ch.interlis.ili2c.metamodel.Function;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.iom.IomObject;
import ch.interlis.iox.IoxException;
import ch.interlis.iox.IoxValidationConfig;
import ch.interlis.iox_j.logging.LogEventFactory;
import ch.interlis.iox_j.validator.InterlisFunction;
import ch.interlis.iox_j.validator.ObjectPool;
import ch.interlis.iox_j.validator.Value;

// FUNCTION  expectEgidEdidGWR(egid: GWR_EGID;edid: GWR_EDID;Lokalisation: LIST OF TEXT; Hausnummer:TEXT; plz:0..9999; ortschaft:TEXT;gstatEqual:BAG OF NUMERIC):BOOLEAN;
public class ExpectEgidEdidGwrPlugin implements InterlisFunction {
    public static final String ILI_QUALIFIED_FUNCTION_NAME = "IliValidGwr_V1_0.expectEgidEdidGWR";
    private TransferDescription td=null;
    private GwrDownload gwr=null;
    @Override
    public void init(TransferDescription td, Settings settings,
            IoxValidationConfig validationConfig, ObjectPool objectPool,
            LogEventFactory logEventFactory) {
        ch.interlis.ilirepository.IliManager iliManager=(ch.interlis.ilirepository.IliManager) settings
                .getTransientObject(UserSettings.CUSTOM_ILI_MANAGER);
        if(iliManager==null) {
            iliManager=new ch.interlis.ilirepository.IliManager();
        }
        this.td=td;
        gwr=new GwrDownload(iliManager);
    }
    
    @Override
    public Value evaluate(String validationKind, String usageScope, IomObject mainObj, Value[] actualArguments) {
     // FUNCTION  expectEgidEdidGWR(
        // 0 egid: GWR_EGID;
        // 1 edid: GWR_EDID;
        // 2 Lokalisation: LIST OF TEXT; 
        // 3 Hausnummer:TEXT; 
        // 4 plz:0..9999; 
        // 5 ortschaft:TEXT;
        // 6 gstatEqual:BAG OF NUMERIC):BOOLEAN;
        if(actualArguments[0].isUndefined()) {
            return Value.createUndefined();
        }
        if(actualArguments[1].isUndefined()) {
            return Value.createUndefined();
        }
        if(actualArguments[2].isUndefined()) {
            return Value.createUndefined();
        }
        //if(actualArguments[3].isUndefined()) {
        //    return Value.createUndefined();
        //}
        if(actualArguments[4].isUndefined()) {
            return Value.createUndefined();
        }
        if(actualArguments[5].isUndefined()) {
            return Value.createUndefined();
        }
        if(actualArguments[6].isUndefined()) {
            return Value.createUndefined();
        }
        int egid=Integer.parseInt(actualArguments[0].getValue());
        int edid=Integer.parseInt(actualArguments[1].getValue());
        String lokalisation[]=actualArguments[2].getValues();
        String hausnummer=actualArguments[3].isUndefined() ? null : actualArguments[3].getValue();
        int plz=Integer.parseInt(actualArguments[4].getValue());
        String ortschaft=actualArguments[5].getValue();
        int gstatEqual[]=parseInts(actualArguments[6].getValues());
                
        // get CH file from BfS website
        File gwrFile=null;
        try {
            gwrFile = gwr.getLocalGwrDump();
        } catch (IoxException e) {
            EhiLogger.logError(e);
            return Value.createSkipEvaluation();
        }
        boolean exists=false;
        try {
            exists = expectEgidEdidGWR(egid,edid,lokalisation,hausnummer,plz,ortschaft,gstatEqual,gwrFile);
        } catch (SQLException e) {
            EhiLogger.logError(e);
            return Value.createSkipEvaluation();
        }
        return new Value(exists);
    }

    private int[] parseInts(String[] values) {
        int ret[]=new int[values.length];
        for(int i=0;i<values.length;i++) {
            ret[i]=Integer.parseInt(values[i]);
        }
        return ret;
    }

// FUNCTION  expectEgidEdidGWR(egid: GWR_EGID;edid: GWR_EDID;Lokalisation: LIST OF TEXT; Hausnummer:TEXT; plz:0..9999; ortschaft:TEXT;gstatEqual:BAG OF NUMERIC):BOOLEAN;
    private boolean expectEgidEdidGWR(int egid, int edid,String lokalisation[],String hausnummer, int plz, String ortschaft, int gstatEqual[],File gwrFile) throws SQLException {
        Connection jdbcConnection=null;
        PreparedStatement stmt=null;
        try {
            jdbcConnection = DriverManager.getConnection("jdbc:sqlite:"+gwrFile, null, null);
            StringBuffer stmtS=new StringBuffer();
            stmtS.append("SELECT entrance.egid,entrance.edid FROM entrance inner join building on entrance.egid=building.egid WHERE ");
            if(hausnummer!=null) {
                stmtS.append("entrance.DEINR=?"); // hausnummer
            }else {
                //stmtS.append("entrance.DEINR is null");
                stmtS.append("entrance.DEINR=''");
            }
            stmtS.append(" and ");
            stmtS.append("entrance.DPLZ4=?"); // plz
            stmtS.append(" and ");
            stmtS.append("entrance.DPLZNAME=?"); // ortschaft
            {
                stmtS.append(" and ");
                stmtS.append("entrance.STRNAME in (");
                String sep="";
                for(int idx=0;idx<lokalisation.length;idx++) {
                    stmtS.append(sep);
                    stmtS.append("?");
                    sep=",";
                }
                stmtS.append(")");
            }
            {
                stmtS.append(" and ");
                stmtS.append("building.GSTAT in (");
                String sep="";
                for(int idx=0;idx<gstatEqual.length;idx++) {
                    stmtS.append(sep);
                    stmtS.append("?");
                    sep=",";
                }
                stmtS.append(")");
            }
            stmt=jdbcConnection.prepareStatement(stmtS.toString());
            int pi=1;
            if(hausnummer!=null) {
                stmt.setString(pi++,hausnummer);
            }
            stmt.setInt(pi++,plz);
            stmt.setString(pi++,ortschaft);
            for(int idx=0;idx<lokalisation.length;idx++) {
                stmt.setString(pi++,lokalisation[idx]);
            }
            for(int idx=0;idx<gstatEqual.length;idx++) {
                stmt.setInt(pi++,gstatEqual[idx]);
            }
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                return rs.getInt(1)==egid && rs.getInt(2)==edid;
            }
        }finally {
            if(stmt!=null) {
                stmt.close();
            }
            if(jdbcConnection!=null) {
                jdbcConnection.close();
            }
        }
        return false;
    }


    @Override
    public String getQualifiedIliName() {
        return ILI_QUALIFIED_FUNCTION_NAME;
    }

}
