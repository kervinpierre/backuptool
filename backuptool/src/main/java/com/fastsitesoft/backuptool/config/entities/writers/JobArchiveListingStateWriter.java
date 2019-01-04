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

import com.fastsitesoft.backuptool.config.entities.JobArchiveListing;
import com.fastsitesoft.backuptool.config.entities.JobArchiveListingState;
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
import java.nio.file.Path;
import java.util.List;

/**
 * Write a Job Archive Listing document to disk.
 *
 * Created by kervin on 2015-08-18.
 */
public final class JobArchiveListingStateWriter
{
    private static final Logger log = LogManager.getLogger(JobArchiveListingStateWriter.class);

    public static void write(JobArchiveListingState js, Path file) throws BackupToolException
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
        Element rootElement = doc.createElement("jobArchiveListing");
        doc.appendChild(rootElement);

        Element listings = doc.createElement("listings");
        rootElement.appendChild(listings);

        List<JobArchiveListing> currListings = js.getListing();
        for( JobArchiveListing j : currListings )
        {
            Element currElem = JobArchiveListingWriter.toElement(doc, j);
            listings.appendChild(currElem);
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

    public static void write(JobArchiveListingState js) throws BackupToolException
    {
        Path out = js.getPath();

        if( !out.isAbsolute() )
        {
            log.debug(String.format("Writing listing to a relative path '%s'", out));
        }

        write(js, out);
    }
}
