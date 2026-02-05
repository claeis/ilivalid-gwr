package org.interlis2.ilivalid_gwr;

import org.junit.Test;

import ch.ehi.basics.logging.EhiLogger;
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

public class EgidSetCompareGwrPluginTest {
    private static final String ILI_FUNCTION_NAME=EgidSetCompareGwrIoxPlugin.ILI_QUALIFIED_FUNCTION_NAME;
    private static final Class PLUGIN=EgidSetCompareGwrIoxPlugin.class;
    private static final String ILI_MODEL="EgidSetCompareTest";
    private static final String ILI_TOPIC_A=ILI_MODEL+".TopicA";
    private static final String ILI_A_BODENBEDECKUNG_MUNICIPALITY_ID = "MunicipalityId";
    private static final String ILI_A_BODENBEDECKUNG_EGID = "EGID";
    private static final String ILI_A_BODENBEDECKUNG = ILI_TOPIC_A+".Bodenbedeckung";

    private TransferDescription td=null;
    @Before
    public void setUp() throws Exception {
        // ili-datei lesen
        Configuration ili2cConfig=new Configuration();
        ili2cConfig.addFileEntry(new FileEntry("src/test/ili/IliValidGwr_V1_0.ili", FileEntryKind.ILIMODELFILE));
        ili2cConfig.addFileEntry(new FileEntry("src/test/ili/EgidSetCompareTest.ili", FileEntryKind.ILIMODELFILE));
        td=ch.interlis.ili2c.Ili2c.runCompiler(ili2cConfig);
        Assert.assertNotNull(td);
    }
    @Test
    public void pluginClassName_Ok() {
        Assert.assertTrue(PLUGIN.getName().endsWith(ch.interlis.iox_j.plugins.PluginLoader.IOX_PLUGIN));
    }
    @Test
    public void egidExists_Ok() {
        Map<String,Class> userFunctions=new java.util.HashMap<String,Class>();
        userFunctions.put(ILI_FUNCTION_NAME,PLUGIN);
        
        
        ValidationConfig modelConfig=new ValidationConfig();
        LogCollector logger=new LogCollector();
        LogEventFactory errFactory=new LogEventFactory();
        Settings settings=new Settings();
        settings.setTransientObject(ch.interlis.iox_j.validator.Validator.CONFIG_CUSTOM_FUNCTIONS, userFunctions);
        Validator validator=new Validator(td, modelConfig,logger,errFactory,settings);
        validator.validate(new StartTransferEvent());
        validator.validate(new StartBasketEvent(ILI_TOPIC_A,"b1"));
        {
            Iom_jObject iomObj1=new Iom_jObject(ILI_A_BODENBEDECKUNG, "o1");
            iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_EGID,"191956173");
            iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_MUNICIPALITY_ID,"5391");
            validator.validate(new ObjectEvent(iomObj1));
        }
        {
            Iom_jObject iomObj1=new Iom_jObject(ILI_A_BODENBEDECKUNG, "o2");
            iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_EGID,"191956175");
            iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_MUNICIPALITY_ID,"5391");
            validator.validate(new ObjectEvent(iomObj1));
        }
        {
            Iom_jObject iomObj1=new Iom_jObject(ILI_A_BODENBEDECKUNG, "o3");
            iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_EGID,"191956177");
            iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_MUNICIPALITY_ID,"5391");
            validator.validate(new ObjectEvent(iomObj1));
        }
        {
            Iom_jObject iomObj1=new Iom_jObject(ILI_A_BODENBEDECKUNG, "o4");
            iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_EGID,"191956178");
            iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_MUNICIPALITY_ID,"5391");
            validator.validate(new ObjectEvent(iomObj1));
        }
        {
            Iom_jObject iomObj1=new Iom_jObject(ILI_A_BODENBEDECKUNG, "o5");
            iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_EGID,"192057172");
            iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_MUNICIPALITY_ID,"5391");
            validator.validate(new ObjectEvent(iomObj1));
        }
        validator.validate(new EndBasketEvent());
        validator.validate(new EndTransferEvent());
        // Asserts
        Assert.assertEquals(0,logger.getErrs().size());
    }
    @Test
    public void egidExists_Fail() {
        EhiLogger.getInstance().setTraceFilter(false);
        Map<String,Class> userFunctions=new java.util.HashMap<String,Class>();
        userFunctions.put(ILI_FUNCTION_NAME,PLUGIN);
        ValidationConfig modelConfig=new ValidationConfig();
        LogCollector logger=new LogCollector();
        LogEventFactory errFactory=new LogEventFactory();
        Settings settings=new Settings();
        settings.setTransientObject(ch.interlis.iox_j.validator.Validator.CONFIG_CUSTOM_FUNCTIONS, userFunctions);
        Validator validator=new Validator(td, modelConfig,logger,errFactory,settings);
        validator.validate(new StartTransferEvent());
        validator.validate(new StartBasketEvent(ILI_TOPIC_A,"b1"));
        {
            Iom_jObject iomObj1=new Iom_jObject(ILI_A_BODENBEDECKUNG, "o1");
            iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_EGID,"191956173");
            iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_MUNICIPALITY_ID,"5391");
            validator.validate(new ObjectEvent(iomObj1));
        }
        validator.validate(new EndBasketEvent());
        validator.validate(new EndTransferEvent());
        // Asserts
        Assert.assertEquals(1,logger.getErrs().size());
        Assert.assertEquals("Set Constraint EgidSetCompareTest.TopicA.Bodenbedeckung.GWRC04 is not true.",logger.getErrs().get(0).getEventMsg());
    }
    @Test
    public void egidExists_undefinedMunicipality_Fail() {
        Map<String,Class> userFunctions=new java.util.HashMap<String,Class>();
        userFunctions.put(ILI_FUNCTION_NAME,PLUGIN);
        ValidationConfig modelConfig=new ValidationConfig();
        LogCollector logger=new LogCollector();
        LogEventFactory errFactory=new LogEventFactory();
        Settings settings=new Settings();
        settings.setTransientObject(ch.interlis.iox_j.validator.Validator.CONFIG_CUSTOM_FUNCTIONS, userFunctions);
        Validator validator=new Validator(td, modelConfig,logger,errFactory,settings);
        validator.validate(new StartTransferEvent());
        validator.validate(new StartBasketEvent(ILI_TOPIC_A,"b1"));
        {
            Iom_jObject iomObj1=new Iom_jObject(ILI_A_BODENBEDECKUNG, "o1");
            iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_EGID,"191956173");
            //iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_MUNICIPALITY_ID,"5391"); // undefined municipality triggers error
            validator.validate(new ObjectEvent(iomObj1));
        }
        validator.validate(new EndBasketEvent());
        validator.validate(new EndTransferEvent());
        // Asserts
        Assert.assertEquals(1,logger.getErrs().size());
        Assert.assertEquals("IliValidGwr_V1_0.egidSetCompareGWR() municipality must be defined",logger.getErrs().get(0).getEventMsg());
    }
}
