/*
 *  SLU Dev Inc. CONFIDENTIAL
 *  DO NOT COPY
 * 
 *  Copyright (c) [2012] - [2015] SLU Dev Inc. <info@sludev.com>
 *  All Rights Reserved.
 * 
 *  NOTICE:  All information contained herein is, and remains
 *  the property of SLU Dev Inc. and its suppliers,
 *  if any.  The intellectual and technical concepts contained
 *  herein are proprietary to SLU Dev Inc. and its suppliers and
 *  may be covered by U.S. and Foreign Patents, patents in process,
 *  and are protected by trade secret or copyright law.
 *  Dissemination of this information or reproduction of this material
 *  is strictly forbidden unless prior written permission is obtained
 *  from SLU Dev Inc.
 */
package com.fastsitesoft.backuptool.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.logging.log4j.LogManager;

/**
 * Digest services using SHA1
 * 
 * @author Kervin Pierre <info@sludev.com>
 */
public class FSSDigestSHA1 implements FSSDigest
{
    private static final org.apache.logging.log4j.Logger log 
                                = LogManager.getLogger(FSSDigestSHA1.class);
    
    /**
     * Calculate the SHA1 digest
     * 
     * @param f
     * @return
     * @throws com.fastsitesoft.backuptool.utils.BackupToolException
     */
    @Override
    public byte [] getDigest(File f) throws BackupToolException
    {
        byte[] res = null;
        
        try
        {
            MessageDigest sha = MessageDigest.getInstance("SHA");
            try (FileInputStream fin = new FileInputStream(f);
                    DigestInputStream din = new DigestInputStream(fin, sha)) 
            {
                while(din.read() != -1)
                {
                    ;
                }
            }
            res = sha.digest();
        } 
        catch (NoSuchAlgorithmException ex)
        {
            String errMsg = String.format("Error creating MessageDigest() for 'SHA'");
            log.info(errMsg);
            throw new BackupToolException(errMsg, ex);
        } 
        catch (FileNotFoundException ex)
        {
            String errMsg = String.format("Error finding file for digest '%s'", f.getAbsolutePath());
            log.info(errMsg);
            throw new BackupToolException(errMsg, ex);
        } 
        catch (IOException ex)
        {
            String errMsg = String.format("I/O exceptiong building digest for '%s'", f.getAbsolutePath());
            log.info(errMsg);
            throw new BackupToolException(errMsg, ex);
        }
        
        return res;
    }
}
