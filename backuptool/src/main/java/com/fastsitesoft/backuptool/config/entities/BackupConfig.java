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
package com.fastsitesoft.backuptool.config.entities;

import com.fastsitesoft.backuptool.BackupId;
import com.fastsitesoft.backuptool.config.validators.BackupConfigValidator;
import com.fastsitesoft.backuptool.enums.BackupToolNameComponentType;
import com.fastsitesoft.backuptool.enums.BackupToolResultStatus;
import com.fastsitesoft.backuptool.enums.FSSBackupHashType;
import com.fastsitesoft.backuptool.enums.FSSBackupType;
import com.fastsitesoft.backuptool.enums.FSSReportType;
import com.fastsitesoft.backuptool.enums.FSSVerbosity;
import com.fastsitesoft.backuptool.utils.BackupToolException;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.fastsitesoft.backuptool.utils.BackupToolResult;
import com.fastsitesoft.backuptool.utils.FSSValidateConfigResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class represents the configuration for a backup set.
 *
 * @author Kervin Pierre <info@sludev.com>
 */
public final class BackupConfig
{
    private static final Logger log
            = LogManager.getLogger(BackupConfig.class);

    private final String setName;
    private final FSSBackupType setType;
    private final List<BackupConfigDirectory> dirList;
    private final List<BackupConfigFile> fileList;
    private final Path stateFileName;
    private final Path errorFileName;
    private final Path holdingDirectory;
    private final BackupId backupId;
    private final Boolean useChecksum;
    private final FSSBackupHashType checksumType;
    private final Boolean useModifiedDate;
    private final BackupConfigCompression compression;
    private final BackupConfigChunk chunk;
    private final FSSReportType backupReportType;
    private final Path backupReportPath;
    private final Boolean restore;
    private final Boolean backup;
    private final Boolean preservePermissions;
    private final Boolean preserveOwnership;
    private final Boolean noClobber;
    private final Boolean backupDescribe;
    private final Boolean backupStatus;
    private final Path restoreDestination;
    private final Pattern archiveFileNamePattern;
    private final Path archiveFileNameTemplate;
    private final List<BackupToolNameComponentType> jobFileNameComponent;
    private final Pattern jobFileNamePattern;
    private final Path jobFileNameTemplate;
    private final List<BackupToolNameComponentType> archiveFileNameComponent;
    private final BackupConfigStorageBackend storageBackend;
    private final BackupConfigEncryption encryption;

    /**
     * Application logging verbosity.
     */
    private final FSSVerbosity verbosity;


    /**
     * File for redirecting StdOut and StdErr
     */
    private final Path outputFile;

    /**
     * Do not change anything on the server, just simulate.
     */
    private final Boolean dryRun;

    /**
     * Ignore existing lock file
     */
    private final Boolean ignoreLock;

    /**
     * If set to true, the component may send out status emails
     */
    private final Boolean emailOnCompletion;

    /**
     * List of contacts to be emailed if needed.
     */
    private final List<String> emailContacts;

    /**
     * The process priority of the agent
     */
    private final Integer priority;

    /**
     * Lock file for allowing single program access.
     */
    private final Path lockFilePath;

    /**
     * If true run the process as a background service instead of an interactive
     * process.
     */
    private final Boolean runAsService;

    /**
     * Display the version of the application then exit.
     */
    private final Boolean displayVersion;

    /**
     * Display the usage options for the application the exit.
     */
    private final Boolean displayUsage;

    /**
     * Holds the 'Display Usage' options
     */
    private final UsageConfig usageConfig;

    public UsageConfig getUsageConfig()
    {
        return usageConfig;
    }

    public Path getLockFilePath()
    {
        return lockFilePath;
    }

    public Integer getPriority()
    {
        return priority;
    }

    public Boolean isEmailOnCompletion()
    {
        return emailOnCompletion;
    }

    public Boolean isPreserveOwnership()
    {
        return preserveOwnership;
    }

    public Boolean isPreservePermissions()
    {
        return preservePermissions;
    }

    public Boolean isBackupDescribe()
    {
        return backupDescribe;
    }

    public Boolean isBackupStatus()
    {
        return backupStatus;
    }

    /**
     * If true, then do not change anything on the server.  Simply simulate what
     * would happen if we ran the command.
     */
    public Boolean isDryRun()
    {
        return dryRun;
    }

    public Boolean isIgnoreLock()
    {
        return ignoreLock==null?false:ignoreLock;
    }

    public FSSVerbosity getVerbosity()
    {
        return verbosity;
    }

    public Boolean isRunAsService()
    {
        return runAsService;
    }

    public Boolean isDisplayVersion()
    {
        return displayVersion==null?false:displayVersion;
    }

    public Boolean isDisplayUsage()
    {
        return displayUsage==null?false:displayUsage;
    }

    public Boolean isNoClobber()
    {
        return noClobber;
    }

    public Pattern getArchiveFileNamePattern()
    {
        return archiveFileNamePattern;
    }

    public List<BackupToolNameComponentType> getArchiveFileNameComponent()
    {
        return archiveFileNameComponent;
    }

    public Path getArchiveFileNameTemplate()
    {
        return archiveFileNameTemplate;
    }

