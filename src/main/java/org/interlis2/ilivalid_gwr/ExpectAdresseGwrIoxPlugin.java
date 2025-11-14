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
import ch.ehi.basics.tools.StringUtility;
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

// FUNCTION  expectAdresseGWR(Lokalisation: LIST OF TEXT; Hausnummer:TEXT; plz:0..9999; ortschaft:TEXT;egid: GWR_EGID;edid: GWR_EDID;gstatNotEqual:BAG OF NUMERIC):BOOLEAN;
public class ExpectAdresseGwrIoxPlugin implements InterlisFunction {
    public static final String ILI_QUALIFIED_FUNCTION_NAME = "IliValidGwr_V1_0.expectAdresseGWR";
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
     // FUNCTION  expectAdresseGWR(
        // 0 Lokalisation: LIST OF TEXT; 
        // 1 Hausnummer:TEXT; 
        // 2 plz:0..9999; 
        // 3 ortschaft:TEXT;
        // 4 egid: GWR_EGID;
        // 5 edid: GWR_EDID;
        // 6 gstatNotEqual:BAG OF NUMERIC):BOOLEAN;
        if(actualArguments[0].isUndefined()) {
            return Value.createUndefined();
        }
        //if(actualArguments[1].isUndefined()) {
        //    return Value.createUndefined();
        //}
        if(actualArguments[2].isUndefined()) {
            return Value.createUndefined();
        }
        if(actualArguments[3].isUndefined()) {
            return Value.createUndefined();
        }
        if(actualArguments[4].isUndefined()) {
            return Value.createUndefined();
        }
        if(actualArguments[5].isUndefined()) {
            return Value.createUndefined();
        }
        if(actualArguments[6].isUndefined()) {
            return Value.createUndefined();
        }
        String lokalisation[]=actualArguments[0].getValues();
        String hausnummer=actualArguments[1].isUndefined() ? null : actualArguments[1].getValue();
        int plz=Integer.parseInt(actualArguments[2].getValue());
        String ortschaft=actualArguments[3].getValue();
        int egid=Integer.parseInt(actualArguments[4].getValue());
        int edid=Integer.parseInt(actualArguments[5].getValue());
        int gstatNotEqual[]=parseInts(actualArguments[6].getValues());
                
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
            exists = expectAdresseGWR(lokalisation,hausnummer,plz,ortschaft,egid,edid,gstatNotEqual,gwrFile);
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

 // FUNCTION  expectAdresseGWR(Lokalisation: LIST OF TEXT; Hausnummer:TEXT; plz:0..9999; ortschaft:TEXT;egid: GWR_EGID;edid: GWR_EDID;gstatNotEqual:BAG OF NUMERIC):BOOLEAN;
    private boolean expectAdresseGWR(String lokalisation[],String hausnummer, int plz, String ortschaft, int egid, int edid,int gstatNotEqual[],File gwrFile) throws SQLException {
        Connection jdbcConnection=null;
        PreparedStatement stmt=null;
        try {
            jdbcConnection = DriverManager.getConnection("jdbc:sqlite:"+gwrFile, null, null);
            StringBuffer stmtS=new StringBuffer();
            stmtS.append("SELECT entrance.STRNAME,entrance.DEINR,entrance.DPLZ4,entrance.DPLZNAME FROM entrance inner join building on entrance.egid=building.egid WHERE ");
            stmtS.append("entrance.egid=?");
            stmtS.append(" and ");
            stmtS.append("entrance.edid=?");
            {
                stmtS.append(" and ");
                stmtS.append("building.GSTAT not in (");
                String sep="";
                for(int idx=0;idx<gstatNotEqual.length;idx++) {
                    stmtS.append(sep);
                    stmtS.append("?");
                    sep=",";
                }
                stmtS.append(")");
            }
            stmt=jdbcConnection.prepareStatement(stmtS.toString());
            int pi=1;
            stmt.setInt(pi++,egid);
            stmt.setInt(pi++,edid);
            for(int idx=0;idx<gstatNotEqual.length;idx++) {
                stmt.setInt(pi++,gstatNotEqual[idx]);
            }
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                {
                    java.util.HashSet<String> lokv=new java.util.HashSet<String>();
                    for(String lok:lokalisation) {
                        if(lok!=null && lok.length()>0) {
                            lokv.add(lok);
                        }
                    }
                    if(!lokv.contains(rs.getString(1))){
                        return false;
                    }
                }
                String nr=StringUtility.purge(rs.getString(2));
                hausnummer=StringUtility.purge(hausnummer);
                if(hausnummer!=null && !hausnummer.equals(nr)) {
                    return false;
                }
                if(hausnummer==null && nr!=null) {
                    return false;
                }
                if(plz!=rs.getInt(3)) {
                    return false;
                }
                if(!ortschaft.equals(rs.getString(4))) {
                    return false;
                }
                return true;
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
