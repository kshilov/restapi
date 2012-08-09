//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.07.19 at 06:08:25 PM MSK 
//


package com.heymoose.infrastructure.service.yml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "url",
    "buyurl",
    "price",
    "wprice",
    "currencyId",
    "xCategory",
    "categoryId",
    "picture",
    "store",
    "pickup",
    "delivery",
    "deliveryIncluded",
    "localDeliveryCost",
    "orderingTime",
    "typePrefixOrVendorOrVendorCodeOrModelOrProviderOrTarifplanOrAuthorOrNameOrPublisherOrSeriesOrYearOrISBNOrVolumeOrPartOrLanguageOrBindingOrPageExtentOrTableOfContentsOrPerformedByOrPerformanceTypeOrStorageOrFormatOrRecordingLengthOrArtistOrTitleOrMediaOrStarringOrDirectorOrOriginalNameOrCountryOrWorldRegionOrRegionOrDaysOrDataTourOrHotelStarsOrRoomOrMealOrIncludedOrTransportOrPriceMinOrPriceMaxOrOptionsOrPlaceOrHallOrHallPartOrDateOrIsPremiereOrIsKids",
    "aliases",
    "additional",
    "description",
    "salesNotes",
    "promo",
    "manufacturerWarranty",
    "countryOfOrigin",
    "downloadable",
    "adult",
    "barcode",
    "param"
})
@XmlRootElement(name = "offer")
public class Offer {

    @XmlAttribute
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String id;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String type;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String available;
    @XmlAttribute
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String bid;
    @XmlAttribute
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String cbid;
    protected String url;
    protected String buyurl;
    @XmlElement(required = true)
    protected String price;
    protected String wprice;
    @XmlElement(required = true)
    protected String currencyId;
    protected String xCategory;
    @XmlElement(required = true)
    protected List<CategoryId> categoryId;
    protected String picture;
    protected String store;
    protected String pickup;
    protected String delivery;
    protected DeliveryIncluded deliveryIncluded;
    @XmlElement(name = "local_delivery_cost")
    protected String localDeliveryCost;
    protected OrderingTime orderingTime;
    @XmlElements({
        @XmlElement(name = "typePrefix", required = true, type = TypePrefix.class),
        @XmlElement(name = "vendor", required = true, type = Vendor.class),
        @XmlElement(name = "vendorCode", required = true, type = VendorCode.class),
        @XmlElement(name = "model", required = true, type = Model.class),
        @XmlElement(name = "provider", required = true, type = Provider.class),
        @XmlElement(name = "tarifplan", required = true, type = Tarifplan.class),
        @XmlElement(name = "author", required = true, type = Author.class),
        @XmlElement(name = "name", required = true, type = Name.class),
        @XmlElement(name = "publisher", required = true, type = Publisher.class),
        @XmlElement(name = "series", required = true, type = Series.class),
        @XmlElement(name = "year", required = true, type = Year.class),
        @XmlElement(name = "ISBN", required = true, type = ISBN.class),
        @XmlElement(name = "volume", required = true, type = Volume.class),
        @XmlElement(name = "part", required = true, type = Part.class),
        @XmlElement(name = "language", required = true, type = Language.class),
        @XmlElement(name = "binding", required = true, type = Binding.class),
        @XmlElement(name = "page_extent", required = true, type = PageExtent.class),
        @XmlElement(name = "table_of_contents", required = true, type = TableOfContents.class),
        @XmlElement(name = "performed_by", required = true, type = PerformedBy.class),
        @XmlElement(name = "performance_type", required = true, type = PerformanceType.class),
        @XmlElement(name = "storage", required = true, type = Storage.class),
        @XmlElement(name = "format", required = true, type = Format.class),
        @XmlElement(name = "recording_length", required = true, type = RecordingLength.class),
        @XmlElement(name = "artist", required = true, type = Artist.class),
        @XmlElement(name = "title", required = true, type = Title.class),
        @XmlElement(name = "media", required = true, type = Media.class),
        @XmlElement(name = "starring", required = true, type = Starring.class),
        @XmlElement(name = "director", required = true, type = Director.class),
        @XmlElement(name = "originalName", required = true, type = OriginalName.class),
        @XmlElement(name = "country", required = true, type = Country.class),
        @XmlElement(name = "worldRegion", required = true, type = WorldRegion.class),
        @XmlElement(name = "region", required = true, type = Region.class),
        @XmlElement(name = "days", required = true, type = Days.class),
        @XmlElement(name = "dataTour", required = true, type = DataTour.class),
        @XmlElement(name = "hotel_stars", required = true, type = HotelStars.class),
        @XmlElement(name = "room", required = true, type = Room.class),
        @XmlElement(name = "meal", required = true, type = Meal.class),
        @XmlElement(name = "included", required = true, type = Included.class),
        @XmlElement(name = "transport", required = true, type = Transport.class),
        @XmlElement(name = "price_min", required = true, type = PriceMin.class),
        @XmlElement(name = "price_max", required = true, type = PriceMax.class),
        @XmlElement(name = "options", required = true, type = Options.class),
        @XmlElement(name = "place", required = true, type = Place.class),
        @XmlElement(name = "hall", required = true, type = Hall.class),
        @XmlElement(name = "hall_part", required = true, type = HallPart.class),
        @XmlElement(name = "date", required = true, type = Date.class),
        @XmlElement(name = "is_premiere", required = true, type = IsPremiere.class),
        @XmlElement(name = "is_kids", required = true, type = IsKids.class)
    })
    protected List<Object> typePrefixOrVendorOrVendorCodeOrModelOrProviderOrTarifplanOrAuthorOrNameOrPublisherOrSeriesOrYearOrISBNOrVolumeOrPartOrLanguageOrBindingOrPageExtentOrTableOfContentsOrPerformedByOrPerformanceTypeOrStorageOrFormatOrRecordingLengthOrArtistOrTitleOrMediaOrStarringOrDirectorOrOriginalNameOrCountryOrWorldRegionOrRegionOrDaysOrDataTourOrHotelStarsOrRoomOrMealOrIncludedOrTransportOrPriceMinOrPriceMaxOrOptionsOrPlaceOrHallOrHallPartOrDateOrIsPremiereOrIsKids;
    protected String aliases;
    protected List<Additional> additional;
    protected String description;
    @XmlElement(name = "sales_notes")
    protected String salesNotes;
    protected String promo;
    @XmlElement(name = "manufacturer_warranty")
    protected String manufacturerWarranty;
    @XmlElement(name = "country_of_origin")
    protected String countryOfOrigin;
    protected String downloadable;
    protected String adult;
    protected List<Barcode> barcode;
    protected List<Param> param;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the available property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAvailable() {
        return available;
    }