    public Pattern getJobFileNamePattern()
    {
        return jobFileNamePattern;
    }

    public Path getJobFileNameTemplate()
    {
        return jobFileNameTemplate;
    }

    public List<BackupToolNameComponentType> getJobFileNameComponent()
    {
        return jobFileNameComponent;
    }

    /**
     * * An option for setting a file for standard out and standard error.
     * <p>
     * This is the member variable behind the "--output-redirect" command line
     * argument.  But this argument
     * only takes effect after command line arguments are parsed.  This means we
     * would have missed the first, sometimes important, logs going to the console.
     * <p>
     * Use "-DFSSOUTREDIRECT" to catch log messages prior to command line argument
     * parsing.
     * <p>
     * The file we will append the standard out and standard error to.
     * If it does not exist it will be created.
     */
    public Path getOutputFile()
    {
        return outputFile;
    }

    /**
     * The 'Holding Folder' is the temp folder used while backing up files.
     *
     * @return Path to the currently configured Holding Folder
     */
    public Path getHoldingDirectory()
    {
        return holdingDirectory;
    }

    /**
     * An ID that uniquely identifies a backup in a single set.
     * <p>
     * This is a unique ID relating to all backups at a specified location.
     * Please note this ID is not guaranteed to be unique across all backups in entirety.
     *
     * @return
     */
    public BackupId getBackupId()
    {
        return backupId;
    }

    /**
     * Use file checksums during differential backup related operations. Default for
     * this flag is normally 'on'.  But if getUseModifiedDate() flag is on, then
     * the default for this flag is 'off'.
     *
     * @return
     */
    public Boolean getUseChecksum()
    {
        return useChecksum;
    }

    /**
     * The type of checksum to be used.
     *
     * @return
     */
    public FSSBackupHashType getChecksumType()
    {
        return checksumType;
    }

    /**
     * Use file-system modified-date during differential backup related operations.
     * Default for this flag is normally 'on'.  But if getUseChecksum() flag is on,
     * then the default for this flag is 'off'.
     *
     * @return
     */
    public Boolean getUseModifiedDate()
    {
        return useModifiedDate;
    }

    /**
     * Type of report to generate. Default 'PLAIN'. Also 'XML' an option
     *
     * @return
     */
    public FSSReportType getBackupReportType()
    {
        return backupReportType;
    }


    /**
     * The location of the backup report.
     *
     * @return
     */
    public Path getBackupReportPath()
    {
        return backupReportPath;
    }

    /**
     * Restores a backup to the file-system.
     *
     * @return
     */
    public Boolean getRestore()
    {
        return restore==null?false:restore;
    }

    /**
     * Run the backup described in the config file.
     *
     * @return
     */
    public Boolean getBackup()
    {
        return backup==null?false:backup;
    }

    public List<String> getEmailContacts()
    {
        return emailContacts;
    }

    /**
     * Path for restoring backed up data.
     *
     * @return
     */
    public Path getRestoreDestination()
    {
        return restoreDestination;
    }


    /**
     * The name of the current backup set.
     *
     * @return the setName
     */
    public String getSetName()
    {
        return setName;
    }

    /**
     * @return the setType
     */
    public FSSBackupType getSetType()
    {
        return setType;
    }

    /**
     * The list of backups to be backed up in the current set
     *
     * @return the dirList
     */
    public List<BackupConfigDirectory> getDirList()
    {
        return dirList;
    }

    /**
     * The name of the file that will hold status information relating to a backup set.
     *
     * @return the stateFileName
     */
    public Path getStateFileName()
    {
        return stateFileName;
    }

    public Path getErrorFileName()
    {
        return errorFileName;
    }

    /**
     * The type of compression used in the current backup set
     *
     * @return the compression
     */
    public BackupConfigCompression getCompression()
    {
        return compression;
    }

    /**
     * The chunk configuration used in the current backup set, if any.
     *
     * @return the chunk
     */
    public BackupConfigChunk getChunk()
    {
        return chunk;
    }

    /**
     * List of individual files that will be backed up in the current set
     *
     * @return
     */
    public List<BackupConfigFile> getFileList()
    {
        return fileList;
    }

    /**
     * Stores various information about the current backend being used.
     * <p>
     * This includes the 'Backup Location URL', which is the path to the backup
     * folder for the backup being created containing all the new files
     * ( compressed, encrypted, etc. ) created during the backup.
     *
     * @return
     */
    public BackupConfigStorageBackend getStorageBackend()
    {
        return storageBackend;
    }

    /**
     * The type of encryption used on disk ( at rest ) for the current backup set.
     *
     * @return
     */
    public BackupConfigEncryption getEncryption()
    {
        return encryption;
    }

