/*
 *  CityMSP LLC CONFIDENTIAL
 *  DO NOT COPY
 *
 * Copyright (c) [2012] - [2019] CityMSP LLC <info@citymsp.nyc>
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 *  the property of CityMSP LLC and its suppliers,
 *  if any.  The intellectual and technical concepts contained
 *  herein are proprietary to CityMSP LLC and its suppliers and
 *  may be covered by U.S. and Foreign Patents, patents in process,
 *  and are protected by trade secret or copyright law.
 *  Dissemination of this information or reproduction of this material
 *  is strictly forbidden unless prior written permission is obtained
 *  from CityMSP LLC
 */
package com.fastsitesoft.backuptool.config.entities;

import com.fastsitesoft.backuptool.BackupId;
import com.fastsitesoft.backuptool.config.validators.BackupConfigValidator;
import com.fastsitesoft.backuptool.enums.BackupToolIgnoreFlags;
import com.fastsitesoft.backuptool.enums.BackupToolNameComponentType;
import com.fastsitesoft.backuptool.enums.BackupToolResultStatus;
import com.fastsitesoft.backuptool.enums.FSSBackupHashType;
import com.fastsitesoft.backuptool.enums.FSSBackupType;
import com.fastsitesoft.backuptool.enums.FSSReportType;
import com.fastsitesoft.backuptool.enums.FSSVerbosity;
import com.fastsitesoft.backuptool.utils.BackupToolException;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.fastsitesoft.backuptool.utils.BackupToolResult;
import com.fastsitesoft.backuptool.utils.FSSValidateConfigResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class represents the configuration for a m_backup set.
 *
 * @author Kervin Pierre <info@sludev.com>
 */
public final class BackupConfig
{
    private static final Logger LOGGER
            = LogManager.getLogger(BackupConfig.class);

    private final String m_setName;
    private final FSSBackupType m_setType;
    private final List<BackupConfigDirectory> m_dirList;
    private final List<BackupConfigFile> m_fileList;
    private final Path m_stateFileName;
    private final Path m_errorFileName;
    private final Path m_holdingDirectory;
    private final BackupId m_backupId;
    private final Boolean m_useChecksum;
    private final FSSBackupHashType m_checksumType;
    private final Boolean m_useModifiedDate;
    private final BackupConfigCompression m_compression;
    private final BackupConfigChunk m_chunk;
    private final FSSReportType m_backupReportType;
    private final Path m_backupReportPath;
    private final Boolean m_restore;
    private final Boolean m_backup;
    private final Boolean m_preservePermissions;
    private final Boolean m_preserveOwnership;
    private final Boolean m_noClobber;
    private final Boolean m_backupDescribe;
    private final Boolean m_backupStatus;
    private final Path m_restoreDestination;
    private final Pattern m_archiveFileNamePattern;
    private final Path m_archiveFileNameTemplate;
    private final List<BackupToolNameComponentType> m_jobFileNameComponent;
    private final Pattern m_jobFileNamePattern;
    private final Path m_jobFileNameTemplate;
    private final Set<BackupToolIgnoreFlags> m_backupToolIgnoreFlags;
    private final List<BackupToolNameComponentType> m_archiveFileNameComponent;
    private final BackupConfigStorageBackend m_storageBackend;
    private final BackupConfigEncryption m_encryption;

    /**
     * Application logging m_verbosity.
     */
    private final FSSVerbosity m_verbosity;


    /**
     * File for redirecting StdOut and StdErr
     */
    private final Path m_outputFile;

    /**
     * Do not change anything on the server, just simulate.
     */
    private final Boolean m_dryRun;

    /**
     * Ignore existing lock file
     */
    private final Boolean m_ignoreLock;

    /**
     * If set to true, the component may send out status emails
     */
    private final Boolean m_emailOnCompletion;

    /**
     * List of contacts to be emailed if needed.
     */
    private final List<String> m_emailContacts;

    /**
     * The process m_priority of the agent
     */
    private final Integer m_priority;

    /**
     * Lock file for allowing single program access.
     */
    private final Path m_lockFilePath;

    /**
     * If true run the process as a background service instead of an interactive
     * process.
     */
    private final Boolean m_runAsService;

    /**
     * Display the version of the application then exit.
     */
    private final Boolean m_displayVersion;

    /**
     * Display the usage options for the application the exit.
     */
    private final Boolean m_displayUsage;

    /**
     * Holds the 'Display Usage' options
     */
    private final UsageConfig m_usageConfig;

    public UsageConfig getUsageConfig()
    {
        return m_usageConfig;
    }

    public Path getLockFilePath()
    {
        return m_lockFilePath;
    }

    public Integer getPriority()
    {
        return m_priority;
    }

    public Boolean isEmailOnCompletion()
    {
        return m_emailOnCompletion;
    }

    public Boolean isPreserveOwnership()
    {
        return m_preserveOwnership;
    }

    public Boolean isPreservePermissions()
    {
        return m_preservePermissions;
    }

