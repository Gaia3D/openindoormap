<?xml version="1.0" encoding="UTF-8"?>
<StyledLayerDescriptor version="1.0.0" xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc"
  xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd">
  <NamedLayer>
    <Name>airquality</Name>
    <UserStyle>
      <Name>pm10</Name>
      <Title>Simple AirQuality(PM10) style</Title>
      <Abstract>Classic air quality color progression</Abstract>
      <FeatureTypeStyle>
        <Rule>
          <RasterSymbolizer>
            <Opacity>1.0</Opacity>
            <ColorMap>
              <ColorMapEntry color="#2b83ba" quantity="0" label="0" />
              <ColorMapEntry color="#abdda4" quantity="50" label="50"/>
              <ColorMapEntry color="#ffffbf" quantity="100" label="100" />
              <ColorMapEntry color="#fdae61" quantity="250" label="250" />
              <ColorMapEntry color="#d7191c" quantity="500" label="500" />
            </ColorMap>
          </RasterSymbolizer>
        </Rule>
      </FeatureTypeStyle>
    </UserStyle>
  </NamedLayer>
</StyledLayerDescriptor>