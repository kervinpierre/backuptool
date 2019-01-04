/*
 * SLU Dev Inc. CONFIDENTIAL
 * DO NOT COPY
 *
 * Copyright (c) [2012] - [2015] SLU Dev Inc. <info@sludev.com>
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

package com.fastsitesoft.backuptool.config.entities.writers;

import com.fastsitesoft.backuptool.config.entities.JobError;
import com.fastsitesoft.backuptool.enums.BackupToolJobDisposition;
import com.fastsitesoft.backuptool.utils.BackupToolException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Created by kervin on 2015-08-21.
 */
public final class JobErrorWriter
{
    private static final Logger log = LogManager.getLogger(JobErrorWriter.class);

    public static void write( List<JobError> errs, Path file ) throws BackupToolException
    {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;

        try
        {
            docBuilder = docFactory.newDocumentBuilder();
        }
        catch( ParserConfigurationException ex )
        {
            String errMsg = "Error creating document builder.";

            log.debug(errMsg, ex);
            throw new BackupToolException(errMsg, ex);
        }

        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("errors");
        doc.appendChild(rootElement);

        for( JobError e : errs )
        {
            Element currElem = JobErrorWriter.toElement(doc, e);
            rootElement.appendChild(currElem);
        }

        // write the content into xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = null;
        try
        {
            transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        }
        catch( TransformerConfigurationException ex )
        {
            String errMsg = "Error creating transformer";

            log.debug(errMsg, ex);
            throw new BackupToolException(errMsg, ex);
        }

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(file.toFile());

        try
        {
            transformer.transform(source, result);
        }
        catch( TransformerException | NullPointerException ex )
        {
            String errMsg = "Error writing '%s'";

            log.debug(errMsg, ex);
            throw new BackupToolException(errMsg, ex);
        }

    }


    public static Element toElement( Document doc, JobError je ) throws BackupToolException
    {
        Element res = null;
        Element currElem = null;

        res = doc.createElement("error");

        // Job ID
        UUID currId = je.getJobId();
        if( currId == null )
        {
            throw new BackupToolException("Missing Job Id");
        }
        currElem = doc.createElement("jobId");
        currElem.appendChild(doc.createTextNode( currId.toString() ));
        res.appendChild(currElem);

        // Error ID
        currId = je.getErrorId();
        if( currId == null )
        {
            throw new BackupToolException("Missing Error Id");
        }
        currElem = doc.createElement("errorId");
        currElem.appendChild(doc.createTextNode( currId.toString() ));
        res.appendChild(currElem);

        // archive path
        Path currPath = je.getArchivePath();
        if( currPath == null )
        {
            ; //throw new BackupToolException("Missing Archive Path");
        }
        else
        {
            currElem = doc.createElement("archivePath");
            currElem.appendChild(doc.createTextNode(currPath.toString()));
            res.appendChild(currElem);
        }

        // Disposition
        BackupToolJobDisposition currDisp = je.getDisposition();
        if( currDisp == null )
        {
            throw new BackupToolException("Missing Disposition");
        }
        currElem = doc.createElement("disposition");
        currElem.appendChild(doc.createTextNode( currDisp.toString() ));
        res.appendChild(currElem);

        // Error Code
        Integer currInt = je.getErrorCode();
        if( currInt == null )
        {
            ;//throw new BackupToolException("Missing Error Code");
        }
        else
        {
            currElem = doc.createElement("errorCode");
            currElem.appendChild(doc.createTextNode(currInt.toString()));
            res.appendChild(currElem);
        }

        // Error Code Type
        Type currType = je.getErrorCodeType();
        if( currType == null )
        {
            ; // throw new BackupToolException("Missing Error Code Type");
        }
        else
        {
            currElem = doc.createElement("errorCodeType");
            currElem.appendChild(doc.createTextNode(currType.toString()));
            res.appendChild(currElem);
        }

        // Item Path
        currPath = je.getItemPath();
        if( currPath == null )
        {
            throw new BackupToolException("Missing Item Path");
        }
        currElem = doc.createElement("itemPath");
        currElem.appendChild(doc.createTextNode( currPath.toString() ));
        res.appendChild(currElem);

        // Text
        String currStr = je.getText();
        if( currStr == null )
        {
            throw new BackupToolException("Missing Text");
        }
        currElem = doc.createElement("text");
        currElem.appendChild(doc.createCDATASection( currStr ));
        res.appendChild(currElem);

        // Summary
        currStr = je.getSummary();
        if( currStr == null )
        {
            ; //throw new BackupToolException("Missing Summary");
        }
        else
        {
            currElem = doc.createElement("summary");
            currElem.appendChild(doc.createCDATASection(currStr));
            res.appendChild(currElem);
        }

        // timestamp
        Instant currInst = je.getTimestamp();
        if( currInst == null )
        {
            ; //throw new BackupToolException("Missing Timestamp");
        }
        else
        {
            currElem = doc.createElement("timestamp");
            currElem.appendChild(doc.createTextNode(currInst.toString()));
            res.appendChild(currElem);
        }

        // Exception
        Exception currEx = je.getException();
        if( currEx == null )
        {
            ; //throw new BackupToolException("Missing Exception");
        }
        else
        {
            currElem = doc.createElement("exception");
            currElem.appendChild(doc.createCDATASection(currEx.toString()));
            res.appendChild(currElem);
        }

        return res;
    }
}
