/*
 *  SLU Dev Inc. CONFIDENTIAL
 *  DO NOT COPY
 * 
 * Copyright (c) [2012] - [2014] SLU Dev Inc. <info@sludev.com>
 * All Rights Reserved.
 * 
 * NOTICE:  All information contained herein is, and remains
 *  the property of SLU Dev Inc. and its suppliers,
 *  if any.  The intellectual and technical concepts contained
 *  herein are proprietary to SLU Dev Inc. and its suppliers and
 *  may be covered by U.S. and Foreign Patents, patents in process,
 *  and are protected by trade secret or copyright law.
 *  Dissemination of this information or reproduction of this material
 *  is strictly forbidden unless prior written permission is obtained
 *  from SLU Dev Inc.
 */

package com.fastsitesoft.backuptool;

import java.nio.file.Path;
import java.nio.file.Paths;
import junit.framework.TestCase;
import org.junit.Ignore;

/**
 *
 * @author Kervin Pierre <info@sludev.com>
 */
public class BackupToolClassTest extends TestCase
{
    public BackupToolClassTest(String testName)
    {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    /**
     * Test of readConfig method, of class BackupToolClass.
     * @throws java.lang.Exception
     */
    @Ignore
    public void testReadBackupConfig() throws Exception
    {
        System.out.println("readConfig");

        Path confPath = Paths.get("src/test/resources/dataset01/config00001.xml");
        
       // BackupToolClass instance = new BackupToolClass();
       // FSBackupConfigClass conf = instance.readConfig(confPath);
        
      //  instance.validateBackupConfig(conf);
    }
    
}