    /**
     * Sets the value of the available property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAvailable(String value) {
        this.available = value;
    }

    /**
     * Gets the value of the bid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBid() {
        return bid;
    }

    /**
     * Sets the value of the bid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBid(String value) {
        this.bid = value;
    }

    /**
     * Gets the value of the cbid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCbid() {
        return cbid;
    }

    /**
     * Sets the value of the cbid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCbid(String value) {
        this.cbid = value;
    }

    /**
     * Gets the value of the url property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the value of the url property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUrl(String value) {
        this.url = value;
    }

    /**
     * Gets the value of the buyurl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBuyurl() {
        return buyurl;
    }

    /**
     * Sets the value of the buyurl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBuyurl(String value) {
        this.buyurl = value;
    }

    /**
     * Gets the value of the price property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPrice() {
        return price;
    }

    /**
     * Sets the value of the price property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPrice(String value) {
        this.price = value;
    }

    /**
     * Gets the value of the wprice property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWprice() {
        return wprice;
    }

    /**
     * Sets the value of the wprice property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWprice(String value) {
        this.wprice = value;
    }

    /**
     * Gets the value of the currencyId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCurrencyId() {
        return currencyId;
    }

    /**
     * Sets the value of the currencyId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCurrencyId(String value) {
        this.currencyId = value;
    }

    /**
     * Gets the value of the xCategory property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getXCategory() {
        return xCategory;
    }

    /**
     * Sets the value of the xCategory property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setXCategory(String value) {
        this.xCategory = value;
    }

    /**
     * Gets the value of the categoryId property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the categoryId property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCategoryId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CategoryId }
     * 
     * 
     */
    public List<CategoryId> getCategoryId() {
        if (categoryId == null) {
            categoryId = new ArrayList<CategoryId>();
        }
        return this.categoryId;
    }

    /**
     * Gets the value of the picture property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPicture() {
        return picture;
    }

    /**
     * Sets the value of the picture property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPicture(String value) {
        this.picture = value;
    }

    /**
     * Gets the value of the store property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStore() {
        return store;
    }

    /**
     * Sets the value of the store property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStore(String value) {
        this.store = value;
    }

    /**
     * Gets the value of the pickup property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPickup() {
        return pickup;
    }

    /**
     * Sets the value of the pickup property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPickup(String value) {
        this.pickup = value;
    }

    /**
     * Gets the value of the delivery property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDelivery() {
        return delivery;
    }

    /**
     * Sets the value of the delivery property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDelivery(String value) {
        this.delivery = value;
    }

    /**
     * Gets the value of the deliveryIncluded property.
     * 
     * @return
     *     possible object is
     *     {@link DeliveryIncluded }
     *     
     */
    public DeliveryIncluded getDeliveryIncluded() {
        return deliveryIncluded;
    }

