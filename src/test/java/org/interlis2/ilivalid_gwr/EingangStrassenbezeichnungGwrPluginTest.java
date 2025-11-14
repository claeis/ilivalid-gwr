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

public class EingangStrassenbezeichnungGwrPluginTest {
    private static final String ILI_FUNCTION_NAME=EingangStrassenbezeichnungGwrIoxPlugin.ILI_QUALIFIED_FUNCTION_NAME;
    private static final Class PLUGIN=EingangStrassenbezeichnungGwrIoxPlugin.class;
    private static final String ILI_MODEL="EingangStrassenbezeichnungTest";
    private static final String ILI_TOPIC_A=ILI_MODEL+".TopicA";
    private static final String ILI_A_BODENBEDECKUNG_EGID = "EGID";
    private static final String ILI_A_BODENBEDECKUNG_EDID = "EDID";
    private static final String ILI_A_BODENBEDECKUNG_STRNAME = "strname";
    private static final String ILI_A_BODENBEDECKUNG = ILI_TOPIC_A+".Bodenbedeckung";

    private TransferDescription td=null;
    @Before
    public void setUp() throws Exception {
        // ili-datei lesen
        Configuration ili2cConfig=new Configuration();
        ili2cConfig.addFileEntry(new FileEntry("src/test/ili/IliValidGwr_V1_0.ili", FileEntryKind.ILIMODELFILE));
        ili2cConfig.addFileEntry(new FileEntry("src/test/ili/EingangStrassenbezeichnungTest.ili", FileEntryKind.ILIMODELFILE));
        td=ch.interlis.ili2c.Ili2c.runCompiler(ili2cConfig);
        Assert.assertNotNull(td);
    }
    @Test
    public void pluginClassName_Ok() {
        Assert.assertTrue(PLUGIN.getName().endsWith(ch.interlis.iox_j.plugins.PluginLoader.IOX_PLUGIN));
    }
    @Test
    public void egidExists_gstatValues_Ok() {
        Map<String,Class> userFunctions=new java.util.HashMap<String,Class>();
        userFunctions.put(ILI_FUNCTION_NAME,PLUGIN);
        Iom_jObject iomObj1=new Iom_jObject(ILI_A_BODENBEDECKUNG, "o1");
        iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_EGID,"191956173");
        iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_EDID,"0");
        iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_STRNAME,"Corte Inferiore");
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
    public void egidExists_Fail() {
        Map<String,Class> userFunctions=new java.util.HashMap<String,Class>();
        userFunctions.put(ILI_FUNCTION_NAME,PLUGIN);
        Iom_jObject iomObj1=new Iom_jObject(ILI_A_BODENBEDECKUNG, "o1");
        iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_EGID,"191956173");
        iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_EDID,"0");
        iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_STRNAME,"x");
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
        Assert.assertEquals("Mandatory Constraint EingangStrassenbezeichnungTest.TopicA.Bodenbedeckung.GWRC02a is not true.",logger.getErrs().get(0).getEventMsg());
    }
    @Test
    public void egidDoesntExist_Ok() {
        Map<String,Class> userFunctions=new java.util.HashMap<String,Class>();
        userFunctions.put(ILI_FUNCTION_NAME,PLUGIN);
        Iom_jObject iomObj1=new Iom_jObject(ILI_A_BODENBEDECKUNG, "o1");
        iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_EGID,"999999999");
        iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_EDID,"0");
        iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_STRNAME,"Corte Inferiore");
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
}
