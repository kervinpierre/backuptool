/*
 *  BackupLogic LLC CONFIDENTIAL
 *  DO NOT COPY
 *
 * Copyright (c) [2012] - [2019] BackupLogic LLC <info@backuplogic.com>
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

package com.fastsitesoft.backuptool.config.validators;

import com.fastsitesoft.backuptool.BackupId;
import com.fastsitesoft.backuptool.config.entities.BackupConfig;
import com.fastsitesoft.backuptool.config.entities.BackupConfigChunk;
import com.fastsitesoft.backuptool.config.entities.BackupConfigCompression;
import com.fastsitesoft.backuptool.config.entities.BackupConfigDirectory;
import com.fastsitesoft.backuptool.config.entities.BackupConfigEncryption;
import com.fastsitesoft.backuptool.config.entities.BackupConfigFile;
import com.fastsitesoft.backuptool.config.entities.BackupConfigStorageBackend;
import com.fastsitesoft.backuptool.enums.BackupToolCompressionType;
import com.fastsitesoft.backuptool.enums.BackupToolEncryptionType;
import com.fastsitesoft.backuptool.enums.BackupToolNameComponentType;
import com.fastsitesoft.backuptool.enums.FSSBackupHashType;
import com.fastsitesoft.backuptool.enums.FSSBackupItemPathTypeEnum;
import com.fastsitesoft.backuptool.enums.FSSBackupSizeType;
import com.fastsitesoft.backuptool.enums.FSSConfigMembersEnum;
import com.fastsitesoft.backuptool.enums.FSSReportType;
import com.fastsitesoft.backuptool.enums.FSSValidateResultCodeEnum;
import com.fastsitesoft.backuptool.enums.FSSValidateResultErrorEnum;
import com.fastsitesoft.backuptool.enums.FSSVerbosity;
import com.fastsitesoft.backuptool.utils.BackupToolException;
import com.fastsitesoft.backuptool.enums.FSSBackupType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.fastsitesoft.backuptool.utils.FSSValidateConfigError;
import com.fastsitesoft.backuptool.utils.FSSValidateConfigResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Kervin Pierre <info@sludev.com>
 */
public class BackupConfigValidator
{

    private static final Logger log 
                            = LogManager.getLogger(BackupConfigValidator.class);
    
    public BackupConfigValidator()
    {
        
    }
    
    public void check( BackupConfig conf ) throws BackupToolException
    {
        Path hld = conf.getHoldingDirectory();
        
        if( StringUtils.isBlank(conf.getSetName())
                || conf.getSetName().length() < 3)
        {
            String errMsg = String.format(
                         "Error : Invalid backup set name '{0}'.  Should be 3 characters or more in length.",
                         conf.getSetName());
            
            log.debug(errMsg);
            
            throw new BackupToolException(errMsg);
        }
        
        if( conf.getSetType() == FSSBackupType.NONE
                || conf.getSetType() == null )
        {
            String errMsg = String.format(
                         "Error : Invalid backup set type. 'Full' or 'Incremental' expected");
            
            log.debug(errMsg);
            
            throw new BackupToolException(errMsg);
        }
           
        if( conf.getDirList() == null || conf.getDirList().size() < 1 )
        {
            String errMsg = String.format(
                         "Error : Folder list is empty or invalid. We need at least 1 folder to backup."
                         );
            
            log.debug(errMsg);
            
            throw new BackupToolException(errMsg);
        }
        
        if( StringUtils.isBlank( conf.getSetName() ))
        {
            throw new BackupToolException("Error : Blank Name : Invalid Backup Set name. ");
        }
        
        if( conf.getSetType() == null )
        {
            throw new BackupToolException("Error : Null Type : Invalid Backup Set type. ");
        }
        
        if( conf.getDirList() == null )
        {
            throw new BackupToolException("Error : Null Folder List : Invalid Folder List. ");
        }
        
        if( conf.getDirList().size() < 1 && conf.getFileList().size() < 1 )
        {
            throw new BackupToolException("Error : Empty Item List : Invalid Folder/File List. ");
        }
        
        if( conf.getStateFileName() == null )
        {
            throw new BackupToolException("Error : Blank State File : Invalid Backup State File. ");
        }
        
        if( Files.exists(hld) == false )
        {
            String errMsg = String.format(
                    "Error : Holding folder '{0}' does not exist.",
                    hld);
            
            log.debug(errMsg);
            
            throw new BackupToolException(errMsg);
        }
        
        if( Files.isDirectory(hld) == false )
        {
            String errMsg = String.format(
                         "Error : Holding folder '{0}' does not seem like a folder.",
                         hld);
            
            log.debug(errMsg);
            
            throw new BackupToolException(errMsg);
        }
        
        long fileCount = Long.MAX_VALUE;
        
        try
        {
            fileCount = Files.list(hld).count();
        }
        catch (IOException ex)
        {
            String errMsg = String.format(
                         "Error : Holding folder '{0}' throw IOException on count().",
                          hld);
            
            log.debug(errMsg, ex);
            
            throw new BackupToolException(errMsg, ex);
        }
        
        if( fileCount > 0 )
        {
            String errMsg = String.format(
                         "Error : Holding folder '{0}' is not empty.",
                          hld);
            
            throw new BackupToolException(errMsg);
        }
        
        if( Files.isWritable(hld) == false )
        {
            String errMsg = String.format(
                         "Error : Holding folder '{0}' is not writable.",
                         hld);
            
            log.debug(errMsg);
            
            throw new BackupToolException(errMsg);
        }
        
        BackupConfigStorageBackend currStorage = conf.getStorageBackend();
        if( currStorage.getEnable() == false )
        {
            String errMsg = String.format(
                         "Error : Storage configuration is disabled.");
            
            throw new BackupToolException(errMsg);
        }
    }

