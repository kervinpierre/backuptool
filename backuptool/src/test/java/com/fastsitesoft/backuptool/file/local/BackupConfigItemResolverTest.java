/*
 *   SLU Dev Inc. CONFIDENTIAL
 *   DO NOT COPY
 *  
 *  Copyright (c) [2012] - [2015] SLU Dev Inc. <info@sludev.com>
 *  All Rights Reserved.
 *  
 *  NOTICE:  All information contained herein is, and remains
 *   the property of SLU Dev Inc. and its suppliers,
 *   if any.  The intellectual and technical concepts contained
 *   herein are proprietary to SLU Dev Inc. and its suppliers and
 *   may be covered by U.S. and Foreign Patents, patents in process,
 *   and are protected by trade secret or copyright law.
 *   Dissemination of this information or reproduction of this material
 *   is strictly forbidden unless prior written permission is obtained
 *   from SLU Dev Inc.
 */
package com.fastsitesoft.backuptool.file.local;

import com.fastsitesoft.backuptool.config.entities.BackupConfigFile;
import com.fastsitesoft.backuptool.config.entities.BackupConfigDirectory;
import com.fastsitesoft.backuptool.config.entities.JobState;
import com.fastsitesoft.backuptool.enums.BackupToolJobDisposition;
import com.fastsitesoft.backuptool.enums.BackupToolJobStatus;
import com.fastsitesoft.backuptool.enums.BackupToolResultStatus;
import com.fastsitesoft.backuptool.enums.BackupToolResultStatusDetail;
import com.fastsitesoft.backuptool.enums.FSSBackupHashType;
import com.fastsitesoft.backuptool.enums.FSSBackupItemBackupOption;
import com.fastsitesoft.backuptool.enums.FSSBackupItemPathTypeEnum;
import com.fastsitesoft.backuptool.enums.FSSBackupType;
import com.fastsitesoft.backuptool.testutils.BackupToolTestProperties;
import com.fastsitesoft.backuptool.testutils.BackupToolTestWatcher;
import com.fastsitesoft.backuptool.utils.BackupToolResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;

import com.fastsitesoft.backuptool.utils.BackupToolSystemID;
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

