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
package com.fastsitesoft.backuptool.backend;

import com.fastsitesoft.backuptool.testutils.BackupToolTestWatcher;
import com.fastsitesoft.backuptool.testutils.BackupToolTestProperties;
import com.fastsitesoft.backuptool.testutils.StorageBackendTestUtils;
import com.fastsitesoft.backuptool.file.IBackupFilePart;
import com.sludev.commons.vfs2.provider.azure.AzConstants;
import com.sludev.commons.vfs2.provider.s3.SS3Constants;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runners.MethodSorters;

/**
 *
 * @author kervin
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StorageBackendFileSystemTest
{
    private static final Logger log = LogManager.getLogger(StorageBackendFileSystemTest.class);
    
    private Properties testProperties;
    
    private boolean remoteAzureSetup;
    private boolean remoteS3Setup;
    
    @Rule
    public TestWatcher testWatcher = new BackupToolTestWatcher();
    
    @Before
    public void setUp() 
    {
        
        /**
         * Get the current test properties from a file so we don't hard-code
         * in our source code.
         */
        testProperties = BackupToolTestProperties.GetProperties();
        
        remoteAzureSetup = false;
        remoteS3Setup = false;
    }
    public StorageBackendFileSystemTest()
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
    
    @After
    public void tearDown()
    {
    }

    /**
     * Test of copy method, of class StorageBackendFileSystem.
     */
    @Test
    public void A001_testAzUpload() throws Exception
    {
        log.debug("A001_testAzUpload");
        
        String currAccountStr = testProperties.getProperty("azure.account.name");
        String currKey = testProperties.getProperty("azure.account.key");
        String currContainerStr = testProperties.getProperty("azure.test0001.container.name");
        String currHost = testProperties.getProperty("azure.host");  // <account>.blob.core.windows.net
        
        File temp = File.createTempFile("uploadFile01", ".tmp");
        try(FileWriter fw = new FileWriter(temp))
        {
            BufferedWriter bw = new BufferedWriter(fw);
            bw.append("testing...");
            bw.flush();
        }
        temp.deleteOnExit();
        
        log.debug( String.format("uploading '%s'...\n", temp.getAbsolutePath()) );
        
        IBackendFileSystem instance 
                              = StorageBackendFileSystem.from( currAccountStr, currKey, currHost,
                                      AzConstants.AZSBSCHEME );
        
        instance.init();

        Path contPath = Paths.get(currContainerStr);

        IBackupFilePart src = instance.resolveLocalFile(temp.toPath());
        IBackupFilePart dst = instance.resolveRemoteFile(contPath.resolve("uploadTest001.txt"));
        
        instance.copy(src, dst);
    }
    
    /**
     * Test of copy method, of class StorageBackendFileSystem.
     */
    @Test
    public void A002_testS3Upload() throws Exception
    {
        log.debug("A002_testS3Upload() start");
        
        String currAccountStr = testProperties.getProperty("s3.access.id"); 
        String currKey = testProperties.getProperty("s3.access.secret");
        String currContainerStr = testProperties.getProperty("s3.test0001.bucket.name");
        String currHost = testProperties.getProperty("s3.host");
        String currRegion = testProperties.getProperty("s3.region");
        
        File temp = File.createTempFile("uploadFile01", ".tmp");
        try(FileWriter fw = new FileWriter(temp))
        {
            BufferedWriter bw = new BufferedWriter(fw);
            bw.append("testing...");
            bw.flush();
        }
        temp.deleteOnExit();
        
        log.debug( String.format("uploading '%s'...\n", temp.getAbsolutePath()) );
        
        IBackendFileSystem instance 
                              = StorageBackendFileSystem.from( currAccountStr, currKey, currHost,
                                      SS3Constants.S3SCHEME );
        
        instance.init();

        Path contPath = Paths.get(currContainerStr);

        IBackupFilePart src = instance.resolveLocalFile(temp.toPath());
        IBackupFilePart dst = instance.resolveRemoteFile(contPath.resolve("uploadTest001.txt"));
        
        instance.copy(src, dst);
    }
    
    @Test
    public void A003_testAzDownload() throws Exception
    {
        log.debug("A003_testAzDownload");
        
        String currAccountStr = testProperties.getProperty("azure.account.name");
        String currKey = testProperties.getProperty("azure.account.key");
        String currContainerStr = testProperties.getProperty("azure.test0001.container.name");
        String currHost = testProperties.getProperty("azure.host");  // <account>.blob.core.windows.net
        
        File temp = File.createTempFile("uploadFile01", ".tmp");
        String currFile = "uploadTest001.txt";
        log.debug( String.format("downloading '%s' to '%s'...\n", 
                                 currFile, temp.getAbsolutePath()) );
        
        IBackendFileSystem instance 
                              = StorageBackendFileSystem.from( currAccountStr, currKey, currHost,
                                      AzConstants.AZSBSCHEME );
        
        instance.init();

        Path contPath = Paths.get(currContainerStr);

        IBackupFilePart dst = instance.resolveLocalFile(temp.toPath());
        IBackupFilePart src = instance.resolveRemoteFile(contPath.resolve(currFile));
        
        instance.copy(src, dst);
    }
    
    @Test
    public void A004_testS3Download() throws Exception
    {
        log.debug("A004_testS3Download");
        
        String currAccountStr = testProperties.getProperty("s3.access.id"); 
        String currKey = testProperties.getProperty("s3.access.secret");
        String currContainerStr = testProperties.getProperty("s3.test0001.bucket.name");
        String currHost = testProperties.getProperty("s3.host");
        String currRegion = testProperties.getProperty("s3.region");
        
        File temp = File.createTempFile("uploadFile01", ".tmp");
        String currFile = "uploadTest001.txt";
        log.debug( String.format("downloading '%s' to '%s'...\n", 
                                 currFile, temp.getAbsolutePath()) );
        
        IBackendFileSystem instance 
                              = StorageBackendFileSystem.from( currAccountStr, currKey, currHost,
                                      SS3Constants.S3SCHEME );
        
        instance.init();

        Path contPath = Paths.get(currContainerStr);

        IBackupFilePart dst = instance.resolveLocalFile(temp.toPath());
        IBackupFilePart src = instance.resolveRemoteFile(contPath.resolve(currFile));
        
        instance.copy(src, dst);
    }
    
    @Test
    public void A005_testAzExists() throws Exception
    {
        log.debug("A005_testAzExists");
        
        String currAccountStr = testProperties.getProperty("azure.account.name");
        String currKey = testProperties.getProperty("azure.account.key");
        String currContainerStr = testProperties.getProperty("azure.test0001.container.name");
        String currHost = testProperties.getProperty("azure.host");  // <account>.blob.core.windows.net
        
        String currFile = "uploadTest001.txt";
        log.debug( String.format("checking exist() on '%s'...\n", 
                                 currFile ) );
        
        IBackendFileSystem instance 
                              = StorageBackendFileSystem.from( currAccountStr, currKey, currHost,
                                      AzConstants.AZSBSCHEME );
        
        instance.init();


        Path contPath = Paths.get(currContainerStr);

        IBackupFilePart file = instance.resolveRemoteFile(contPath.resolve(currFile));
        
        boolean existRes = instance.exists(file);
        Assert.assertTrue(existRes);
        
        file = instance.resolveRemoteFile(contPath.resolve("non-existant-file-8632857264.tmp"));
        
        existRes = instance.exists(file);
        Assert.assertFalse(existRes);
    }
    
    @Test
    public void A006_testS3Exists() throws Exception
    {
        log.debug("A006_testS3Exists");
        
        String currAccountStr = testProperties.getProperty("s3.access.id"); 
        String currKey = testProperties.getProperty("s3.access.secret");
        String currContainerStr = testProperties.getProperty("s3.test0001.bucket.name");
        String currHost = testProperties.getProperty("s3.host");
        String currRegion = testProperties.getProperty("s3.region");
        
        String currFile = "uploadTest001.txt";
        log.debug( String.format("checking exist() on '%s'...\n", 
                                 currFile ) );
        
        IBackendFileSystem instance 
                              = StorageBackendFileSystem.from( currAccountStr, currKey, currHost,
                                      SS3Constants.S3SCHEME );


        Path contPath = Paths.get(currContainerStr);

        instance.init();
        
        IBackupFilePart file = instance.resolveRemoteFile(contPath.resolve(currFile));
        
        boolean existRes = instance.exists(file);
        Assert.assertTrue(existRes);
        
        file = instance.resolveRemoteFile(contPath.resolve("non-existant-file-8632857264.tmp"));
        
        existRes = instance.exists(file);
        Assert.assertFalse(existRes);
    }
    
    @Test
    public void A007_testAzContentSize() throws Exception
    {
        log.debug("A007_testAzContentSize");
        
        String currAccountStr = testProperties.getProperty("azure.account.name");
        String currKey = testProperties.getProperty("azure.account.key");
        String currContainerStr = testProperties.getProperty("azure.test0001.container.name");
        String currHost = testProperties.getProperty("azure.host");  // <account>.blob.core.windows.net
        
        String currFile = "uploadTest001.txt";
        log.debug( String.format("checking exist() on '%s'...\n", 
                                 currFile ) );
        
        IBackendFileSystem instance 
                              = StorageBackendFileSystem.from( currAccountStr, currKey, currHost,
                                      AzConstants.AZSBSCHEME );
        
        instance.init();

        Path contPath = Paths.get(currContainerStr);

        IBackupFilePart file = instance.resolveRemoteFile(contPath.resolve(currFile));
        
        long sizeRes = instance.getContentSize(file);
        Assert.assertTrue(sizeRes>0);
    }
    
    @Test
    public void A008_testS3ContentSize() throws Exception
    {
        log.debug("A008_testS3ContentSize");
        
        String currAccountStr = testProperties.getProperty("s3.access.id"); 
        String currKey = testProperties.getProperty("s3.access.secret");
        String currContainerStr = testProperties.getProperty("s3.test0001.bucket.name");
        String currHost = testProperties.getProperty("s3.host");
        String currRegion = testProperties.getProperty("s3.region");
        
        String currFile = "uploadTest001.txt";
        log.debug( String.format("checking exist() on '%s'...\n", 
                                 currFile ) );
        
        IBackendFileSystem instance 
                              = StorageBackendFileSystem.from( currAccountStr, currKey, currHost,
                                      SS3Constants.S3SCHEME );


        Path contPath = Paths.get(currContainerStr);

        instance.init();
        
        IBackupFilePart file = instance.resolveRemoteFile(contPath.resolve(currFile));
        
        long sizeRes = instance.getContentSize(file);
        Assert.assertTrue(sizeRes>0);
    }
    
    @Test
    public void A009_testAzCreateFile() throws Exception
    {
        log.debug("A009_testAzCreateFile");
        
        String currAccountStr = testProperties.getProperty("azure.account.name");
        String currKey = testProperties.getProperty("azure.account.key");
        String currContainerStr = testProperties.getProperty("azure.test0001.container.name");
        String currHost = testProperties.getProperty("azure.host");  // <account>.blob.core.windows.net
        
        String currFile = "createFileTest001.txt";
        log.debug( String.format("checking createFile() on '%s'...\n", 
                                 currFile ) );
        
        IBackendFileSystem instance 
                              = StorageBackendFileSystem.from( currAccountStr, currKey, currHost,
                                      AzConstants.AZSBSCHEME );
        
        instance.init();

        Path contPath = Paths.get(currContainerStr);

        IBackupFilePart file = instance.resolveRemoteFile(contPath.resolve(currFile));
        
        instance.createFile(file);
        long sizeRes = instance.getContentSize(file);
        Assert.assertTrue(sizeRes==0);
        
        instance.delete(file);
    }
    
    @Test
    public void A010_testS3CreateFile() throws Exception
    {
        log.debug("A009_testS3CreateFile");
        
        String currAccountStr = testProperties.getProperty("s3.access.id"); 
        String currKey = testProperties.getProperty("s3.access.secret");
        String currContainerStr = testProperties.getProperty("s3.test0001.bucket.name");
        String currHost = testProperties.getProperty("s3.host");
        String currRegion = testProperties.getProperty("s3.region");
        
        String currFile = "createFileTest001.txt";
        log.debug( String.format("checking createFile() on '%s'...\n", 
                                 currFile ) );
        
        IBackendFileSystem instance 
                              = StorageBackendFileSystem.from( currAccountStr, currKey, currHost,
                                      SS3Constants.S3SCHEME );


        Path contPath = Paths.get(currContainerStr);

        instance.init();
        
        IBackupFilePart file = instance.resolveRemoteFile(contPath.resolve(currFile));
        
        instance.createFile(file);
        long sizeRes = instance.getContentSize(file);
        Assert.assertTrue(sizeRes==0);
        
        instance.delete(file);
    }
    
    @Test
    public void A011_testAzListChildren() throws Exception
    {
        log.debug("A011_testAzListChildren");
        
        String currAccountStr = testProperties.getProperty("azure.account.name");
        String currKey = testProperties.getProperty("azure.account.key");
        String currContainerStr = testProperties.getProperty("azure.test0001.container.name");
        String currHost = testProperties.getProperty("azure.host");  // <account>.blob.core.windows.net
        
        try
        {
            uploadFileSetup02(AzConstants.AZSBSCHEME);

            String currFolder = "uploadFile02";
            log.debug( String.format("checking listChildren() on '%s'...\n", 
                                     currFolder ) );

            IBackendFileSystem instance 
                                  = StorageBackendFileSystem.from( currAccountStr, currKey, currHost,
                                          AzConstants.AZSBSCHEME );

            instance.init();

            Path contPath = Paths.get(currContainerStr);

            IBackupFilePart folder = instance.resolveRemoteFile(contPath.resolve(currFolder));

            List<IBackupFilePart> children = instance.getChildren(folder);
            
            Assert.assertTrue(children.size() == 3);
        }
        finally
        {
            if( remoteAzureSetup )
            {
                removeFileSetup02(AzConstants.AZSBSCHEME);
            }
        }
    }
    
    
    @Test
    public void A012_testS3ListChildren() throws Exception
    {
        log.debug("A012_testS3ListChildren");
        
        String currAccountStr = testProperties.getProperty("s3.access.id"); 
        String currKey = testProperties.getProperty("s3.access.secret");
        String currContainerStr = testProperties.getProperty("s3.test0001.bucket.name");
        String currHost = testProperties.getProperty("s3.host");
        String currRegion = testProperties.getProperty("s3.region");
        
        try
        {
            uploadFileSetup02(SS3Constants.S3SCHEME);

            String currFolder = "uploadFile02";
            log.debug( String.format("checking listChildren() on '%s'...\n", 
                                     currFolder ) );

            IBackendFileSystem instance 
                                  = StorageBackendFileSystem.from( currAccountStr, currKey, currHost,
                                          SS3Constants.S3SCHEME );

            instance.init();


            Path contPath = Paths.get(currContainerStr);

            IBackupFilePart folder = instance.resolveRemoteFile(contPath.resolve(currFolder));

            List<IBackupFilePart> children = instance.getChildren(folder);
            
            Assert.assertTrue(children.size() == 3);
        }
        finally
        {
            if( remoteS3Setup )
            {
                removeFileSetup02(SS3Constants.S3SCHEME);
            }
        }
    }
    
    @Test
    public void A013_testAzFolderExists() throws Exception
    {
        log.debug("A013_testAzFolderExists");
        
        String currAccountStr = testProperties.getProperty("azure.account.name");
        String currKey = testProperties.getProperty("azure.account.key");
        String currContainerStr = testProperties.getProperty("azure.test0001.container.name");
        String currHost = testProperties.getProperty("azure.host");  // <account>.blob.core.windows.net
        
        try
        {
            uploadFileSetup02(AzConstants.AZSBSCHEME);

            String currFolder = "uploadFile02";
            log.debug( String.format("checking exist() on '%s'...\n", 
                                     currFolder ) );

            IBackendFileSystem instance 
                                  = StorageBackendFileSystem.from( currAccountStr, currKey, currHost,
                                          AzConstants.AZSBSCHEME );

            instance.init();


            Path contPath = Paths.get(currContainerStr);

            IBackupFilePart folder = instance.resolveRemoteFile(contPath.resolve(currFolder));

            boolean existRes = instance.exists(folder);
            Assert.assertTrue(existRes);
        }
        finally
        {
            if( remoteS3Setup )
            {
                removeFileSetup02(AzConstants.AZSBSCHEME);
            }
        }
    }
    
    
    @Test
    public void A014_testS3FolderExists() throws Exception
    {
        log.debug("A014_testS3FolderExists");
        
        try
        {
            uploadFileSetup02(SS3Constants.S3SCHEME);

            String currAccountStr = testProperties.getProperty("s3.access.id"); 
            String currKey = testProperties.getProperty("s3.access.secret");
            String currContainerStr = testProperties.getProperty("s3.test0001.bucket.name");
            String currHost = testProperties.getProperty("s3.host");
            String currRegion = testProperties.getProperty("s3.region");

            String currFolder = "uploadFile02";
            log.debug( String.format("checking exist() on '%s'...\n", 
                                     currFolder ) );

            IBackendFileSystem instance 
                                  = StorageBackendFileSystem.from( currAccountStr, currKey, currHost,
                                         SS3Constants.S3SCHEME );

            instance.init();


            Path contPath = Paths.get(currContainerStr);

            IBackupFilePart folder = instance.resolveRemoteFile(contPath.resolve(currFolder));

            boolean existRes = instance.exists(folder);
            Assert.assertTrue(existRes);
        }
        finally
        {
            if( remoteS3Setup )
            {
                removeFileSetup02(SS3Constants.S3SCHEME);
            }
        }
    }
    
    public void uploadFileSetup02(String scheme) throws Exception
    {
        String currAccountStr = null; 
        String currKey = null;
        String currContainerStr = null;
        String currHost = null;
        //String currRegion = null;
        
        switch(scheme)
        {
            case SS3Constants.S3SCHEME:
                if( remoteS3Setup )
                {
                    return;
                }
                
                currAccountStr = testProperties.getProperty("s3.access.id"); 
                currKey = testProperties.getProperty("s3.access.secret");
                currContainerStr = testProperties.getProperty("s3.test0001.bucket.name");
                currHost = testProperties.getProperty("s3.host");
                //currRegion = testProperties.getProperty("s3.region");
                break;
                
            case AzConstants.AZSBSCHEME:
                if( remoteAzureSetup )
                {
                    return;
                }
                
                currAccountStr = testProperties.getProperty("azure.account.name");
                currKey = testProperties.getProperty("azure.account.key");
                currContainerStr = testProperties.getProperty("azure.test0001.container.name");
                currHost = testProperties.getProperty("azure.host");  // <account>.blob.core.windows.net
                break;
        }
        
        IBackendFileSystem instance 
                              = StorageBackendFileSystem.from( currAccountStr, currKey, currHost,
                                      scheme );
        
        instance.init();


        Path contPath = Paths.get(currContainerStr);

        File temp = StorageBackendTestUtils.createTempFile("uploadFile02", ".tmp", "File 01");      
        StorageBackendTestUtils.uploadFile(instance, temp.toPath(),
                               contPath.resolve("uploadFile02/dir01/file01"));
        temp.delete();
        
        temp = StorageBackendTestUtils.createTempFile("uploadFile02", ".tmp", "File 02");      
        StorageBackendTestUtils.uploadFile(instance, temp.toPath(),
                                contPath.resolve("uploadFile02/dir01/file02"));
        temp.delete();
        
        temp = StorageBackendTestUtils.createTempFile("uploadFile02", ".tmp", "File 03");      
        StorageBackendTestUtils.uploadFile(instance, temp.toPath(),
                                contPath.resolve("uploadFile02/dir02/file03"));
        temp.delete();
        
        temp = StorageBackendTestUtils.createTempFile("uploadFile02", ".tmp", "File 04");      
        StorageBackendTestUtils.uploadFile(instance, temp.toPath(),
                                contPath.resolve("uploadFile02/file04"));
        temp.delete();
        
        temp = StorageBackendTestUtils.createTempFile("uploadFile02", ".tmp", "File 05");      
        StorageBackendTestUtils.uploadFile(instance, temp.toPath(),
                                contPath.resolve("file05"));
        temp.delete();
        
        temp = StorageBackendTestUtils.createTempFile("uploadFile02", ".tmp", "File 06");      
        StorageBackendTestUtils.uploadFile( instance, temp.toPath(),
                                contPath.resolve("uploadFile02/dir02/file06"));
        temp.delete();
        
        switch(scheme)
        {
            case SS3Constants.S3SCHEME:
                remoteS3Setup = true;
                break;
                
            case AzConstants.AZSBSCHEME:
                remoteAzureSetup = true;
                break;
        }
    }
    
    public void removeFileSetup02(String scheme) throws Exception
    {
        String currAccountStr = null; 
        String currKey = null;
        String currContainerStr = null;
        String currHost = null;
        //String currRegion = null;
        
        switch(scheme)
        {
            case SS3Constants.S3SCHEME:
                currAccountStr = testProperties.getProperty("s3.access.id"); 
                currKey = testProperties.getProperty("s3.access.secret");
                currContainerStr = testProperties.getProperty("s3.test0001.bucket.name");
                currHost = testProperties.getProperty("s3.host");
                //currRegion = testProperties.getProperty("s3.region");
                break;
                
            case AzConstants.AZSBSCHEME:
                currAccountStr = testProperties.getProperty("azure.account.name");
                currKey = testProperties.getProperty("azure.account.key");
                currContainerStr = testProperties.getProperty("azure.test0001.container.name");
                currHost = testProperties.getProperty("azure.host");  // <account>.blob.core.windows.net
                break;
        }
            
        IBackendFileSystem instance 
                              = StorageBackendFileSystem.from( currAccountStr, currKey, currHost,
                                      scheme );
        
        instance.init();

        Path contPath = Paths.get(currContainerStr);

        StorageBackendTestUtils.deleteFolder(instance,
                               contPath.resolve("uploadFile02"), true);

     
        StorageBackendTestUtils.deleteFile(instance,
                               contPath.resolve("file05"));
    }
}