    public Boolean isBackupDescribe()
    {
        return m_backupDescribe;
    }

    public Boolean isBackupStatus()
    {
        return m_backupStatus;
    }

    /**
     * If true, then do not change anything on the server.  Simply simulate what
     * would happen if we ran the command.
     */
    public Boolean isDryRun()
    {
        return m_dryRun;
    }

    public Boolean isIgnoreLock()
    {
        return m_ignoreLock ==null?false: m_ignoreLock;
    }

    public FSSVerbosity getVerbosity()
    {
        return m_verbosity;
    }

    public Boolean isRunAsService()
    {
        return m_runAsService;
    }

    public Boolean isDisplayVersion()
    {
        return m_displayVersion ==null?false: m_displayVersion;
    }

    public Boolean isDisplayUsage()
    {
        return m_displayUsage ==null?false: m_displayUsage;
    }

    public Boolean isNoClobber()
    {
        return m_noClobber;
    }

    public Pattern getArchiveFileNamePattern()
    {
        return m_archiveFileNamePattern;
    }

    public List<BackupToolNameComponentType> getArchiveFileNameComponent()
    {
        return m_archiveFileNameComponent;
    }

    public Path getArchiveFileNameTemplate()
    {
        return m_archiveFileNameTemplate;
    }

    public Pattern getJobFileNamePattern()
    {
        return m_jobFileNamePattern;
    }

    public Path getJobFileNameTemplate()
    {
        return m_jobFileNameTemplate;
    }

    public List<BackupToolNameComponentType> getJobFileNameComponent()
    {
        return m_jobFileNameComponent;
    }

    public Set<BackupToolIgnoreFlags> getIgnoreFlags()
    {
        return m_backupToolIgnoreFlags;
    }
    /**
     * * An option for setting a file for standard out and standard error.
     * <p>
     * This is the member variable behind the "--output-redirect" command line
     * argument.  But this argument
     * only takes effect after command line arguments are parsed.  This means we
     * would have missed the first, sometimes important, logs going to the console.
     * <p>
     * Use "-DFSSOUTREDIRECT" to catch LOGGER messages prior to command line argument
     * parsing.
     * <p>
     * The file we will append the standard out and standard error to.
     * If it does not exist it will be created.
     */
    public Path getOutputFile()
    {
        return m_outputFile;
    }

    /**
     * The 'Holding Folder' is the temp folder used while backing up files.
     *
     * @return Path to the currently configured Holding Folder
     */
    public Path getHoldingDirectory()
    {
        return m_holdingDirectory;
    }

    /**
     * An ID that uniquely identifies a m_backup in a single set.
     * <p>
     * This is a unique ID relating to all backups at a specified location.
     * Please note this ID is not guaranteed to be unique across all backups in entirety.
     *
     * @return
     */
    public BackupId getBackupId()
    {
        return m_backupId;
    }

    /**
     * Use file checksums during differential m_backup related operations. Default for
     * this flag is normally 'on'.  But if getUseModifiedDate() flag is on, then
     * the default for this flag is 'off'.
     *
     * @return
     */
    public Boolean getUseChecksum()
    {
        return m_useChecksum;
    }

    /**
     * The type of checksum to be used.
     *
     * @return
     */
    public FSSBackupHashType getChecksumType()
    {
        return m_checksumType;
    }

    /**
     * Use file-system modified-date during differential m_backup related operations.
     * Default for this flag is normally 'on'.  But if getUseChecksum() flag is on,
     * then the default for this flag is 'off'.
     *
     * @return
     */
    public Boolean getUseModifiedDate()
    {
        return m_useModifiedDate;
    }

    /**
     * Type of report to generate. Default 'PLAIN'. Also 'XML' an option
     *
     * @return
     */
    public FSSReportType getBackupReportType()
    {
        return m_backupReportType;
    }


    /**
     * The location of the m_backup report.
     *
     * @return
     */
    public Path getBackupReportPath()
    {
        return m_backupReportPath;
    }

    /**
     * Restores a m_backup to the file-system.
     *
     * @return
     */
    public Boolean getRestore()
    {
        return m_restore ==null?false: m_restore;
    }

    /**
     * Run the m_backup described in the config file.
     *
     * @return
     */
    public Boolean getBackup()
    {
        return m_backup ==null?false: m_backup;
    }

    public List<String> getEmailContacts()
    {
        return m_emailContacts;
    }

    /**
     * Path for restoring backed up data.
     *
     * @return
     */
    public Path getRestoreDestination()
    {
        return m_restoreDestination;
    }


    /**
     * The name of the current m_backup set.
     *
     * @return the m_setName
     */
    public String getSetName()
    {
        return m_setName;
    }

    /**
     * @return the m_setType
     */
    public FSSBackupType getSetType()
    {
        return m_setType;
    }

    /**
     * The list of backups to be backed up in the current set
     *
     * @return the m_dirList
     */
    public List<BackupConfigDirectory> getDirList()
    {
        return m_dirList;
    }

