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

package com.fastsitesoft.backuptool.enums;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by kervin on 2015-08-24.
 */
public enum BackupToolFileFormats
{
    NONE,
    JOBSETSTATE,
    JOBSTATE,
    JOBERROR,
    BACKUPCONFIGURATION,
    JOBARCHIVELISTING;

    private static final Logger log = LogManager.getLogger(BackupToolFileFormats.class);

    public static String getSchema( BackupToolFileFormats format )
    {
        String res;
        switch( format )
        {
            case JOBSETSTATE:
                res = "jobSetState.xsd";
                break;

            case JOBSTATE:
                res = "jobState.xsd";
                break;

            case JOBERROR:
                res = "jobError.xsd";
                break;

            case JOBARCHIVELISTING:
                res = "jobArchiveListing.xsd";
                break;

            case BACKUPCONFIGURATION:
                res = "backupSetConfig.xsd";
                break;

            default:
                res = "";
        }

        return res;
    }
}