    /**
     * Sets the value of the deliveryIncluded property.
     * 
     * @param value
     *     allowed object is
     *     {@link DeliveryIncluded }
     *     
     */
    public void setDeliveryIncluded(DeliveryIncluded value) {
        this.deliveryIncluded = value;
    }

    /**
     * Gets the value of the localDeliveryCost property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocalDeliveryCost() {
        return localDeliveryCost;
    }

    /**
     * Sets the value of the localDeliveryCost property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocalDeliveryCost(String value) {
        this.localDeliveryCost = value;
    }

    /**
     * Gets the value of the orderingTime property.
     * 
     * @return
     *     possible object is
     *     {@link OrderingTime }
     *     
     */
    public OrderingTime getOrderingTime() {
        return orderingTime;
    }

    /**
     * Sets the value of the orderingTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link OrderingTime }
     *     
     */
    public void setOrderingTime(OrderingTime value) {
        this.orderingTime = value;
    }

    /**
     * Gets the value of the typePrefixOrVendorOrVendorCodeOrModelOrProviderOrTarifplanOrAuthorOrNameOrPublisherOrSeriesOrYearOrISBNOrVolumeOrPartOrLanguageOrBindingOrPageExtentOrTableOfContentsOrPerformedByOrPerformanceTypeOrStorageOrFormatOrRecordingLengthOrArtistOrTitleOrMediaOrStarringOrDirectorOrOriginalNameOrCountryOrWorldRegionOrRegionOrDaysOrDataTourOrHotelStarsOrRoomOrMealOrIncludedOrTransportOrPriceMinOrPriceMaxOrOptionsOrPlaceOrHallOrHallPartOrDateOrIsPremiereOrIsKids property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the typePrefixOrVendorOrVendorCodeOrModelOrProviderOrTarifplanOrAuthorOrNameOrPublisherOrSeriesOrYearOrISBNOrVolumeOrPartOrLanguageOrBindingOrPageExtentOrTableOfContentsOrPerformedByOrPerformanceTypeOrStorageOrFormatOrRecordingLengthOrArtistOrTitleOrMediaOrStarringOrDirectorOrOriginalNameOrCountryOrWorldRegionOrRegionOrDaysOrDataTourOrHotelStarsOrRoomOrMealOrIncludedOrTransportOrPriceMinOrPriceMaxOrOptionsOrPlaceOrHallOrHallPartOrDateOrIsPremiereOrIsKids property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTypePrefixOrVendorOrVendorCodeOrModelOrProviderOrTarifplanOrAuthorOrNameOrPublisherOrSeriesOrYearOrISBNOrVolumeOrPartOrLanguageOrBindingOrPageExtentOrTableOfContentsOrPerformedByOrPerformanceTypeOrStorageOrFormatOrRecordingLengthOrArtistOrTitleOrMediaOrStarringOrDirectorOrOriginalNameOrCountryOrWorldRegionOrRegionOrDaysOrDataTourOrHotelStarsOrRoomOrMealOrIncludedOrTransportOrPriceMinOrPriceMaxOrOptionsOrPlaceOrHallOrHallPartOrDateOrIsPremiereOrIsKids().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TypePrefix }
     * {@link Vendor }
     * {@link VendorCode }
     * {@link Model }
     * {@link Provider }
     * {@link Tarifplan }
     * {@link Author }
     * {@link Name }
     * {@link Publisher }
     * {@link Series }
     * {@link Year }
     * {@link ISBN }
     * {@link Volume }
     * {@link Part }
     * {@link Language }
     * {@link Binding }
     * {@link PageExtent }
     * {@link TableOfContents }
     * {@link PerformedBy }
     * {@link PerformanceType }
     * {@link Storage }
     * {@link Format }
     * {@link RecordingLength }
     * {@link Artist }
     * {@link Title }
     * {@link Media }
     * {@link Starring }
     * {@link Director }
     * {@link OriginalName }
     * {@link Country }
     * {@link WorldRegion }
     * {@link Region }
     * {@link Days }
     * {@link DataTour }
     * {@link HotelStars }
     * {@link Room }
     * {@link Meal }
     * {@link Included }
     * {@link Transport }
     * {@link PriceMin }
     * {@link PriceMax }
     * {@link Options }
     * {@link Place }
     * {@link Hall }
     * {@link HallPart }
     * {@link Date }
     * {@link IsPremiere }
     * {@link IsKids }
     * 
     * 
     */
    public List<Object> getTypePrefixOrVendorOrVendorCodeOrModelOrProviderOrTarifplanOrAuthorOrNameOrPublisherOrSeriesOrYearOrISBNOrVolumeOrPartOrLanguageOrBindingOrPageExtentOrTableOfContentsOrPerformedByOrPerformanceTypeOrStorageOrFormatOrRecordingLengthOrArtistOrTitleOrMediaOrStarringOrDirectorOrOriginalNameOrCountryOrWorldRegionOrRegionOrDaysOrDataTourOrHotelStarsOrRoomOrMealOrIncludedOrTransportOrPriceMinOrPriceMaxOrOptionsOrPlaceOrHallOrHallPartOrDateOrIsPremiereOrIsKids() {
        if (typePrefixOrVendorOrVendorCodeOrModelOrProviderOrTarifplanOrAuthorOrNameOrPublisherOrSeriesOrYearOrISBNOrVolumeOrPartOrLanguageOrBindingOrPageExtentOrTableOfContentsOrPerformedByOrPerformanceTypeOrStorageOrFormatOrRecordingLengthOrArtistOrTitleOrMediaOrStarringOrDirectorOrOriginalNameOrCountryOrWorldRegionOrRegionOrDaysOrDataTourOrHotelStarsOrRoomOrMealOrIncludedOrTransportOrPriceMinOrPriceMaxOrOptionsOrPlaceOrHallOrHallPartOrDateOrIsPremiereOrIsKids == null) {
            typePrefixOrVendorOrVendorCodeOrModelOrProviderOrTarifplanOrAuthorOrNameOrPublisherOrSeriesOrYearOrISBNOrVolumeOrPartOrLanguageOrBindingOrPageExtentOrTableOfContentsOrPerformedByOrPerformanceTypeOrStorageOrFormatOrRecordingLengthOrArtistOrTitleOrMediaOrStarringOrDirectorOrOriginalNameOrCountryOrWorldRegionOrRegionOrDaysOrDataTourOrHotelStarsOrRoomOrMealOrIncludedOrTransportOrPriceMinOrPriceMaxOrOptionsOrPlaceOrHallOrHallPartOrDateOrIsPremiereOrIsKids = new ArrayList<Object>();
        }
        return this.typePrefixOrVendorOrVendorCodeOrModelOrProviderOrTarifplanOrAuthorOrNameOrPublisherOrSeriesOrYearOrISBNOrVolumeOrPartOrLanguageOrBindingOrPageExtentOrTableOfContentsOrPerformedByOrPerformanceTypeOrStorageOrFormatOrRecordingLengthOrArtistOrTitleOrMediaOrStarringOrDirectorOrOriginalNameOrCountryOrWorldRegionOrRegionOrDaysOrDataTourOrHotelStarsOrRoomOrMealOrIncludedOrTransportOrPriceMinOrPriceMaxOrOptionsOrPlaceOrHallOrHallPartOrDateOrIsPremiereOrIsKids;
    }