    /**
     * The name of the file that will hold status information relating to a m_backup set.
     *
     * @return the m_stateFileName
     */
    public Path getStateFileName()
    {
        return m_stateFileName;
    }

    public Path getErrorFileName()
    {
        return m_errorFileName;
    }

    /**
     * The type of m_compression used in the current m_backup set
     *
     * @return the m_compression
     */
    public BackupConfigCompression getCompression()
    {
        return m_compression;
    }

    /**
     * The m_chunk configuration used in the current m_backup set, if any.
     *
     * @return the m_chunk
     */
    public BackupConfigChunk getChunk()
    {
        return m_chunk;
    }

    /**
     * List of individual files that will be backed up in the current set
     *
     * @return
     */
    public List<BackupConfigFile> getFileList()
    {
        return m_fileList;
    }

    /**
     * Stores various information about the current backend being used.
     * <p>
     * This includes the 'Backup Location URL', which is the path to the m_backup
     * folder for the m_backup being created containing all the new files
     * ( compressed, encrypted, etc. ) created during the m_backup.
     *
     * @return
     */
    public BackupConfigStorageBackend getStorageBackend()
    {
        return m_storageBackend;
    }

    /**
     * The type of m_encryption used on disk ( at rest ) for the current m_backup set.
     *
     * @return
     */
    public BackupConfigEncryption getEncryption()
    {
        return m_encryption;
    }

    private BackupConfig( UsageConfig conf )
    {
        m_usageConfig = conf;
        m_displayUsage = true;

        m_emailContacts = null;
        m_setName = null;
        m_setType = null;
        m_dirList = null;
        m_fileList = null;
        m_stateFileName = null;
        m_errorFileName = null;
        m_holdingDirectory = null;
        m_backupId = null;
        m_useChecksum = null;
        m_checksumType = null;
        m_useModifiedDate = null;
        m_compression = null;
        m_chunk = null;
        m_backupReportType = null;
        m_backupReportPath = null;
        m_restore = null;
        m_backup = null;
        m_preservePermissions = null;
        m_preserveOwnership = null;
        m_noClobber = null;
        m_backupDescribe = null;
        m_backupStatus = null;
        m_restoreDestination = null;
        m_storageBackend = null;
        m_encryption = null;
        m_verbosity = null;
        m_outputFile = null;
        m_dryRun = null;
        m_ignoreLock = null;
        m_emailOnCompletion = null;
        m_priority = null;
        m_lockFilePath = null;
        m_runAsService = null;
        m_displayVersion = null;
        m_archiveFileNameComponent = null;
        m_archiveFileNamePattern = null;
        m_archiveFileNameTemplate = null;
        m_jobFileNameComponent = null;
        m_jobFileNamePattern = null;
        m_jobFileNameTemplate = null;
        m_backupToolIgnoreFlags = null;
    }

    public Boolean getEmailOnCompletion()
    {
        return m_emailOnCompletion;
    }

    public Boolean getDryRun()
    {
        return m_dryRun;
    }

    public Boolean getIgnoreLock()
    {
        return m_ignoreLock;
    }

    public Boolean getRunAsService()
    {
        return m_runAsService;
    }

    public Boolean getDisplayUsage()
    {
        return m_displayUsage;
    }

    public Boolean getDisplayVersion()
    {
        return m_displayVersion;
    }

    public Boolean getPreservePermissions()
    {
        return m_preservePermissions;
    }

    public Boolean getPreserveOwnership()
    {
        return m_preserveOwnership;
    }

    public Boolean getNoClobber()
    {
        return m_noClobber;
    }

    public Boolean getBackupDescribe()
    {
        return m_backupDescribe;
    }

    public Boolean getBackupStatus()
    {
        return m_backupStatus;
    }

    public Set<BackupToolIgnoreFlags> getBackupToolIgnoreFlags()
    {
        return m_backupToolIgnoreFlags;
    }