    /**
     * Check that the current configuration object can be run without runtime
     * errors.
     *
     * @return An object that contains a list of all errors and overall status of the validation call.
     * @throws BackupToolException
     */
    public static FSSValidateConfigResult validate(BackupConfig conf) throws BackupToolException
    {
        log.debug("BackupConfigValidator validate()");

        FSSValidateConfigResult res = new FSSValidateConfigResult();

        if( conf == null )
        {
            String errMsg = String.format("Validating a null configuration");

            log.debug(errMsg);
            throw new BackupToolException(errMsg);
        }

        // Check set name is not empty
        if( StringUtils.isBlank(conf.getSetName()) )
        {
            res.addError(FSSConfigMembersEnum.BACKUPCONFIG_SETNAME,
                    FSSValidateResultCodeEnum.ERROR,
                    String.format("Invalid 'setName' value '%s'", conf.getSetName()),
                    conf.getSetName(), FSSValidateResultErrorEnum.INVALID_VALUE);
        }

        if( conf.getSetType() == null )
        {
            res.addError(FSSConfigMembersEnum.BACKUPCONFIG_SETTYPE,
                    FSSValidateResultCodeEnum.ERROR,
                    "Invalid 'Set Type' value is null",
                    conf.getSetType(), FSSValidateResultErrorEnum.INVALID_VALUE);
        }
        else
        {
            // Check type is valid
            switch( conf.getSetType() )
            {
                case FULL:
                    // TODO : Validate FULL backup option in here
                    break;

                case INCREMENTAL:
                    // TODO : Validate INCREMENTAL backup option in here
                    break;

                default:
                    res.addError(FSSConfigMembersEnum.BACKUPCONFIG_SETTYPE,
                            FSSValidateResultCodeEnum.ERROR,
                            String.format("Invalid 'Set Type' value '%s'", conf.getSetType()),
                            conf.getSetType(), FSSValidateResultErrorEnum.INVALID_VALUE);
                    break;
            }
        }

        // Check state file name is valid
        if( conf.getStateFileName() == null )
        {
            res.addError(FSSConfigMembersEnum.BACKUPCONFIG_STATEFILENAME,
                    FSSValidateResultCodeEnum.WARNING,
                    String.format("Invalid 'State Filename' value '%s'", conf.getStateFileName()),
                    conf.getStateFileName(), FSSValidateResultErrorEnum.INVALID_VALUE);
        }

        // Check holding path is valid
        if( conf.getHoldingDirectory() == null || !Files.exists(conf.getHoldingDirectory()) )
        {
            res.addError(FSSConfigMembersEnum.BACKUPCONFIG_HOLDINGFOLDERPATH,
                    FSSValidateResultCodeEnum.ERROR,
                    String.format("Invalid 'Holding Directory Path' value '%s' doesn't exist", conf.getHoldingDirectory()),
                    conf.getHoldingDirectory(), FSSValidateResultErrorEnum.MISSING_FS_OBJECT);
        }
        else if( !Files.isDirectory(conf.getHoldingDirectory()) )
        {
            res.addError(FSSConfigMembersEnum.BACKUPCONFIG_HOLDINGFOLDERPATH,
                    FSSValidateResultCodeEnum.ERROR,
                    String.format("Invalid 'Holding Directory Path' value '%s' is not a directory", conf.getHoldingDirectory()),
                    conf.getHoldingDirectory(), FSSValidateResultErrorEnum.EXPECTED_FOLDER);
        }

        // Check Folder List is valid
        for( BackupConfigDirectory currFld : conf.getDirList() )
        {
            if( currFld.getPathType() != FSSBackupItemPathTypeEnum.REGEX )
            {
                Path currPath = Paths.get(currFld.getPathString());
                if( !Files.exists(currPath) )
                {
                    // Folders to be backed up must exist
                    res.addError(FSSConfigMembersEnum.BACKUPCONFIG_FOLDERLIST,
                            FSSValidateResultCodeEnum.ERROR,
                            String.format("Invalid 'Folder Path' value '%s' does not exists", currPath),
                            currPath, FSSValidateResultErrorEnum.MISSING_FS_OBJECT);
                }
                else if( !Files.isDirectory(currPath) )
                {
                    // Folders must be a folder
                    res.addError(FSSConfigMembersEnum.BACKUPCONFIG_FOLDERLIST,
                            FSSValidateResultCodeEnum.ERROR,
                            String.format("Invalid 'Folder Path' value '%s' is not a folder", currPath),
                            currPath, FSSValidateResultErrorEnum.EXPECTED_FOLDER);
                }
            }
        }

        if( conf.getChecksumType() == null )
        {
            ;
        }
        else
        {
            // Check checksum configuration
            switch( conf.getChecksumType() )
            {
                case MD5:
                    break;

                case SHA1:
                    break;

                case SHA2:
                    break;

                default:
                    res.addError(FSSConfigMembersEnum.BACKUPCONFIG_CHECKSUMTYPE,
                            FSSValidateResultCodeEnum.WARNING,
                            String.format("Invalid 'Checksum Type' value '%s' is not valid", conf.getChecksumType()),
                            conf.getChecksumType(), FSSValidateResultErrorEnum.INVALID_VALUE);
                    break;
            }
        }

        // Check Chunk config
        if( conf.getChunk() != null )
        {
            List<FSSValidateConfigError> chunkRes = BackupConfigChunk.validate(conf.getChunk());
            for( FSSValidateConfigError err : chunkRes )
            {
                if( err.getStatus() != FSSValidateResultCodeEnum.NONE )
                {
                    res.addError(err);
                }
            }
        }

        if( conf.getEncryption() == null )
        {
            res.addError(FSSConfigMembersEnum.BACKUPCONFIG_ENCRYPTIONTYPE,
                    FSSValidateResultCodeEnum.WARNING,
                    String.format("Invalid 'Encryption' value null is not valid"),
                    conf.getEncryption(), FSSValidateResultErrorEnum.INVALID_VALUE);
        }
        else
        {
            // Check encryption type
            switch( conf.getEncryption().getType() )
            {
                case AES:
                    break;

                default:
                    res.addError(FSSConfigMembersEnum.BACKUPCONFIG_ENCRYPTIONTYPE,
                            FSSValidateResultCodeEnum.WARNING,
                            String.format("Invalid 'Encryption Type' value '%s' is not valid", conf.getEncryption()),
                            conf.getEncryption(), FSSValidateResultErrorEnum.INVALID_VALUE);
                    break;
            }
        }

        // Check exclude regex

        // Check exclude path list

        if( FSSValidateConfigResult.getStatus(res) != FSSValidateResultCodeEnum.VALID )
        {
            log.debug(String.format(
                    "Issues validating the backup configuration : %s\n", res));
        }

        return res;
    }

