//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.07.31 at 01:53:34 AM CDT 
//


package com.rackspace.docs.usage.lbaas;

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
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;&lt;html:p xmlns:html="http://www.w3.org/1999/xhtml" xmlns="http://www.w3.org/2001/XMLSchema" xmlns:p="http://docs.rackspace.com/usage/lbaas" xmlns:saxon="http://saxon.sf.net/" xmlns:usage="http://docs.rackspace.com/core/usage" xmlns:vc="http://www.w3.org/2007/XMLSchema-versioning" xmlns:xerces="http://xerces.apache.org" xmlns:xsd="http://www.w3.org/2001/XMLSchema"&gt;Lbaas load balancer usage fields.&lt;/html:p&gt;
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
 *       &lt;attribute name="resourceType" use="required" type="{http://docs.rackspace.com/usage/lbaas}ResourceTypes" /&gt;
 *       &lt;attribute name="avgConcurrentConnections" use="required" type="{http://docs.rackspace.com/usage/lbaas}avgConcurrentConnectionsType" /&gt;
 *       &lt;attribute name="avgConcurrentConnectionsSsl" use="required" type="{http://docs.rackspace.com/usage/lbaas}avgConcurrentConnectionsSslType" /&gt;
 *       &lt;attribute name="bandWidthIn" use="required" type="{http://docs.rackspace.com/usage/lbaas}bandWidthInType" /&gt;
 *       &lt;attribute name="bandWidthOut" use="required" type="{http://docs.rackspace.com/usage/lbaas}bandWidthOutType" /&gt;
 *       &lt;attribute name="bandWidthInSsl" use="required" type="{http://docs.rackspace.com/usage/lbaas}bandWidthInSslType" /&gt;
 *       &lt;attribute name="bandWidthOutSsl" use="required" type="{http://docs.rackspace.com/usage/lbaas}bandWidthOutSslType" /&gt;
 *       &lt;attribute name="numPolls" use="required" type="{http://docs.rackspace.com/usage/lbaas}numPollsType" /&gt;
 *       &lt;attribute name="numVips" use="required" type="{http://docs.rackspace.com/usage/lbaas}numVipsType" /&gt;
 *       &lt;attribute name="vipType" use="required" type="{http://docs.rackspace.com/usage/lbaas}vipTypeEnum" /&gt;
 *       &lt;attribute name="sslMode" use="required" type="{http://docs.rackspace.com/usage/lbaas}sslModeEnum" /&gt;
 *       &lt;attribute name="status" use="required" type="{http://docs.rackspace.com/usage/lbaas}statusEnum" /&gt;
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
    @XmlAttribute(name = "avgConcurrentConnections", required = true)
    protected double avgConcurrentConnections;
    @XmlAttribute(name = "avgConcurrentConnectionsSsl", required = true)
    protected double avgConcurrentConnectionsSsl;
    @XmlAttribute(name = "bandWidthIn", required = true)
    protected long bandWidthIn;
    @XmlAttribute(name = "bandWidthOut", required = true)
    protected long bandWidthOut;
    @XmlAttribute(name = "bandWidthInSsl", required = true)
    protected long bandWidthInSsl;
    @XmlAttribute(name = "bandWidthOutSsl", required = true)
    protected long bandWidthOutSsl;
    @XmlAttribute(name = "numPolls", required = true)
    protected int numPolls;
    @XmlAttribute(name = "numVips", required = true)
    protected int numVips;
    @XmlAttribute(name = "vipType", required = true)
    protected VipTypeEnum vipType;
    @XmlAttribute(name = "sslMode", required = true)
    protected SslModeEnum sslMode;
    @XmlAttribute(name = "status", required = true)
    protected StatusEnum status;

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

    /**
     * Gets the value of the avgConcurrentConnections property.
     * 
     */
    public double getAvgConcurrentConnections() {
        return avgConcurrentConnections;
    }

    /**
     * Sets the value of the avgConcurrentConnections property.
     * 
     */
    public void setAvgConcurrentConnections(double value) {
        this.avgConcurrentConnections = value;
    }

    /**
     * Gets the value of the avgConcurrentConnectionsSsl property.
     * 
     */
    public double getAvgConcurrentConnectionsSsl() {
        return avgConcurrentConnectionsSsl;
    }

    /**
     * Sets the value of the avgConcurrentConnectionsSsl property.
     * 
     */
    public void setAvgConcurrentConnectionsSsl(double value) {
        this.avgConcurrentConnectionsSsl = value;
    }

    /**
     * Gets the value of the bandWidthIn property.
     * 
     */
    public long getBandWidthIn() {
        return bandWidthIn;
    }

    /**
     * Sets the value of the bandWidthIn property.
     * 
     */
    public void setBandWidthIn(long value) {
        this.bandWidthIn = value;
    }

    /**
     * Gets the value of the bandWidthOut property.
     * 
     */
    public long getBandWidthOut() {
        return bandWidthOut;
    }

    /**
     * Sets the value of the bandWidthOut property.
     * 
     */
    public void setBandWidthOut(long value) {
        this.bandWidthOut = value;
    }

    /**
     * Gets the value of the bandWidthInSsl property.
     * 
     */
    public long getBandWidthInSsl() {
        return bandWidthInSsl;
    }

    /**
     * Sets the value of the bandWidthInSsl property.
     * 
     */
    public void setBandWidthInSsl(long value) {
        this.bandWidthInSsl = value;
    }

    /**
     * Gets the value of the bandWidthOutSsl property.
     * 
     */
    public long getBandWidthOutSsl() {
        return bandWidthOutSsl;
    }

    /**
     * Sets the value of the bandWidthOutSsl property.
     * 
     */
    public void setBandWidthOutSsl(long value) {
        this.bandWidthOutSsl = value;
    }

    /**
     * Gets the value of the numPolls property.
     * 
     */
    public int getNumPolls() {
        return numPolls;
    }

    /**
     * Sets the value of the numPolls property.
     * 
     */
    public void setNumPolls(int value) {
        this.numPolls = value;
    }

    /**
     * Gets the value of the numVips property.
     * 
     */
    public int getNumVips() {
        return numVips;
    }

    /**
     * Sets the value of the numVips property.
     * 
     */
    public void setNumVips(int value) {
        this.numVips = value;
    }

    /**
     * Gets the value of the vipType property.
     * 
     * @return
     *     possible object is
     *     {@link VipTypeEnum }
     *     
     */
    public VipTypeEnum getVipType() {
        return vipType;
    }

    /**
     * Sets the value of the vipType property.
     * 
     * @param value
     *     allowed object is
     *     {@link VipTypeEnum }
     *     
     */
    public void setVipType(VipTypeEnum value) {
        this.vipType = value;
    }

    /**
     * Gets the value of the sslMode property.
     * 
     * @return
     *     possible object is
     *     {@link SslModeEnum }
     *     
     */
    public SslModeEnum getSslMode() {
        return sslMode;
    }

    /**
     * Sets the value of the sslMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link SslModeEnum }
     *     
     */
    public void setSslMode(SslModeEnum value) {
        this.sslMode = value;
    }

    /**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link StatusEnum }
     *     
     */
    public StatusEnum getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link StatusEnum }
     *     
     */
    public void setStatus(StatusEnum value) {
        this.status = value;
    }

}
