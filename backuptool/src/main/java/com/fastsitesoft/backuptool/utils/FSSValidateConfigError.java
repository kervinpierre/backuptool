/*
 *  SLU Dev Inc. CONFIDENTIAL
 *  DO NOT COPY
 * 
 *  Copyright (c) [2012] - [2015] SLU Dev Inc. <info@sludev.com>
 *  All Rights Reserved.
 * 
 *  NOTICE:  All information contained herein is, and remains
 *  the property of SLU Dev Inc. and its suppliers,
 *  if any.  The intellectual and technical concepts contained
 *  herein are proprietary to SLU Dev Inc. and its suppliers and
 *  may be covered by U.S. and Foreign Patents, patents in process,
 *  and are protected by trade secret or copyright law.
 *  Dissemination of this information or reproduction of this material
 *  is strictly forbidden unless prior written permission is obtained
 *  from SLU Dev Inc.
 */
package com.fastsitesoft.backuptool.utils;

import com.fastsitesoft.backuptool.enums.FSSConfigMembersEnum;
import com.fastsitesoft.backuptool.enums.FSSValidateResultCodeEnum;
import com.fastsitesoft.backuptool.enums.FSSValidateResultErrorEnum;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;

/**
 * Represents a single validation error.
 * 
 * @author Kervin Pierre <info@sludev.com>
 */
public final class FSSValidateConfigError
{
    private static final org.apache.logging.log4j.Logger log 
                                = LogManager.getLogger(FSSValidateConfigError.class);
    
    private final FSSValidateResultCodeEnum status;
    private final String statusDescription;
    private final Object objectValue;
    private final FSSConfigMembersEnum member;
    private final List<FSSValidateResultErrorEnum> errors;

    public FSSValidateResultCodeEnum getStatus()
    {
        return status;
    }

    public String getStatusDescription()
    {
        return statusDescription;
    }

    public Object getObjectValue()
    {
        return objectValue;
    }

    public FSSConfigMembersEnum getMember()
    {
        return member;
    }

    public List<FSSValidateResultErrorEnum> getErrors()
    {
        return errors;
    }

    /**
     *
     * @param s The object's result code after validation.
     * @param desc Textual description of the status
     * @param val The object value.
     * @param m The member being validated
     * @param e
     */
    public FSSValidateConfigError(final FSSValidateResultCodeEnum s,
                                  final String desc,
                                  final Object val,
                                  final FSSConfigMembersEnum m,
                                  final List<FSSValidateResultErrorEnum> e)
    {
        member = m;
        statusDescription = desc;
        objectValue = val;
        status = s;
        errors = e;
    }

    public FSSValidateConfigError(final FSSValidateResultCodeEnum s,
                                  final String desc,
                                  final Object val,
                                  final FSSConfigMembersEnum m)
    {
        this(s, desc, val, m, new ArrayList<>());
    }

    public FSSValidateConfigError(final FSSValidateResultCodeEnum s,
                                  final String desc,
                                  final Object val,
                                  final FSSConfigMembersEnum m,
                                  final FSSValidateResultErrorEnum e)
    {
        this(s, desc, val, m);

        errors.add(e);
    }
    
    /**
     * Return the String representation of this configuration objection
     * 
     * @return
     */
    @Override
    public String toString()
    {
        return toString(0);
    }
    
    public String toString(int indent)
    {
        String res;
        String indentStr = "";
        
        if( indent > 0 )
        {
            indentStr = StringUtils.repeat(" ", indent);
        }
        
        StringBuilder sb = new StringBuilder();
        
        sb.append( String.format("%sFSSVALIDATEERROR\n", indentStr) );
        sb.append(String.format("%s  STATUS           : '%s'\n", indentStr, status) );
        sb.append(String.format("%s  STATUS DESC      : '%s'\n", indentStr, statusDescription) );
        sb.append(String.format("%s  MEMBER           : '%s'\n", indentStr, member) );
        
        for( FSSValidateResultErrorEnum err : errors )
        {
            sb.append( String.format("%s ERROR : %s\n", indentStr, err) );
        }
        
        res = sb.toString();
        
        return res;
    }
}