    /**
     * Gets the value of the aliases property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAliases() {
        return aliases;
    }

    /**
     * Sets the value of the aliases property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAliases(String value) {
        this.aliases = value;
    }

    /**
     * Gets the value of the additional property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the additional property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAdditional().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Additional }
     * 
     * 
     */
    public List<Additional> getAdditional() {
        if (additional == null) {
            additional = new ArrayList<Additional>();
        }
        return this.additional;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the salesNotes property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSalesNotes() {
        return salesNotes;
    }

    /**
     * Sets the value of the salesNotes property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSalesNotes(String value) {
        this.salesNotes = value;
    }

    /**
     * Gets the value of the promo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPromo() {
        return promo;
    }

    /**
     * Sets the value of the promo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPromo(String value) {
        this.promo = value;
    }

    /**
     * Gets the value of the manufacturerWarranty property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getManufacturerWarranty() {
        return manufacturerWarranty;
    }

    /**
     * Sets the value of the manufacturerWarranty property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setManufacturerWarranty(String value) {
        this.manufacturerWarranty = value;
    }

    /**
     * Gets the value of the countryOfOrigin property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCountryOfOrigin() {
        return countryOfOrigin;
    }

    /**
     * Sets the value of the countryOfOrigin property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCountryOfOrigin(String value) {
        this.countryOfOrigin = value;
    }

    /**
     * Gets the value of the downloadable property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDownloadable() {
        return downloadable;
    }

    /**
     * Sets the value of the downloadable property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDownloadable(String value) {
        this.downloadable = value;
    }

    /**
     * Gets the value of the adult property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAdult() {
        return adult;
    }

    /**
     * Sets the value of the adult property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAdult(String value) {
        this.adult = value;
    }

    /**
     * Gets the value of the barcode property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the barcode property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBarcode().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Barcode }
     * 
     * 
     */
    public List<Barcode> getBarcode() {
        if (barcode == null) {
            barcode = new ArrayList<Barcode>();
        }
        return this.barcode;
    }

    /**
     * Gets the value of the param property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the param property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getParam().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Param }
     * 
     * 
     */
    public List<Param> getParam() {
        if (param == null) {
            param = new ArrayList<Param>();
        }
        return this.param;
    }

}