    private BackupConfig( UsageConfig conf )
    {
        usageConfig = conf;
        displayUsage = true;

        emailContacts = null;
        setName = null;
        setType = null;
        dirList = null;
        fileList = null;
        stateFileName = null;
        errorFileName = null;
        holdingDirectory = null;
        backupId = null;
        useChecksum = null;
        checksumType = null;
        useModifiedDate = null;
        compression = null;
        chunk = null;
        backupReportType = null;
        backupReportPath = null;
        restore = null;
        backup = null;
        preservePermissions = null;
        preserveOwnership = null;
        noClobber = null;
        backupDescribe = null;
        backupStatus = null;
        restoreDestination = null;
        storageBackend = null;
        encryption = null;
        verbosity = null;
        outputFile = null;
        dryRun = null;
        ignoreLock = null;
        emailOnCompletion = null;
        priority = null;
        lockFilePath = null;
        runAsService = null;
        displayVersion = null;
        archiveFileNameComponent = null;
        archiveFileNamePattern = null;
        archiveFileNameTemplate = null;
        jobFileNameComponent = null;
        jobFileNamePattern = null;
        jobFileNameTemplate = null;
    }

    private BackupConfig(
            final String setName,
            final Integer priority,
            final Boolean useChecksum,
            final Boolean useModifiedDate,
            final Boolean restore,
            final Boolean backup,
            final Boolean emailOnCompletion,
            final Boolean dryRun,
            final Boolean ignoreLock,
            final Boolean runAsService,
            final Boolean displayUsage,
            final Boolean displayVersion,
            final Boolean preserveOwnership,
            final Boolean preservePermissions,
            final Boolean noClobber,
            final Boolean backupDescribe,
            final Boolean backupStatus,
            final Path holdingDirectory,
            final Path backupReportPath,
            final Path restoreDestination,
            final Path outputFile,
            final Path lockFilePath,
            final Path stateFileName,
            final Path errorFileName,
            final List<String> emailContacts,
            final List<BackupConfigDirectory> dirList,
            final List<BackupConfigFile> fileList,
            final FSSReportType backupReportType,
            final BackupId backupId,
            final FSSBackupType setType,
            final FSSBackupHashType checksumType,
            final BackupConfigCompression compression,
            final BackupConfigEncryption encryption,
            final BackupConfigChunk chunk,
            final Pattern archiveFileNamePattern,
            final List<BackupToolNameComponentType> archiveFileNameComponent,
            final Path archiveFileNameTemplate,
            final Pattern jobFileNamePattern,
            final List<BackupToolNameComponentType> jobFileNameComponent,
            final Path jobFileNameTemplate,
            final FSSVerbosity verbosity,
            final BackupConfigStorageBackend storageBackend,
            final UsageConfig usageConfig) throws BackupToolException
    {
        if( setName != null )
        {
            this.setName = setName;
        }
        else
        {
            throw new BackupToolException("setName cannot be null");
        }

        if( holdingDirectory != null )
        {
            this.holdingDirectory = holdingDirectory;
        }
        else
        {
            throw new BackupToolException("holding directory cannot be null");
        }

        if( backupReportPath != null )
        {
            this.backupReportPath = backupReportPath;
        }
        else
        {
            this.backupReportPath = null;
        }

        if( restoreDestination != null )
        {
            this.restoreDestination = restoreDestination;
        }
        else
        {
            this.restoreDestination = null;
        }

        if( outputFile != null )
        {
            this.outputFile = outputFile;
        }
        else
        {
            this.outputFile = null;
        }

        if( lockFilePath != null )
        {
            this.lockFilePath = lockFilePath;
        }
        else
        {
            this.lockFilePath = null;
        }

        if( stateFileName != null )
        {
            this.stateFileName = stateFileName;
        }
        else
        {
            this.stateFileName = null;
        }

        if( errorFileName != null )
        {
            this.errorFileName = errorFileName;
        }
        else
        {
            this.errorFileName = null;
        }

        if( priority != null )
        {
            this.priority = priority;
        }
        else
        {
            this.priority = 0;
        }

        if( backupId != null )
        {
            this.backupId = backupId;
        }
        else
        {
            this.backupId = null;
        }

        if( useChecksum != null )
        {
            this.useChecksum = useChecksum;
        }
        else
        {
            this.useChecksum = null;
        }

        if( useModifiedDate != null )
        {
            this.useModifiedDate = useModifiedDate;
        }
        else
        {
            this.useModifiedDate = null;
        }

        if( backup != null )
        {
            this.backup = backup;
        }
        else
        {
            this.backup = null;
        }

        if( restore != null )
        {
            this.restore = restore;
        }
        else
        {
            this.restore = null;
        }

        if( emailOnCompletion != null )
        {
            this.emailOnCompletion = emailOnCompletion;
        }
        else
        {
            this.emailOnCompletion = null;
        }

        if( dryRun != null )
        {
            this.dryRun = dryRun;
        }
        else
        {
            this.dryRun = null;
        }

        if( ignoreLock != null )
        {
            this.ignoreLock = ignoreLock;
        }
        else
        {
            this.ignoreLock = null;
        }

        if( runAsService != null )
        {
            this.runAsService = runAsService;
        }
        else
        {
            this.runAsService = null;
        }

        if( displayUsage != null )
        {
            this.displayUsage = displayUsage;
        }
        else
        {
            this.displayUsage = null;
        }

        if( displayVersion != null )
        {
            this.displayVersion = displayVersion;
        }
        else
        {
            this.displayVersion = null;
        }

        if( preserveOwnership != null )
        {
            this.preserveOwnership = preserveOwnership;
        }
        else
        {
            this.preserveOwnership = null;
        }

        if( preservePermissions != null )
        {
            this.preservePermissions = preservePermissions;
        }
        else
        {
            this.preservePermissions = null;
        }

        if( noClobber != null )
        {
            this.noClobber = noClobber;
        }
        else
        {
            this.noClobber = null;
        }

        if( backupDescribe != null )
        {
            this.backupDescribe = backupDescribe;
        }
        else
        {
            this.backupDescribe = null;
        }

        if( backupStatus != null )
        {
            this.backupStatus = backupStatus;
        }
        else
        {
            this.backupStatus = null;
        }

        if( compression != null )
        {
            this.compression = compression;
        }
        else
        {
            this.compression = null;
        }

        if( backupReportType != null )
        {
            this.backupReportType = backupReportType;
        }
        else
        {
            this.backupReportType = null;
        }

        if( checksumType != null )
        {
            this.checksumType = checksumType;
        }
        else
        {
            this.checksumType = null;
        }

        if( verbosity != null )
        {
            this.verbosity = verbosity;
        }
        else
        {
            this.verbosity = null;
        }

        if( emailContacts != null )
        {
            this.emailContacts = emailContacts;
        }
        else
        {
            this.emailContacts = null;
        }

        if( archiveFileNamePattern != null )
        {
            this.archiveFileNamePattern = archiveFileNamePattern;
        }
        else
        {
            this.archiveFileNamePattern = null;
        }

        if( archiveFileNameComponent != null )
        {
            this.archiveFileNameComponent = archiveFileNameComponent;
        }
        else
        {
            this.archiveFileNameComponent = new ArrayList<>();
        }

        if( archiveFileNameTemplate != null )
        {
            this.archiveFileNameTemplate = archiveFileNameTemplate;
        }
        else
        {
            this.archiveFileNameTemplate = null;
        }

        if( jobFileNamePattern != null )
        {
            this.jobFileNamePattern = jobFileNamePattern;
        }
        else
        {
            this.jobFileNamePattern = null;
        }

        if( jobFileNameComponent != null )
        {
            this.jobFileNameComponent = jobFileNameComponent;
        }
        else
        {
            this.jobFileNameComponent = new ArrayList<>();
        }

        if( jobFileNameTemplate != null )
        {
            this.jobFileNameTemplate = jobFileNameTemplate;
        }
        else
        {
            this.jobFileNameTemplate = null;
        }

        if( usageConfig != null )
        {
            this.usageConfig = usageConfig;
        }
        else
        {
            this.usageConfig = null;
        }

        if( dirList != null )
        {
            this.dirList = dirList;
        }
        else
        {
            this.dirList = null;
        }

        if( fileList != null )
        {
            this.fileList = fileList;
        }
        else
        {
            this.fileList = null;
        }

        if( setType != null )
        {
            this.setType = setType;
        }
        else
        {
            this.setType = null;
        }

        if( encryption != null )
        {
            this.encryption = encryption;
        }
        else
        {
            this.encryption = null;
        }

        if( chunk != null )
        {
            this.chunk = chunk;
        }
        else
        {
            this.chunk = null;
        }

        if( storageBackend != null )
        {
            this.storageBackend = storageBackend;
        }
        else
        {
            this.storageBackend = null;
        }
    }

