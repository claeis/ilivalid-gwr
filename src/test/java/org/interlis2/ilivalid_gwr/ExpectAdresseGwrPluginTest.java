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

public class ExpectAdresseGwrPluginTest {
    private static final String ILI_FUNCTION_NAME=ExpectAdresseGwrPlugin.ILI_QUALIFIED_FUNCTION_NAME;
    private static final Class PLUGIN=ExpectAdresseGwrPlugin.class;
    private static final String ILI_MODEL="ExpectAdresseTest";
    private static final String ILI_TOPIC_A=ILI_MODEL+".TopicA";
    private static final String ILI_A_BODENBEDECKUNG_EGID = "egid";
    private static final String ILI_A_BODENBEDECKUNG_EDID = "edid";
    private static final String ILI_A_BODENBEDECKUNG_LOKALISATION = "Lokalisation";
    private static final String ILI_A_BODENBEDECKUNG_HAUSNUMMER = "Hausnummer";
    private static final String ILI_A_BODENBEDECKUNG_PLZ = "plz";
    private static final String ILI_A_BODENBEDECKUNG_ORTSCHAFT = "ortschaft";
    private static final String ILI_A_BODENBEDECKUNG_GSTAT_NOT_EQUAL = "gstatNotEqual";
    
    private static final String ILI_A_BODENBEDECKUNG = ILI_TOPIC_A+".Bodenbedeckung";

