package org.interlis2.ilivalid_gwr;

import org.junit.Test;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ili2c.config.Configuration;
import ch.interlis.ili2c.config.FileEntry;
import ch.interlis.ili2c.config.FileEntryKind;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.iom_j.Iom_jObject;
import ch.interlis.iox_j.EndBasketEvent;
import ch.interlis.iox_j.EndTransferEvent;
import ch.interlis.iox_j.ObjectEvent;
import ch.interlis.iox_j.StartBasketEvent;
import ch.interlis.iox_j.StartTransferEvent;
import ch.interlis.iox_j.logging.LogEventFactory;
import ch.interlis.iox_j.validator.ValidationConfig;
import ch.interlis.iox_j.validator.Validator;

import org.junit.Before;

import org.junit.Assert;

import java.util.Map;

public class EdidExistsInGwrPluginTest {
    private static final String ILI_FUNCTION_NAME=EdidExistsInGwrPlugin.ILI_QUALIFIED_FUNCTION_NAME;
    private static final Class PLUGIN=EdidExistsInGwrPlugin.class;
    private static final String ILI_MODEL="EdidExistsTest";
    private static final String ILI_TOPIC_A=ILI_MODEL+".TopicA";
    private static final String ILI_A_GEBAEUDEEINGANG_MUNICIPALITY_ID = "MunicipalityId";
    private static final String ILI_A_GEBAEUDEEINGANG_EGID = "EGID";
    private static final String ILI_A_GEBAEUDEEINGANG_EDID = "EDID";
    private static final String ILI_A_GEBAEUDEEINGANG = ILI_TOPIC_A+".Gebaeudeeingang";

