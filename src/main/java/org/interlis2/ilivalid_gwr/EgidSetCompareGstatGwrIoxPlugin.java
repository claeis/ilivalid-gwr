package org.interlis2.ilivalid_gwr;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import org.interlis2.ilivalid_gwr.impl.GwrDownload;

import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.basics.settings.Settings;
import ch.interlis.ili2c.Ili2cException;
import ch.interlis.ili2c.gui.UserSettings;
import ch.interlis.ili2c.metamodel.ObjectPath;
import ch.interlis.ili2c.metamodel.PathEl;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.ili2c.metamodel.Viewable;
import ch.interlis.iom.IomObject;
import ch.interlis.iox.IoxException;
import ch.interlis.iox.IoxLogging;
import ch.interlis.iox.IoxValidationConfig;
import ch.interlis.iox_j.logging.LogEventFactory;
import ch.interlis.iox_j.validator.InterlisFunction;
import ch.interlis.iox_j.validator.ObjectPool;
import ch.interlis.iox_j.validator.Validator;
import ch.interlis.iox_j.validator.Value;

//FUNCTION egidSetCompareGstatGWR(Objects: OBJECTS OF ANYCLASS; AttrPath: ATTRIBUTE OF @ Objects;municipality: MunicipalityId;gstat:BAG OF NUMERIC): BOOLEAN;
public class EgidSetCompareGstatGwrIoxPlugin implements InterlisFunction {
    public static final String ILI_QUALIFIED_FUNCTION_NAME = "IliValidGwr_V1_0.egidSetCompareGstatGWR";
    private TransferDescription td=null;
    private Validator validator=null;
    private GwrDownload gwr=null;
    private IoxLogging errs=null;
    private LogEventFactory errFact=null;
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
        this.validator=(Validator)settings.getTransientObject(InterlisFunction.IOX_VALIDATOR);
        this.gwr=new GwrDownload(iliManager);
        this.errFact=logEventFactory;
        this.errs=errFact.getLogger();
    }
    
    @Override
    public Value evaluate(String validationKind, String usageScope, IomObject mainObj, Value[] actualArguments) {
        // get egid from arguments
        if(actualArguments[0].isUndefined()) {
            return Value.createUndefined();
        }
        if(actualArguments[1].isUndefined()) {
            return Value.createUndefined();
        }
        String attrPathArg=actualArguments[1].getValue();
        Viewable currentClass=(Viewable) td.getElement(mainObj.getobjecttag());
        
        PathEl attrPath[] = null;
        ObjectPath attrObjPath = null;
        try {
            attrObjPath = validator.parseObjectOrAttributePath(currentClass, attrPathArg);
            if (attrObjPath.getPathElements() != null) {
                PathEl surfaceAttrPathEl[] = attrObjPath.getPathElements();
                attrPath = surfaceAttrPathEl;
            }
        } catch (Ili2cException e) {
            EhiLogger.logError(e);
        }                    
        // get municipality id from arguments
        String municipalityId=null;
        if(!actualArguments[2].isUndefined()) {
            municipalityId=actualArguments[2].getValue();
        }
        if(municipalityId==null) {
            errs.addEvent(errFact.logErrorMsg(ILI_QUALIFIED_FUNCTION_NAME+"() municipality must be defined"));
            return Value.createSkipEvaluation();
        }
        // get CH file from BfS website
        File gwrFile=null;
        try {
            gwrFile = gwr.getLocalGwrDump();
        } catch (IoxException e) {
            EhiLogger.logError(e);
            return Value.createSkipEvaluation();
        }
        java.util.HashSet<Integer> xtfEgids=new java.util.HashSet<Integer>();
        for(IomObject iomObj:actualArguments[0].getComplexObjects()) {
            Value valueOfObjectPath=validator.getValueFromObjectPath(null, iomObj, attrPath, null);
            int egid=Integer.parseInt(valueOfObjectPath.getValue());
            xtfEgids.add(egid);
        }
        java.util.Set<Integer> xtfGstat=new java.util.HashSet<Integer>();
        if(actualArguments[1].isUndefined()) {
            
        }else {
            for(String value:actualArguments[3].getValues()) {
                int gstat=Integer.parseInt(value);
                xtfGstat.add(gstat);
            }
        }
        java.util.Set<Integer> gwrEgids=null;
        try {
            gwrEgids = getGwrEgids(municipalityId,xtfGstat,gwrFile);
        } catch (SQLException e) {
            EhiLogger.logError(e);
            return Value.createSkipEvaluation();
        }
        gwrEgids.removeAll(xtfEgids);
        if(!gwrEgids.isEmpty()) {
            errs.addEvent(errFact.logDetailInfoMsg("EGID "+toString(gwrEgids)+" (from GWR) not in XTF"));
        }
        return new Value(gwrEgids.isEmpty());
    }

    private String toString(Set<Integer> egids) {
        java.util.ArrayList<Integer> egidsS=new java.util.ArrayList<Integer>(egids);
        java.util.Collections.sort(egidsS);
        String sep="";
        StringBuffer ret=new StringBuffer();
        for(Integer egid:egidsS) {
            ret.append(sep);
            ret.append(egid.toString());
            sep=", ";
        }
        return ret.toString();
    }

    private java.util.Set<Integer> getGwrEgids(String municipalityId, java.util.Set<Integer> gstats,File gwrFile) throws SQLException {
        java.util.Set<Integer> egids=new java.util.HashSet<Integer>();
        Connection jdbcConnection=null;
        PreparedStatement stmt=null;
        try {
            jdbcConnection = DriverManager.getConnection("jdbc:sqlite:"+gwrFile, null, null);
            StringBuffer gstatWhere=new StringBuffer();
            if(gstats!=null && gstats.size()>0) {
                gstatWhere.append(" AND GSTAT IN (");
                String sep="";
                java.util.ArrayList<Integer> gstatv=new java.util.ArrayList<Integer>(gstats);
                java.util.Collections.sort(gstatv);
                for(Integer gstat:gstatv) {
                    if(gstat!=null) {
                        gstatWhere.append(sep);
                        gstatWhere.append(gstat.toString());
                        sep=", ";
                    }
                }
                gstatWhere.append(")");
            }
            stmt=jdbcConnection.prepareStatement("SELECT EGID FROM building WHERE GGDENR=?"+gstatWhere.toString());
            stmt.setString(1,municipalityId);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                egids.add(rs.getInt(1));
            }
        }finally {
            if(stmt!=null) {
                stmt.close();
            }
            if(jdbcConnection!=null) {
                jdbcConnection.close();
            }
        }
        return egids;
    }


    @Override
    public String getQualifiedIliName() {
        return ILI_QUALIFIED_FUNCTION_NAME;
    }

}