    /**
     * Return the String representation of this configuration objection
     *
     * @return
     */
    @Override
    public String toString()
    {
        return toString(0);
    }

    public String toString(int indent)
    {
        String res;
        String indentStr = "";

        if( indent > 0 )
        {
            indentStr = StringUtils.repeat(" ", indent);
        }

        StringBuilder sb = new StringBuilder();

        sb.append(String.format("%sBACKUPCONFIG\n", indentStr));
        sb.append(String.format("%s  SET NAME           : '%s'\n", indentStr, setName));
        sb.append(String.format("%s  SET TYPE           : '%s'\n", indentStr, setType));
        sb.append(String.format("%s  STATE FILE NAME    : '%s'\n", indentStr, stateFileName));
        sb.append(String.format("%s  HOLDING FOLDER     : '%s'\n", indentStr, holdingDirectory));
        sb.append(String.format("%s  ID                 : '%s'\n", indentStr, backupId));
        sb.append(String.format("%s  USE CHECKSUM       : '%b'\n", indentStr, useChecksum));
        sb.append(String.format("%s  CHECKSUM TYPE      : '%s'\n", indentStr, checksumType));
        sb.append(String.format("%s  USE MODIFIED DATE  : '%s'\n", indentStr, useModifiedDate));
        sb.append(String.format("%s  COMPRESSION        : '%s'\n", indentStr, compression));
        sb.append(String.format("%s  CHUNK              :\n%s", indentStr, chunk!=null?chunk.toString(indent + 2):null));
        sb.append(String.format("%s  REPORT TYPE        : '%s'\n", indentStr, backupReportType));
        sb.append(String.format("%s  REPORT PATH        : '%s'\n", indentStr, backupReportPath));
        sb.append(String.format("%s  RESTORE            : '%b'\n", indentStr, restore));
        sb.append(String.format("%s  BACKUP             : '%b'\n", indentStr, backup));
        sb.append(String.format("%s  PRESERVE PERMS     : '%b'\n", indentStr, preservePermissions));
        sb.append(String.format("%s  PRESERVE OWNERSHIP : '%b'\n", indentStr, preserveOwnership));
        sb.append(String.format("%s  NO CLOBBER         : '%b'\n", indentStr, noClobber));
        sb.append(String.format("%s  DESCRIBE           : '%b'\n", indentStr, backupDescribe));
        sb.append(String.format("%s  STATUS             : '%b'\n", indentStr, backupStatus));
        sb.append(String.format("%s  RESTORE DESTINATION: '%s'\n", indentStr, restoreDestination));
        sb.append(String.format("%s  BACKEND STORAGE    :\n%s", indentStr, storageBackend!=null?storageBackend.toString(indent + 2):null));
        sb.append(String.format("%s  ENCRYPTION TYPE    : '%s'\n", indentStr, encryption));

        sb.append(String.format("%s  FOLDER LIST        :\n", indentStr));
        if( dirList != null )
        {
            for( BackupConfigDirectory fl : dirList )
            {
                sb.append(fl.toString(indent + 2));
            }
        }

        sb.append(String.format("%s  FILE LIST          :\n", indentStr));
        if( fileList != null )
        {
            for( BackupConfigFile fl : fileList )
            {
                sb.append(fl.toString(indent + 2));
            }
        }

//        sb.append(String.format("%s  EXCLUDE REGEX LIST :\n", indentStr));
//        for( Pattern pt : excludeRegexList )
//        {
//            sb.append(String.format("%s  %s\n", indentStr, pt.toString()));
//        }
//
//        sb.append(String.format("%s  EXCLUDE PATH LIST  :\n", indentStr));
//        for( Path pt : excludePathList )
//        {
//            sb.append(String.format("%s  %s\n", indentStr, pt.toString()));
//        }

        res = sb.toString();

        return res;
    }

