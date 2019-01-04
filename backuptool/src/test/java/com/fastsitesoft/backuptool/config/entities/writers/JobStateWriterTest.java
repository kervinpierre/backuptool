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

import com.fastsitesoft.backuptool.config.entities.JobArchiveSummary;
import com.fastsitesoft.backuptool.config.entities.JobState;
import com.fastsitesoft.backuptool.enums.BackupToolCompressionType;
import com.fastsitesoft.backuptool.enums.BackupToolEncryptionType;
import com.fastsitesoft.backuptool.enums.BackupToolJobDisposition;
import com.fastsitesoft.backuptool.enums.BackupToolJobStatus;
import com.fastsitesoft.backuptool.enums.FSSBackupHashType;
import com.fastsitesoft.backuptool.enums.FSSBackupType;
import com.fastsitesoft.backuptool.testutils.BackupToolTestProperties;
import com.fastsitesoft.backuptool.testutils.BackupToolTestWatcher;
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Properties;
import java.util.UUID;

/**
 * Created by kervin on 2015-08-22.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JobStateWriterTest
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
        Path backupDir = Paths.get("/tmp/backupTest/tmp/JobStateWriterTest/A001_testWrite/");
        if( Files.exists(backupDir) )
        {
            FileUtils.deleteDirectory(backupDir.toFile());
        }
        Assert.assertTrue(Files.notExists(backupDir));
        Files.createDirectories( backupDir );
        Path outPath = backupDir.resolve("jobStateWriteState_A001_A001.xml");
        Path errPath = backupDir.resolve("jobStateWriteErrors_A001.xml");

        JobState currJobState = JobState.from(null, UUID.randomUUID(),
                Paths.get("/tmp/backupTest/tmp"),
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

        JobArchiveSummary currSumm = JobArchiveSummary.from(Paths.get("/tmp/backupTest/tmp/test01.btaf"),
                Paths.get("test01.btaf.listing"),
                null,
                Instant.now(), null, BackupToolCompressionType.GZIP, BackupToolEncryptionType.NONE);

        currJobState.getArchives().add(currSumm);

        JobStateWriter.write(currJobState, outPath, errPath);

        Assert.assertTrue(Files.exists(outPath));

        // TODO : Validate written XML file
    }
}