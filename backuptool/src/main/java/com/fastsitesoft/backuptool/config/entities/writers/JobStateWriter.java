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

import com.fastsitesoft.backuptool.config.entities.JobArchiveSummary;
import com.fastsitesoft.backuptool.config.entities.JobState;
import com.fastsitesoft.backuptool.enums.BackupToolJobDisposition;
import com.fastsitesoft.backuptool.enums.BackupToolJobStatus;
import com.fastsitesoft.backuptool.enums.FSSBackupHashType;
import com.fastsitesoft.backuptool.enums.FSSBackupType;
import com.fastsitesoft.backuptool.utils.BackupToolException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
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
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Writes the current job state to a file.
 *
 * Created by kervin on 2015-08-13.
 */
public final class JobStateWriter
{
    private static final Logger log = LogManager.getLogger(JobStateWriter.class);

    public static void write( JobState js, Path stateFile, Path errorFile ) throws BackupToolException
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

        Element currElem = JobStateWriter.toElement(doc, js);
        doc.appendChild(currElem);

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
        StreamResult result = new StreamResult(stateFile.toFile());

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

        if( js.getErrors() != null && js.getErrors().size() > 0  )
        {
            // We have errors, so we need to write them to an errors file

            JobErrorWriter.write(js.getErrors(), errorFile);
        }
    }

    public static Pair<Path, Path> write(JobState js) throws BackupToolException
    {
        Path resStateFile = null;
        Path resErrFile = null;

        try
        {
            resStateFile = Files.createTempFile("JobState", ".xml.tmp");
            resErrFile = Files.createTempFile("JobState", ".xml.tmp");
        }
        catch( IOException ex )
        {
            String errMsg = String.format("Error creating temp file JobStateWriter");

            log.debug(errMsg, ex);
            throw new BackupToolException(errMsg, ex);
        }

        write(js, resStateFile, resErrFile);

        return Pair.of(resStateFile, resErrFile);
    }

    public static Element toElement( Document doc, JobState js ) throws BackupToolException
    {
        Element res = null;
        Element currElem = null;

        res = doc.createElement("job");

        // ID
        UUID currId = js.getJobId();
        if( currId == null )
        {
            throw new BackupToolException("Missing Id");
        }
        currElem = doc.createElement("jobId");
        currElem.appendChild(doc.createTextNode( currId.toString() ));
        res.appendChild(currElem);

        // path
        Path currPath = js.getPath();
        if( currPath == null )
        {
            throw new BackupToolException("Missing Path");
        }

        // We only need the last 2 directories, since it's relative
        // to the Job Set base directory
        currPath = currPath.subpath(currPath.getNameCount() - 2, currPath.getNameCount());

        currElem = doc.createElement("path");
        currElem.appendChild(doc.createTextNode( currPath.toString() ));
        res.appendChild(currElem);

        // start
        Instant currInst = js.getStart();
        if( currInst == null )
        {
            throw new BackupToolException("Missing Start");
        }
        currElem = doc.createElement("start");
        currElem.appendChild(doc.createTextNode( currInst.toString() ));
        res.appendChild(currElem);

        // end
        currInst = js.getEnd();
        if( currInst == null )
        {
            log.debug("Missing <end /> tag."); // throw new BackupToolException("Missing End");
        }
        else
        {
            currElem = doc.createElement("end");
            currElem.appendChild(doc.createTextNode(currInst.toString()));
            res.appendChild(currElem);
        }

        // type
        FSSBackupType currType = js.getType();
        if( currType == null )
        {
            throw new BackupToolException("Missing Type");
        }
        currElem = doc.createElement("type");
        currElem.appendChild(doc.createTextNode(
                StringUtils.lowerCase(currType.toString()) ));
        res.appendChild(currElem);

        // systemId
        String currStr = js.getSystemId();
        if( currStr == null )
        {
            throw new BackupToolException("Missing System ID");
        }
        currElem = doc.createElement("systemId");
        currElem.appendChild(doc.createTextNode( currStr ));
        res.appendChild(currElem);

        // status
        BackupToolJobStatus currStatus = js.getStatus();
        if( currStatus == null )
        {
            throw new BackupToolException("Missing Status");
        }
        currElem = doc.createElement("status");
        currElem.appendChild(doc.createTextNode(
                StringUtils.lowerCase(currStatus.toString()) ));
        res.appendChild(currElem);

        // Disposition
        BackupToolJobDisposition currDisp = js.getDisposition();
        if( currDisp == null )
        {
            throw new BackupToolException("Missing Disposition");
        }
        currElem = doc.createElement("disposition");
        currElem.appendChild(doc.createTextNode(
                StringUtils.lowerCase(currDisp.toString()) ));
        res.appendChild(currElem);

        // jobSetId
        currId = js.getJobSetId();
        if( currId == null )
        {
            throw new BackupToolException("Missing Job Set ID");
        }
        currElem = doc.createElement("jobSetId");
        currElem.appendChild(doc.createTextNode( currId.toString() ));
        res.appendChild(currElem);

        // errorsFilePath
        currPath = js.getJobErrorsFile();
        if( currPath == null )
        {
            log.debug("Missing <errorsFilePath /> tag.");
            // throw new BackupToolException("Missing Errors File Path");
        }
        else
        {
            currElem = doc.createElement("jobErrorsFile");
            currElem.appendChild(doc.createTextNode(currPath.toString()));
            res.appendChild(currElem);
        }

        // previousJobId
        currId = js.getPreviousJobId();
        if( currId == null )
        {
            log.debug("Missing <previousJobId /> tag."); // throw new BackupToolException("Missing Previous Job ID");
        }
        else
        {
            currElem = doc.createElement("previousJobId");
            currElem.appendChild(doc.createTextNode(currId.toString()));
            res.appendChild(currElem);
        }

        // hashType
        FSSBackupHashType currHash = js.getHashType();
        if( currHash == null )
        {
            throw new BackupToolException("Missing Hash");
        }
        currElem = doc.createElement("hash");
        currElem.appendChild(doc.createTextNode(
                StringUtils.lowerCase(currHash.toString()) ));
        res.appendChild(currElem);

        Element archives = doc.createElement("archives");

        List<JobArchiveSummary> currArchives = js.getArchives();
        if( currArchives == null )
        {
            throw new BackupToolException("Missing archives");
        }

        for( JobArchiveSummary archive : currArchives )
        {
            currElem = JobArchiveSummaryWriter.toElement(doc, archive);
            archives.appendChild(currElem);
        }

        res.appendChild(archives);

        return res;
    }
}
