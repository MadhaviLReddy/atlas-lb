//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.07.31 at 01:53:34 AM CDT 
//


package com.rackspace.docs.core.event;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;&lt;html:p xmlns:html="http://www.w3.org/1999/xhtml" xmlns="http://www.w3.org/2001/XMLSchema" xmlns:event="http://docs.rackspace.com/core/event" xmlns:saxon="http://saxon.sf.net/" xmlns:vc="http://www.w3.org/2007/XMLSchema-versioning" xmlns:xerces="http://xerces.apache.org" xmlns:xsd="http://www.w3.org/2001/XMLSchema"&gt;
 *                     This is the base type for all event records.  The
 *                     only thing that is required is a version
 *                     attribute.
 *                 &lt;/html:p&gt;
 * </pre>
 * 
 *             
 * 
 * <p>Java class for EventBase complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EventBase"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="version" use="required" type="{http://docs.rackspace.com/core/event}NonEmptyString" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EventBase")
@XmlSeeAlso({
    EventV1 .class
})
public abstract class EventBase {

    @XmlAttribute(name = "version", required = true)
    protected String version;

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

}
