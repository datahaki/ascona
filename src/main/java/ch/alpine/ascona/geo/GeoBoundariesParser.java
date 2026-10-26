// code by jph
package ch.alpine.ascona.geo;

import java.io.File;

import org.locationtech.jts.geom.Geometry;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.alpine.tensor.ext.HomeDirectory;

public class GeoBoundariesParser {
  public static void main(String[] args) throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JtsModule());
    File file = HomeDirectory.Downloads.resolve("geoBoundariesCGAZ_ADM1.geojson").toFile();
    JsonNode jsonNode = objectMapper.readTree(file);
    for (JsonNode feature : jsonNode.path("features")) {
      String name = feature.path("properties").path("shapeName").asText("Unknown");
      JsonNode geometryNode = feature.path("geometry");
      Geometry geometry = objectMapper.treeToValue(geometryNode, Geometry.class);
      System.out.printf("Country: %s, Type: %s, Area: %s%n", name, geometry.getGeometryType(), geometry.getArea());
    }
  }
}
