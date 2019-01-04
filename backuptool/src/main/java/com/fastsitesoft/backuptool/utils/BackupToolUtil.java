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
package com.fastsitesoft.backuptool.utils;

import com.fastsitesoft.backuptool.config.entities.UsageConfig;
import com.fastsitesoft.backuptool.constants.BackupConstants;

/**
 *
 * @author kervin
 */
public class BackupToolUtil
{
    public static void displayVersion()
    {
        System.out.println( String.format(
                "\n%s : Version %s: Build [%s]\n\u00a9 All Rights Reserved.\n", 
                    BackupConstants.PROD_LONG_NAME,
                    BackupConstants.PROD_VERSION,
                    BackupConstants.PROD_BUILD) );
    }
    
    public static void displayUsage( UsageConfig uc )
    {
        System.out.println( uc.getHelpString() );
        
        System.out.println( String.format(
                "\n%s : Version %s: Build [%s]\n\u00a9 All Rights Reserved.\n", 
                    BackupConstants.PROD_LONG_NAME,
                    BackupConstants.PROD_VERSION,
                    BackupConstants.PROD_BUILD) );
    }
}