/**
 *
 * @author kervin
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BackupConfigItemResolverTest
{
    private static final Logger log = LogManager.getLogger(BackupConfigItemResolverTest.class);

    private Properties testProperties;

    @Rule
    public TestWatcher testWatcher = new BackupToolTestWatcher();

    public BackupConfigItemResolverTest()
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
    public void A001_testResolveBasePath() throws Exception
    {
        log.debug("A001_testResolveBasePath");

        Path currPath;

        // No item named '*' exists
        currPath = BackupConfigItemResolver.resolveBasePath(
                Paths.get("/tmp/backupTest/b/*"));
        Assert.assertTrue(currPath.equals(Paths.get("/tmp/backupTest/b/")));

        // The '*' folder does exist
        currPath = BackupConfigItemResolver.resolveBasePath(
                Paths.get("/tmp/backupTest/*"));
        Assert.assertTrue(currPath.equals(Paths.get("/tmp/backupTest/*/")));

        // A character class regex
        currPath = BackupConfigItemResolver.resolveBasePath(
                Paths.get("/tmp/backupTest/b/[c]"));
        Assert.assertTrue(currPath.equals(Paths.get("/tmp/backupTest/b/")));

        // The base folder does not exist, but the parent does
        currPath = BackupConfigItemResolver.resolveBasePath(
                Paths.get("/tmp/backupTest/b/\\*/"));
        Assert.assertTrue(currPath.equals(Paths.get("/tmp/backupTest/b/")));

        // Full path exists
        currPath = BackupConfigItemResolver.resolveBasePath(
                Paths.get("/tmp/backupTest/\\*/"));
        Assert.assertTrue(currPath.equals(Paths.get("/tmp/backupTest/\\*/")));

        // Full path exists
        currPath = BackupConfigItemResolver.resolveBasePath(
                Paths.get("/tmp/backupTest/b/"));
        Assert.assertTrue(currPath.equals(Paths.get("/tmp/backupTest/b/")));

        // Parent with space in the name exists
        currPath = BackupConfigItemResolver.resolveBasePath(
                Paths.get("/tmp/backupTest/a b/*"));
        Assert.assertTrue(currPath.equals(Paths.get("/tmp/backupTest/a b/")));
    }

    @Test
    public void A002_testCall() throws Exception
    {
        log.debug("A002_testCall");

        List<BackupConfigDirectory> folderItems = new ArrayList<>();
        List<BackupConfigFile> fileItems = new ArrayList<>();
        Path currPath = Paths.get("/tmp/backupTest/a");
        /**
         * Standard literal path folder rule.  Simplest test.
         */
        folderItems.add(new BackupConfigDirectory(currPath.toString(),
                null,
                FSSBackupItemPathTypeEnum.LITERAL,
                null, null, null,
                false));

        BackupConfigItemResolver bcir = BackupConfigItemResolver.from(null,
                fileItems,
                folderItems,
                null,
                false);

        BackupToolResult res = bcir.call();

        Assert.assertNotNull(res);
        Assert.assertTrue(res.getStatus() == BackupToolResultStatus.SUCCESS);

        Map<Path,Map<Path,BackupConfigDirectory>> foundFolders = bcir.getFoundFolders();

        Assert.assertTrue(foundFolders.size() == 1);
        Assert.assertTrue(foundFolders.containsKey(currPath));

        Map<Path,BackupConfigDirectory> foundFoldersMap = foundFolders.get(currPath);
        Assert.assertNotNull(foundFoldersMap);
        Set<Path> foundFoldersSet = foundFoldersMap.keySet();

        Set<Path> expectedSet = new HashSet<>();

        expectedSet.add(Paths.get("/tmp/backupTest/a"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/b"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/b/logs"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/logs"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/logs/b"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/logs/b/logs"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/logs/b/logs/b"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/logs/b/logs/c"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/logs/b/logs/a"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/logs/c"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/logs/c/logs"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/logs/c/logs/b"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/logs/c/logs/c"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/logs/c/logs/a"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/logs/a"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/logs/a/logs"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/logs/a/logs/b"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/logs/a/logs/c"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/logs/a/logs/a"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/logstest"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/logstest/b"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/logstest/b/logs"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/logstest/b/logs/b"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/logstest/b/logs/c"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/logstest/b/logs/a"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/logstest/c"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/logstest/c/logs"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/logstest/c/logs/b"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/logstest/c/logs/c"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/logstest/c/logs/a"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/logstest/a"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/logstest/a/logs"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/logstest/a/logs/b"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/logstest/a/logs/c"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/logstest/a/logs/a"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/c"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/c/logs"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/a"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/a/logs"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/testlogs"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/testlogs/b"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/testlogs/b/logstest"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/testlogs/b/logstest/b"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/testlogs/b/logstest/c"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/testlogs/b/logstest/a"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/testlogs/c"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/testlogs/c/logstest"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/testlogs/c/logstest/b"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/testlogs/c/logstest/c"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/testlogs/c/logstest/a"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/testlogs/a"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/testlogs/a/logstest"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/testlogs/a/logstest/b"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/testlogs/a/logstest/c"));
        expectedSet.add(Paths.get("/tmp/backupTest/a/testlogs/a/logstest/a"));

        Assert.assertTrue(foundFoldersSet.equals(expectedSet));

        // Now test the returned files
        expectedSet.clear();

        Map<Path,Map<Path,BackupConfigFile>> foundFiles = bcir.getFoundFiles();

        Assert.assertTrue(foundFiles.size() == 1);
        Assert.assertTrue(foundFiles.containsKey(currPath));

        Map<Path,BackupConfigFile> foundFilesMap = foundFiles.get(currPath);
        Assert.assertNotNull(foundFilesMap);
        Set<Path> foundFilesSet = foundFilesMap.keySet();

        Assert.assertTrue(foundFilesSet.equals(expectedSet));
    }

    @Test
    public void A003_testCall() throws Exception
    {
        log.debug("A003_testCall");

        List<BackupConfigDirectory> folderItems = new ArrayList<>();
        List<BackupConfigFile> fileItems = new ArrayList<>();

        List<BackupConfigDirectory> childDirs;
        List<BackupConfigFile> childFiles;

        /**
         * Standard base file rule
         */
        fileItems.add(new BackupConfigFile("/tmp/backupTest/c/test.txt",
                null,
                FSSBackupItemPathTypeEnum.LITERAL, null,
                false));

        /**
         * Standard base file exclusion rule
         */
        fileItems.add(new BackupConfigFile("/tmp/backupTest/c/logfile.log",
                null,
                FSSBackupItemPathTypeEnum.LITERAL, null,
                true));

        /**
         * Base directory regex rule
         */
        folderItems.add(new BackupConfigDirectory("/tmp/backupTest/c/a",
                null,
                FSSBackupItemPathTypeEnum.LITERAL, null,
                null, null,
                false));

        /**
         * Slightly more complicated base directory regex rule
         */
        folderItems.add(new BackupConfigDirectory("/tmp/backupTest/c/[b]",
                null,
                FSSBackupItemPathTypeEnum.REGEX, null,
                null, null,
                false));

        /**
         * Folder regex rule with a child directory and child file rule each.
         */
        childDirs = new ArrayList<>();
        childFiles = new ArrayList<>();
        childDirs.add(new BackupConfigDirectory("(.*/)?logs",
                null,
                FSSBackupItemPathTypeEnum.REGEX,
                null, null, null,
                true));

        childFiles.add(new BackupConfigFile(".*\\.log",
                null,
                FSSBackupItemPathTypeEnum.REGEX, null,
                true));

        // Simple regex on the base folder
        folderItems.add(new BackupConfigDirectory("/tmp/backupTest/[b]",
                null,
                FSSBackupItemPathTypeEnum.REGEX,
                childDirs, childFiles, null,
                false));

        /**
         * Exclude rule with no child matchers.  This file system subtree should be skipped
         * entirely and never traversed.
         *
         * This would break any base matchers under this match.  I.e. it's takes absolute precedence.
         */
        HashSet<FSSBackupItemBackupOption> currOpts = new HashSet<>();
        currOpts.add(FSSBackupItemBackupOption.BREAK_TRAVERSAL);

        folderItems.add(new BackupConfigDirectory("/tmp/backupTest/e/c-link-loop-22",
                currOpts,
                FSSBackupItemPathTypeEnum.REGEX,
                null, null, null,
                true));

        /**
         * Exclude, with no child matchers.  And also set the BREAK_TRAVERSAL option to
         */
        folderItems.add(new BackupConfigDirectory("/tmp/backupTest/e/c/logs/c-link-loop-dest",
                currOpts,
                FSSBackupItemPathTypeEnum.REGEX,
                null, null, null,
                true));

        Path currPath = Paths.get("/tmp/backupTest");

        BackupConfigItemResolver bcir = BackupConfigItemResolver.from(null, fileItems, folderItems, null, false);
        BackupToolResult res = bcir.call();

        Map<Path,Map<Path,BackupConfigDirectory>> foundFolders = bcir.getFoundFolders();

        Assert.assertNotNull(res);
        Assert.assertTrue(res.getStatus() == BackupToolResultStatus.SUCCESS);

        Assert.assertTrue(foundFolders.size() == 1);
        Assert.assertTrue(foundFolders.containsKey(currPath));

        Map<Path,BackupConfigDirectory> foundFoldersMap = foundFolders.get(currPath);
        Assert.assertNotNull(foundFoldersMap);
        Set<Path> foundFoldersSet = foundFoldersMap.keySet();

        Set<Path> expectedSet = new HashSet<>();
        expectedSet.add(Paths.get("/tmp/backupTest/b"));
        expectedSet.add(Paths.get("/tmp/backupTest/b/b"));
        expectedSet.add(Paths.get("/tmp/backupTest/b/logstest"));
        expectedSet.add(Paths.get("/tmp/backupTest/b/logstest/b"));
        expectedSet.add(Paths.get("/tmp/backupTest/b/logstest/c"));
        expectedSet.add(Paths.get("/tmp/backupTest/b/logstest/a"));
        expectedSet.add(Paths.get("/tmp/backupTest/b/c"));
        expectedSet.add(Paths.get("/tmp/backupTest/b/a"));
        expectedSet.add(Paths.get("/tmp/backupTest/b/testlogs"));
        expectedSet.add(Paths.get("/tmp/backupTest/b/testlogs/b"));
        expectedSet.add(Paths.get("/tmp/backupTest/b/testlogs/b/logstest"));
        expectedSet.add(Paths.get("/tmp/backupTest/b/testlogs/b/logstest/b"));
        expectedSet.add(Paths.get("/tmp/backupTest/b/testlogs/b/logstest/c"));
        expectedSet.add(Paths.get("/tmp/backupTest/b/testlogs/b/logstest/a"));
        expectedSet.add(Paths.get("/tmp/backupTest/b/testlogs/c"));
        expectedSet.add(Paths.get("/tmp/backupTest/b/testlogs/c/logstest"));
        expectedSet.add(Paths.get("/tmp/backupTest/b/testlogs/c/logstest/b"));
        expectedSet.add(Paths.get("/tmp/backupTest/b/testlogs/c/logstest/c"));
        expectedSet.add(Paths.get("/tmp/backupTest/b/testlogs/c/logstest/a"));
        expectedSet.add(Paths.get("/tmp/backupTest/b/testlogs/a"));
        expectedSet.add(Paths.get("/tmp/backupTest/b/testlogs/a/logstest"));
        expectedSet.add(Paths.get("/tmp/backupTest/b/testlogs/a/logstest/b"));
        expectedSet.add(Paths.get("/tmp/backupTest/b/testlogs/a/logstest/c"));
        expectedSet.add(Paths.get("/tmp/backupTest/b/testlogs/a/logstest/a"));

        // We allow "logs" directory under "c" directory
        expectedSet.add(Paths.get("/tmp/backupTest/c/a"));
        expectedSet.add(Paths.get("/tmp/backupTest/c/a/logs"));

        expectedSet.add(Paths.get("/tmp/backupTest/c/b"));
        expectedSet.add(Paths.get("/tmp/backupTest/c/b/logs"));

        Assert.assertTrue(foundFoldersSet.equals(expectedSet));

        // Now test the returned files
        expectedSet.clear();

        Map<Path,Map<Path,BackupConfigFile>> foundFiles = bcir.getFoundFiles();

        Assert.assertTrue(foundFiles.size() == 1);
        Assert.assertTrue(foundFiles.containsKey(currPath));

        Map<Path,BackupConfigFile> foundFilesMap = foundFiles.get(currPath);
        Assert.assertNotNull(foundFilesMap);
        Set<Path> foundFilesSet = foundFilesMap.keySet();

        expectedSet.add(Paths.get("/tmp/backupTest/c/test.txt"));

        expectedSet.add(Paths.get("/tmp/backupTest/c/a/file01.dat"));
        expectedSet.add(Paths.get("/tmp/backupTest/c/a/file02.dat"));

        // We did not filter the "logs" directory under the "c/a" directory only under "b" directory
        expectedSet.add(Paths.get("/tmp/backupTest/c/a/logs/test-15.log"));
        expectedSet.add(Paths.get("/tmp/backupTest/c/a/logs/test-16.dat"));

        expectedSet.add(Paths.get("/tmp/backupTest/b/b/test-07.txt"));
        expectedSet.add(Paths.get("/tmp/backupTest/b/b/test-04.txt"));
        expectedSet.add(Paths.get("/tmp/backupTest/b/c/test-05.txt"));
        expectedSet.add(Paths.get("/tmp/backupTest/b/c/test-08.txt"));
        expectedSet.add(Paths.get("/tmp/backupTest/b/a/test-03.txt"));
        expectedSet.add(Paths.get("/tmp/backupTest/b/a/test-06.txt"));
        expectedSet.add(Paths.get("/tmp/backupTest/b/test-14.dat"));

        Assert.assertTrue(foundFilesSet.equals(expectedSet));
    }

    @Test
    public void A004_testCall() throws Exception
    {
        log.debug("A004_testCall");

        List<BackupConfigDirectory> folderItems = new ArrayList<>();
        List<BackupConfigFile> fileItems = new ArrayList<>();

        List<BackupConfigFile> childFiles;

        /**
         * Negate the base directory, but define a match for files.
         *
         * This should force an "exclude by default" rule as it traverses the base directory.
         */
        childFiles = new ArrayList<>();
        childFiles.add(new BackupConfigFile(".*\\.dat",
                null,
                FSSBackupItemPathTypeEnum.REGEX, null,
                false));

        folderItems.add(new BackupConfigDirectory("/tmp/backupTest/[c]",
                null,
                FSSBackupItemPathTypeEnum.REGEX,
                null, childFiles, null,
                true));

        BackupConfigItemResolver bcir = BackupConfigItemResolver.from(null, fileItems, folderItems, null, false);
        BackupToolResult res = bcir.call();

        Assert.assertNotNull(res);
    }

    @Test
    public void A005_testCall() throws Exception
    {
        log.debug("A005_testCall");

        List<BackupConfigDirectory> folderItems = new ArrayList<>();
        List<BackupConfigFile> fileItems = new ArrayList<>();

        /**
         * Base file regex exclusion rule
         */
        fileItems.add(new BackupConfigFile("/tmp/backupTest/b/.*\\.log",
                null,
                FSSBackupItemPathTypeEnum.REGEX, null,
                true));

        BackupConfigItemResolver bcir = BackupConfigItemResolver.from(null, fileItems, folderItems, null, false);
        BackupToolResult res = bcir.call();

        Assert.assertNotNull(res);
    }

    /**
     * Simulates the situation were folders have all resolved to one base folder,
     * but files have resolved to a descendant of that first folder.
     *
     * In this situation, all the files should be corrected with the ancestor folder,
     * in place of the descendant.
     *
     * @throws Exception
     */
    @Test
    public void A006_deduplicateFolders() throws Exception
    {
        log.debug("A006_testDeduplicateFolders");

        Map<BackupConfigDirectory, Path> fldrPaths = new HashMap<>();
        Map<BackupConfigFile, Path> filePaths = new HashMap<>();

        fldrPaths.put(new BackupConfigDirectory("/tmp/backupTest/c/a(/.*)?",
                null,
                FSSBackupItemPathTypeEnum.REGEX,
                null, null, null,
                false), Paths.get("/tmp/backupTest"));

        fldrPaths.put(new BackupConfigDirectory("/tmp/backupTest/b(/.*)?",
                null,
                FSSBackupItemPathTypeEnum.REGEX,
                null, null, null,
                false), Paths.get("/tmp/backupTest"));

        fldrPaths.put(new BackupConfigDirectory("/tmp/backupTest/c/[b](/.*)?",
                null,
                FSSBackupItemPathTypeEnum.REGEX,
                null, null, null,
                false), Paths.get("/tmp/backupTest"));

        filePaths.put(new BackupConfigFile("/tmp/backupTest/c/test.txt",
                null,
                FSSBackupItemPathTypeEnum.LITERAL, null,
                false), Paths.get("/tmp/backupTest/c"));

        filePaths.put(new BackupConfigFile("/tmp/backupTest/c/test6.txt",
                null,
                FSSBackupItemPathTypeEnum.LITERAL, null,
                false), Paths.get("/tmp/backupTest/c"));

        filePaths.put(new BackupConfigFile("/tmp/backupTest/c/test2.txt",
                null,
                FSSBackupItemPathTypeEnum.LITERAL, null,
                false), Paths.get("/tmp/backupTest/c"));

        filePaths.put(new BackupConfigFile("/tmp/backupTest/c/b/test3.txt",
                null,
                FSSBackupItemPathTypeEnum.LITERAL, null,
                false), Paths.get("/tmp/backupTest/c"));

        filePaths.put(new BackupConfigFile("/tmp/backupTest/c/test4.txt",
                null,
                FSSBackupItemPathTypeEnum.LITERAL, null,
                false), Paths.get("/tmp/backupTest/c"));

        BackupToolResult res = BackupConfigItemResolver.deduplicateFolders(fldrPaths, filePaths);
        Assert.assertNotNull(res);
        Assert.assertTrue(res.getStatus() == BackupToolResultStatus.SUCCESS);

        // All files should have the folder set paths as a base
        for( BackupConfigFile currConf : filePaths.keySet() )
        {
            Path currPath = filePaths.get(currConf);

            Assert.assertTrue( currPath.endsWith(Paths.get("/tmp/backupTest")));
        }

        // Double check that the folder base paths weren't accidentally changed.
        for( BackupConfigDirectory currConf : fldrPaths.keySet() )
        {
            Path currPath = fldrPaths.get(currConf);

            Assert.assertTrue( currPath.endsWith(Paths.get("/tmp/backupTest")));
        }
    }

    /**
     * Test that we handle hard, soft links to files and directories.
     *
     * By default BackupConfigItemResolverWalker has the FileVisitOption.FOLLOW_LINKS set
     * in visit options.  We should turn off that flag and test ser well.
     *
     * @throws Exception
     */
    @Test
    public void A007_basicLinks() throws Exception
    {
        List<BackupConfigFile> fileItems = new ArrayList<>();

        /**
         * Backup hard link
         */
        fileItems.add(new BackupConfigFile("/tmp/backupTest/c/hlink-17.dat",
                null,
                FSSBackupItemPathTypeEnum.LITERAL, null,
                false));

        /**
         * Backup symbolic link
         */
        fileItems.add(new BackupConfigFile("/tmp/backupTest/c/c/slink-18.dat",
                null,
                FSSBackupItemPathTypeEnum.LITERAL, null,
                false));

        /**
         * Backup symbolic link that points to another symbolic link
         */
        fileItems.add(new BackupConfigFile("/tmp/backupTest/c/c/slink-20.dat",
                null,
                FSSBackupItemPathTypeEnum.LITERAL, null,
                false));

        Path currPath = Paths.get("/tmp/backupTest/c");

        BackupConfigItemResolver bcir = BackupConfigItemResolver.from(null, fileItems, null, null, false);
        BackupToolResult res = bcir.call();

        Assert.assertNotNull(res);
        Assert.assertTrue(res.getStatus() == BackupToolResultStatus.SUCCESS);

        // Now test the returned files
        Set<Path> expectedSet = new HashSet<>();

        Map<Path,Map<Path,BackupConfigFile>> foundFiles = bcir.getFoundFiles();

        Assert.assertTrue(foundFiles.size() == 1);
        Assert.assertTrue(foundFiles.containsKey(currPath));

        Map<Path,BackupConfigFile> foundFilesMap = foundFiles.get(currPath);
        Assert.assertNotNull(foundFilesMap);
        Set<Path> foundFilesSet = foundFilesMap.keySet();

        expectedSet.add(Paths.get("/tmp/backupTest/c/hlink-17.dat"));
        expectedSet.add(Paths.get("/tmp/backupTest/c/c/slink-18.dat"));
        expectedSet.add(Paths.get("/tmp/backupTest/c/c/slink-20.dat"));


        Assert.assertTrue(foundFilesSet.equals(expectedSet));
    }

    /**
     * Deal with broken links, and file-system loops.
     *
     * @throws Exception
     */
    @Test
    public void A009_basicLoopErrors() throws Exception
    {
        log.debug("A009_basicLoopErrors");

        List<BackupConfigDirectory> folderItems = new ArrayList<>();

        /**
         * File-system loop
         */
        folderItems.add(new BackupConfigDirectory("/tmp/backupTest/e/c-link-loop-22",
                null,
                FSSBackupItemPathTypeEnum.LITERAL,
                null, null, null,
                false));

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

        BackupConfigItemResolver bcir = BackupConfigItemResolver.from( currJobState,
                null, folderItems, null, false );
        BackupToolResult res = bcir.call();

        Assert.assertNotNull(res);
        Assert.assertTrue(res.getStatus()       == BackupToolResultStatus.FAIL);
        Assert.assertTrue(res.getStatusDetail() == BackupToolResultStatusDetail.FILESYSTEMLOOPDETECTED);
    }


    /**
     * Skip a subtree with a file-system loop that would otherwise stop the traversal
     *
     * @throws Exception
     */
    @Test
    public void A010_skipSubtree() throws Exception
    {
        log.debug("A010_skipSubtree");

        List<BackupConfigDirectory> folderItems = new ArrayList<>();
        List<BackupConfigFile> fileItems = new ArrayList<>();

        List<BackupConfigDirectory> childFolders;
        List<BackupConfigFile> childFiles;

        /**
         * Standard base file rule
         */
        fileItems.add(new BackupConfigFile("/tmp/backupTest/e/c/slink-18.dat",
                null,
                FSSBackupItemPathTypeEnum.LITERAL, null,
                false));

        /**
         * Exclude rule with no child matchers.  This file system subtree should be skipped
         * entirely and never traversed.
         *
         * This would break any base matchers under this match.  I.e. it's takes absolute precedence.
         */
        HashSet<FSSBackupItemBackupOption> currOpts = new HashSet<>();
        currOpts.add(FSSBackupItemBackupOption.BREAK_TRAVERSAL);

        folderItems.add(new BackupConfigDirectory("/tmp/backupTest/e/c-link-loop-22",
                currOpts,
                FSSBackupItemPathTypeEnum.REGEX,
                null, null, null,
                true));

        /**
         * Exclude, with no child matchers.  And also set the BREAK_TRAVERSAL option to
         */
        folderItems.add(new BackupConfigDirectory("/tmp/backupTest/e/c/logs/c-link-loop-dest",
                currOpts,
                FSSBackupItemPathTypeEnum.REGEX,
                null, null, null,
                true));

        Path currPath = Paths.get("/tmp/backupTest/e/c");

        BackupConfigItemResolver bcir = BackupConfigItemResolver.from(null, fileItems, folderItems, null, false);
        BackupToolResult res = bcir.call();

        Map<Path,Map<Path,BackupConfigDirectory>> foundFolders = bcir.getFoundFolders();

        Assert.assertNotNull(res);
        Assert.assertTrue(res.getStatus() == BackupToolResultStatus.SUCCESS);

        HashSet<Path> totalRes = new HashSet<>();
        for( Path currKey : foundFolders.keySet() )
        {
            // We expect no folders to be returned
            totalRes.addAll(foundFolders.get(currKey).keySet());
        }

        Assert.assertTrue( totalRes.isEmpty() );

        Set<Path> expectedSet = new HashSet<>();

        Map<Path,Map<Path,BackupConfigFile>> foundFiles = bcir.getFoundFiles();

        for( Path currKey : foundFiles.keySet() )
        {
            totalRes.addAll(foundFiles.get(currKey).keySet());
        }

        //Assert.assertTrue(foundFiles.size() == 1);
        Assert.assertTrue(foundFiles.containsKey(currPath));

        Assert.assertTrue(totalRes.size() == 1);

        Map<Path,BackupConfigFile> foundFilesMap = foundFiles.get(currPath);
        Assert.assertNotNull(foundFilesMap);
        Set<Path> foundFilesSet = foundFilesMap.keySet();

        expectedSet.add(Paths.get("/tmp/backupTest/e/c/slink-18.dat"));

        Assert.assertTrue(foundFilesSet.equals(expectedSet));
    }

    /**
     * Test that base matchers can indeed override each other.
     *
     * To override a match an exclude match can not stop traversal, since an unrelated base matcher
     * by need to access that subtree.  I.e. BREAK_TRAVERSAL option cannot be set.
     *
     * @throws Exception
     */
    @Test
    public void A011_overrideBaseMatchers() throws Exception
    {
        log.debug("A011_overrideBaseMatchers");

        List<BackupConfigDirectory> folderItems = new ArrayList<>();
        List<BackupConfigFile> fileItems = new ArrayList<>();

        /**
         * Standard base file rule
         */
        fileItems.add(new BackupConfigFile("/tmp/backupTest/c/a/logs/test-15.log",
                null,
                FSSBackupItemPathTypeEnum.LITERAL, null,
                false));

        /**
         * Exclude the base directory
         */
        folderItems.add(new BackupConfigDirectory("/tmp/backupTest/c/a",
                null,
                FSSBackupItemPathTypeEnum.LITERAL,
                null, null, null,
                true));

        Path currPath = Paths.get("/tmp/backupTest/c/a");

        BackupConfigItemResolver bcir = BackupConfigItemResolver.from(null, fileItems, folderItems, null, false);
        BackupToolResult res = bcir.call();

        Map<Path,Map<Path,BackupConfigDirectory>> foundFolders = bcir.getFoundFolders();

        Assert.assertNotNull(res);
        Assert.assertTrue(res.getStatus() == BackupToolResultStatus.SUCCESS);

        Assert.assertTrue(foundFolders.size() == 1);
        Assert.assertTrue(foundFolders.containsKey(currPath));

        Map<Path,BackupConfigDirectory> foundFoldersMap = foundFolders.get(currPath);
        Assert.assertNotNull(foundFoldersMap);
        Assert.assertTrue(foundFoldersMap.isEmpty());

        Set<Path> expectedSet = new HashSet<>();

        Map<Path,Map<Path,BackupConfigFile>> foundFiles = bcir.getFoundFiles();

        Assert.assertTrue(foundFiles.size() == 1);
        Assert.assertTrue(foundFiles.containsKey(currPath));

        Map<Path,BackupConfigFile> foundFilesMap = foundFiles.get(currPath);
        Assert.assertNotNull(foundFilesMap);
        Set<Path> foundFilesSet = foundFilesMap.keySet();

        expectedSet.add(Paths.get("/tmp/backupTest/c/a/logs/test-15.log"));

        Assert.assertTrue(foundFilesSet.equals(expectedSet));
    }

    @Test
    public void A012_brokenLink() throws Exception
    {
        List<BackupConfigFile> fileItems = new ArrayList<>();
        List<BackupConfigDirectory> folderItems = new ArrayList<>();

        /**
         * Broken link
         */
        fileItems.add(new BackupConfigFile("/tmp/backupTest/e/non-existent-link-19.log",
                null,
                FSSBackupItemPathTypeEnum.LITERAL, null,
                false));

        HashSet<FSSBackupItemBackupOption> currOpts = new HashSet<>();
        currOpts.add(FSSBackupItemBackupOption.BREAK_TRAVERSAL);

        folderItems.add(new BackupConfigDirectory("/tmp/backupTest/e/c-link-loop-22",
                currOpts,
                FSSBackupItemPathTypeEnum.REGEX,
                null, null, null,
                true));

        /**
         * Exclude, with no child matchers.
         */
        folderItems.add(new BackupConfigDirectory("/tmp/backupTest/e/c/logs/c-link-loop-dest",
                currOpts,
                FSSBackupItemPathTypeEnum.REGEX,
                null, null, null,
                true));

        Path currPath = Paths.get("/tmp/backupTest/e");

        BackupConfigItemResolver bcir = BackupConfigItemResolver.from(null, fileItems, folderItems, null, false);
        BackupToolResult res = bcir.call();

        Assert.assertNotNull(res);
        Assert.assertTrue(res.getStatus() == BackupToolResultStatus.SUCCESS);

        // Now test the returned files
        Set<Path> expectedSet = new HashSet<>();

        Map<Path,Map<Path,BackupConfigFile>> foundFiles = bcir.getFoundFiles();

        Assert.assertTrue(foundFiles.size() == 1);
        Assert.assertTrue(foundFiles.containsKey(currPath));

        Map<Path,BackupConfigFile> foundFilesMap = foundFiles.get(currPath);
        Assert.assertNotNull(foundFilesMap);
        Set<Path> foundFilesSet = foundFilesMap.keySet();

        expectedSet.add(Paths.get("/tmp/backupTest/e/non-existent-link-19.log"));

        Assert.assertTrue(foundFilesSet.equals(expectedSet));
    }
}
