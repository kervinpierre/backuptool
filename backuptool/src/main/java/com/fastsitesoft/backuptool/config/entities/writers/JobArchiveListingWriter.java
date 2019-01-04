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
import com.fastsitesoft.backuptool.enums.BackupToolFSItemType;
import com.fastsitesoft.backuptool.enums.BackupToolJobDisposition;
import com.fastsitesoft.backuptool.enums.BackupToolJobStatus;
import com.fastsitesoft.backuptool.enums.FSSBackupHashType;
import com.fastsitesoft.backuptool.utils.BackupToolException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.nio.file.Path;
import java.time.Instant;

/**
 * Write a Job Archive Listing object to disk or convert to DOM.
 *
 * Created by kervin on 2015-08-18.
 */
public final class JobArchiveListingWriter
{
    private static final Logger log = LogManager.getLogger(JobArchiveListingWriter.class);

    public static Element toElement( Document doc, JobArchiveListing js ) throws BackupToolException
    {
        Element res = null;
        Element currElem = null;

        res = doc.createElement("jobArchiveListing");

        // path
        Path currPath = js.getPath();
        if( currPath == null )
        {
            throw new BackupToolException("Missing Path");
        }
        currElem = doc.createElement("path");
        currElem.appendChild(doc.createTextNode( currPath.toString() ));
        res.appendChild(currElem);

        // hashValue
        String currStr = js.getHashValue();
        if( currStr == null )
        {
            ; // throw new BackupToolException("Missing Hash Value");
        }
        else
        {
            currElem = doc.createElement("hashValue");
            currElem.appendChild(doc.createTextNode(currStr));
            res.appendChild(currElem);
        }

        // lastModValue
        Instant currInst = js.getLastMod();
        if( currInst == null )
        {
            ;//throw new BackupToolException("Missing Last Modified Timestamp");
        }
        else
        {
            currElem = doc.createElement("lastModValue");
            currElem.appendChild(doc.createTextNode(currInst.toString()));
            res.appendChild(currElem);
        }

        // type
        BackupToolFSItemType currType = js.getType();
        if( currType == null )
        {
            throw new BackupToolException("Missing Type");
        }
        currElem = doc.createElement("type");
        currElem.appendChild(doc.createTextNode( currType.toString() ));
        res.appendChild(currElem);

        // status
        BackupToolJobStatus currStatus = js.getStatus();
        if( currStatus == null )
        {
            throw new BackupToolException("Missing Status");
        }
        currElem = doc.createElement("status");
        currElem.appendChild(doc.createTextNode( currStatus.toString() ));
        res.appendChild(currElem);

        // Disposition
        BackupToolJobDisposition currDisp = js.getDisposition();
        if( currDisp == null )
        {
            throw new BackupToolException("Missing Disposition");
        }
        currElem = doc.createElement("disposition");
        currElem.appendChild(doc.createTextNode( currDisp.toString() ));
        res.appendChild(currElem);

        // hashType
        FSSBackupHashType currHash = js.getHashType();
        if( currHash == null )
        {
            ; // throw new BackupToolException("Missing Hash");
        }
        else
        {
            currElem = doc.createElement("hash");
            currElem.appendChild(doc.createTextNode(currHash.toString()));
            res.appendChild(currElem);
        }

        return res;
    }
}
