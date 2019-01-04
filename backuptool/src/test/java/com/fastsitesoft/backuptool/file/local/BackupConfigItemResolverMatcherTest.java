package com.fastsitesoft.backuptool.file.local;

import com.fastsitesoft.backuptool.config.entities.BackupConfigFile;
import com.fastsitesoft.backuptool.config.entities.BackupConfigDirectory;
import com.fastsitesoft.backuptool.enums.FSSBackupItemPathTypeEnum;
import com.fastsitesoft.backuptool.testutils.BackupToolTestProperties;
import com.fastsitesoft.backuptool.testutils.BackupToolTestWatcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.*;
import org.junit.rules.TestWatcher;
import org.junit.runners.MethodSorters;

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Properties;


/**
 * Created by kervin on 6/19/15.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BackupConfigItemResolverMatcherTest
{
    private static final Logger log = LogManager.getLogger(BackupConfigItemResolverMatcherTest.class);

    private Properties testProperties;

    @Rule
    public TestWatcher testWatcher = new BackupToolTestWatcher();

    public BackupConfigItemResolverMatcherTest()
    {
    }

    @BeforeClass
    public static void setUpClass()
    {
    }

    @AfterClass
    public static void tearDownClass()
    {
    }

    @Before
    public void setUp()
    {
        testProperties = BackupToolTestProperties.GetProperties();
    }

    @After
    public void tearDown()
    {
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void A001_testGetMatcher() throws Exception
    {
        BackupConfigFile bcf = new BackupConfigFile("/tmp/backupTest/b/b/test-[0-9][0-9]\\.txt",
                null, FSSBackupItemPathTypeEnum.REGEX,  null,false);

        PathMatcher pm = BackupConfigItemResolverMatcher.getMatcher(bcf);
        Assert.assertNotNull(pm);

        Path currPath;

        currPath = Paths.get("/tmp/backupTest/b/b/test-01.txt");
        Assert.assertTrue(pm.matches(currPath));

        currPath = Paths.get("/tmp/backupTest/b/b/test-02.txt");
        Assert.assertTrue(pm.matches(currPath));

        currPath = Paths.get("tmp/backupTest/b/b/test-02.txt");
        Assert.assertFalse(pm.matches(currPath));

        currPath = Paths.get("/tmp/backupTest/b/b/test-0b.txt");
        Assert.assertFalse(pm.matches(currPath));

        currPath = Paths.get("/tmp/backupTest/b/b/test-012.txt");
        Assert.assertFalse(pm.matches(currPath));
    }

    /**
     * A common regular expression for "folder and option descendants which
     * does not work with PathMatcher and regex because Unix provider
     * removes trailing slashes on directories during normalization.
     *
     * @throws Exception
     */
    @Test
    public void A002_testGetMatcher() throws Exception
    {
        BackupConfigDirectory bcf = new BackupConfigDirectory("/tmp/backupTest/b/.*",
                null, FSSBackupItemPathTypeEnum.REGEX, null, null,  null, false);

        PathMatcher pm = BackupConfigItemResolverMatcher.getMatcher(bcf);
        Assert.assertNotNull(pm);

        Path currPath;

        currPath = Paths.get("/tmp/backupTest/b");
        Assert.assertFalse(pm.matches(currPath));

        currPath = Paths.get("/tmp/backupTest/b/");
        Assert.assertFalse(pm.matches(currPath));

        currPath = Paths.get("/tmp/backupTest/b/b");
        Assert.assertTrue(pm.matches(currPath));
    }

    /**
     * Match a base folder, with or without a trailing slash, along with any descendants.
     *
     * @throws Exception
     */
    @Test
    public void A003_testGetMatcher() throws Exception
    {
        BackupConfigDirectory bcf = new BackupConfigDirectory("/tmp/backupTest/b(/.*)?",
                null, FSSBackupItemPathTypeEnum.REGEX, null, null,  null,false);

        PathMatcher pm = BackupConfigItemResolverMatcher.getMatcher(bcf);
        Assert.assertNotNull(pm);

        Path currPath;

        currPath = Paths.get("/tmp/backupTest/b");
        Assert.assertTrue(pm.matches(currPath));

        currPath = Paths.get("/tmp/backupTest/b/");
        Assert.assertTrue(pm.matches(currPath));

        currPath = Paths.get("/tmp/backupTest/b/b");
        Assert.assertTrue(pm.matches(currPath));
    }

    /**
     * Match relative paths.
     *
     * @throws Exception
     */
    @Test
    public void A004_testGetMatcher() throws Exception
    {
        BackupConfigDirectory bcf = new BackupConfigDirectory("backupTest/b(/.*)?",
                null, FSSBackupItemPathTypeEnum.REGEX, null, null,  null,false);

        PathMatcher pm = BackupConfigItemResolverMatcher.getMatcher(bcf);
        Assert.assertNotNull(pm);

        Path currPath;

        currPath = Paths.get("backupTest/b");
        Assert.assertTrue(pm.matches(currPath));

        currPath = Paths.get("backupTest/b/");
        Assert.assertTrue(pm.matches(currPath));

        currPath = Paths.get("backupTest/b/b");
        Assert.assertTrue(pm.matches(currPath));
    }
}