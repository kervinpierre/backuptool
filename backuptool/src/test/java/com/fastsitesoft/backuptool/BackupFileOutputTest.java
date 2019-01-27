/*
 *  CityMSP LLC CONFIDENTIAL
 *  DO NOT COPY
 *
 * Copyright (c) [2012] - [2019] CityMSP LLC <info@citymsp.nyc>
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 *  the property of CityMSP LLC and its suppliers,
 *  if any.  The intellectual and technical concepts contained
 *  herein are proprietary to CityMSP LLC and its suppliers and
 *  may be covered by U.S. and Foreign Patents, patents in process,
 *  and are protected by trade secret or copyright law.
 *  Dissemination of this information or reproduction of this material
 *  is strictly forbidden unless prior written permission is obtained
 *  from CityMSP LLC
 */
package com.fastsitesoft.backuptool;

import com.fastsitesoft.backuptool.backend.IBackendFileSystem;
import com.fastsitesoft.backuptool.config.entities.BackupConfigChunk;
import com.fastsitesoft.backuptool.config.entities.JobArchive;
import com.fastsitesoft.backuptool.config.entities.JobState;
import com.fastsitesoft.backuptool.enums.BackupToolCompressionType;
import com.fastsitesoft.backuptool.enums.BackupToolJobDisposition;
import com.fastsitesoft.backuptool.enums.BackupToolJobStatus;
import com.fastsitesoft.backuptool.enums.BackupToolNameComponentType;
import com.fastsitesoft.backuptool.enums.BackupToolResultStatus;
import com.fastsitesoft.backuptool.enums.FSSBackupHashType;
import com.fastsitesoft.backuptool.enums.FSSBackupSizeType;
import com.fastsitesoft.backuptool.enums.FSSBackupType;
import com.fastsitesoft.backuptool.testutils.BackupToolTestProperties;
import com.fastsitesoft.backuptool.testutils.BackupToolTestWatcher;
import com.fastsitesoft.backuptool.utils.BackupToolResult;
import com.fastsitesoft.backuptool.utils.BackupToolSystemID;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Pattern;


