// code by jph
package ch.alpine.ascona.geo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.Throw;
import ch.alpine.tensor.ext.Cache;
import ch.alpine.tensor.ext.HomeDirectory;
import ch.alpine.tensor.qty.Degree;

public enum GeoBoundaries {
  INSTANCE;

  private final ObjectMapper objectMapper = new ObjectMapper();
  private JsonNode jsonNode;
  private final Set<String> set = new TreeSet<>();
  private final List<String> all = new ArrayList<>();
  private final Cache<String, Tensor> cache = Cache.of(this::compute, 10);

  private GeoBoundaries() {
    try {
      objectMapper.registerModule(new JtsModule());
      File file = HomeDirectory.Database.resolve("GeoBoundaries", "geoBoundariesCGAZ_ADM0.geojson").toFile();
      jsonNode = objectMapper.readTree(file);
      for (JsonNode feature : jsonNode.path("features")) {
        String name = feature.path("properties").path("shapeName").asText("Unknown");
        set.add(name);
      }
      all.addAll(set);
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  public List<String> countries() {
    return Collections.unmodifiableList(all);
  }

  public String inside(Point point) {
    for (JsonNode feature : jsonNode.path("features")) {
      JsonNode geometryNode = feature.path("geometry");
      try {
        Geometry geometry = objectMapper.treeToValue(geometryNode, Geometry.class);
        if (geometry.contains(point)) {
          String name = feature.path("properties").path("shapeName").asText("Unknown");
          return name;
        }
      } catch (Exception e) {
        // TODO handle exception
      }
    }
    return "";
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

  Tensor cache_apply(String name) {
    return set.contains(name) //
        ? cache.apply(name)
        : null;
  }

  private Tensor compute(String name) {
    Throw.unless(set.contains(name));
    try {
      Geometry geometry = getCountry(name);
      if (Objects.nonNull(geometry)) {
        Tensor paths = Tensors.empty();
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
          Geometry polygon = geometry.getGeometryN(i);
          Coordinate[] coordinates = polygon.getCoordinates();
          int last = coordinates.length - 1;
          boolean equals2d = coordinates[0].equals2D(coordinates[last], 1e-12);
          int limit = equals2d ? last : coordinates.length;
          if (3 < limit) {
            Tensor path = Tensor.of(Stream.of(coordinates) //
                .limit(limit) //
                .map(GeoBoundaries::toPoint));
            paths.append(path);
          }
        }
        return paths;
      }
    } catch (Exception e) {
      // ---
    }
    return null;
  }

  /** @param coordinate
   * @return interprets the input coordinate as if with unit "deg" and maps to rad */
  private static Tensor toPoint(Coordinate coordinate) {
    return Tensors.of(Degree.of(coordinate.x), Degree.of(coordinate.y));
  }
}
