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
package com.fastsitesoft.backuptool.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * Communicates the disposition of a running or completed job.
 *
 * Created by kervin on 8/10/15.
 */
public enum BackupToolJobDisposition
{
    NONE,
    DEBUG,
    INFO,
    WARN,
    ERROR;

    public static BackupToolJobDisposition from(String val)
    {
        BackupToolJobDisposition res;

        res = BackupToolJobDisposition.valueOf(
                StringUtils.upperCase(val) );

        return res;
    }
}
