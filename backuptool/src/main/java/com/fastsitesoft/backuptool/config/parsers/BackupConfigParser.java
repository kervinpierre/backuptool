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
package com.fastsitesoft.backuptool.config.parsers;

import com.fastsitesoft.backuptool.config.builders.BackupConfigBuilder;
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

        Element currEl = doc.getDocumentElement();
        
        XPathFactory currXPathfactory = XPathFactory.newInstance();
        XPath currXPath = currXPathfactory.newXPath();

        BackupConfigBuilder bcbuilder = BackupConfigBuilder.from();

        try
        {
            tempStr = currXPath.compile("./setName").evaluate(currEl);
            bcbuilder.setSetName(tempStr);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }
        
        try
        {
            tempStr = currXPath.compile("./setType").evaluate(currEl);
            bcbuilder.setSetType(tempStr);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }
        
        try
        {
            tempStr = currXPath.compile("./encrypt/cipher").evaluate(currEl);
            bcbuilder.setEncryptionCipher(tempStr);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }
        
        try
        {
            tempStr = currXPath.compile("./encrypt/key").evaluate(currEl);
            bcbuilder.setEncryptionKey(tempStr);
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
                    bcbuilder.setDirList((BackupConfigDirectory)BackupConfigItem.from(tempEl));
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
                    bcbuilder.setFileList((BackupConfigFile)BackupConfigItem.from(tempEl));
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
            tempStr = currXPath.compile("./stateFile").evaluate(currEl);
            bcbuilder.setStateFileName(tempStr);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            tempStr = currXPath.compile("./errorFile").evaluate(currEl);
            bcbuilder.setErrorFileName(tempStr);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            tempStr = currXPath.compile("./holdingDirectoryPath").evaluate(currEl);
            bcbuilder.setHoldingDirectory(tempStr);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            tempStr = currXPath.compile("./archiveNameRegex").evaluate(currEl);
            bcbuilder.setArchiveFileNamePattern(tempStr);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            tempStr = currXPath.compile("./archiveNameComponent").evaluate(currEl);
            bcbuilder.setArchiveFileNameComponent(tempStr);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            tempStr = currXPath.compile("./archiveNameTemplate").evaluate(currEl);
            bcbuilder.setArchiveFileNameTemplate(tempStr);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            tempStr = currXPath.compile("./jobNameRegex").evaluate(currEl);
            bcbuilder.setJobFileNamePattern(tempStr);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            tempStr = currXPath.compile("./jobNameComponent").evaluate(currEl);
            bcbuilder.setJobFileNameComponent(tempStr);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            tempStr = currXPath.compile("./jobNameTemplate").evaluate(currEl);
            bcbuilder.setJobFileNameTemplate(tempStr);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            tempStr = currXPath.compile("./compressionScheme").evaluate(currEl);
            bcbuilder.setCompression(tempStr);
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
                bcbuilder.setChunk(currChunkObj);
            }
        }
        catch (Exception ex)
        {
            log.debug("Error parsing 'chunk' section from the configuration file.", ex);
        }
        
//        try
//        {
//            currLogFileStr = currXPath.compile("./logFile").evaluate(currEl);
//        }
//        catch (XPathExpressionException ex)
//        {
//            log.debug("", ex);
//        }
        
        try
        {
            tempStr = currXPath.compile("./emailOnCompletion").evaluate(currEl);
            bcbuilder.setEmailOnCompletion(tempStr);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }
        
        try
        {
            tempStr = currXPath.compile("./priority").evaluate(currEl);
            bcbuilder.setPriority(tempStr);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }
        
        try
        {
            tempStr = currXPath.compile("./lockFilePath").evaluate(currEl);
            bcbuilder.setLockFilePath(tempStr);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }


        List<String> emailContacts = new ArrayList<>();
        try
        {
            tempStr = currXPath.compile("./logEmail").evaluate(currEl);
            bcbuilder.setEmailContacts(tempStr);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            tempEl = (Element)currXPath.compile("./storageType")
                    .evaluate(doc.getDocumentElement(), XPathConstants.NODE);

            bcbuilder.setStorageBackend(BackupConfigStorageBackend.from(tempEl));
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        BackupConfig res = bcbuilder.toConfig(null);

        return res;
    }

}