    public static BackupConfig from( final UsageConfig conf )
    {
        BackupConfig res = new BackupConfig( conf );

        return res;
    }

    public static BackupConfig from(
            final String setName,
            final Integer priority,
            final Boolean useChecksum,
            final Boolean useModifiedDate,
            final Boolean restore,
            final Boolean backup,
            final Boolean emailOnCompletion,
            final Boolean dryRun,
            final Boolean ignoreLock,
            final Boolean runAsService,
            final Boolean displayUsage,
            final Boolean displayVersion,
            final Boolean preserveOwnership,
            final Boolean preservePermissions,
            final Boolean noClobber,
            final Boolean backupDescribe,
            final Boolean backupStatus,
            final Path holdingDirectory,
            final Path backupReportPath,
            final Path restoreDestination,
            final Path outputFile,
            final Path lockFilePath,
            final Path stateFileName,
            final Path errorFileName,
            final List<String> emailContacts,
            final List<BackupConfigDirectory> dirList,
            final List<BackupConfigFile> fileList,
            final FSSReportType backupReportType,
            final BackupId backupId,
            final FSSBackupType setType,
            final FSSBackupHashType checksumType,
            final BackupConfigCompression compression,
            final BackupConfigEncryption encryption,
            final BackupConfigChunk chunk,
            final Pattern archiveFileNamePattern,
            final List<BackupToolNameComponentType> archiveFileNameComponent,
            final Path archiveFileNameTemplate,
            final Pattern jobFileNamePattern,
            final List<BackupToolNameComponentType> jobFileNameComponent,
            final Path jobFileNameTemplate,
            final FSSVerbosity verbosity,
            final BackupConfigStorageBackend storageBackend,
            final UsageConfig usageConfig) throws BackupToolException
    {
        BackupConfig res = new BackupConfig(
                setName,
                priority,
                useChecksum,
                useModifiedDate,
                restore,
                backup,
                emailOnCompletion,
                dryRun,
                ignoreLock,
                runAsService,
                displayUsage,
                displayVersion,
                preserveOwnership,
                preservePermissions,
                noClobber,
                backupDescribe,
                backupStatus,
                holdingDirectory,
                backupReportPath,
                restoreDestination,
                outputFile,
                lockFilePath,
                stateFileName,
                errorFileName,
                emailContacts,
                dirList,
                fileList,
                backupReportType,
                backupId,
                setType,
                checksumType,
                compression,
                encryption,
                chunk,
                archiveFileNamePattern,
                archiveFileNameComponent,
                archiveFileNameTemplate,
                jobFileNamePattern,
                jobFileNameComponent,
                jobFileNameTemplate,
                verbosity,
                storageBackend,
                usageConfig
        );

        return res;
    }

