/* Copyright (c) 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.kml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.geoserver.kml.decorator.KmlDecoratorFactory;
import org.geoserver.kml.decorator.KmlDecoratorFactory.KmlDecorator;
import org.geoserver.kml.utils.KMLUtils;
import org.geoserver.kml.utils.LookAtOptions;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.wms.GetMapRequest;
import org.geoserver.wms.WMS;
import org.geoserver.wms.WMSMapContent;
import org.geoserver.wms.featureinfo.FeatureTemplate;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.map.Layer;
import org.geotools.styling.Symbolizer;
import org.geotools.util.Converters;
import org.opengis.feature.simple.SimpleFeature;

import de.micromata.opengis.kml.v_2_2_0.Feature;

/**
 * A class used by {@link KmlDecorator} to get the current encoding context (request, map content,
 * current layer, feature and so on).
 * 
 * @author Andrea Aime - GeoSolutions
 */
public class KmlEncodingContext {
    
    boolean kmz;

    WMSMapContent mapContent;

    GetMapRequest request;

    List<Symbolizer> currentSymbolizers;

    Layer currentLayer;

    SimpleFeatureCollection currentFeatureCollection;

    SimpleFeature currentFeature;

    Map<String, Object> metadata = new HashMap<String, Object>();

    boolean descriptionEnabled;

    FeatureTemplate template = new FeatureTemplate();

    LookAtOptions lookAtOptions;

    WMS wms;
    
    Map<String, Layer> kmzGroundOverlays = new LinkedHashMap<String, Layer>();

    boolean placemarkForced;

    String superOverlayMode;

    boolean superOverlayEnabled;

    boolean networkLinksFormat;

    private boolean extendedDataEnabled;

    private int kmScore;

    public KmlEncodingContext(WMSMapContent mapContent, WMS wms, boolean kmz) {
        this.mapContent = mapContent;
        this.request = mapContent.getRequest();
        this.wms = wms;
        this.descriptionEnabled = KMLUtils.getKMAttr(request, wms);
        this.lookAtOptions = new LookAtOptions(request.getFormatOptions());
        this.placemarkForced = KMLUtils.getKmplacemark(mapContent.getRequest(), wms);
        this.superOverlayMode = computeSuperOverlayMode();
        this.superOverlayEnabled = computeSuperOverlayEnabled();
        this.extendedDataEnabled =  computeExtendedDataEnabled();
        this.kmScore = computeKmScore();
        this.networkLinksFormat = KMLMapOutputFormat.NL_KML_MIME_TYPE.equals(request.getFormat()) || KMZMapOutputFormat.NL_KMZ_MIME_TYPE.equals(request.getFormat());
        this.kmz = kmz;
    }
    
    private int computeKmScore() {
        int kmScore = wms.getKmScore();
        Map fo = request.getFormatOptions();
        Object kmScoreObj = fo.get("kmscore");
        if (kmScoreObj != null) {
            kmScore = (Integer) kmScoreObj;
        }

        return kmScore;
    }

    /**
     * Checks if the extended data is enabled or not
     * @param request
     * @return
     */
    boolean computeExtendedDataEnabled() {
        Map formatOptions = request.getFormatOptions();
        Boolean extendedData = Converters.convert(formatOptions.get("extendedData"), Boolean.class); 
        if (extendedData == null) {
            extendedData = Boolean.FALSE;
        }

        return extendedData;
    }
    
    /**
     * Checks if the superoverlay is enabled or not
     * @param request
     * @return
     */
    boolean computeSuperOverlayEnabled() {
        Map formatOptions = request.getFormatOptions();
        Boolean superoverlay = (Boolean) formatOptions.get("superoverlay");
        if (superoverlay == null) {
            superoverlay = Boolean.FALSE;
        }

        return superoverlay;
    }
    
    
    /**
     * Returns the superoverlay mode (either specified in the request, or the default one)
     * @return
     */
    String computeSuperOverlayMode() {
        String overlayMode = (String) request.getFormatOptions().get("superoverlay_mode");
        if(overlayMode != null) {
            return overlayMode;
        } 
        
        overlayMode = (String) request.getFormatOptions().get("overlayMode");
        if(overlayMode != null) {
            return overlayMode;
        } else {
            return wms.getKmlSuperoverlayMode();
        }
    }