    private TransferDescription td=null;
    @Before
    public void setUp() throws Exception {
        // ili-datei lesen
        Configuration ili2cConfig=new Configuration();
        ili2cConfig.addFileEntry(new FileEntry("src/test/ili/IliValidGwr_V1_0.ili", FileEntryKind.ILIMODELFILE));
        ili2cConfig.addFileEntry(new FileEntry("src/test/ili/EdidExistsTest.ili", FileEntryKind.ILIMODELFILE));
        td=ch.interlis.ili2c.Ili2c.runCompiler(ili2cConfig);
        Assert.assertNotNull(td);
    }
    @Test
    public void edidExists_Ok() {
        Map<String,Class> userFunctions=new java.util.HashMap<String,Class>();
        userFunctions.put(ILI_FUNCTION_NAME,PLUGIN);
        Iom_jObject iomObj1=new Iom_jObject(ILI_A_GEBAEUDEEINGANG, "o1");
        iomObj1.setattrvalue(ILI_A_GEBAEUDEEINGANG_EGID,"191956173");
        iomObj1.setattrvalue(ILI_A_GEBAEUDEEINGANG_EDID,"0");
        iomObj1.setattrvalue(ILI_A_GEBAEUDEEINGANG_MUNICIPALITY_ID,"5391");
        ValidationConfig modelConfig=new ValidationConfig();
        LogCollector logger=new LogCollector();
        LogEventFactory errFactory=new LogEventFactory();
        Settings settings=new Settings();
        settings.setTransientObject(ch.interlis.iox_j.validator.Validator.CONFIG_CUSTOM_FUNCTIONS, userFunctions);
        Validator validator=new Validator(td, modelConfig,logger,errFactory,settings);
        validator.validate(new StartTransferEvent());
        validator.validate(new StartBasketEvent(ILI_TOPIC_A,"b1"));
        validator.validate(new ObjectEvent(iomObj1));
        validator.validate(new EndBasketEvent());
        validator.validate(new EndTransferEvent());
        // Asserts
        Assert.assertEquals(0,logger.getErrs().size());
    }
    @Test
    public void edidExists_Fail() {
        Map<String,Class> userFunctions=new java.util.HashMap<String,Class>();
        userFunctions.put(ILI_FUNCTION_NAME,PLUGIN);
        Iom_jObject iomObj1=new Iom_jObject(ILI_A_GEBAEUDEEINGANG, "o1");
        iomObj1.setattrvalue(ILI_A_GEBAEUDEEINGANG_EGID,"1");
        iomObj1.setattrvalue(ILI_A_GEBAEUDEEINGANG_EDID,"0");
        iomObj1.setattrvalue(ILI_A_GEBAEUDEEINGANG_MUNICIPALITY_ID,"5391");
        ValidationConfig modelConfig=new ValidationConfig();
        LogCollector logger=new LogCollector();
        LogEventFactory errFactory=new LogEventFactory();
        Settings settings=new Settings();
        settings.setTransientObject(ch.interlis.iox_j.validator.Validator.CONFIG_CUSTOM_FUNCTIONS, userFunctions);
        Validator validator=new Validator(td, modelConfig,logger,errFactory,settings);
        validator.validate(new StartTransferEvent());
        validator.validate(new StartBasketEvent(ILI_TOPIC_A,"b1"));
        validator.validate(new ObjectEvent(iomObj1));
        validator.validate(new EndBasketEvent());
        validator.validate(new EndTransferEvent());
        // Asserts
        Assert.assertEquals(1,logger.getErrs().size());
        Assert.assertEquals("Mandatory Constraint EdidExistsTest.TopicA.Gebaeudeeingang.GWRC03 is not true.",logger.getErrs().get(0).getEventMsg());
    }
    @Test
    public void edidExists_undefinedMunicipality_Ok() {
        Map<String,Class> userFunctions=new java.util.HashMap<String,Class>();
        userFunctions.put(ILI_FUNCTION_NAME,PLUGIN);
        Iom_jObject iomObj1=new Iom_jObject(ILI_A_GEBAEUDEEINGANG, "o1");
        iomObj1.setattrvalue(ILI_A_GEBAEUDEEINGANG_EGID,"191956173");
        iomObj1.setattrvalue(ILI_A_GEBAEUDEEINGANG_EDID,"0");
        ValidationConfig modelConfig=new ValidationConfig();
        LogCollector logger=new LogCollector();
        LogEventFactory errFactory=new LogEventFactory();
        Settings settings=new Settings();
        settings.setTransientObject(ch.interlis.iox_j.validator.Validator.CONFIG_CUSTOM_FUNCTIONS, userFunctions);
        Validator validator=new Validator(td, modelConfig,logger,errFactory,settings);
        validator.validate(new StartTransferEvent());
        validator.validate(new StartBasketEvent(ILI_TOPIC_A,"b1"));
        validator.validate(new ObjectEvent(iomObj1));
        validator.validate(new EndBasketEvent());
        validator.validate(new EndTransferEvent());
        // Asserts
        Assert.assertEquals(0,logger.getErrs().size());
    }
    @Test
    public void edidExists_undefinedMunicipality_Fail() {
        Map<String,Class> userFunctions=new java.util.HashMap<String,Class>();
        userFunctions.put(ILI_FUNCTION_NAME,PLUGIN);
        Iom_jObject iomObj1=new Iom_jObject(ILI_A_GEBAEUDEEINGANG, "o1");
        iomObj1.setattrvalue(ILI_A_GEBAEUDEEINGANG_EGID,"999999999");
        iomObj1.setattrvalue(ILI_A_GEBAEUDEEINGANG_EDID,"0");
        ValidationConfig modelConfig=new ValidationConfig();
        LogCollector logger=new LogCollector();
        LogEventFactory errFactory=new LogEventFactory();
        Settings settings=new Settings();
        settings.setTransientObject(ch.interlis.iox_j.validator.Validator.CONFIG_CUSTOM_FUNCTIONS, userFunctions);
        Validator validator=new Validator(td, modelConfig,logger,errFactory,settings);
        validator.validate(new StartTransferEvent());
        validator.validate(new StartBasketEvent(ILI_TOPIC_A,"b1"));
        validator.validate(new ObjectEvent(iomObj1));
        validator.validate(new EndBasketEvent());
        validator.validate(new EndTransferEvent());
        // Asserts
        Assert.assertEquals(1,logger.getErrs().size());
        Assert.assertEquals("Mandatory Constraint EdidExistsTest.TopicA.Gebaeudeeingang.GWRC03 is not true.",logger.getErrs().get(0).getEventMsg());
    }
}
