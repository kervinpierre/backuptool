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

import com.fastsitesoft.backuptool.utils.BackupToolException;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Kervin Pierre <info@sludev.com>
 */
public enum FSSBackupSizeType
{
    NONE,
    B,
    KB,
    MB,
    GB,
    TB;

    public static FSSBackupSizeType from(String t) throws BackupToolException
    {
        if( StringUtils.isBlank(t))
        {
            throw new BackupToolException(
                    String.format("Error setting size type '{0}'",
                            t));
        }

        FSSBackupSizeType sizeType = FSSBackupSizeType.valueOf(t.trim().toUpperCase());

        return sizeType;
    }
}
