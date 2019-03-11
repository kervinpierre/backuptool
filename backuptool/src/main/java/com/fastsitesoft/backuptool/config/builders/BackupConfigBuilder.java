/*
 * SLU Dev Inc. CONFIDENTIAL
 * DO NOT COPY
 *
 * Copyright (c) [2012] - [2019] SLU Dev Inc. <info@sludev.com>
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
package com.fastsitesoft.backuptool.config.builders;

import com.fastsitesoft.backuptool.BackupId;
import com.fastsitesoft.backuptool.config.entities.BackupConfig;
import com.fastsitesoft.backuptool.config.entities.BackupConfigChunk;
import com.fastsitesoft.backuptool.config.entities.BackupConfigCompression;
import com.fastsitesoft.backuptool.config.entities.BackupConfigDirectory;
import com.fastsitesoft.backuptool.config.entities.BackupConfigEncryption;
import com.fastsitesoft.backuptool.config.entities.BackupConfigFile;
import com.fastsitesoft.backuptool.config.entities.BackupConfigStorageBackend;
import com.fastsitesoft.backuptool.config.entities.UsageConfig;
import com.fastsitesoft.backuptool.config.validators.BackupConfigValidator;
import com.fastsitesoft.backuptool.enums.BackupToolIgnoreFlags;
import com.fastsitesoft.backuptool.enums.BackupToolNameComponentType;
import com.fastsitesoft.backuptool.enums.BackupToolResultStatus;
import com.fastsitesoft.backuptool.enums.FSSBackupHashType;
import com.fastsitesoft.backuptool.enums.FSSBackupType;
import com.fastsitesoft.backuptool.enums.FSSReportType;
import com.fastsitesoft.backuptool.enums.FSSVerbosity;
import com.fastsitesoft.backuptool.utils.BackupToolException;
import com.fastsitesoft.backuptool.utils.BackupToolResult;
import com.fastsitesoft.backuptool.utils.FSSValidateConfigResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * This class represents the configuration for a m_backup set.
 *
 * @author Kervin Pierre <info@sludev.com>
 */
public final class BackupConfigBuilder
{
    private static final Logger LOGGER
            = LogManager.getLogger(BackupConfigBuilder.class);

    private Integer m_id;

    private String m_setName;
    private FSSBackupType m_setType;
    private List<BackupConfigDirectory> m_dirList;
    private List<BackupConfigFile> m_fileList;
    private Path m_stateFileName;
    private Path m_errorFileName;
    private Path m_holdingDirectory;
    private BackupId m_backupId;
    private Boolean m_useChecksum;
    private FSSBackupHashType m_checksumType;
    private Boolean m_useModifiedDate;
    private BackupConfigCompression m_compression;
    private BackupConfigChunk m_chunk;
    private FSSReportType m_backupReportType;
    private Path m_backupReportPath;
    private Boolean m_restore;
    private Boolean m_backup;
    private Boolean m_preservePermissions;
    private Boolean m_preserveOwnership;
    private Boolean m_noClobber;
    private Boolean m_backupDescribe;
    private Boolean m_backupStatus;
    private Path m_restoreDestination;
    private Pattern m_archiveFileNamePattern;
    private Path m_archiveFileNameTemplate;
    private List<BackupToolNameComponentType> m_jobFileNameComponent;
    private Pattern m_jobFileNamePattern;
    private Path m_jobFileNameTemplate;
    private Set<BackupToolIgnoreFlags> m_backupToolIgnoreFlags;
    private List<BackupToolNameComponentType> m_archiveFileNameComponent;
    private BackupConfigStorageBackend m_storageBackend;
    private BackupConfigEncryption m_encryption;

    /**
     * Application logging verbosity.
     */
    private FSSVerbosity verbosity;


    /**
     * File for redirecting StdOut and StdErr
     */
    private Path outputFile;

    /**
     * Do not change anything on the server, just simulate.
     */
    private Boolean dryRun;

    /**
     * Ignore existing lock file
     */
    private Boolean ignoreLock;

    /**
     * If set to true, the component may send out status emails
     */
    private Boolean emailOnCompletion;

    /**
     * List of contacts to be emailed if needed.
     */
    private List<String> emailContacts;

    /**
     * The process priority of the agent
     */
    private Integer priority;

    /**
     * Lock file for allowing single program access.
     */
    private Path lockFilePath;

    /**
     * If true run the process as a background service instead of an interactive
     * process.
     */
    private Boolean runAsService;

    /**
     * Display the version of the application then exit.
     */
    private Boolean displayVersion;

    /**
     * Display the usage options for the application the exit.
     */
    private Boolean displayUsage;

    public Integer getId()
    {
        return m_id;
    }

    public void setId(Integer id)
    {
        m_id = id;
    }

    public void setId( String idStr )
            throws BackupToolException
    {
        if(StringUtils.isNoneBlank(idStr))
        {
            try
            {
                m_id = Integer.parseInt(idStr);
            }
            catch( NumberFormatException ex )
            {
                String errMsg = String.format("Error parsing integer '%s'", idStr);

                LOGGER.debug(errMsg, ex);
                throw new BackupToolException(errMsg, ex);
            }
        }
    }

    /**
     * Holds the 'Display Usage' options
     */
    private UsageConfig usageConfig;

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
        return outputFile;
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
        return emailContacts;
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

    private BackupConfigBuilder( )
    {

    }

    public static BackupConfigBuilder from()
    {
        return from(null);
    }

    public static BackupConfigBuilder from(final Integer id)
    {
        BackupConfigBuilder res = new BackupConfigBuilder();

        res.setId(id);

        return res;
    }

    public BackupConfig toConfig(final BackupConfig orig)
            throws BackupToolException
    {
        BackupConfig res = BackupConfig.from(orig,
                getSetName(),
                getPriority(),
                getUseChecksum(),
                getUseModifiedDate(),
                getRestore(),
                getBackup(),
                isEmailOnCompletion(),
                isDryRun(),
                isIgnoreLock(),
                isRunAsService(),
                isDisplayUsage(),
                isDisplayVersion(),
                isPreserveOwnership(),
                isPreservePermissions(),
                isNoClobber(),
                isBackupDescribe(),
                isBackupStatus(),
                getHoldingDirectory(),
                getBackupReportPath(),
                getRestoreDestination(),
                getOutputFile(),
                getLockFilePath(),
                getStateFileName(),
                getErrorFileName(),
                getEmailContacts(),
                getDirList(),
                getFileList(),
                getBackupReportType(),
                getBackupId(),
                getSetType(),
                getChecksumType(),
                getCompression(),
                getEncryption(),
                getChunk(),
                getArchiveFileNamePattern(),
                getArchiveFileNameComponent(),
                getArchiveFileNameTemplate(),
                getJobFileNamePattern(),
                getJobFileNameComponent(),
                getJobFileNameTemplate(),
                getIgnoreFlags(),
                getVerbosity(),
                getStorageBackend(),
                getUsageConfig());

        return res;
    }
}
