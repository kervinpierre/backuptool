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

package com.fastsitesoft.backuptool.config.entities.writers;

import com.fastsitesoft.backuptool.config.entities.JobArchiveListing;
import com.fastsitesoft.backuptool.config.entities.JobArchiveListingState;
import com.fastsitesoft.backuptool.enums.BackupToolFSItemType;
import com.fastsitesoft.backuptool.enums.BackupToolJobDisposition;
import com.fastsitesoft.backuptool.enums.BackupToolJobStatus;
import com.fastsitesoft.backuptool.enums.FSSBackupHashType;
import com.fastsitesoft.backuptool.testutils.BackupToolTestProperties;
import com.fastsitesoft.backuptool.testutils.BackupToolTestWatcher;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.UUID;

/**
 * Created by kervin on 2015-08-22.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JobArchiveListingStateWriterTest
{
    private static final Logger log = LogManager.getLogger(JobArchiveListingStateWriterTest.class);


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
     * Test write() method
     */
    @Test
    public void A001_testWrite() throws Exception
    {

        Path backupDir = Paths.get("/tmp/backupTest/tmp/JobArchiveListingStateWriterTest/A001_testWrite/");
        if( Files.exists(backupDir) )
        {
            FileUtils.deleteDirectory(backupDir.toFile());
        }
        Assert.assertTrue(Files.notExists(backupDir));
        Files.createDirectories( backupDir );
        Path outPath = backupDir.resolve("jobArchiveListingStateWrite_A001.xml");

        JobArchiveListingState currObj = JobArchiveListingState.from(UUID.randomUUID(),
                Paths.get("/tmp/backupTest/tmp"));

        JobArchiveListing currListing = JobArchiveListing.from(
                Paths.get("/tmp/backupTest/tmp/a"),
                BackupToolJobStatus.COMPLETED,
                BackupToolJobDisposition.NONE,
                null,
                FSSBackupHashType.NONE,
                null,
                BackupToolFSItemType.GENERIC_FILE);
        currObj.getListing().add(currListing);

        currListing = JobArchiveListing.from(
                Paths.get("/tmp/backupTest/tmp/b"),
                BackupToolJobStatus.COMPLETED,
                BackupToolJobDisposition.NONE,
                null,
                FSSBackupHashType.NONE,
                null,
                BackupToolFSItemType.DIRECTORY);
        currObj.getListing().add(currListing);

        JobArchiveListingStateWriter.write(currObj, outPath);

        Assert.assertTrue(Files.exists(outPath));

        // TODO : validate against the schema
    }
}