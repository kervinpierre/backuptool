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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Kervin Pierre <info@sludev.com>
 */
public enum FSSReportType
{
    NONE,
    PLAIN,
    XML;

    private static final Logger log
            = LogManager.getLogger(FSSReportType.class);

    public static FSSReportType from(String b) throws BackupToolException
    {
        FSSReportType currRept = null;

        try
        {
            String currV = StringUtils.upperCase(b);
            currRept = FSSReportType.valueOf(currV);
        }
        catch (Exception ex)
        {
            throw new BackupToolException( String.format("Invalid Checksum type specified '%s'", b), ex);
        }

        return currRept;
    }
};
