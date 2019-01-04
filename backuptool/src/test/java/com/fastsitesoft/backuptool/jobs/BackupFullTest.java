/*
 * SLU Dev Inc. CONFIDENTIAL
 * DO NOT COPY
 *
 * Copyright (c) [2012] - [2015] SLU Dev Inc. <info@sludev.com>
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of SLU Dev Inc. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to SLU Dev Inc. and its suppliers and
 * may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from SLU Dev Inc.
 *
 */

package com.fastsitesoft.backuptool.jobs;

import com.fastsitesoft.backuptool.BackupId;
import com.fastsitesoft.backuptool.backend.IBackendFileSystem;
import com.fastsitesoft.backuptool.backend.StorageBackendFileSystem;
import com.fastsitesoft.backuptool.config.entities.BackupConfig;
import com.fastsitesoft.backuptool.config.entities.BackupConfigChunk;
import com.fastsitesoft.backuptool.config.entities.BackupConfigCompression;
import com.fastsitesoft.backuptool.config.entities.BackupConfigDirectory;
import com.fastsitesoft.backuptool.config.entities.BackupConfigEncryption;
import com.fastsitesoft.backuptool.config.entities.BackupConfigFile;
import com.fastsitesoft.backuptool.config.entities.BackupConfigStorageBackend;
import com.fastsitesoft.backuptool.constants.BackupConstants;
import com.fastsitesoft.backuptool.enums.BackupToolNameComponentType;
import com.fastsitesoft.backuptool.enums.BackupToolResultStatus;
import com.fastsitesoft.backuptool.enums.FSSBackupHashType;
import com.fastsitesoft.backuptool.enums.FSSBackupItemPathTypeEnum;
import com.fastsitesoft.backuptool.enums.FSSBackupSizeType;
import com.fastsitesoft.backuptool.enums.FSSBackupType;
import com.fastsitesoft.backuptool.file.local.BackupConfigItemResolver;
import com.fastsitesoft.backuptool.testutils.BackupToolTestProperties;
import com.fastsitesoft.backuptool.testutils.BackupToolTestWatcher;
import com.fastsitesoft.backuptool.utils.BackupToolResult;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Created by kervin on 2015-09-12.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BackupFullTest
{
    private static final Logger log = LogManager.getLogger(BackupFullTest.class);

    private Properties testProperties;

    @Rule
    public TestWatcher testWatcher = new BackupToolTestWatcher();

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
     * A simple Full Backup test.
     *
     * @throws Exception
     */
    @Test
    public void A001_testDoBackup() throws Exception
    {
        log.debug("A001_testDoBackup");

        List<BackupConfigDirectory> dirItems = new ArrayList<>();
        List<BackupConfigFile> fileItems = new ArrayList<>();
        BackupToolResult res;

        dirItems.add(new BackupConfigDirectory("/tmp/backupTest/c/b/",
                null,
                FSSBackupItemPathTypeEnum.LITERAL,
                null,
                null,
                null,
                false));

        String backupDir = "/tmp/backupTest/tmp/A001_testDoBackup/";

        if( Files.exists( Paths.get(backupDir)) )
        {
            FileUtils.deleteDirectory(new File(backupDir));
        }

        Assert.assertFalse(Files.exists(Paths.get(backupDir)));

        BackupConfigStorageBackend bsbe = BackupConfigStorageBackend.from(
                "file://" + backupDir,
                "",
                "",
                true);

        Pattern archName = Pattern.compile(BackupConstants.DEFAULT_ARCHIVE_NAME_RULE_PATTERN);
        Pattern jobName = Pattern.compile(BackupConstants.DEFAULT_JOB_NAME_RULE_PATTERN);

        List<BackupToolNameComponentType> archComps = new ArrayList<>();
        List<BackupToolNameComponentType> jobComps = new ArrayList<>();

        archComps.add(BackupConstants.DEFAULT_ARCHIVE_NAME_RULE_COMPONENT);
        jobComps.add(BackupConstants.DEFAULT_JOB_NAME_RULE_COMPONENT);

        BackupConfig bc = BackupConfig.from( "testdefault",
                1,
                false,
                false,
                false,
                true,
                false,
                false,
                true,
                false,
                false,
                false,
                true,
                true,
                false,
                false,
                false,
                Paths.get("/tmp/backupTest/holdingDirectory"),
                null,
                null,
                null,
                null,
                Paths.get("backupState.xml"),
                Paths.get("backupErrors.xml"),
                null,
                dirItems,
                fileItems,
                null,
                null, // Backup ID
                FSSBackupType.FULL,
                FSSBackupHashType.NONE,
                null,
                null,
                null,
                archName,
                archComps,
                Paths.get(BackupConstants.DEFAULT_ARCHIVE_NAME_TEMPLATE),
                jobName,
                jobComps,
                Paths.get(BackupConstants.DEFAULT_JOB_NAME_TEMPLATE),
                null,
                bsbe,
                null
                 );

        res = BackupFull.doBackup(bc);

        Assert.assertNotNull(res);
        Assert.assertTrue(res.getStatus() == BackupToolResultStatus.SUCCESS);
    }


    /**
     * A simple Full Backup test twice.
     *
     * This forces the backup set to reread and update the JobSet configuration.
     *
     * @throws Exception
     */
    @Test
    public void A002_testDoBackupTwice() throws Exception
    {
        log.debug("A002_testDoBackupTwice");

        List<BackupConfigDirectory> dirItems = new ArrayList<>();
        List<BackupConfigFile> fileItems = new ArrayList<>();
        BackupToolResult res;

        String currBackFldr;

        // Large backup, good for testing chunking
        currBackFldr = "/tmp/backupTest/archivetest01/c/";

        // Smaller backup, but significant
        currBackFldr = "/tmp/backupTest/archivetest01/c/b/";

        currBackFldr = "/tmp/backupTest/archivetest01/c/b/a/";

        // Folder has enough data to be chunked on 2MB boundaries.
        dirItems.add(new BackupConfigDirectory(currBackFldr,
                null,
                FSSBackupItemPathTypeEnum.LITERAL,
                null,
                null,
                null,
                false));

        String backupDir = "/tmp/backupTest/tmp/A002_testDoBackupTwice/";

        if( Files.exists( Paths.get(backupDir)) )
        {
            FileUtils.deleteDirectory(new File(backupDir));
        }

        Assert.assertFalse(Files.exists(Paths.get(backupDir)));

        BackupConfigStorageBackend bsbe = BackupConfigStorageBackend.from(
                "file://" + backupDir,
                "",
                "",
                true);

        Pattern archName = Pattern.compile(BackupConstants.DEFAULT_ARCHIVE_NAME_RULE_PATTERN);
        Pattern jobName = Pattern.compile(BackupConstants.DEFAULT_JOB_NAME_RULE_PATTERN);

        List<BackupToolNameComponentType> archComps = new ArrayList<>();
        List<BackupToolNameComponentType> jobComps = new ArrayList<>();

        archComps.add(BackupConstants.DEFAULT_ARCHIVE_NAME_RULE_COMPONENT);
        jobComps.add(BackupConstants.DEFAULT_JOB_NAME_RULE_COMPONENT);

        BackupConfigChunk chunk = new BackupConfigChunk(2, FSSBackupSizeType.MB, true);

        // Run the first full backup
        BackupConfig bc = BackupConfig.from( "testdefault",
                1,
                false,
                false,
                false,
                true,
                false,
                false,
                true,
                false,
                false,
                false,
                true,
                true,
                false,
                false,
                false,
                Paths.get("/tmp/backupTest/holdingDirectory"),
                null,
                null,
                null,
                null,
                Paths.get("backupState.xml"),
                Paths.get("backupErrors.xml"),
                null,
                dirItems,
                fileItems,
                null,
                null, // Backup ID
                FSSBackupType.FULL,
                FSSBackupHashType.NONE,
                null,
                null,
                chunk,
                archName,
                archComps,
                Paths.get(BackupConstants.DEFAULT_ARCHIVE_NAME_TEMPLATE),
                jobName,
                jobComps,
                Paths.get(BackupConstants.DEFAULT_JOB_NAME_TEMPLATE),
                null,
                bsbe,
                null
        );

        res = BackupFull.doBackup(bc);

        Assert.assertNotNull(res);
        Assert.assertTrue(res.getStatus() == BackupToolResultStatus.SUCCESS);

        // Sleep a few seconds before the second backup
        Thread.sleep(5000);

        // Run the second full backup
        bc = BackupConfig.from( "testdefault",
                1,
                false,
                false,
                false,
                true,
                false,
                false,
                true,
                false,
                false,
                false,
                true,
                true,
                false,
                false,
                false,
                Paths.get("/tmp/backupTest/holdingDirectory"),
                null,
                null,
                null,
                null,
                Paths.get("backupState.xml"),
                Paths.get("backupErrors.xml"),
                null,
                dirItems,
                fileItems,
                null,
                null, // Backup ID
                FSSBackupType.FULL,
                FSSBackupHashType.NONE,
                null,
                null,
                chunk,
                archName,
                archComps,
                Paths.get(BackupConstants.DEFAULT_ARCHIVE_NAME_TEMPLATE),
                jobName,
                jobComps,
                Paths.get(BackupConstants.DEFAULT_JOB_NAME_TEMPLATE),
                null,
                bsbe,
                null
        );

        res = BackupFull.doBackup(bc);

        Assert.assertNotNull(res);
        Assert.assertTrue(res.getStatus() == BackupToolResultStatus.SUCCESS);
    }
}