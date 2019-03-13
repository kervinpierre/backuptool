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
import com.fastsitesoft.backuptool.enums.FSSBackupHashType;
import com.fastsitesoft.backuptool.enums.FSSBackupType;
import com.fastsitesoft.backuptool.enums.FSSReportType;
import com.fastsitesoft.backuptool.enums.FSSVerbosity;
import com.fastsitesoft.backuptool.utils.BackupToolException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
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
     * Application logging m_verbosity.
     */
    private FSSVerbosity m_verbosity;


    /**
     * File for redirecting StdOut and StdErr
     */
    private Path m_outputFile;

    /**
     * Do not change anything on the server, just simulate.
     */
    private Boolean m_dryRun;

    /**
     * Ignore existing lock file
     */
    private Boolean m_ignoreLock;

    /**
     * If set to true, the component may send out status emails
     */
    private Boolean m_emailOnCompletion;

    /**
     * List of contacts to be emailed if needed.
     */
    private List<String> m_emailContacts;

    /**
     * The process m_priority of the agent
     */
    private Integer m_priority;

    /**
     * Lock file for allowing single program access.
     */
    private Path m_lockFilePath;

    /**
     * If true run the process as a background service instead of an interactive
     * process.
     */
    private Boolean m_runAsService;

    /**
     * Display the version of the application then exit.
     */
    private Boolean m_displayVersion;

    /**
     * Display the usage options for the application the exit.
     */
    private Boolean m_displayUsage;

    private String m_encryptionCipher;

    private String m_encryptionKey;

    public String getEncryptionCipher()
    {
        return m_encryptionCipher;
    }

    public void setEncryptionCipher(final String encryptionCipher)
    {
        m_encryptionCipher = encryptionCipher;
    }

    public String getEncryptionKey()
    {
        return m_encryptionKey;
    }

    public void setEncryptionKey(final String encryptionKey)
    {
        m_encryptionKey = encryptionKey;
    }

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

    public void setUsageConfig(final UsageConfig usageConfig)
    {
        this.usageConfig = usageConfig;
    }

    public Path getLockFilePath()
    {
        return m_lockFilePath;
    }

    public void setLockFilePath(final Path lockFilePath)
    {
        m_lockFilePath = lockFilePath;
    }

    public Integer getPriority()
    {
        return m_priority;
    }

    public void setPriority(final Integer priority)
    {
        m_priority = priority;
    }

    public Boolean isEmailOnCompletion()
    {
        return m_emailOnCompletion;
    }

    public void setEmailOnCompletion(final Boolean emailOnCompletion)
    {
        m_emailOnCompletion = emailOnCompletion;
    }

    public Boolean isPreserveOwnership()
    {
        return m_preserveOwnership;
    }

    public void setPreserveOwnership(final Boolean preserveOwnership)
    {
        m_preserveOwnership = preserveOwnership;
    }

    public Boolean isPreservePermissions()
    {
        return m_preservePermissions;
    }

    public void setPreservePermissions(final Boolean preservePermissions)
    {
        m_preservePermissions = preservePermissions;
    }

    public Boolean isBackupDescribe()
    {
        return m_backupDescribe;
    }

    public void setBackupDescribe(final Boolean backupDescribe)
    {
        m_backupDescribe = backupDescribe;
    }

    public Boolean isBackupStatus()
    {
        return m_backupStatus;
    }

    public void setBackupStatus(final Boolean backupStatus)
    {
        m_backupStatus = backupStatus;
    }

    /**
     * If true, then do not change anything on the server.  Simply simulate what
     * would happen if we ran the command.
     */
    public Boolean isDryRun()
    {
        return m_dryRun;
    }

    public void setDryRun(Boolean dryRun)
    {
        m_dryRun = dryRun;
    }

    public Boolean isIgnoreLock()
    {
        return m_ignoreLock ==null?false: m_ignoreLock;
    }

    public void setIgnoreLock(final Boolean ignoreLock)
    {
        m_ignoreLock = ignoreLock;
    }

    public FSSVerbosity getVerbosity()
    {
        return m_verbosity;
    }

    public void setVerbosity(FSSVerbosity verbosity)
    {
        m_verbosity = verbosity;
    }

    public void setVerbosity(String verbosityStr)
    {
        FSSVerbosity verbosity = FSSVerbosity.from(verbosityStr);

        m_verbosity = verbosity;
    }

    public Boolean isRunAsService()
    {
        return m_runAsService;
    }

    public void setRunAsService(Boolean runAsService)
    {
        m_runAsService = runAsService;
    }

    public Boolean isDisplayVersion()
    {
        return m_displayVersion ==null?false: m_displayVersion;
    }

    public void setDisplayVersion(Boolean displayVersion)
    {
        m_displayVersion = displayVersion;
    }

    public Boolean isDisplayUsage()
    {
        return m_displayUsage ==null?false: m_displayUsage;
    }

    public void setDisplayUsage(final Boolean displayUsage)
    {
        m_displayUsage = displayUsage;
    }

    public Boolean isNoClobber()
    {
        return m_noClobber;
    }

    public void setNoClobber(final Boolean noClobber)
    {
        m_noClobber = noClobber;
    }

    public Pattern getArchiveFileNamePattern()
    {
        return m_archiveFileNamePattern;
    }

    public List<BackupToolNameComponentType> getArchiveFileNameComponent()
    {
        return m_archiveFileNameComponent;
    }

    public void setArchiveFileNameComponent(
            final List<BackupToolNameComponentType> archiveFileNameComponent)
    {
        m_archiveFileNameComponent = archiveFileNameComponent;
    }

    public void setArchiveFileNameComponent(
            final String archiveFileNameComponent) throws BackupToolException
    {
        m_archiveFileNameComponent
                = BackupConfigValidator.normalizeArchiveNameComp(archiveFileNameComponent);
    }

    public Path getArchiveFileNameTemplate()
    {
        return m_archiveFileNameTemplate;
    }

    public void setArchiveFileNameTemplate(final Path archiveFileNameTemplate)
    {
        m_archiveFileNameTemplate = archiveFileNameTemplate;
    }

    public void setArchiveFileNameTemplate(final String archiveFileNameTemplateStr)
    {
        Path archiveFileNameTemplate = Paths.get(archiveFileNameTemplateStr);

        m_archiveFileNameTemplate = archiveFileNameTemplate;
    }

    public Pattern getJobFileNamePattern()
    {
        return m_jobFileNamePattern;
    }

    public void setJobFileNamePattern(final Pattern jobFileNamePattern)
    {
        m_jobFileNamePattern = jobFileNamePattern;
    }

    public void setJobFileNamePattern(final String jobFileNamePatternStr) throws BackupToolException
    {
        Pattern jobFileNamePattern
                = BackupConfigValidator.normalizeJobNamePattern(jobFileNamePatternStr);

        m_jobFileNamePattern = jobFileNamePattern;
    }

    public void setArchiveFileNamePattern(final Pattern archiveFileNamePattern)
    {
        m_archiveFileNamePattern = archiveFileNamePattern;
    }

    public void setArchiveFileNamePattern(final String archiveFileNamePatternStr) throws BackupToolException
    {
        Pattern archiveFileNamePattern
                = BackupConfigValidator.normalizeArchiveNamePattern(archiveFileNamePatternStr);

        m_archiveFileNamePattern = archiveFileNamePattern;
    }

    public Path getJobFileNameTemplate()
    {
        return m_jobFileNameTemplate;
    }

    public void setJobFileNameTemplate(final Path jobFileNameTemplate)
    {
        m_jobFileNameTemplate = jobFileNameTemplate;
    }

    public void setJobFileNameTemplate(final String jobFileNameTemplate) throws BackupToolException
    {
        m_jobFileNameTemplate = BackupConfigValidator.normalizeJobNameTemplate(jobFileNameTemplate);
    }

    public List<BackupToolNameComponentType> getJobFileNameComponent()
    {
        return m_jobFileNameComponent;
    }

    public void setJobFileNameComponent(
            final List<BackupToolNameComponentType> jobFileNameComponent)
    {
        m_jobFileNameComponent = jobFileNameComponent;
    }

    public void setJobFileNameComponent(
            final String jobFileNameComponentStr) throws BackupToolException
    {
        final List<BackupToolNameComponentType> jobFileNameComponent =
                BackupConfigValidator.normalizeJobNameComp(jobFileNameComponentStr);

        m_jobFileNameComponent = jobFileNameComponent;
    }

    public Set<BackupToolIgnoreFlags> getIgnoreFlags()
    {
        return m_backupToolIgnoreFlags;
    }

    public void setBackupToolIgnoreFlags(
            final Set<BackupToolIgnoreFlags> backupToolIgnoreFlags)
    {
        m_backupToolIgnoreFlags = backupToolIgnoreFlags;
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

    public void setOutputFile(Path outputFile)
    {
        m_outputFile = outputFile;
    }

    public void setOutputFile(String outputFileStr)
    {
        m_outputFile = Paths.get(outputFileStr);
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

    public void setHoldingDirectory(final Path holdingDirectory)
    {
        m_holdingDirectory = holdingDirectory;
    }

    public void setHoldingDirectory(final String holdingDirectoryStr)
    {
        Path holdingDirectory = Paths.get(holdingDirectoryStr);

        m_holdingDirectory = holdingDirectory;
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

    public void setBackupId(final BackupId backupId)
    {
        m_backupId = backupId;
    }

    public void setBackupId(final String backupIdStr)
    {
        BackupId id = BackupId.from(backupIdStr);

        m_backupId = id;
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

    public void setUseChecksum(final Boolean useChecksum)
    {
        m_useChecksum = useChecksum;
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

    public void setChecksumType(final FSSBackupHashType checksumType)
    {
        m_checksumType = checksumType;
    }

    public void setChecksumType(final String checksumType) throws BackupToolException
    {
        m_checksumType = BackupConfigValidator.normalizeChecksum(checksumType);
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

    public void setUseModifiedDate(final Boolean useModifiedDate)
    {
        m_useModifiedDate = useModifiedDate;
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

    public void setBackupReportType(final FSSReportType backupReportType)
    {
        m_backupReportType = backupReportType;
    }

    public void setBackupReportType(final String backupReportType) throws BackupToolException
    {
        m_backupReportType = BackupConfigValidator.normalizeReportType(backupReportType);
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

    public void setBackupReportPath(final Path backupReportPath)
    {
        m_backupReportPath = backupReportPath;
    }

    public void setBackupReportPath(final String backupReportPath) throws BackupToolException
    {
        m_backupReportPath = BackupConfigValidator.normalizeReportPath(backupReportPath);
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

    public void setRestore(final Boolean restore)
    {
        m_restore = restore;
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

    public void setBackup(Boolean backup)
    {
        m_backup = backup;
    }

    public List<String> getEmailContacts()
    {
        return m_emailContacts;
    }

    public void setEmailContacts(final List<String> emailContacts)
    {
        m_emailContacts = emailContacts;
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

    public void setRestoreDestination(final Path restoreDestination)
    {
        m_restoreDestination = restoreDestination;
    }

    public void setRestoreDestination(final String restoreDestination) throws BackupToolException
    {
        m_restoreDestination
                = BackupConfigValidator.normalizeRestoreDestinationPath(restoreDestination);
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

    public void setSetName(final String setName)
    {
        m_setName = setName;
    }

    /**
     * @return the m_setType
     */
    public FSSBackupType getSetType()
    {
        return m_setType;
    }

    public void setSetType(final FSSBackupType setType)
    {
        m_setType = setType;
    }

    public void setSetType(final String setTypeStr) throws BackupToolException
    {
        FSSBackupType t = FSSBackupType.from(setTypeStr);

        m_setType = t;
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

    public void setDirList(final List<BackupConfigDirectory> dirList)
    {
        m_dirList = dirList;
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

    public void setStateFileName(final Path stateFileName)
    {
        m_stateFileName = stateFileName;
    }

    public Path getErrorFileName()
    {
        return m_errorFileName;
    }

    public void setErrorFileName(final Path errorFileName)
    {
        m_errorFileName = errorFileName;
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

    public void setCompression(final BackupConfigCompression compression)
    {
        m_compression = compression;
    }

    public void setCompression(final String compression) throws BackupToolException
    {
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

        m_compression = currCompression;
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

    public void setChunk(final BackupConfigChunk chunk)
    {
        m_chunk = chunk;
    }

    public void setChunk(final String chunkStr, String sizeTypeStr) throws BackupToolException
    {
        BackupConfigChunk currChunk = null;

        String currChunkSizeType = sizeTypeStr;
        if( StringUtils.isBlank(sizeTypeStr) )
        {
            currChunkSizeType = "KB";
        }

        if( StringUtils.isBlank(chunkStr) )
        {
            // Default or error
            LOGGER.debug("Missing BackupConfig Chunk.");
        }
        else
        {
            currChunk = BackupConfigValidator.normalizeChunk(true, chunkStr, currChunkSizeType);
        }

        m_chunk = currChunk;
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

    public void setFileList(final List<BackupConfigFile> fileList)
    {
        m_fileList = fileList;
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

    public void setStorageBackend(final BackupConfigStorageBackend storageBackend)
    {
        m_storageBackend = storageBackend;
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

    public void setEncryption(final BackupConfigEncryption encryption)
    {
        m_encryption = encryption;
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