    private BackupConfig(
            final BackupConfig orig,
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
            final Set<BackupToolIgnoreFlags> ignoreFlags,
            final FSSVerbosity verbosity,
            final BackupConfigStorageBackend storageBackend,
            final UsageConfig usageConfig) throws BackupToolException
    {
        if( setName != null )
        {
            this.m_setName = setName;
        }
        else if( (orig != null)
                && (orig.getSetName() != null))
        {
            this.m_setName = orig.getSetName();
        }
        else
        {
            // NB Don't validate here
            // throw new BackupToolException("m_setName cannot be null");
            this.m_setName = "<NONE>";
        }

        if( holdingDirectory != null )
        {
            this.m_holdingDirectory = holdingDirectory;
        }
        else if( (orig != null)
                && (orig.getHoldingDirectory() != null))
        {
            this.m_holdingDirectory = orig.getHoldingDirectory();
        }
        else
        {
            //throw new BackupToolException("holding directory cannot be null");
            this.m_holdingDirectory = null;
        }

        if( backupReportPath != null )
        {
            this.m_backupReportPath = backupReportPath;
        }
        else if( (orig != null)
                && (orig.getBackupReportPath() != null))
        {
            this.m_backupReportPath = orig.getBackupReportPath();
        }
        else
        {
            this.m_backupReportPath = null;
        }

        if( restoreDestination != null )
        {
            this.m_restoreDestination = restoreDestination;
        }
        else if( (orig != null)
                && (orig.getRestoreDestination() != null))
        {
            this.m_restoreDestination = orig.getRestoreDestination();
        }
        else
        {
            this.m_restoreDestination = null;
        }

        if( outputFile != null )
        {
            this.m_outputFile = outputFile;
        }
        else if( (orig != null)
                && (orig.getOutputFile() != null))
        {
            this.m_outputFile = orig.getOutputFile();
        }
        else
        {
            this.m_outputFile = null;
        }

        if( lockFilePath != null )
        {
            this.m_lockFilePath = lockFilePath;
        }
        else if( (orig != null)
                && (orig.getLockFilePath() != null))
        {
            this.m_lockFilePath = orig.getLockFilePath();
        }
        else
        {
            this.m_lockFilePath = null;
        }

        if( stateFileName != null )
        {
            this.m_stateFileName = stateFileName;
        }
        else if( (orig != null)
                && (orig.getStateFileName() != null))
        {
            this.m_stateFileName = orig.getStateFileName();
        }
        else
        {
            this.m_stateFileName = null;
        }

        if( errorFileName != null )
        {
            this.m_errorFileName = errorFileName;
        }
        else if( (orig != null)
                && (orig.getErrorFileName() != null))
        {
            this.m_errorFileName = orig.getErrorFileName();
        }
        else
        {
            this.m_errorFileName = null;
        }

        if( priority != null )
        {
            this.m_priority = priority;
        }
        else if( (orig != null)
                && (orig.getPriority() != null))
        {
            this.m_priority = orig.getPriority();
        }
        else
        {
            this.m_priority = 0;
        }

        if( backupId != null )
        {
            this.m_backupId = backupId;
        }
        else if( (orig != null)
                && (orig.getBackupId() != null))
        {
            this.m_backupId = orig.getBackupId();
        }
        else
        {
            this.m_backupId = null;
        }

        if( useChecksum != null )
        {
            this.m_useChecksum = useChecksum;
        }
        else if( (orig != null)
                && (orig.getUseChecksum() != null))
        {
            this.m_useChecksum = orig.getUseChecksum();
        }
        else
        {
            this.m_useChecksum = null;
        }

        if( useModifiedDate != null )
        {
            this.m_useModifiedDate = useModifiedDate;
        }
        else if( (orig != null)
                && (orig.getUseModifiedDate() != null))
        {
            this.m_useModifiedDate = orig.getUseModifiedDate();
        }
        else
        {
            this.m_useModifiedDate = null;
        }

        if( backup != null )
        {
            this.m_backup = backup;
        }
        else if( (orig != null)
                && (orig.getBackup() != null))
        {
            this.m_backup = orig.getBackup();
        }
        else
        {
            this.m_backup = null;
        }

        if( restore != null )
        {
            this.m_restore = restore;
        }
        else if( (orig != null)
                && (orig.getRestore() != null))
        {
            this.m_restore = orig.getRestore();
        }
        else
        {
            this.m_restore = null;
        }

        if( emailOnCompletion != null )
        {
            this.m_emailOnCompletion = emailOnCompletion;
        }
        else if( (orig != null)
                && (orig.getEmailOnCompletion() != null))
        {
            this.m_emailOnCompletion = orig.getEmailOnCompletion();
        }
        else
        {
            this.m_emailOnCompletion = null;
        }

        if( dryRun != null )
        {
            this.m_dryRun = dryRun;
        }
        else if( (orig != null)
                && (orig.getDryRun() != null))
        {
            this.m_dryRun = orig.getDryRun();
        }
        else
        {
            this.m_dryRun = null;
        }

        if( ignoreLock != null )
        {
            this.m_ignoreLock = ignoreLock;
        }
        else if( (orig != null)
                && (orig.getIgnoreLock() != null))
        {
            this.m_ignoreLock = orig.getIgnoreLock();
        }
        else
        {
            this.m_ignoreLock = null;
        }

        if( runAsService != null )
        {
            this.m_runAsService = runAsService;
        }
        else if( (orig != null)
                && (orig.getRunAsService() != null))
        {
            this.m_runAsService = orig.getRunAsService();
        }
        else
        {
            this.m_runAsService = null;
        }

        if( displayUsage != null )
        {
            this.m_displayUsage = displayUsage;
        }
        else if( (orig != null)
                && (orig.getDisplayUsage() != null))
        {
            this.m_displayUsage = orig.getDisplayUsage();
        }
        else
        {
            this.m_displayUsage = null;
        }

        if( displayVersion != null )
        {
            this.m_displayVersion = displayVersion;
        }
        else if( (orig != null)
                && (orig.getDisplayVersion() != null))
        {
            this.m_displayVersion = orig.getDisplayVersion();
        }
        else
        {
            this.m_displayVersion = null;
        }

        if( preserveOwnership != null )
        {
            this.m_preserveOwnership = preserveOwnership;
        }
        else if( (orig != null)
                && (orig.getPreserveOwnership() != null))
        {
            this.m_preserveOwnership= orig.getPreserveOwnership();
        }
        else
        {
            this.m_preserveOwnership = null;
        }

        if( preservePermissions != null )
        {
            this.m_preservePermissions = preservePermissions;
        }
        else if( (orig != null)
                && (orig.getPreservePermissions() != null))
        {
            this.m_preservePermissions = orig.getPreservePermissions();
        }
        else
        {
            this.m_preservePermissions = null;
        }

        if( noClobber != null )
        {
            this.m_noClobber = noClobber;
        }
        else if( (orig != null)
                && (orig.getNoClobber() != null))
        {
            this.m_noClobber = orig.getNoClobber();
        }
        else
        {
            this.m_noClobber = null;
        }

        if( backupDescribe != null )
        {
            this.m_backupDescribe = backupDescribe;
        }
        else if( (orig != null)
                && (orig.getBackupDescribe() != null))
        {
            this.m_backupDescribe = orig.getBackupDescribe();
        }
        else
        {
            this.m_backupDescribe = null;
        }

        if( backupStatus != null )
        {
            this.m_backupStatus = backupStatus;
        }
        else if( (orig != null)
                && (orig.getBackupStatus() != null))
        {
            this.m_backupStatus = orig.getBackupStatus();
        }
        else
        {
            this.m_backupStatus = null;
        }

        if( compression != null )
        {
            this.m_compression = compression;
        }
        else if( (orig != null)
                && (orig.getCompression() != null))
        {
            this.m_compression = orig.getCompression();
        }
        else
        {
            this.m_compression = null;
        }

        if( backupReportType != null )
        {
            this.m_backupReportType = backupReportType;
        }
        else if( (orig != null)
                && (orig.getBackupReportType() != null))
        {
            this.m_backupReportType = orig.getBackupReportType();
        }
        else
        {
            this.m_backupReportType = null;
        }

        if( checksumType != null )
        {
            this.m_checksumType = checksumType;
        }
        else if( (orig != null)
                && (orig.getChecksumType() != null))
        {
            this.m_checksumType = orig.getChecksumType();
        }
        else
        {
            this.m_checksumType = null;
        }

        if( verbosity != null )
        {
            this.m_verbosity = verbosity;
        }
        else if( (orig != null)
                && (orig.getVerbosity() != null))
        {
            this.m_verbosity = orig.getVerbosity();
        }
        else
        {
            this.m_verbosity = null;
        }

        if( emailContacts != null )
        {
            this.m_emailContacts = emailContacts;
        }
        else if( (orig != null)
                && (orig.getEmailContacts() != null))
        {
            this.m_emailContacts = orig.getEmailContacts();
        }
        else
        {
            this.m_emailContacts = null;
        }

        if( archiveFileNamePattern != null )
        {
            this.m_archiveFileNamePattern = archiveFileNamePattern;
        }
        else if( (orig != null)
                && (orig.getArchiveFileNamePattern() != null))
        {
            this.m_archiveFileNamePattern = orig.getArchiveFileNamePattern();
        }
        else
        {
            this.m_archiveFileNamePattern = null;
        }

        if( archiveFileNameComponent != null )
        {
            this.m_archiveFileNameComponent = archiveFileNameComponent;
        }
        else if( (orig != null)
                && (orig.getArchiveFileNameComponent() != null))
        {
            this.m_archiveFileNameComponent = orig.getArchiveFileNameComponent();
        }
        else
        {
            this.m_archiveFileNameComponent = new ArrayList<>();
        }

        if( archiveFileNameTemplate != null )
        {
            this.m_archiveFileNameTemplate = archiveFileNameTemplate;
        }
        else if( (orig != null)
                && (orig.getArchiveFileNameTemplate() != null))
        {
            this.m_archiveFileNameTemplate = orig.getArchiveFileNameTemplate();
        }
        else
        {
            this.m_archiveFileNameTemplate = null;
        }

        if( archiveFileNameComponent != null )
        {
            this.m_backupToolIgnoreFlags = ignoreFlags;
        }
        else if( (orig != null)
                && (orig.getBackupToolIgnoreFlags() != null))
        {
            this.m_backupToolIgnoreFlags = orig.getBackupToolIgnoreFlags();
        }
        else
        {
            this.m_backupToolIgnoreFlags = new HashSet<>();
        }

        if( jobFileNamePattern != null )
        {
            this.m_jobFileNamePattern = jobFileNamePattern;
        }
        else if( (orig != null)
                && (orig.getJobFileNamePattern() != null))
        {
            this.m_jobFileNamePattern = orig.getJobFileNamePattern();
        }
        else
        {
            this.m_jobFileNamePattern = null;
        }

        if( jobFileNameComponent != null )
        {
            this.m_jobFileNameComponent = jobFileNameComponent;
        }
        else if( (orig != null)
                && (orig.getArchiveFileNameComponent() != null))
        {
            this.m_jobFileNameComponent = orig.getJobFileNameComponent();
        }
        else
        {
            this.m_jobFileNameComponent = new ArrayList<>();
        }

        if( jobFileNameTemplate != null )
        {
            this.m_jobFileNameTemplate = jobFileNameTemplate;
        }
        else if( (orig != null)
                && (orig.getJobFileNameTemplate() != null))
        {
            this.m_jobFileNameTemplate = orig.getJobFileNameTemplate();
        }
        else
        {
            this.m_jobFileNameTemplate = null;
        }

        if( usageConfig != null )
        {
            this.m_usageConfig = usageConfig;
        }
        else if( (orig != null)
                && (orig.getUsageConfig() != null))
        {
            this.m_usageConfig = orig.getUsageConfig();
        }
        else
        {
            this.m_usageConfig = null;
        }

        if( dirList != null )
        {
            this.m_dirList = dirList;
        }
        else if( (orig != null)
                && (orig.getDirList() != null))
        {
            this.m_dirList = orig.getDirList();
        }
        else
        {
            this.m_dirList = null;
        }

        if( fileList != null )
        {
            this.m_fileList = fileList;
        }
        else if( (orig != null)
                && (orig.getFileList() != null))
        {
            this.m_fileList = orig.getFileList();
        }
        else
        {
            this.m_fileList = null;
        }

        if( setType != null )
        {
            this.m_setType = setType;
        }
        else if( (orig != null)
                && (orig.getSetType() != null))
        {
            this.m_setType = orig.getSetType();
        }
        else
        {
            this.m_setType = null;
        }

        if( encryption != null )
        {
            this.m_encryption = encryption;
        }
        else if( (orig != null)
                && (orig.getEncryption() != null))
        {
            this.m_encryption = orig.getEncryption();
        }
        else
        {
            this.m_encryption = null;
        }

        if( chunk != null )
        {
            this.m_chunk = chunk;
        }
        else if( (orig != null)
                && (orig.getChunk() != null))
        {
            this.m_chunk = orig.getChunk();
        }
        else
        {
            this.m_chunk = null;
        }

        if( storageBackend != null )
        {
            this.m_storageBackend = storageBackend;
        }
        else if( (orig != null)
                && (orig.getStorageBackend() != null))
        {
            this.m_storageBackend = orig.getStorageBackend();
        }
        else
        {
            this.m_storageBackend = null;
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
        sb.append(String.format("%s  SET NAME           : '%s'\n", indentStr, m_setName));
        sb.append(String.format("%s  SET TYPE           : '%s'\n", indentStr, m_setType));
        sb.append(String.format("%s  STATE FILE NAME    : '%s'\n", indentStr, m_stateFileName));
        sb.append(String.format("%s  HOLDING FOLDER     : '%s'\n", indentStr, m_holdingDirectory));
        sb.append(String.format("%s  ID                 : '%s'\n", indentStr, m_backupId));
        sb.append(String.format("%s  USE CHECKSUM       : '%b'\n", indentStr, m_useChecksum));
        sb.append(String.format("%s  CHECKSUM TYPE      : '%s'\n", indentStr, m_checksumType));
        sb.append(String.format("%s  USE MODIFIED DATE  : '%s'\n", indentStr, m_useModifiedDate));
        sb.append(String.format("%s  COMPRESSION        : '%s'\n", indentStr, m_compression));
        sb.append(String.format("%s  CHUNK              :\n%s", indentStr, m_chunk !=null? m_chunk.toString(indent + 2):null));
        sb.append(String.format("%s  REPORT TYPE        : '%s'\n", indentStr, m_backupReportType));
        sb.append(String.format("%s  REPORT PATH        : '%s'\n", indentStr, m_backupReportPath));
        sb.append(String.format("%s  RESTORE            : '%b'\n", indentStr, m_restore));
        sb.append(String.format("%s  BACKUP             : '%b'\n", indentStr, m_backup));
        sb.append(String.format("%s  PRESERVE PERMS     : '%b'\n", indentStr, m_preservePermissions));
        sb.append(String.format("%s  PRESERVE OWNERSHIP : '%b'\n", indentStr, m_preserveOwnership));
        sb.append(String.format("%s  NO CLOBBER         : '%b'\n", indentStr, m_noClobber));
        sb.append(String.format("%s  DESCRIBE           : '%b'\n", indentStr, m_backupDescribe));
        sb.append(String.format("%s  STATUS             : '%b'\n", indentStr, m_backupStatus));
        sb.append(String.format("%s  RESTORE DESTINATION: '%s'\n", indentStr, m_restoreDestination));
        sb.append(String.format("%s  BACKEND STORAGE    :\n%s", indentStr, m_storageBackend !=null? m_storageBackend.toString(indent + 2):null));
        sb.append(String.format("%s  ENCRYPTION TYPE    : '%s'\n", indentStr, m_encryption));

        sb.append(String.format("%s  FOLDER LIST        :\n", indentStr));
        if( m_dirList != null )
        {
            for( BackupConfigDirectory fl : m_dirList)
            {
                sb.append(fl.toString(indent + 2));
            }
        }

        sb.append(String.format("%s  FILE LIST          :\n", indentStr));
        if( m_fileList != null )
        {
            for( BackupConfigFile fl : m_fileList)
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
            final BackupConfig orig,
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
            final Set<BackupToolIgnoreFlags> ignoreFlags,
            final FSSVerbosity verbosity,
            final BackupConfigStorageBackend storageBackend,
            final UsageConfig usageConfig) throws BackupToolException
    {
        BackupConfig res = new BackupConfig(
                orig,
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
                ignoreFlags,
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
            final Set<BackupToolIgnoreFlags> ignoreFlags,
            final String verbosity,
            final BackupConfigStorageBackend storageBackend,
            final UsageConfig usageConfig) throws BackupToolException
    {
        LOGGER.debug("Creating Backup Configuration.\n");

        BackupConfig conf;

        try
        {
            String currSetName = null;
            if( StringUtils.isBlank(setName) )
            {
                // Default or error
                LOGGER.debug("Missing BackupConfig SetName.");
            }
            else
            {
                currSetName = BackupConfigValidator.normalizeSetName(setName);
            }

            List<BackupConfigDirectory> currBackupDirectory = null;
            if( dirList == null || dirList.isEmpty() )
            {
                // Default or error
                LOGGER.debug("Missing BackupConfig Directory List.");
            }
            else
            {
                currBackupDirectory = BackupConfigValidator.normalizeBackupFolderList(dirList);
            }

            List<BackupConfigFile> currBackupFiles = null;
            if( fileList == null || fileList.isEmpty() )
            {
                LOGGER.debug("Missing BackupConfig File List.");
            }
            else
            {
                currBackupFiles = BackupConfigValidator.normalizeBackupFileList(fileList);
            }

            Integer currPriority = null;
            if( StringUtils.isBlank(priority) )
            {
                // Default or error
                LOGGER.debug("Missing BackupConfig Priority.");
            }
            else
            {
                currPriority = BackupConfigValidator.normalizePriority(priority);
            }

            Path currHoldingDir = null;
            if( StringUtils.isBlank(holdingDir) )
            {
                // Default or error
                LOGGER.debug("Missing BackupConfig Holding Directory.");
            }
            else
            {
                currHoldingDir = BackupConfigValidator.normalizeHoldingFolder(holdingDir);
            }

            Path currReportPath = null;
            if( StringUtils.isBlank(backupReportPath) )
            {
                // Default or error
                LOGGER.debug("Missing BackupConfig Report Path.");
            }
            else
            {
                currReportPath = BackupConfigValidator.normalizeReportPath(backupReportPath);
            }

            Path currRestoreDestPath = null;
            if( restoreDestination == null )
            {
                // Default or error
                LOGGER.debug("Missing BackupConfig Restore Destination Path.");
            }
            else
            {
                currRestoreDestPath = BackupConfigValidator.normalizeRestoreDestinationPath(restoreDestination);
            }

            Path currOutputFilePath = null;
            if( StringUtils.isBlank(outputFile) )
            {
                // Default or error
                LOGGER.debug("Missing BackupConfig Output File Path.");
            }
            else
            {
                currOutputFilePath = BackupConfigValidator.normalizeOutputFilePath(outputFile);
            }

            Path currLockFilePath = null;
            if( StringUtils.isBlank(lockFilePath) )
            {
                // Default or error
                LOGGER.debug("Missing BackupConfig Lock File Path.");
            }
            else
            {
                currLockFilePath = BackupConfigValidator.normalizeLockFilePath(lockFilePath);
            }

            Path currStateFilename = null;
            if( StringUtils.isBlank(stateFilename) )
            {
                // Default or error
                LOGGER.debug("Missing BackupConfig State Filename.");
            }
            else
            {
                currStateFilename = BackupConfigValidator.normalizeStateFilename(stateFilename);
            }

            Path currErrorFilename = null;
            if( StringUtils.isBlank(errorFilename) )
            {
                // Default or error
                LOGGER.debug("Missing BackupConfig Error Filename.");
            }
            else
            {
                currErrorFilename = BackupConfigValidator.normalizeErrorFilename(errorFilename);
            }

            Pattern currArchiveNamePattern = null;
            if( StringUtils.isBlank(archiveNamePattern) )
            {
                // Default or error
                LOGGER.debug("Missing Name Pattern.");
            }
            else
            {
                currArchiveNamePattern = BackupConfigValidator.normalizeArchiveNamePattern(archiveNamePattern);
            }

            List<BackupToolNameComponentType> currArchiveNameComp = null;
            if( StringUtils.isBlank(archiveNameComp) )
            {
                // Default or error
                LOGGER.debug("Missing Name Pattern Component.");
            }
            else
            {
                currArchiveNameComp = BackupConfigValidator.normalizeArchiveNameComp(archiveNameComp);
            }

            Path currArchiveNameTemplate = null;
            if( StringUtils.isBlank(archiveNameTemplate) )
            {
                // Default or error
                LOGGER.debug("Missing Name Template.");
            }
            else
            {
                currArchiveNameTemplate = BackupConfigValidator.normalizeArchiveNameTemplate(archiveNameTemplate);
            }

            Pattern currJobNamePattern = null;
            if( StringUtils.isBlank(jobNamePattern) )
            {
                // Default or error
                LOGGER.debug("Missing Job Name Pattern.");
            }
            else
            {
                currJobNamePattern = BackupConfigValidator.normalizeJobNamePattern(jobNamePattern);
            }

            List<BackupToolNameComponentType> currJobNameComp = null;
            if( StringUtils.isBlank(jobNameComp) )
            {
                // Default or error
                LOGGER.debug("Missing Job Name Pattern Component.");
            }
            else
            {
                currJobNameComp = BackupConfigValidator.normalizeJobNameComp(jobNameComp);
            }

            Set<BackupToolIgnoreFlags> currIgnoreFlags = null;
            if( ignoreFlags == null || ignoreFlags.isEmpty() )
            {
                // Default or error
                LOGGER.debug("Missing BackupConfig Ignore flags.");
            }
            else
            {
                currIgnoreFlags = ignoreFlags;
            }

            Path currJobNameTemplate =  null;
            if( StringUtils.isBlank(jobNameTemplate) )
            {
                // Default or error
                LOGGER.debug("Missing Job Template.");
            }
            else
            {
                currJobNameTemplate = BackupConfigValidator.normalizeJobNameTemplate(jobNameTemplate);
            }

            FSSReportType currReportType = null;
            if( StringUtils.isBlank(backupReportType) )
            {
                // Default or error
                LOGGER.debug("Missing BackupConfig Report Type.");
            }
            else
            {
                currReportType = BackupConfigValidator.normalizeReportType(backupReportType);
            }

            BackupId currBackupId = null;
            if( StringUtils.isBlank(backupId) )
            {
                // Default or error
                LOGGER.debug("Missing BackupConfig Backup ID.");
            }
            else
            {
                currBackupId = BackupConfigValidator.normalizeBackupId(backupId);
            }

            FSSBackupType currBackupType = null;
            if( StringUtils.isBlank(setType) )
            {
                // Default or error
                LOGGER.debug("Missing BackupConfig Backup Type");
            }
            else
            {
                currBackupType = BackupConfigValidator.normalizeBackupType(setType);
            }

            FSSBackupHashType currChecksumType = null;
            if( StringUtils.isBlank(checksumType) )
            {
                // Default or error
                LOGGER.debug("Missing BackupConfig Checksum Type.");
            }
            else
            {
                currChecksumType = BackupConfigValidator.normalizeChecksum(checksumType);
            }

            BackupConfigCompression currCompression = null;
            if( StringUtils.isBlank(compression) )
            {
                // Default or error
                LOGGER.debug("Missing BackupConfig Compression.");
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
                // Default or error
                LOGGER.debug("Missing BackupConfig Encryption.");
            }

            BackupConfigChunk currChunk = null;

            String currChunkSizeType = chunkSizeType;
            if( StringUtils.isBlank(chunkSizeType) )
            {
                currChunkSizeType = "KB";
            }

            if( StringUtils.isBlank(chunkSize) )
            {
                // Default or error
                LOGGER.debug("Missing BackupConfig Chunk.");
            }
            else
            {
                currChunk = BackupConfigValidator.normalizeChunk(true, chunkSize, currChunkSizeType);
            }

            FSSVerbosity currVerb = null;

            if( StringUtils.isBlank(verbosity) )
            {
                // Default or error
                LOGGER.debug("Missing BackupConfig Verbosity.");
            }
            else
            {
                currVerb = BackupConfigValidator.normalizeVerbosity(verbosity);
            }

            BackupConfigStorageBackend currStoreBackend = null;
            if( storageBackend == null )
            {
                // Default or error
                LOGGER.debug("Missing BackupConfig Storage Backend.");
            }
            else
            {
                currStoreBackend = storageBackend;
            }

            conf = from(
                    orig,
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
                    currIgnoreFlags,
                    currVerb,
                    currStoreBackend,
                    usageConfig
            );
        }
        catch( BackupToolException ex )
        {
            String errMsg = "Error creating new configuration object.";

            LOGGER.debug(errMsg, ex);
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
                        "Error validating the m_backup configuration : %s\n", valRes);

                LOGGER.debug(errMsg);

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
