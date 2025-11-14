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

// FUNCTION  strassenbezeichnungExistsInGWR(names: LIST OF TEXT; municipality: MunicipalityId): BOOLEAN;
public class StrassenbezeichnungExistsInGwrIoxPlugin implements InterlisFunction {
    public static final String ILI_QUALIFIED_FUNCTION_NAME = "IliValidGwr_V1_0.strassenbezeichnungExistsInGWR";
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
        if(actualArguments[0].isUndefined()) {
            return Value.createUndefined();
        }
        if(actualArguments[1].isUndefined()) {
            return Value.createUndefined();
        }
        String names[]=actualArguments[0].getValues();
        if(names==null) {
            names=new String[1];
            names[0]=actualArguments[0].getValue();
        }
        String municipalityId=null;
        if(!actualArguments[1].isUndefined()) {
            municipalityId=actualArguments[1].getValue();
        }
        // get CH file from BfS website
        File gwrFile=null;
        try {
            gwrFile = gwr.getLocalGwrDump();
        } catch (IoxException e) {
            EhiLogger.logError(e);
            return Value.createSkipEvaluation();
        }
        boolean nameExists=false;
        try {
            nameExists = strassenbezeichnungExistsInGWR(names,municipalityId,gwrFile);
        } catch (SQLException e) {
            EhiLogger.logError(e);
            return Value.createSkipEvaluation();
        }
        return new Value(nameExists);
    }

    private boolean strassenbezeichnungExistsInGWR(String names[],String municipalityId, File gwrFile) throws SQLException {
        Connection jdbcConnection=null;
        PreparedStatement stmt=null;
        try {
            jdbcConnection = DriverManager.getConnection("jdbc:sqlite:"+gwrFile, null, null);
            StringBuffer stmtS=new StringBuffer();
            stmtS.append("SELECT count(strname) FROM entrance inner join building on entrance.egid=building.egid WHERE GGDENR=? and strname in (");
            String sep="";
            for(int idx=0;idx<names.length;idx++) {
                stmtS.append(sep);
                stmtS.append("?");
                sep=",";
            }
            stmtS.append(")");
            stmt=jdbcConnection.prepareStatement(stmtS.toString());
            stmt.setString(1,municipalityId);
            for(int idx=0;idx<names.length;idx++) {
                stmt.setString(2+idx,names[idx]);
            }
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                return rs.getInt(1)>=1;
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
