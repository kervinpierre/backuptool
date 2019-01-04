/*
 *  SLU Dev Inc. CONFIDENTIAL
 *  DO NOT COPY
 * 
 * Copyright (c) [2012] - [2015] SLU Dev Inc. <info@sludev.com>
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

package com.fastsitesoft.backuptool.enums;


import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Kervin Pierre <info@sludev.com>
 */
public enum FSSBackupHashType
{
    NONE,
    MD5,
    SHA1,
    SHA2;

    private static final Logger log
            = LogManager.getLogger(FSSBackupHashType.class);

    public static FSSBackupHashType from(String c)
    {
        FSSBackupHashType currCS = null;

        try
        {
            String currV = StringUtils.upperCase(c);
            currCS = FSSBackupHashType.valueOf(currV);
        }
        catch (Exception ex)
        {
            log.error( String.format("Invalid Checksum type specified '%s'", c), ex);
        }

        return currCS;
    }
}