/**
 * Created by kervin on 7/12/15.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BackupFileOutputTest
{
    private static final Logger log = LogManager.getLogger(BackupFileOutputTest.class);

    private Properties testProperties;

    @Rule
    public TestWatcher testWatcher = new BackupToolTestWatcher();

    public BackupFileOutputTest()
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
     * Test of resolveBasePath method, of class BackupConfigItemResolver.
     */
    @Test
    public void A001_nextName() throws Exception
    {
        Path currPath = Paths.get("/tmp/backupTest/archivetest01/b/testArch00001.btaf");
        Pattern regex = Pattern.compile(".*?([0-9]+)+\\.btaf");
        List<BackupToolNameComponentType> comps = new ArrayList<>();
        comps.add(BackupToolNameComponentType.INTEGER_SEQUENCE);

        Path res = IBackendFileSystem.nextName(currPath, regex, comps, false, null);

        Assert.assertNotNull(res);
        Assert.assertTrue(res.equals(Paths.get("testArch00002.btaf")));
    }

    @Test
    public void A002_nextNameTwoFiles() throws Exception
    {
        Path currPath = Paths.get("/tmp/backupTest/archivetest01/c/testArch00002.btaf");
        Pattern regex = Pattern.compile(".*?([0-9]+)+\\.btaf");
        List<BackupToolNameComponentType> comps = new ArrayList<>();
        comps.add(BackupToolNameComponentType.INTEGER_SEQUENCE);

        Path res = IBackendFileSystem.nextName(currPath, regex, comps, false, null);

        Assert.assertNotNull(res);
        Assert.assertTrue(res.equals(Paths.get("testArch00003.btaf")));
    }

    /**
     * Simple addPath() call for a file.
     *
     * @throws Exception
     */
    @Test
    public void A003_addFile() throws Exception
    {
        Path outDir = Paths.get("/tmp/backupTest/tmp/A003_addFile");
        Path currPath = outDir.resolve("test03_00001.btaf");
        Path backupFile = Paths.get("/tmp/backupTest/archivetest01/c/testfile00003.bin");
        Deque<Path> backupFiles = new ArrayDeque<>();

        if( Files.exists(outDir) )
        {
            FileUtils.deleteDirectory(outDir.toFile());
        }

        Assert.assertFalse(Files.exists(outDir));

        Files.createDirectory(outDir);

        backupFiles.add(backupFile);

        Deque<JobArchive> archives = new ArrayDeque<>();
        archives.add(JobArchive.from(null, null, currPath, null, true, null, null,
                null, null));

        Pattern regex = Pattern.compile(".*?([0-9]+)+\\.btaf");
        List<BackupToolNameComponentType> comps = new ArrayList<>();
        comps.add(BackupToolNameComponentType.INTEGER_SEQUENCE);

        Files.deleteIfExists(currPath);
        Assert.assertFalse(Files.exists(currPath));

        BackupToolResult res = BackupFileOutput.addPaths(null,
                backupFiles,
                LinkOption.NOFOLLOW_LINKS,
                archives, regex, null, comps,
                null,
                BackupToolCompressionType.NONE,
                true,
                null);

        Assert.assertNotNull(res);
        Assert.assertTrue(res.getStatus() == BackupToolResultStatus.SUCCESS);

        Assert.assertTrue(Files.exists(currPath));
        Assert.assertTrue(Files.size(currPath) > 0);
    }

    /**
     * Add file using chunks
     *
     * @throws Exception
     */
    @Test
    public void A004_addFileChunked() throws Exception
    {
        Path outDir = Paths.get("/tmp/backupTest/tmp/a004_addfilechunked");
        Path firstArchive = outDir.resolve("test04_00001.btaf");

        if( Files.exists(outDir) )
        {
            FileUtils.deleteDirectory(outDir.toFile());
        }

        Assert.assertFalse(Files.exists(outDir));

        Files.createDirectory(outDir);

        // 10MB test files
        Path backupFile = Paths.get("/tmp/backupTest/archivetest01/c/a/testfile00007.bin");
        Deque<Path> backupFiles = new ArrayDeque<>();

        backupFiles.add(backupFile);
        Deque<JobArchive> archives = new ArrayDeque<>();
        archives.add(JobArchive.from(null, null, firstArchive, null, true, null, null,
                null, null));

        Pattern regex = Pattern.compile(".*?([0-9]+)+\\.btaf");
        List<BackupToolNameComponentType> comps = new ArrayList<>();
        comps.add(BackupToolNameComponentType.INTEGER_SEQUENCE);

        BackupConfigChunk chunk = new BackupConfigChunk(2, FSSBackupSizeType.MB, true);

        BackupToolResult res = BackupFileOutput.addPaths(null,
                backupFiles,
                LinkOption.NOFOLLOW_LINKS,
                archives, regex, chunk, comps,
                null,
                BackupToolCompressionType.NONE,
                true,
                null);

        Assert.assertNotNull(res);
        Assert.assertTrue(res.getStatus() == BackupToolResultStatus.SUCCESS);

        Assert.assertTrue(Files.exists(firstArchive));
        Assert.assertTrue(Files.size(firstArchive) > 0);
    }

    /**
     * Simple addPath() call for an empty folder.
     *
     * @throws Exception
     */
    @Test
    public void A005_addPathFolder() throws Exception
    {
        String backupDir = "/tmp/backupTest/tmp/A005_addPathFolder/";

        if( Files.exists( Paths.get(backupDir)) )
        {
            FileUtils.deleteDirectory(new File(backupDir));
        }

        Assert.assertFalse(Files.exists(Paths.get(backupDir)));

        Files.createDirectory(Paths.get(backupDir));

        Path currPath =  Paths.get(backupDir).resolve("test005_00001.btaf");
        Deque<Path> backupFiles = new ArrayDeque<>();

        backupFiles.add(Paths.get("/tmp/backupTest/archivetest01/a/a"));
        backupFiles.add(Paths.get("/tmp/backupTest/archivetest01/a/a/a"));
        backupFiles.add(Paths.get("/tmp/backupTest/archivetest01/a/a/b"));
        backupFiles.add(Paths.get("/tmp/backupTest/archivetest01/a/a/c"));

        Deque<JobArchive> archives = new ArrayDeque<>();
        archives.add(JobArchive.from(null, null, currPath, null, true, null, null,
                null, null));

        Pattern regex = Pattern.compile(".*?([0-9]+)+\\.btaf");
        List<BackupToolNameComponentType> comps = new ArrayList<>();
        comps.add(BackupToolNameComponentType.INTEGER_SEQUENCE);

        Files.deleteIfExists(currPath);
        Assert.assertFalse(Files.exists(currPath));

        BackupToolResult res = BackupFileOutput.addPaths(null,
                backupFiles,
                    LinkOption.NOFOLLOW_LINKS,
                    archives, regex, null, comps,
                    null,
                    BackupToolCompressionType.NONE,
                true,
                null);

        Assert.assertNotNull(res);
        Assert.assertTrue(res.getStatus() == BackupToolResultStatus.SUCCESS);

        Assert.assertTrue(Files.exists(currPath));
        Assert.assertTrue(Files.size(currPath) > 0);
    }

    /**
     * Add file using chunks, slightly more complicated
     *
     * @throws Exception
     */
    @Test
    public void A006_addFileChunked() throws Exception
    {
        Path outDir = Paths.get("/tmp/backupTest/tmp/a006_addfilechunked");
        Path firstArchive = outDir.resolve("test06_00001.btaf");

        if( Files.exists(outDir) )
        {
            FileUtils.deleteDirectory(outDir.toFile());
        }

        Assert.assertFalse(Files.exists(outDir));

        Files.createDirectory(outDir);

        // 10MB test files
        Deque<Path> backupFiles = new ArrayDeque<>();

        backupFiles.add(Paths.get("/tmp/backupTest/archivetest01/c/a/testfile00007.bin"));
        backupFiles.add(Paths.get("/tmp/backupTest/archivetest01/c/a/testfile00006.bin"));
        backupFiles.add(Paths.get("/tmp/backupTest/archivetest01/c/a/testfile00005.bin"));

        Deque<JobArchive> archives = new ArrayDeque<>();
        archives.add(JobArchive.from(null, null, firstArchive, null, true, null, null,
                null, null));

        Pattern regex = Pattern.compile(".*?([0-9]+)+\\.btaf");
        List<BackupToolNameComponentType> comps = new ArrayList<>();
        comps.add(BackupToolNameComponentType.INTEGER_SEQUENCE);

        BackupConfigChunk chunk = new BackupConfigChunk(2, FSSBackupSizeType.MB, true);

        JobState currJobState = JobState.from(null, UUID.randomUUID(),
                Paths.get("backupDir"),
                Instant.now(),
                null,
                FSSBackupType.FULL,
                BackupToolSystemID.getType1(),
                BackupToolJobStatus.NONE,
                BackupToolJobDisposition.NONE,
                UUID.randomUUID(),
                null,
                null,
                FSSBackupHashType.NONE);

        BackupToolResult res = BackupFileOutput.addPaths(currJobState,
                backupFiles,
                LinkOption.NOFOLLOW_LINKS,
                archives, regex, chunk, comps,
                null,
                BackupToolCompressionType.NONE,
                true,
                null);

        Assert.assertNotNull(res);
        Assert.assertTrue(res.getStatus() == BackupToolResultStatus.SUCCESS);

        Assert.assertTrue(Files.exists(firstArchive));
        Assert.assertTrue(Files.size(firstArchive) > 0);
    }

    /**
     * Simple addPath() call for an empty file to an archive.
     *
     * @throws Exception
     */
    @Test
    public void A007_addEmptyFile() throws Exception
    {
        Path outDir = Paths.get("/tmp/backupTest/tmp/A007_addEmptyFile");
        Path currPath = outDir.resolve("test07_00001.btaf");
        Path backupFile = Paths.get("/tmp/backupTest/archivetest01/c/b/testfile2800.bin");
        Deque<Path> backupFiles = new ArrayDeque<>();

        if( Files.exists(outDir) )
        {
            FileUtils.deleteDirectory(outDir.toFile());
        }

        Assert.assertFalse(Files.exists(outDir));

        Files.createDirectory(outDir);

        // We testing the handling of empty files
        Assert.assertTrue(Files.size(backupFile)==0);

        backupFiles.add(backupFile);

        Deque<JobArchive> archives = new ArrayDeque<>();
        archives.add(JobArchive.from(null, null, currPath, null, true, null, null,
                null, null));

        Pattern regex = Pattern.compile(".*?([0-9]+)+\\.btaf");
        List<BackupToolNameComponentType> comps = new ArrayList<>();
        comps.add(BackupToolNameComponentType.INTEGER_SEQUENCE);

        Files.deleteIfExists(currPath);
        Assert.assertFalse(Files.exists(currPath));

        BackupToolResult res = BackupFileOutput.addPaths(null,
                backupFiles,
                LinkOption.NOFOLLOW_LINKS,
                archives, regex, null, comps,
                null,
                BackupToolCompressionType.NONE,
                true,
                null);

        Assert.assertNotNull(res);
        Assert.assertTrue(res.getStatus() == BackupToolResultStatus.SUCCESS);

        Assert.assertTrue(Files.exists(currPath));
        Assert.assertTrue(Files.size(currPath) > 0);
    }

}