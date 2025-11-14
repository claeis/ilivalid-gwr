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

// FUNCTION edidExistsInGWR(egid: GWR_EGID;edid:GWR_EDID;municipality: MunicipalityId): BOOLEAN;
public class EdidExistsInGwrIoxPlugin implements InterlisFunction {
    public static final String ILI_QUALIFIED_FUNCTION_NAME = "IliValidGwr_V1_0.edidExistsInGWR";
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
        int egid=Integer.parseInt(actualArguments[0].getValue());
        int edid=Integer.parseInt(actualArguments[1].getValue());
        String municipalityId=null;
        if(!actualArguments[2].isUndefined()) {
            municipalityId=actualArguments[2].getValue();
        }
        // get CH file from BfS website
        File gwrFile=null;
        try {
            gwrFile = gwr.getLocalGwrDump();
        } catch (IoxException e) {
            EhiLogger.logError(e);
            return Value.createSkipEvaluation();
        }
        boolean edidExists=false;
        try {
            edidExists = edidExistsInGWR(egid,edid,municipalityId,gwrFile);
        } catch (SQLException e) {
            EhiLogger.logError(e);
            return Value.createSkipEvaluation();
        }
        return new Value(edidExists);
    }

    private boolean edidExistsInGWR(int egid,int edid,String municipalityId, File gwrFile) throws SQLException {
        List<String> ret=new ArrayList<String>();
        Connection jdbcConnection=null;
        PreparedStatement stmt=null;
        try {
            jdbcConnection = DriverManager.getConnection("jdbc:sqlite:"+gwrFile, null, null);
            if(municipalityId!=null) {
                stmt=jdbcConnection.prepareStatement("SELECT entrance.EGID,entrance.EDID FROM entrance inner join building on entrance.egid=building.egid WHERE entrance.EGID=? and entrance.EDID=? AND GGDENR=?");
                stmt.setInt(1,egid);
                stmt.setInt(2,edid);
                stmt.setString(3,municipalityId);
            }else {
                stmt=jdbcConnection.prepareStatement("SELECT entrance.EGID,entrance.EDID FROM entrance inner join building on entrance.egid=building.egid WHERE entrance.EGID=? and entrance.EDID=?");
                stmt.setInt(1,egid);
                stmt.setInt(2,edid);
            }
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
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
