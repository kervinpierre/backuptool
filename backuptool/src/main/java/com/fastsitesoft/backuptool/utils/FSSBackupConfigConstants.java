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

package com.fastsitesoft.backuptool.utils;

import com.fastsitesoft.backuptool.enums.BackupToolCompressionType;
import com.fastsitesoft.backuptool.enums.FSSBackupSizeType;
import com.fastsitesoft.backuptool.enums.FSSBackupStorageBackendType;
import com.fastsitesoft.backuptool.enums.FSSBackupType;

/**
 *
 * @author Kervin Pierre <info@sludev.com>
 */
public final class FSSBackupConfigConstants
{
    public static final String FSDEFAULT_BACKUP_STATEFILE = ".BACKUPSTATE";
    public static final FSSBackupType FSDEFAULT_BACKUP_TYPE
                    = FSSBackupType.FULL;
    public static final BackupToolCompressionType FSDEFAULT_BACKUP_COMPRESSION
                    = BackupToolCompressionType.GZIP;
    
    public static final Boolean FSDEFAULT_BACKUP_CHUNK_ENABLE = true;
    public static final int FSDEFAULT_BACKUP_CHUNK_SIZE = 10;
    public static final FSSBackupSizeType FSDEFAULT_BACKUP_CHUNK_SIZETYPE
                    = FSSBackupSizeType.MB;
    
    public static final String FSDEFAULT_BACKUP_LOGFILE  = "fssbackup.log";
    public static final Boolean FSDEFAULT_BACKUP_EMAILONCOMPLETION = false;
    public static final String FSDEFAULT_BACKUP_LOCKFILE  = "fssbackup.lock";
    
    public static final Boolean FSDEFAULT_BACKUP_STORAGE_ENABLE = false;
    public static final FSSBackupStorageBackendType FSDEFAULT_BACKUP_STORAGE_BACKEND
                    = FSSBackupStorageBackendType.NONE;
    
    public static final String FSDEFAULT_BACKUP_CONF  = "fssbackup.conf";
    
    
    // 128 bits key for AES Encryption , If want increase key size, need to
    // update Security policy of JRE
    public static final String SEC_KEY = "1A2B3C4D5E6F7G8H";
    // AES Alogorithm
    public static final String ALGORITHUM = "AES/CBC/NoPadding";
    public static final byte[] iv = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
    public static int FSDEFAULT_PROCESS_PRIORITY = 5;
}
