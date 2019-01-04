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

import com.fastsitesoft.backuptool.config.entities.JobSetState;
import com.fastsitesoft.backuptool.config.entities.JobState;
import com.fastsitesoft.backuptool.utils.BackupToolException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Write JobSet objects to disk or convert to an Element.
 *
 * Created by kervin on 2015-08-12.
 */
public final class JobSetStateWriter
{
    private static final Logger log = LogManager.getLogger(JobSetStateWriter.class);

    public static Element toElement( Document doc, JobSetState js ) throws BackupToolException
    {
        Element res = null;
        Element currElem = null;

        res = doc.createElement("jobSetState");

        // ID
        UUID currId = js.getJobSetId();
        if( currId == null )
        {
            throw new BackupToolException("Missing Id");
        }
        currElem = doc.createElement("jobSetId");
        currElem.appendChild(doc.createTextNode( currId.toString() ));
        res.appendChild(currElem);

        // LAST JOB ID
        currId = js.getLastJobId();
        if( currId == null )
        {
            ;
            //throw new BackupToolException("Missing Last Job Id");
        }
        else
        {
            currElem = doc.createElement("lastJobId");
            currElem.appendChild(doc.createTextNode(currId.toString()));
            res.appendChild(currElem);
        }

        // LAST SUCCESSFUL JOB ID
        currId = js.getLastSuccessfulJobId();
        if( currId == null )
        {
            ;
            //throw new BackupToolException("Missing Last Successful Job Id");
        }
        else
        {
            currElem = doc.createElement("lastSuccessfulJobId");
            currElem.appendChild(doc.createTextNode(currId.toString()));
            res.appendChild(currElem);
        }

        // PATH
        Path currPath = js.getPath();
        if( currPath == null )
        {
            ;
            //throw new BackupToolException("Missing Last Successful Job Id");
        }
        else
        {
            currElem = doc.createElement("path");
            currElem.appendChild(doc.createTextNode(currPath.getFileName().toString()));
            res.appendChild(currElem);
        }

        // JOBS
        Element jobs = doc.createElement("jobs");
        res.appendChild(jobs);

        Map<UUID, JobState> currJobs = js.getJobs();
        for( JobState j : currJobs.values() )
        {
            currElem = JobStateWriter.toElement(doc, j);
            jobs.appendChild(currElem);
        }

        return res;
    }

    public static void write(JobSetState js, Path file) throws BackupToolException
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
        Element rootElement = toElement(doc, js);
        doc.appendChild(rootElement);

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

    public static Path write(JobSetState js) throws BackupToolException
    {
        Path res = null;

        try
        {
            res = Files.createTempFile("JobSetState", ".xml.tmp");
        }
        catch( IOException ex )
        {
            String errMsg = String.format("Error creating temp file JobSetStateWriter");

            log.debug(errMsg, ex);
            throw new BackupToolException(errMsg, ex);
        }

        write(js, res);

        return res;
    }
}