    public static String normalizeSetName( String s ) throws BackupToolException
    {
        String res = null;

        if( StringUtils.isEmpty(s) )
        {
            throw new BackupToolException("Invalid 'setName' cannot be empty");
        }

        res = s.trim().toUpperCase();

        return res;
    }

    public static int normalizePriority( String s ) throws BackupToolException
    {
        int res;

        if( StringUtils.isEmpty(s) )
        {
            throw new BackupToolException("Invalid 'priority' cannot be empty");
        }

        try
        {
            res = Integer.parseInt(s.trim());
        }
        catch(Exception ex)
        {
            throw new BackupToolException(String.format("Invalid 'priority' '%s'", s), ex);
        }

        return res;
    }

    public static Path normalizePath( String p, String argName ) throws BackupToolException
    {
        Path res;

        if( StringUtils.isEmpty(p) )
        {
            throw new BackupToolException(String.format("Invalid '%s' cannot be empty", argName));
        }

        try
        {
            res = Paths.get(p);
        }
        catch(Exception ex)
        {
            throw new BackupToolException(String.format("Invalid '%s' '%s'", argName, p), ex);
        }

        return res;
    }

    public static Path normalizeHoldingFolder( String s ) throws BackupToolException
    {
        Path res;

        res = normalizePath(s, "holding folder");

        return res;
    }

