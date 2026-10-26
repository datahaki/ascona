// code by jph
package ch.alpine.ascona.geo;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.alpine.sophis.decim.CurveDecimation;
import ch.alpine.sophis.decim.RnCurveDecimation;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Reverse;
import ch.alpine.tensor.ext.Cache;
import ch.alpine.tensor.ext.HomeDirectory;
import ch.alpine.tensor.qty.Degree;

public enum GeoBoundaries {
  INSTACE;

  private final ObjectMapper objectMapper = new ObjectMapper();
  private JsonNode jsonNode;
  private final List<String> all = new LinkedList<>();
  final Cache<Boundary, Tensor> cache = Cache.of(this::compute, 1);

  private GeoBoundaries() {
    try {
      objectMapper.registerModule(new JtsModule());
      File file = HomeDirectory.Downloads.resolve("geoBoundariesCGAZ_ADM0.geojson").toFile();
      jsonNode = objectMapper.readTree(file);
      for (JsonNode feature : jsonNode.path("features")) {
        String name = feature.path("properties").path("shapeName").asText("Unknown");
        all.add(name);
      }
      Collections.sort(all);
      IO.println("SIZE " + all.size());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private Geometry getCountry(String target) throws JsonProcessingException, IllegalArgumentException {
    for (JsonNode feature : jsonNode.path("features")) {
      String name = feature.path("properties").path("shapeName").asText("Unknown");
      if (name.equals(target)) {
        JsonNode geometryNode = feature.path("geometry");
        return objectMapper.treeToValue(geometryNode, Geometry.class);
      }
    }
    return null;
  }

  public List<String> countries() {
    return Collections.unmodifiableList(all);
  }

  private Tensor compute(Boundary boundary) {
    CurveDecimation curveDecimation = RnCurveDecimation.of(boundary.epsilon());
    try {
      Geometry geometry = getCountry(boundary.name());
      if (Objects.nonNull(geometry)) {
        Tensor paths = Tensors.empty();
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
          Geometry polygon = geometry.getGeometryN(i);
          Coordinate[] coordinates = polygon.getCoordinates();
          Tensor path = Tensors.empty();
          for (Coordinate coordinate : coordinates) {
            Scalar lat = Degree.of(coordinate.y);
            Scalar lon = Degree.of(coordinate.x);
            path.append(Tensors.of(lat, lon));
          }
          paths.append(Reverse.of(curveDecimation.apply(path)));
          
        }
        return paths;
      }
    } catch (Exception e) {
      // ---
    }
    return null;
  }
}