    private TransferDescription td=null;
    @Before
    public void setUp() throws Exception {
        // ili-datei lesen
        Configuration ili2cConfig=new Configuration();
        ili2cConfig.addFileEntry(new FileEntry("src/test/ili/IliValidGwr_V1_0.ili", FileEntryKind.ILIMODELFILE));
        ili2cConfig.addFileEntry(new FileEntry("src/test/ili/ExpectAdresseTest.ili", FileEntryKind.ILIMODELFILE));
        td=ch.interlis.ili2c.Ili2c.runCompiler(ili2cConfig);
        Assert.assertNotNull(td);
    }
    @Test
    public void exists_Ok() {
        Map<String,Class> userFunctions=new java.util.HashMap<String,Class>();
        userFunctions.put(ILI_FUNCTION_NAME,PLUGIN);
        Iom_jObject iomObj1 = createValidObj();
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
    private Iom_jObject createValidObj() {
        Iom_jObject iomObj1=new Iom_jObject(ILI_A_BODENBEDECKUNG, "o1");
        iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_EGID,"192029861");
        iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_EDID,"0");
        iomObj1.addattrvalue(ILI_A_BODENBEDECKUNG_LOKALISATION,"x");
        iomObj1.addattrvalue(ILI_A_BODENBEDECKUNG_LOKALISATION,"Via della Crotta");
        iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_HAUSNUMMER,"4");
        iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_PLZ,"6809");
        iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_ORTSCHAFT,"Medeglia");
        iomObj1.addattrvalue(ILI_A_BODENBEDECKUNG_GSTAT_NOT_EQUAL,"0");
        return iomObj1;
    }
    @Test
    public void exists_HausnummerUndefined_Ok() {
        Map<String,Class> userFunctions=new java.util.HashMap<String,Class>();
        userFunctions.put(ILI_FUNCTION_NAME,PLUGIN);
        Iom_jObject iomObj1=new Iom_jObject(ILI_A_BODENBEDECKUNG, "o1");
        iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_EGID,"191956173");
        iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_EDID,"0");
        iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_LOKALISATION,"Corte Inferiore");
        iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_PLZ,"6809");
        iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_ORTSCHAFT,"Medeglia");
        iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_GSTAT_NOT_EQUAL,"0");
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
    public void wrongEgid_Fail() {
        Map<String,Class> userFunctions=new java.util.HashMap<String,Class>();
        userFunctions.put(ILI_FUNCTION_NAME,PLUGIN);
        Iom_jObject iomObj1 = createValidObj();
        iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_EGID,"0");
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
        Assert.assertEquals("Mandatory Constraint ExpectAdresseTest.TopicA.Bodenbedeckung.GWRC02a is not true.",logger.getErrs().get(0).getEventMsg());
    }
    @Test
    public void wrongEdid_Fail() {
        Map<String,Class> userFunctions=new java.util.HashMap<String,Class>();
        userFunctions.put(ILI_FUNCTION_NAME,PLUGIN);
        Iom_jObject iomObj1 = createValidObj();
        iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_EDID,"99");
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
        Assert.assertEquals("Mandatory Constraint ExpectAdresseTest.TopicA.Bodenbedeckung.GWRC02a is not true.",logger.getErrs().get(0).getEventMsg());
    }
    @Test
    public void wrongLokalisation_Fail() {
        Map<String,Class> userFunctions=new java.util.HashMap<String,Class>();
        userFunctions.put(ILI_FUNCTION_NAME,PLUGIN);
        Iom_jObject iomObj1 = createValidObj();
        iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_LOKALISATION,"x");
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
        Assert.assertEquals("Mandatory Constraint ExpectAdresseTest.TopicA.Bodenbedeckung.GWRC02a is not true.",logger.getErrs().get(0).getEventMsg());
    }
    @Test
    public void wrongHausnummer_Fail() {
        Map<String,Class> userFunctions=new java.util.HashMap<String,Class>();
        userFunctions.put(ILI_FUNCTION_NAME,PLUGIN);
        Iom_jObject iomObj1 = createValidObj();
        iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_HAUSNUMMER,"99");
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
        Assert.assertEquals("Mandatory Constraint ExpectAdresseTest.TopicA.Bodenbedeckung.GWRC02a is not true.",logger.getErrs().get(0).getEventMsg());
    }
    @Test
    public void undefinedHausnummer_Fail() {
        Map<String,Class> userFunctions=new java.util.HashMap<String,Class>();
        userFunctions.put(ILI_FUNCTION_NAME,PLUGIN);
        Iom_jObject iomObj1 = createValidObj();
        iomObj1.setattrundefined(ILI_A_BODENBEDECKUNG_HAUSNUMMER);
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
        Assert.assertEquals("Mandatory Constraint ExpectAdresseTest.TopicA.Bodenbedeckung.GWRC02a is not true.",logger.getErrs().get(0).getEventMsg());
    }
    @Test
    public void wrongPlz_Fail() {
        Map<String,Class> userFunctions=new java.util.HashMap<String,Class>();
        userFunctions.put(ILI_FUNCTION_NAME,PLUGIN);
        Iom_jObject iomObj1 = createValidObj();
        iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_PLZ,"0");
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
        Assert.assertEquals("Mandatory Constraint ExpectAdresseTest.TopicA.Bodenbedeckung.GWRC02a is not true.",logger.getErrs().get(0).getEventMsg());
    }
    @Test
    public void wrongOrtschaft_Fail() {
        Map<String,Class> userFunctions=new java.util.HashMap<String,Class>();
        userFunctions.put(ILI_FUNCTION_NAME,PLUGIN);
        Iom_jObject iomObj1 = createValidObj();
        iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_ORTSCHAFT,"x");
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
        Assert.assertEquals("Mandatory Constraint ExpectAdresseTest.TopicA.Bodenbedeckung.GWRC02a is not true.",logger.getErrs().get(0).getEventMsg());
    }
    @Test
    public void wrongGstat_Fail() {
        Map<String,Class> userFunctions=new java.util.HashMap<String,Class>();
        userFunctions.put(ILI_FUNCTION_NAME,PLUGIN);
        Iom_jObject iomObj1 = createValidObj();
        iomObj1.setattrvalue(ILI_A_BODENBEDECKUNG_GSTAT_NOT_EQUAL,"1004");
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
        Assert.assertEquals("Mandatory Constraint ExpectAdresseTest.TopicA.Bodenbedeckung.GWRC02a is not true.",logger.getErrs().get(0).getEventMsg());
    }
}
