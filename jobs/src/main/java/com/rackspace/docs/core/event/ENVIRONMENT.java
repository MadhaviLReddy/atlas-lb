//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.07.31 at 01:53:34 AM CDT 
//


package com.rackspace.docs.core.event;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ENVIRONMENT.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ENVIRONMENT"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="PROD"/&gt;
 *     &lt;enumeration value="PREPROD"/&gt;
 *     &lt;enumeration value="STAGE"/&gt;
 *     &lt;enumeration value="QA"/&gt;
 *     &lt;enumeration value="DEV"/&gt;
 *     &lt;enumeration value="UAT"/&gt;
 *     &lt;enumeration value="LOCAL"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ENVIRONMENT")
@XmlEnum
public enum ENVIRONMENT {

    PROD,
    PREPROD,
    STAGE,
    QA,
    DEV,
    UAT,
    LOCAL;

    public String value() {
        return name();
    }

    public static ENVIRONMENT fromValue(String v) {
        return valueOf(v);
    }

}