    public static Path normalizeReportPath( String s ) throws BackupToolException
    {
        Path res;

        res = normalizePath(s, "report path");

        return res;
    }

    public static Path normalizeRestoreDestinationPath( String s ) throws BackupToolException
    {
        Path res;

        res = normalizePath(s, "restore destination path");

        return res;
    }

    public static Path normalizeOutputFilePath( String s ) throws BackupToolException
    {
        Path res;

        res = normalizePath(s, "output file path");

        return res;
    }

    public static Path normalizeLockFilePath( String s ) throws BackupToolException
    {
        Path res;

        res = normalizePath(s, "lock file path");

        return res;
    }

    public static Path normalizeStateFilename( String s ) throws BackupToolException
    {
        Path res;

        res = normalizePath(s, "state filename");

        return res;
    }

    public static Path normalizeErrorFilename( String s ) throws BackupToolException
    {
        Path res;

        res = normalizePath(s, "error filename");

        return res;
    }

    public static List<BackupConfigDirectory> normalizeBackupFolderList(List<BackupConfigDirectory> bfdl) throws BackupToolException
    {
        List<BackupConfigDirectory> res = new ArrayList<>();

        if( bfdl == null )
        {
            throw new BackupToolException("Backup folder list cannot be null");
        }

        for( BackupConfigDirectory currDir : bfdl )
        {
            // TODO : Validate configuration directory

            res.add(currDir);
        }

        return res;
    }

    public static List<BackupConfigFile> normalizeBackupFileList(List<BackupConfigFile> bffl) throws BackupToolException
    {
        List<BackupConfigFile> res = new ArrayList<>();

        if( bffl == null )
        {
            throw new BackupToolException("Backup file list cannot be null");
        }

        for( BackupConfigFile currFile : bffl )
        {
            // TODO : Validate configuration file

            res.add(currFile);
        }

        return res;
    }

    public static FSSReportType normalizeReportType(String s) throws BackupToolException
    {
        FSSReportType res;

        if( StringUtils.isEmpty(s) )
        {
            throw new BackupToolException(String.format("Invalid 'report type' cannot be empty"));
        }

        res = FSSReportType.from(s);

        return res;
    }

    public static BackupId normalizeBackupId(String s) throws BackupToolException
    {
        BackupId res;

        if( StringUtils.isEmpty(s) )
        {
            throw new BackupToolException(String.format("Invalid 'backup id' cannot be empty"));
        }

        res = BackupId.from(s);

        return res;
    }

    public static FSSBackupType normalizeBackupType(String s) throws BackupToolException
    {
        FSSBackupType res;

        if( StringUtils.isEmpty(s) )
        {
            throw new BackupToolException(String.format("Invalid 'backup type' cannot be empty"));
        }

        res = FSSBackupType.from(s);

        return res;
    }

    public static FSSBackupHashType normalizeChecksum(String s) throws BackupToolException
    {
        FSSBackupHashType res;

        if( StringUtils.isEmpty(s) )
        {
            throw new BackupToolException(String.format("Invalid 'checksum type' cannot be empty"));
        }

        res = FSSBackupHashType.from(s);

        return res;
    }

    public static BackupConfigCompression normalizeCompression(String s) throws BackupToolException
    {
        BackupConfigCompression res;

        if( StringUtils.isEmpty(s) )
        {
            throw new BackupToolException(String.format("Invalid 'compression type' cannot be empty"));
        }

        BackupToolCompressionType type = BackupToolCompressionType.from(s);

        res = new BackupConfigCompression(type);

        return res;
    }

