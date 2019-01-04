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
package com.fastsitesoft.backuptool.enums;

/**
 * List of members in the all configuration classes.
 * 
 * @author Kervin Pierre <info@sludev.com>
 */
public enum FSSConfigMembersEnum
{
    NONE,
    
    // BackupConfig values
    BACKUPCONFIG_SETNAME,
    BACKUPCONFIG_SETTYPE,
    BACKUPCONFIG_FOLDERLIST,
    BACKUPCONFIG_FILELIST,
    BACKUPCONFIG_STATEFILENAME,
    BACKUPCONFIG_HOLDINGFOLDERPATH,
    BACKUPCONFIG_BACKUPLOCATION,
    BACKUPCONFIG_BACKUPID,
    BACKUPCONFIG_BACKUPSTATEFOLDERPATH,
    BACKUPCONFIG_BACKUPSTATEFOLDERNAME,
    BACKUPCONFIG_USECHECKSUM,
    BACKUPCONFIG_CHECKSUMTYPE,
    BACKUPCONFIG_USEMODIFIEDDATE,
    BACKUPCONFIG_COMPRESSIONTYPE,
    BACKUPCONFIG_CHUNK,
    BACKUPCONFIG_EXCLUDEREGEXLIST,
    BACKUPCONFIG_EXCLUDEPATHLIST,
    BACKUPCONFIG_BACKUPREPORTTYPE,
    BACKUPCONFIG_BACKUPREPORTPATH,
    BACKUPCONFIG_RESTORE,
    BACKUPCONFIG_BACKUP,
    BACKUPCONFIG_PRESERVEPERMISSIONS,
    BACKUPCONFIG_PRESERVEOWNERSHIP,
    BACKUPCONFIG_NOCLOBBER,
    BACKUPCONFIG_BACKUPDESCRIBE,
    BACKUPCONFIG_BACKUPSTATUS,
    BACKUPCONFIG_RESTOREDESTINATION,
    BACKUPCONFIG_BACKUPBACKENDHOST,
    BACKUPCONFIG_BACKENDSTORAGE,
    BACKUPCONFIG_ENCRYPTIONTYPE,
    BACKUPCONFIG_ENCRYPTIONKEY,
    
    BACKUPCONFIGCHUNK_SIZE,
    BACKUPCONFIGCHUNK_ENABLE,
    BACKUPCONFIGCHUNK_SIZETYPE
}
