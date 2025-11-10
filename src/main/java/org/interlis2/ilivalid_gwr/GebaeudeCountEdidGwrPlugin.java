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

// FUNCTION gebaeudeCountEdidGWR(egid: GWR_EGID): 0..99; !! Anzahl Eingaenge im GWR
public class GebaeudeCountEdidGwrPlugin implements InterlisFunction {
    public static final String ILI_QUALIFIED_FUNCTION_NAME = "IliValidGwr_V1_0.gebaeudeCountEdidGWR";
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
        int egid=Integer.parseInt(actualArguments[0].getValue());
        // get CH file from BfS website
        File gwrFile=null;
        try {
            gwrFile = gwr.getLocalGwrDump();
        } catch (IoxException e) {
            EhiLogger.logError(e);
            return Value.createSkipEvaluation();
        }
        Integer stat=null;
        try {
            stat = gebaeudeCountEdidGWR(egid,gwrFile);
        } catch (SQLException e) {
            EhiLogger.logError(e);
            return Value.createSkipEvaluation();
        }
        if(stat!=null) {
            return new Value(new ch.interlis.ili2c.metamodel.NumericType(),stat.toString());
        }
        return Value.createUndefined();
    }

    private Integer gebaeudeCountEdidGWR(int egid, File gwrFile) throws SQLException {
        Connection jdbcConnection=null;
        PreparedStatement stmt=null;
        try {
            jdbcConnection = DriverManager.getConnection("jdbc:sqlite:"+gwrFile, null, null);
            stmt=jdbcConnection.prepareStatement("SELECT count(*) FROM entrance WHERE EGID=?");
            stmt.setInt(1,egid);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                int count=rs.getInt(1);
                return count;
            }
        }finally {
            if(stmt!=null) {
                stmt.close();
            }
            if(jdbcConnection!=null) {
                jdbcConnection.close();
            }
        }
        return null;
    }


    @Override
    public String getQualifiedIliName() {
        return ILI_QUALIFIED_FUNCTION_NAME;
    }

}