    public static BackupConfigEncryption normalizeEncryption(String cipher, String key) throws BackupToolException
    {
        BackupConfigEncryption res;

        if( StringUtils.isEmpty(cipher) )
        {
            throw new BackupToolException(String.format("Invalid 'encryption cipher' cannot be empty"));
        }

        if( StringUtils.isEmpty(key) )
        {
            throw new BackupToolException(String.format("Invalid 'encryption key' cannot be empty"));
        }

        BackupToolEncryptionType currType = BackupToolEncryptionType.from(cipher);

        res = new BackupConfigEncryption(currType, key);

        return res;
    }

    public static BackupConfigChunk normalizeChunk(boolean en, String size, String sizeType) throws BackupToolException
    {
        BackupConfigChunk res;

        if( StringUtils.isEmpty(size) )
        {
            throw new BackupToolException(String.format("Invalid 'chunk size' cannot be empty"));
        }

        if( StringUtils.isEmpty(sizeType) )
        {
            throw new BackupToolException(String.format("Invalid 'size type' cannot be empty"));
        }

        FSSBackupSizeType currSizeType = FSSBackupSizeType.from(sizeType);
        long currSize = Long.parseLong(size);

        res = new BackupConfigChunk(currSize, currSizeType, en);

        return res;
    }

    public static FSSVerbosity normalizeVerbosity(String s) throws BackupToolException
    {
        FSSVerbosity res;

        if( StringUtils.isEmpty(s) )
        {
            throw new BackupToolException(String.format("Invalid 'verbosity' cannot be empty"));
        }

        res = FSSVerbosity.from(s);

        return res;
    }

    public static List<BackupToolNameComponentType> normalizeArchiveNameComp(String s) throws BackupToolException
    {
        BackupToolNameComponentType res;

        if( StringUtils.isEmpty(s) )
        {
            throw new BackupToolException(String.format("Invalid 'archive name component' cannot be empty"));
        }

        res = BackupToolNameComponentType.from(s);

        List<BackupToolNameComponentType> resArray = new ArrayList<>();
        resArray.add(res);

        return resArray;
    }

    public static Pattern normalizeArchiveNamePattern(String s) throws BackupToolException
    {
        Pattern res;

        if( StringUtils.isEmpty(s) )
        {
            throw new BackupToolException(String.format("Invalid 'archive name pattern' cannot be empty"));
        }

        res = Pattern.compile(s);

        return res;
    }

    public static Path normalizeArchiveNameTemplate(String s) throws BackupToolException
    {
        Path res;

        if( StringUtils.isEmpty(s) )
        {
            throw new BackupToolException(String.format("Invalid 'archive name template' cannot be empty"));
        }

        res = Paths.get(s);

        return res;
    }

    public static List<BackupToolNameComponentType> normalizeJobNameComp(String s) throws BackupToolException
    {
        BackupToolNameComponentType res;

        if( StringUtils.isEmpty(s) )
        {
            throw new BackupToolException(String.format("Invalid 'job name component' cannot be empty"));
        }

        res = BackupToolNameComponentType.from(s);

        List<BackupToolNameComponentType> resArray = new ArrayList<>();
        resArray.add(res);

        return resArray;
    }

    public static Pattern normalizeJobNamePattern(String s) throws BackupToolException
    {
        Pattern res;

        if( StringUtils.isEmpty(s) )
        {
            throw new BackupToolException(String.format("Invalid 'job name pattern' cannot be empty"));
        }

        res = Pattern.compile(s);

        return res;
    }

    public static Path normalizeJobNameTemplate(String s) throws BackupToolException
    {
        Path res;

        if( StringUtils.isEmpty(s) )
        {
            throw new BackupToolException(String.format("Invalid 'job name template' cannot be empty"));
        }

        res = Paths.get(s);

        return res;
    }

    public static BackupConfigStorageBackend normalizeStorageBackend(String ur, String us, String ps, String en) throws BackupToolException
    {
        BackupConfigStorageBackend res;

        if( StringUtils.isEmpty(ur) )
        {
            throw new BackupToolException(String.format("Invalid 'storage url' cannot be empty"));
        }

        res = BackupConfigStorageBackend.from(ur,us,ps,en);

        return res;
    }

}
