//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.11.14 at 04:24:27 PM PST 
//


package com.intuit.ipp.data;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.lang.Equals;
import org.jvnet.jaxb2_commons.lang.EqualsStrategy;
import org.jvnet.jaxb2_commons.lang.HashCode;
import org.jvnet.jaxb2_commons.lang.HashCodeStrategy;
import org.jvnet.jaxb2_commons.lang.JAXBEqualsStrategy;
import org.jvnet.jaxb2_commons.lang.JAXBHashCodeStrategy;
import org.jvnet.jaxb2_commons.locator.ObjectLocator;
import org.jvnet.jaxb2_commons.locator.util.LocatorUtils;


/**
 * Defines Product and Services Prefs details
 * 			
 * 
 * <p>Java class for ProductAndServicesPrefs complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ProductAndServicesPrefs">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ForSales" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="ForPurchase" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="InventoryAndPurchaseOrder" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="QuantityWithPriceAndRate" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="QuantityOnHand" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="UOM" type="{http://schema.intuit.com/finance/v3}UOMFeatureTypeEnum" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProductAndServicesPrefs", propOrder = {
    "forSales",
    "forPurchase",
    "inventoryAndPurchaseOrder",
    "quantityWithPriceAndRate",
    "quantityOnHand",
    "uom"
})
public class ProductAndServicesPrefs
    implements Serializable, Equals, HashCode
{

    private final static long serialVersionUID = 1L;
    @XmlElement(name = "ForSales")
    protected Boolean forSales;
    @XmlElement(name = "ForPurchase")
    protected Boolean forPurchase;
    @XmlElement(name = "InventoryAndPurchaseOrder")
    protected Boolean inventoryAndPurchaseOrder;
    @XmlElement(name = "QuantityWithPriceAndRate")
    protected Boolean quantityWithPriceAndRate;
    @XmlElement(name = "QuantityOnHand")
    protected Boolean quantityOnHand;
    @XmlElement(name = "UOM")
    protected UOMFeatureTypeEnum uom;

    /**
     * Gets the value of the forSales property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isForSales() {
        return forSales;
    }

    /**
     * Sets the value of the forSales property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setForSales(Boolean value) {
        this.forSales = value;
    }

    /**
     * Gets the value of the forPurchase property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isForPurchase() {
        return forPurchase;
    }

    /**
     * Sets the value of the forPurchase property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setForPurchase(Boolean value) {
        this.forPurchase = value;
    }

    /**
     * Gets the value of the inventoryAndPurchaseOrder property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isInventoryAndPurchaseOrder() {
        return inventoryAndPurchaseOrder;
    }

    /**
     * Sets the value of the inventoryAndPurchaseOrder property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setInventoryAndPurchaseOrder(Boolean value) {
        this.inventoryAndPurchaseOrder = value;
    }

    /**
     * Gets the value of the quantityWithPriceAndRate property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isQuantityWithPriceAndRate() {
        return quantityWithPriceAndRate;
    }

    /**
     * Sets the value of the quantityWithPriceAndRate property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setQuantityWithPriceAndRate(Boolean value) {
        this.quantityWithPriceAndRate = value;
    }

    /**
     * Gets the value of the quantityOnHand property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isQuantityOnHand() {
        return quantityOnHand;
    }

    /**
     * Sets the value of the quantityOnHand property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setQuantityOnHand(Boolean value) {
        this.quantityOnHand = value;
    }

    /**
     * Gets the value of the uom property.
     * 
     * @return
     *     possible object is
     *     {@link UOMFeatureTypeEnum }
     *     
     */
    public UOMFeatureTypeEnum getUOM() {
        return uom;
    }

    /**
     * Sets the value of the uom property.
     * 
     * @param value
     *     allowed object is
     *     {@link UOMFeatureTypeEnum }
     *     
     */
    public void setUOM(UOMFeatureTypeEnum value) {
        this.uom = value;
    }

    public boolean equals(ObjectLocator thisLocator, ObjectLocator thatLocator, Object object, EqualsStrategy strategy) {
        if (!(object instanceof ProductAndServicesPrefs)) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final ProductAndServicesPrefs that = ((ProductAndServicesPrefs) object);
        {
            Boolean lhsForSales;
            lhsForSales = this.isForSales();
            Boolean rhsForSales;
            rhsForSales = that.isForSales();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "forSales", lhsForSales), LocatorUtils.property(thatLocator, "forSales", rhsForSales), lhsForSales, rhsForSales)) {
                return false;
            }
        }
        {
            Boolean lhsForPurchase;
            lhsForPurchase = this.isForPurchase();
            Boolean rhsForPurchase;
            rhsForPurchase = that.isForPurchase();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "forPurchase", lhsForPurchase), LocatorUtils.property(thatLocator, "forPurchase", rhsForPurchase), lhsForPurchase, rhsForPurchase)) {
                return false;
            }
        }
        {
            Boolean lhsInventoryAndPurchaseOrder;
            lhsInventoryAndPurchaseOrder = this.isInventoryAndPurchaseOrder();
            Boolean rhsInventoryAndPurchaseOrder;
            rhsInventoryAndPurchaseOrder = that.isInventoryAndPurchaseOrder();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "inventoryAndPurchaseOrder", lhsInventoryAndPurchaseOrder), LocatorUtils.property(thatLocator, "inventoryAndPurchaseOrder", rhsInventoryAndPurchaseOrder), lhsInventoryAndPurchaseOrder, rhsInventoryAndPurchaseOrder)) {
                return false;
            }
        }
        {
            Boolean lhsQuantityWithPriceAndRate;
            lhsQuantityWithPriceAndRate = this.isQuantityWithPriceAndRate();
            Boolean rhsQuantityWithPriceAndRate;
            rhsQuantityWithPriceAndRate = that.isQuantityWithPriceAndRate();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "quantityWithPriceAndRate", lhsQuantityWithPriceAndRate), LocatorUtils.property(thatLocator, "quantityWithPriceAndRate", rhsQuantityWithPriceAndRate), lhsQuantityWithPriceAndRate, rhsQuantityWithPriceAndRate)) {
                return false;
            }
        }
        {
            Boolean lhsQuantityOnHand;
            lhsQuantityOnHand = this.isQuantityOnHand();
            Boolean rhsQuantityOnHand;
            rhsQuantityOnHand = that.isQuantityOnHand();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "quantityOnHand", lhsQuantityOnHand), LocatorUtils.property(thatLocator, "quantityOnHand", rhsQuantityOnHand), lhsQuantityOnHand, rhsQuantityOnHand)) {
                return false;
            }
        }
        {
            UOMFeatureTypeEnum lhsUOM;
            lhsUOM = this.getUOM();
            UOMFeatureTypeEnum rhsUOM;
            rhsUOM = that.getUOM();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "uom", lhsUOM), LocatorUtils.property(thatLocator, "uom", rhsUOM), lhsUOM, rhsUOM)) {
                return false;
            }
        }
        return true;
    }

    public boolean equals(Object object) {
        final EqualsStrategy strategy = JAXBEqualsStrategy.INSTANCE;
        return equals(null, null, object, strategy);
    }

    public int hashCode(ObjectLocator locator, HashCodeStrategy strategy) {
        int currentHashCode = 1;
        {
            Boolean theForSales;
            theForSales = this.isForSales();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "forSales", theForSales), currentHashCode, theForSales);
        }
        {
            Boolean theForPurchase;
            theForPurchase = this.isForPurchase();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "forPurchase", theForPurchase), currentHashCode, theForPurchase);
        }
        {
            Boolean theInventoryAndPurchaseOrder;
            theInventoryAndPurchaseOrder = this.isInventoryAndPurchaseOrder();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "inventoryAndPurchaseOrder", theInventoryAndPurchaseOrder), currentHashCode, theInventoryAndPurchaseOrder);
        }
        {
            Boolean theQuantityWithPriceAndRate;
            theQuantityWithPriceAndRate = this.isQuantityWithPriceAndRate();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "quantityWithPriceAndRate", theQuantityWithPriceAndRate), currentHashCode, theQuantityWithPriceAndRate);
        }
        {
            Boolean theQuantityOnHand;
            theQuantityOnHand = this.isQuantityOnHand();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "quantityOnHand", theQuantityOnHand), currentHashCode, theQuantityOnHand);
        }
        {
            UOMFeatureTypeEnum theUOM;
            theUOM = this.getUOM();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "uom", theUOM), currentHashCode, theUOM);
        }
        return currentHashCode;
    }

    public int hashCode() {
        final HashCodeStrategy strategy = JAXBHashCodeStrategy.INSTANCE;
        return this.hashCode(null, strategy);
    }

}