    public static BackupConfig from(
            final BackupConfig orig,
            final String setName,
            final String priority,
            final Boolean useChecksum,
            final Boolean useModifiedDate,
            final Boolean restore,
            final Boolean backup,
            final Boolean emailOnCompletion,
            final Boolean dryRun,
            final Boolean ignoreLock,
            final Boolean runAsService,
            final Boolean displayUsage,
            final Boolean displayVersion,
            final Boolean preserveOwnership,
            final Boolean preservePermissions,
            final Boolean noClobber,
            final Boolean backupDescribe,
            final Boolean backupStatus,
            final String holdingDir,
            final String backupReportPath,
            final String restoreDestination,
            final String outputFile,
            final String lockFilePath,
            final String stateFilename,
            final String errorFilename,
            final List<String> emailContacts,
            final List<BackupConfigDirectory> dirList,
            final List<BackupConfigFile> fileList,
            final String backupReportType,
            final String backupId,
            final String setType,
            final String checksumType,
            final String compression,
            final String encryptionCipher,
            final String encryptionKey,
            final String chunkSize,
            final String chunkSizeType,
            final String archiveNamePattern,
            final String archiveNameTemplate,
            final String archiveNameComp,
            final String jobNamePattern,
            final String jobNameTemplate,
            final String jobNameComp,
            final String verbosity,
            final BackupConfigStorageBackend storageBackend,
            final UsageConfig usageConfig) throws BackupToolException
    {
        log.debug("Creating Backup Configuration.\n");

        BackupConfig conf;

        try
        {
            String currSetName = null;
            if( StringUtils.isBlank(setName) )
            {
                if( orig != null && StringUtils.isNoneBlank(orig.getSetName()) )
                {
                    currSetName = orig.getSetName();
                }
                else
                {
                    // Default or error
                    log.debug("Missing BackupConfig SetName.");
                }
            }
            else
            {
                currSetName = BackupConfigValidator.normalizeSetName(setName);
            }

            List<BackupConfigDirectory> currBackupDirectory = null;
            if( dirList == null || dirList.isEmpty() )
            {
                if( orig != null && orig.getDirList() != null && !orig.getDirList().isEmpty() )
                {
                    currBackupDirectory = orig.getDirList();
                }
                else
                {
                    // Default or error
                    log.debug("Missing BackupConfig Directory List.");
                }
            }
            else
            {
                currBackupDirectory = BackupConfigValidator.normalizeBackupFolderList(dirList);
            }

            List<BackupConfigFile> currBackupFiles = null;
            if( fileList == null || fileList.isEmpty() )
            {
                if( orig != null && orig.getFileList() != null && !orig.getFileList().isEmpty() )
                {
                    currBackupFiles = orig.getFileList();
                }
                else
                {
                    // Default or error
                    log.debug("Missing BackupConfig File List.");
                }
            }
            else
            {
                currBackupFiles = BackupConfigValidator.normalizeBackupFileList(fileList);
            }

            Integer currPriority = null;
            if( StringUtils.isBlank(priority) )
            {
                if( orig != null && orig.getPriority() != null )
                {
                    currPriority = orig.getPriority();
                }
                else
                {
                    // Default or error
                    log.debug("Missing BackupConfig Priority.");
                }
            }
            else
            {
                currPriority = BackupConfigValidator.normalizePriority(priority);
            }

            Path currHoldingDir = null;
            if( StringUtils.isBlank(holdingDir) )
            {
                if( orig != null && orig.getHoldingDirectory() != null )
                {
                    currHoldingDir = orig.getHoldingDirectory();
                }
                else
                {
                    // Default or error
                    log.debug("Missing BackupConfig Holding Directory.");
                }
            }
            else
            {
                currHoldingDir = BackupConfigValidator.normalizeHoldingFolder(holdingDir);
            }

            Path currReportPath = null;
            if( StringUtils.isBlank(backupReportPath) )
            {
                if( orig != null && orig.getBackupReportPath() != null )
                {
                    currReportPath = orig.getBackupReportPath();
                }
                else
                {
                    // Default or error
                    log.debug("Missing BackupConfig Report Path.");
                }
            }
            else
            {
                currReportPath = BackupConfigValidator.normalizeReportPath(backupReportPath);
            }

            Path currRestoreDestPath = null;
            if( restoreDestination == null )
            {
                if( orig != null && orig.getRestoreDestination() != null )
                {
                    currRestoreDestPath = orig.getRestoreDestination();
                }
                else
                {
                    // Default or error
                    log.debug("Missing BackupConfig Restore Destination Path.");
                }
            }
            else
            {
                currRestoreDestPath = BackupConfigValidator.normalizeRestoreDestinationPath(restoreDestination);
            }

            Path currOutputFilePath = null;
            if( StringUtils.isBlank(outputFile) )
            {
                if( orig != null && orig.getOutputFile() != null )
                {
                    currOutputFilePath = orig.getOutputFile();
                }
                else
                {
                    // Default or error
                    log.debug("Missing BackupConfig Output File Path.");
                }
            }
            else
            {
                currOutputFilePath = BackupConfigValidator.normalizeOutputFilePath(outputFile);
            }

            Path currLockFilePath = null;
            if( StringUtils.isBlank(lockFilePath) )
            {
                if( orig != null && orig.getLockFilePath() != null )
                {
                    currLockFilePath = orig.getLockFilePath();
                }
                else
                {
                    // Default or error
                    log.debug("Missing BackupConfig Lock File Path.");
                }
            }
            else
            {
                currLockFilePath = BackupConfigValidator.normalizeLockFilePath(lockFilePath);
            }

            Path currStateFilename = null;
            if( StringUtils.isBlank(stateFilename) )
            {
                if( orig != null && orig.getStateFileName() != null )
                {
                    currStateFilename = orig.getStateFileName();
                }
                else
                {
                    // Default or error
                    log.debug("Missing BackupConfig State Filename.");
                }
            }
            else
            {
                currStateFilename = BackupConfigValidator.normalizeStateFilename(stateFilename);
            }

            Path currErrorFilename = null;
            if( StringUtils.isBlank(errorFilename) )
            {
                if( orig != null && orig.getErrorFileName() != null )
                {
                    currErrorFilename = orig.getErrorFileName();
                }
                else
                {
                    // Default or error
                    log.debug("Missing BackupConfig Error Filename.");
                }
            }
            else
            {
                currErrorFilename = BackupConfigValidator.normalizeErrorFilename(errorFilename);
            }

            Pattern currArchiveNamePattern = null;
            if( StringUtils.isBlank(archiveNamePattern) )
            {
                if( orig != null && orig.getArchiveFileNamePattern() != null )
                {
                    currArchiveNamePattern = orig.getArchiveFileNamePattern();
                }
                else
                {
                    // Default or error
                    log.debug("Missing Name Pattern.");
                }
            }
            else
            {
                currArchiveNamePattern = BackupConfigValidator.normalizeArchiveNamePattern(archiveNamePattern);
            }

            List<BackupToolNameComponentType> currArchiveNameComp = null;
            if( StringUtils.isBlank(archiveNameComp) )
            {
                if( orig != null && orig.getArchiveFileNameComponent() != null )
                {
                    currArchiveNameComp = orig.getArchiveFileNameComponent();
                }
                else
                {
                    // Default or error
                    log.debug("Missing Name Pattern Component.");
                }
            }
            else
            {
                currArchiveNameComp = BackupConfigValidator.normalizeArchiveNameComp(archiveNameComp);
            }

            Path currArchiveNameTemplate = null;
            if( StringUtils.isBlank(archiveNameTemplate) )
            {
                if( orig != null && orig.getArchiveFileNameTemplate() != null )
                {
                    currArchiveNameTemplate = orig.getArchiveFileNameTemplate();
                }
                else
                {
                    // Default or error
                    log.debug("Missing Name Template.");
                }
            }
            else
            {
                currArchiveNameTemplate = BackupConfigValidator.normalizeArchiveNameTemplate(archiveNameTemplate);
            }

            Pattern currJobNamePattern = null;
            if( StringUtils.isBlank(jobNamePattern) )
            {
                if( orig != null && orig.getJobFileNamePattern() != null )
                {
                    currJobNamePattern = orig.getJobFileNamePattern();
                }
                else
                {
                    // Default or error
                    log.debug("Missing Job Name Pattern.");
                }
            }
            else
            {
                currJobNamePattern = BackupConfigValidator.normalizeJobNamePattern(jobNamePattern);
            }

            List<BackupToolNameComponentType> currJobNameComp = null;
            if( StringUtils.isBlank(jobNameComp) )
            {
                if( orig != null && orig.getJobFileNameComponent() != null )
                {
                    currJobNameComp = orig.getJobFileNameComponent();
                }
                else
                {
                    // Default or error
                    log.debug("Missing Job Name Pattern Component.");
                }
            }
            else
            {
                currJobNameComp = BackupConfigValidator.normalizeJobNameComp(jobNameComp);
            }

            Path currJobNameTemplate =  null;
            if( StringUtils.isBlank(jobNameTemplate) )
            {
                if( orig != null && orig.getJobFileNameTemplate() != null )
                {
                    currJobNameTemplate = orig.getJobFileNameTemplate();
                }
                else
                {
                    // Default or error
                    log.debug("Missing Job Template.");
                }
            }
            else
            {
                currJobNameTemplate = BackupConfigValidator.normalizeJobNameTemplate(jobNameTemplate);
            }

            FSSReportType currReportType = null;
            if( StringUtils.isBlank(backupReportType) )
            {
                if( orig != null && orig.getBackupReportType() != null )
                {
                    currReportType = orig.getBackupReportType();
                }
                else
                {
                    // Default or error
                    log.debug("Missing BackupConfig Report Type.");
                }
            }
            else
            {
                currReportType = BackupConfigValidator.normalizeReportType(backupReportType);
            }

            BackupId currBackupId = null;
            if( StringUtils.isBlank(backupId) )
            {
                if( orig != null && orig.getBackupId() != null )
                {
                    currBackupId = orig.getBackupId();
                }
                else
                {
                    // Default or error
                    log.debug("Missing BackupConfig Backup ID.");
                }
            }
            else
            {
                currBackupId = BackupConfigValidator.normalizeBackupId(backupId);
            }

            FSSBackupType currBackupType = null;
            if( StringUtils.isBlank(setType) )
            {
                if( orig != null && orig.getSetType() != null )
                {
                    currBackupType = orig.getSetType();
                }
                else
                {
                    // Default or error
                    log.debug("Missing BackupConfig Backup Type");
                }
            }
            else
            {
                currBackupType = BackupConfigValidator.normalizeBackupType(setType);
            }

            FSSBackupHashType currChecksumType = null;
            if( StringUtils.isBlank(checksumType) )
            {
                if( orig != null && orig.getChecksumType() != null )
                {
                    currChecksumType = orig.getChecksumType();
                }
                else
                {
                    // Default or error
                    log.debug("Missing BackupConfig Checksum Type.");
                }
            }
            else
            {
                currChecksumType = BackupConfigValidator.normalizeChecksum(checksumType);
            }

            BackupConfigCompression currCompression = null;
            if( StringUtils.isBlank(compression) )
            {
                if( orig != null && orig.getCompression() != null )
                {
                    currCompression = orig.getCompression();
                }
                else
                {
                    // Default or error
                    log.debug("Missing BackupConfig Compression.");
                }
            }
            else
            {
                currCompression = BackupConfigValidator.normalizeCompression(compression);
            }

            BackupConfigEncryption currEncryption = null;
            String encCipherString = null;
            if( StringUtils.isBlank(encryptionCipher) )
            {
                ;
            }
            else
            {
                encCipherString = encryptionCipher;
            }

            String encKeyString = null;
            if( StringUtils.isBlank(encryptionKey) )
            {
                ;
            }
            else
            {
                encKeyString = encryptionKey;
            }

            if( StringUtils.isNoneBlank(encCipherString) && StringUtils.isNoneBlank(encKeyString) )
            {
                currEncryption = BackupConfigValidator.normalizeEncryption(encCipherString, encKeyString);
            }
            else
            {
                if( orig != null && orig.getEncryption() != null )
                {
                    currEncryption = orig.getEncryption();
                }
                else
                {
                    // Default or error
                    log.debug("Missing BackupConfig Encryption.");
                }
            }

            BackupConfigChunk currChunk = null;

            String currChunkSizeType = chunkSizeType;
            if( StringUtils.isBlank(chunkSizeType) )
            {
                currChunkSizeType = "KB";
            }

            if( StringUtils.isBlank(chunkSize) )
            {
                if( orig != null && orig.getChunk() != null )
                {
                    currChunk = orig.getChunk();
                }
                else
                {
                    // Default or error
                    log.debug("Missing BackupConfig Chunk.");
                }
            }
            else
            {
                currChunk = BackupConfigValidator.normalizeChunk(true, chunkSize, currChunkSizeType);
            }

            FSSVerbosity currVerb = null;

            if( StringUtils.isBlank(verbosity) )
            {
                if( orig != null && orig.getVerbosity() != null )
                {
                    currVerb = orig.getVerbosity();
                }
                else
                {
                    // Default or error
                    log.debug("Missing BackupConfig Verbosity.");
                }
            }
            else
            {
                currVerb = BackupConfigValidator.normalizeVerbosity(verbosity);
            }

            BackupConfigStorageBackend currStoreBackend = null;
            if( storageBackend == null )
            {
                if( orig != null && orig.getStorageBackend() != null )
                {
                    currStoreBackend = orig.getStorageBackend();
                }
                else
                {
                    // Default or error
                    log.debug("Missing BackupConfig Storage Backend.");
                }
            }
            else
            {
                currStoreBackend = storageBackend;
            }

            conf = from(
                    currSetName,
                    currPriority,
                    useChecksum,
                    useModifiedDate,
                    restore,
                    backup,
                    emailOnCompletion,
                    dryRun,
                    ignoreLock,
                    runAsService,
                    displayUsage,
                    displayVersion,
                    preserveOwnership,
                    preservePermissions,
                    noClobber,
                    backupDescribe,
                    backupStatus,
                    currHoldingDir,
                    currReportPath,
                    currRestoreDestPath,
                    currOutputFilePath,
                    currLockFilePath,
                    currStateFilename,
                    currErrorFilename,
                    emailContacts,
                    currBackupDirectory,
                    currBackupFiles,
                    currReportType,
                    currBackupId,
                    currBackupType,
                    currChecksumType,
                    currCompression,
                    currEncryption,
                    currChunk,
                    currArchiveNamePattern,
                    currArchiveNameComp,
                    currArchiveNameTemplate,
                    currJobNamePattern,
                    currJobNameComp,
                    currJobNameTemplate,
                    currVerb,
                    currStoreBackend,
                    usageConfig
            );
        }
        catch( BackupToolException ex )
        {
            String errMsg = "Error creating new configuration object.";

            log.debug(errMsg, ex);
            throw new BackupToolException(errMsg, ex);
        }

        return conf;
    }

    /**
     * Check that a configuration file can be run.
     *
     * @param bc
     * @return
     * @throws com.fastsitesoft.backuptool.utils.BackupToolException
     */
    public static BackupToolResult validate(BackupConfig bc) throws BackupToolException
    {
        BackupToolResult pr = null;
        FSSValidateConfigResult valRes = BackupConfigValidator.validate(bc);
        switch(FSSValidateConfigResult.getStatus(valRes))
        {
            case VALID:
            case WARNING:
            case INFO:
                break;

            case ERROR:
            {
                String errMsg = String.format(
                        "Error validating the backup configuration : %s\n", valRes);

                log.debug(errMsg);

               // throw new BackupToolException(errMsg);
            }

            default:
                break;
        }

        if( pr == null )
        {
            pr = new BackupToolResult(BackupToolResultStatus.SUCCESS);
        }

        return pr;
    }
}
