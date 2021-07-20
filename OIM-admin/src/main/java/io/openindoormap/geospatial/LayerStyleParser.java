package io.openindoormap.geospatial;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.sld.SLDConfiguration;
import org.geotools.styling.*;
import org.geotools.xml.styling.SLDTransformer;
import org.geotools.xsd.Parser;
import org.opengis.filter.FilterFactory;

import io.openindoormap.domain.GeometryType;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * sld 파일 스타일링
 * TODO 이름을 바꾸던, 패키지를 바꾸던
 *
 * @author PSH
 */
public class LayerStyleParser {

    private String geometryType;
    private String fillValue;
    private Float fillOpacityValue;
    private String strokeValue;
    private Float strokeWidthValue;
    private String styleData;

    public LayerStyleParser(String geometryType, String fillValue, Float fillOpacityValue, String strokeValue, Float strokeWidthValue, String styleData) {
        this.geometryType = geometryType;
        this.fillValue = fillValue;
        this.fillOpacityValue = fillOpacityValue;
        this.strokeValue = strokeValue;
        this.strokeWidthValue = strokeWidthValue;
        this.styleData = styleData;
    }

    public String getFillValue() {
        return fillValue;
    }

    public Float getFillOpacityValue() {
        return fillOpacityValue;
    }

    public String getStrokeValue() {
        return strokeValue;
    }

    public Float getStrokeWidthValue() {
        return strokeWidthValue;
    }

    public String getStyleData() {
        return styleData;
    }

    public void updateLayerStyle() throws Exception {
        StyleFactory sf = CommonFactoryFinder.getStyleFactory();
        FilterFactory filterFactory = new FilterFactoryImpl();
        SLDTransformer styleTransform = new SLDTransformer();
        StyledLayerDescriptor sld = (StyledLayerDescriptor) parse();
        NamedLayer layer = (NamedLayer) sld.getStyledLayers()[0];
        Fill fill = sf.createFill(filterFactory.literal(this.fillValue), filterFactory.literal(this.fillOpacityValue));
        Stroke stroke = sf.createStroke(filterFactory.literal(this.strokeValue), filterFactory.literal(this.strokeWidthValue));

        if (GeometryType.POINT == GeometryType.valueOf(this.geometryType.toUpperCase())) {
            PointSymbolizer ps = (PointSymbolizer) layer.getStyles()[0]
                    .featureTypeStyles()
                    .get(0)
                    .rules()
                    .get(0)
                    .symbolizers().get(0);
            Mark mark = sf.getDefaultMark();
            mark.setFill(fill);
            mark.setStroke(stroke);
            ps.getGraphic().graphicalSymbols().clear();
            ps.getGraphic().graphicalSymbols().add(mark);


        } else if (GeometryType.LINE == GeometryType.valueOf(geometryType.toUpperCase())) {
            LineSymbolizer ps = (LineSymbolizer) layer.getStyles()[0]
                    .featureTypeStyles()
                    .get(0)
                    .rules()
                    .get(0)
                    .symbolizers()
                    .get(0);
            ps.setStroke(stroke);

        } else if (GeometryType.POLYGON == GeometryType.valueOf(geometryType.toUpperCase())) {
            PolygonSymbolizer ps = (PolygonSymbolizer) layer.getStyles()[0]
                    .featureTypeStyles()
                    .get(0)
                    .rules()
                    .get(0)
                    .symbolizers()
                    .get(0);
            ps.setFill(fill);
            ps.setStroke(stroke);
        }

        this.styleData = styleTransform.transform(sld);
    }

    private Object parse() throws Exception {
        SLDConfiguration sld = new SLDConfiguration();
        InputStream input = new ByteArrayInputStream(this.styleData.getBytes());
        return new Parser(sld).parse(input);
    }
}
