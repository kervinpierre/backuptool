/*
 *  BackupLogic LLC CONFIDENTIAL
 *  DO NOT COPY
 * 
 * Copyright (c) [2012] - [2019] BackupLogic LLC <info@citymsp.nyc>
 * All Rights Reserved.
 * 
 * NOTICE:  All information contained herein is, and remains
 *  the property of BackupLogic LLC and its suppliers,
 *  if any.  The intellectual and technical concepts contained
 *  herein are proprietary to BackupLogic LLC and its suppliers and
 *  may be covered by U.S. and Foreign Patents, patents in process,
 *  and are protected by trade secret or copyright law.
 *  Dissemination of this information or reproduction of this material
 *  is strictly forbidden unless prior written permission is obtained
 *  from BackupLogic LLC
 */

package com.fastsitesoft.backuptool.constants;

import com.fastsitesoft.backuptool.enums.BackupToolNameComponentType;
import com.fastsitesoft.backuptool.enums.FSSBackupType;
import com.fastsitesoft.backuptool.enums.FSSVerbosity;

/**
 *
 * @author Kervin Pierre <info@sludev.com>
 */
public final class BackupConstants
{
    public static final String PROD_BUILD          = "20190106-01";
    public static final String PROD_VERSION        = "1.2";
    public static final String PROD_LONG_NAME      = "Backup Tool";
    
    public static final FSSVerbosity VERBOSITY = FSSVerbosity.WARN;
    
    /**
     * A list of characters that are special in Java Regex
     */
    public static final String REGEX_SPECIAL_CHARS = "([{\\^-=$!|]})?*+.";

    public static final String DEFAULT_JOB_NAME_RULE_PATTERN
                                    = "job-(\\d{4}-\\d{2}-\\d{2}T\\d{2}-\\d{2}-\\d{2}\\.\\d{3}Z)";
    public static final BackupToolNameComponentType DEFAULT_JOB_NAME_RULE_COMPONENT
            = BackupToolNameComponentType.ISO_TIMESTAMP;
    public static final String DEFAULT_JOB_NAME_TEMPLATE = "job-0000-00-00T00-00-00.000Z";

    public static final String DEFAULT_ARCHIVE_NAME_RULE_PATTERN = ".*?(\\d+)+\\.btaf";
    public static final BackupToolNameComponentType DEFAULT_ARCHIVE_NAME_RULE_COMPONENT
            = BackupToolNameComponentType.INTEGER_SEQUENCE;
    public static final String DEFAULT_ARCHIVE_NAME_TEMPLATE = "archive-0000.btaf";

    public static final String DEFAULT_STATE_FILENAME = "backupdata.xml";
    public static final String DEFAULT_ERROR_FILENAME = "backupErr.xml";

    public static final FSSBackupType DEFAULT_BACKUP_TYPE = FSSBackupType.FULL;

    public static final int DEFAULT_MINIMUM_CHUNK_SIZE = 1024;
}
