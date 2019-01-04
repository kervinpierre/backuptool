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
package com.fastsitesoft.backuptool.config.parsers;

import com.fastsitesoft.backuptool.config.entities.BackupConfig;
import com.fastsitesoft.backuptool.config.entities.BackupConfigChunk;
import com.fastsitesoft.backuptool.config.entities.BackupConfigDirectory;
import com.fastsitesoft.backuptool.config.entities.BackupConfigFile;
import com.fastsitesoft.backuptool.config.entities.BackupConfigItem;
import com.fastsitesoft.backuptool.config.entities.BackupConfigStorageBackend;
import com.fastsitesoft.backuptool.enums.BackupToolFileFormats;
import com.fastsitesoft.backuptool.utils.BackupToolException;
import com.fastsitesoft.backuptool.utils.FSSConfigurationFile;
import com.fastsitesoft.backuptool.utils.FSSLSResourceResolver;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author kervin
 */
public class BackupConfigParser
{
    private static final Logger log 
                          = LogManager.getLogger(BackupConfigParser.class);

    public static BackupConfig readConfig(Document doc) throws BackupToolException
    {
        NodeList currElList;
        Element tempEl;
        String tempStr;

        BackupConfig res = null;
        Element currEl = doc.getDocumentElement();
        
        XPathFactory currXPathfactory = XPathFactory.newInstance();
        XPath currXPath = currXPathfactory.newXPath();

        String currSetNameStr = null; 
        String currSetTypeStr = null;
        List<BackupConfigDirectory> currDirList = new ArrayList<>();
        List<BackupConfigFile> currFileList = new ArrayList<>();
        String currStateFileStr = null;
        String currErrorFileStr = null;
        String currHoldingDirStr = null;
        String currArchiveNamePattern = null;
        String currArchiveNameComponent = null;
        String currArchiveNameTemplate = null;
        String currJobNamePattern = null;
        String currJobNameComponent = null;
        String currJobNameTemplate = null;
        String currCompressionStr = null;
        String currChunkSize = null;
        String currChunkSizeType = null;
        String currLogFileStr = null;
        String currLogEmailStr = null;
        Boolean currEmailOnCompletion = false;
        String currPriority = null;
        String currLockFileStr = null;
        BackupConfigStorageBackend currStorage = null;
        String currEncryptionKeyString = null;
        String currEncryptionTypeString = null;
        
        try
        {
            currSetNameStr = currXPath.compile("./setName").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }
        
        try
        {
            currSetTypeStr = currXPath.compile("./setType").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }
        
        try
        {
            currEncryptionTypeString = currXPath.compile("./encrypt/cipher").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }
        
        try
        {
            currEncryptionKeyString = currXPath.compile("./encrypt/key").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }
        
        tempEl = null;
        try
        {
            // Get the list of base directories
            currElList = (NodeList)currXPath.compile("./itemList/directory").evaluate(
                    doc.getDocumentElement(), XPathConstants.NODESET);

            if( currElList != null && currElList.getLength() > 0)
            {
                for(int i=0; i<currElList.getLength(); i++)
                {
                    tempEl = (Element)currElList.item(i);
                    currDirList.add(BackupConfigItem.from(tempEl));
                }
            }

            // Get the list of base files
            currElList = (NodeList)currXPath.compile("./itemList/file").evaluate(
                    doc.getDocumentElement(), XPathConstants.NODESET);
            if( currElList != null && currElList.getLength() > 0)
            {
                for(int i=0; i<currElList.getLength(); i++)
                {

                    tempEl = (Element)currElList.item(i);
                    currFileList.add(BackupConfigItem.from(tempEl));
                }
            }
        }
        catch (XPathExpressionException ex)
        {
            String errMsg = "Failed processing directory and/or file list from configuration file.";

            log.debug(errMsg, ex);
            throw new BackupToolException(errMsg, ex);
        }
        
        try
        {
            currStateFileStr = currXPath.compile("./stateFile").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            currErrorFileStr = currXPath.compile("./errorFile").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            currHoldingDirStr = currXPath.compile("./holdingDirectoryPath").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            tempStr = currXPath.compile("./archiveNameRegex").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                currArchiveNamePattern = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            tempStr = currXPath.compile("./archiveNameComponent").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                currArchiveNameComponent = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            tempStr = currXPath.compile("./archiveNameTemplate").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                currArchiveNameTemplate = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            tempStr = currXPath.compile("./jobNameRegex").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                currJobNamePattern = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            tempStr = currXPath.compile("./jobNameComponent").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                currJobNameComponent = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            tempStr = currXPath.compile("./jobNameTemplate").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                currJobNameTemplate = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            currCompressionStr = currXPath.compile("./compressionScheme").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }
        
        try
        {
            tempEl = (Element)currXPath.compile("./chunk").evaluate(doc.getDocumentElement(), XPathConstants.NODE);
            if( tempEl != null )
            {
                BackupConfigChunk currChunkObj = BackupConfigChunk.from(tempEl);
                currChunkSize = Long.toString(currChunkObj.getSize());
                currChunkSizeType = currChunkObj.getSizeType().toString();
            }
        }
        catch (Exception ex)
        {
            log.debug("Error parsing 'chunk' section from the configuration file.", ex);
        }
        
        try
        {
            currLogFileStr = currXPath.compile("./logFile").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }
        
        try
        {
            String currEmailOnCompletionStr = currXPath.compile("./emailOnCompletion").evaluate(currEl);
            currEmailOnCompletion = Boolean.parseBoolean(currEmailOnCompletionStr);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }
        
        try
        {
            currPriority = currXPath.compile("./priority").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }
        
        try
        {
            currLockFileStr = currXPath.compile("./lockFilePath").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }


        List<String> emailContacts = new ArrayList<>();
        try
        {
            currLogEmailStr = currXPath.compile("./logEmail").evaluate(currEl);
            emailContacts.add(currLogEmailStr);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            tempEl = (Element)currXPath.compile("./storageType")
                    .evaluate(doc.getDocumentElement(), XPathConstants.NODE);
            currStorage = BackupConfigStorageBackend.from(tempEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        res = BackupConfig.from(
                null,
                currSetNameStr,
                currPriority,
                null, // useChecksum,
                null, // useModifiedDate,
                null,// restore,
                null, // backup,
                currEmailOnCompletion,
                null, // dryRun,
                null, // ignoreLock
                null, // runAsService,
                null, // displayUsage,
                null, // displayVersion,
                null, // preserveOwnership,
                null, // preservePermissions,
                null, // noClobber,
                null, // backupDescribe,
                null, // backupStatus,
                currHoldingDirStr,
                null, // backupReportPath,
                null, // restoreDestination,
                null,  // outputFile,
                currLockFileStr,
                currStateFileStr,
                currErrorFileStr,
                emailContacts,
                currDirList,
                currFileList,
                null,
                null, //backupId,
                currSetTypeStr,
                null,
                currCompressionStr,
                currEncryptionTypeString,
                currEncryptionKeyString,
                currChunkSize,
                currChunkSizeType,
                currArchiveNamePattern, // archiveNamePattern
                currArchiveNameTemplate, // archiveNameComp
                currArchiveNameComponent, // archiveNameTemplate
                currJobNamePattern, // jobNamePattern
                currJobNameTemplate, // jobNameComp
                currJobNameComponent, // jobNameTemplate
                null, // verbosity,
                currStorage,
                null // usageConfig
        );

        return res;
    }

}
