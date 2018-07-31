//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.07.31 at 01:53:34 AM CDT 
//


package com.rackspace.docs.event.lbaas.delete;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 *             
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;&lt;html:p xmlns:html="http://www.w3.org/1999/xhtml" xmlns="http://www.w3.org/2001/XMLSchema" xmlns:p="http://docs.rackspace.com/event/lbaas/delete" xmlns:saxon="http://saxon.sf.net/" xmlns:usage="http://docs.rackspace.com/core/usage" xmlns:vc="http://www.w3.org/2007/XMLSchema-versioning" xmlns:xerces="http://xerces.apache.org" xmlns:xsd="http://www.w3.org/2001/XMLSchema"&gt;Delete event for lbaas loadbalancers.&lt;/html:p&gt;
 * </pre>
 * 
 *          
 * 
 * <p>Java class for CloudLoadBalancersType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CloudLoadBalancersType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="version" use="required" type="{http://www.w3.org/2001/XMLSchema}string" fixed="1" /&gt;
 *       &lt;attribute name="serviceCode" use="required" type="{http://www.w3.org/2001/XMLSchema}Name" fixed="CloudLoadBalancers" /&gt;
 *       &lt;attribute name="resourceType" use="required" type="{http://docs.rackspace.com/event/lbaas/delete}ResourceTypes" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CloudLoadBalancersType")
public class CloudLoadBalancersType {

    @XmlAttribute(name = "version", required = true)
    protected String version;
    @XmlAttribute(name = "serviceCode", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "Name")
    protected String serviceCode;
    @XmlAttribute(name = "resourceType", required = true)
    protected ResourceTypes resourceType;

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        if (version == null) {
            return "1";
        } else {
            return version;
        }
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

    /**
     * Gets the value of the serviceCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getServiceCode() {
        if (serviceCode == null) {
            return "CloudLoadBalancers";
        } else {
            return serviceCode;
        }
    }

    /**
     * Sets the value of the serviceCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setServiceCode(String value) {
        this.serviceCode = value;
    }

    /**
     * Gets the value of the resourceType property.
     * 
     * @return
     *     possible object is
     *     {@link ResourceTypes }
     *     
     */
    public ResourceTypes getResourceType() {
        return resourceType;
    }

    /**
     * Sets the value of the resourceType property.
     * 
     * @param value
     *     allowed object is
     *     {@link ResourceTypes }
     *     
     */
    public void setResourceType(ResourceTypes value) {
        this.resourceType = value;
    }

}
