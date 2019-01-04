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
import com.fastsitesoft.backuptool.enums.BackupToolCompressionType;
import com.fastsitesoft.backuptool.enums.BackupToolEncryptionType;
import com.fastsitesoft.backuptool.utils.BackupToolException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.nio.file.Path;
import java.time.Instant;

/**
 * Help write a Job Archive Summary to disk.
 *
 * Created by kervin on 2015-08-13.
 */
public final class JobArchiveSummaryWriter
{
    private static final Logger log = LogManager.getLogger(JobArchiveSummaryWriter.class);

    public static Element toElement( Document doc, JobArchiveSummary ar) throws BackupToolException
    {
        Element res;
        Element currElem;

        res = doc.createElement("archiveSummary");

        // path
        Path currPath = ar.getPath();
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

        // listingFile
        currPath = ar.getListingFile();
        if( currPath == null )
        {
            throw new BackupToolException("Missing Listing File");
        }

        // We only need the last 2 directories, since it's relative
        // to the Job Set base directory
        if( currPath.getNameCount() < 2 )
        {
            log.debug(String.format("Listing File path is shorter than expected."));
        }
        else
        {
            currPath = currPath.subpath(currPath.getNameCount() - 2, currPath.getNameCount());
        }

        currElem = doc.createElement("listingFile");
        currElem.appendChild(doc.createTextNode( currPath.toString() ));
        res.appendChild(currElem);

        // orderId
        Integer currOrderId = ar.getOrderId();
        if( currOrderId == null )
        {
            ;
        }
        else
        {
            currElem = doc.createElement("orderId");
            currElem.appendChild(doc.createTextNode(currOrderId.toString()));
            res.appendChild(currElem);
        }

        // start
        Instant currInst = ar.getCreateStart();
        if( currInst == null )
        {
            throw new BackupToolException("Missing Start");
        }
        currElem = doc.createElement("createStart");
        currElem.appendChild(doc.createTextNode( currInst.toString() ));
        res.appendChild(currElem);

        // end
        currInst = ar.getCreateEnd();
        if( currInst == null )
        {
            ;//throw new BackupToolException("Missing End");
        }
        else
        {
            currElem = doc.createElement("createEnd");
            currElem.appendChild(doc.createTextNode(currInst.toString()));
            res.appendChild(currElem);
        }

        // compressionType
        BackupToolCompressionType compressType = ar.getCompressionType();
        if( compressType == null )
        {
            compressType = BackupToolCompressionType.NONE;
        }
        currElem = doc.createElement("compressionType");
        currElem.appendChild(doc.createTextNode(compressType.toString()));
        res.appendChild(currElem);

        // encryptionType
        BackupToolEncryptionType encryptionType = ar.getEncryptionType();
        if( encryptionType == null )
        {
            encryptionType = BackupToolEncryptionType.NONE;
        }
        currElem = doc.createElement("encryptionType");
        currElem.appendChild(doc.createTextNode( encryptionType.toString() ));
        res.appendChild(currElem);

        return res;
    }
}