    public WMSMapContent getMapContent() {
        return mapContent;
    }

    public void setMapContent(WMSMapContent mapContent) {
        this.mapContent = mapContent;
    }

    public GetMapRequest getRequest() {
        return request;
    }

    public void setRequest(GetMapRequest request) {
        this.request = request;
    }

    public List<Symbolizer> getCurrentSymbolizers() {
        return currentSymbolizers;
    }

    public void setCurrentSymbolizers(List<Symbolizer> symbolizers) {
        this.currentSymbolizers = symbolizers;
    }

    public Layer getCurrentLayer() {
        return currentLayer;
    }

    public void setCurrentLayer(Layer currentLayer) {
        this.currentLayer = currentLayer;
    }

    public SimpleFeature getCurrentFeature() {
        return currentFeature;
    }

    public void setCurrentFeature(SimpleFeature currentFeature) {
        this.currentFeature = currentFeature;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public boolean isDescriptionEnabled() {
        return descriptionEnabled;
    }

    public void setDescriptionEnabled(boolean descriptionEnabled) {
        this.descriptionEnabled = descriptionEnabled;
    }

    public FeatureTemplate getTemplate() {
        return template;
    }

    public LookAtOptions getLookAtOptions() {
        return lookAtOptions;
    }

    public WMS getWms() {
        return wms;
    }

    /**
     * Returns the {@link KmlDecorator} objects for the specified Feature class
     * 
     * @return
     */
    public List<KmlDecorator> getDecoratorsForClass(Class<? extends Feature> clazz) {
        List<KmlDecoratorFactory> factories = GeoServerExtensions
                .extensions(KmlDecoratorFactory.class);
        List<KmlDecorator> result = new ArrayList<KmlDecorator>();
        for (KmlDecoratorFactory factory : factories) {
            KmlDecorator decorator = factory.getDecorator(clazz, this);
            if (decorator != null) {
                result.add(decorator);
            }
        }

        return result;
    }

    public SimpleFeatureCollection getCurrentFeatureCollection() {
        return currentFeatureCollection;
    }

    public void setCurrentFeatureCollection(SimpleFeatureCollection currentFeatureCollection) {
        this.currentFeatureCollection = currentFeatureCollection;
    }

    public boolean isKmz() {
        return kmz;
    }
    
    /**
     * Adds a layer to be generated as ground overlay in the kmz package
     * @param imagePath The path of the ground overlay image inside the kmz archive
     * @param layer
     */
    public void addKmzGroundOverlay(String imagePath, Layer layer) {
        if(!kmz) {
            throw new IllegalStateException("Cannot add ground " +
            		"overlay layers, the output is not supposed to be a KMZ");
        }
        this.kmzGroundOverlays.put(imagePath, layer);
    }

    /**
     * Returns the list of ground overlay layers to be included in the KMZ response
     * @return
     */
    public Map<String, Layer> getKmzGroundOverlays() {
        return kmzGroundOverlays;
    }

    public boolean isPlacemarkForced() {
        return placemarkForced;
    }

    public void setPlacemarkForced(boolean placemarkForced) {
        this.placemarkForced = placemarkForced;
    }

    public boolean isSuperOverlayEnabled() {
        return superOverlayEnabled;
    }

    public String getSuperOverlayMode() {
        return superOverlayMode;
    }

    public boolean isNetworkLinksFormat() {
        return networkLinksFormat;
    }

    public boolean isExtendedDataEnabled() {
        return extendedDataEnabled;
    }

    public int getKmScore() {
        return kmScore;
    }
    
    

}