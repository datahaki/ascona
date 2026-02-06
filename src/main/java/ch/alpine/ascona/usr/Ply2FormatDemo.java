// code by jph
package ch.alpine.ascona.usr;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import ch.alpine.sophis.srf.SurfaceMesh;
import ch.alpine.sophis.srf.io.Ply2Format;
import ch.alpine.tensor.ext.HomeDirectory;
import ch.alpine.tensor.ext.ReadLine;
import ch.alpine.tensor.io.Export;

public enum Ply2FormatDemo {
  ;
  static void main() throws IOException {
    Path file = HomeDirectory.path("doraemon.ply2");
    try (InputStream inputStream = Files.newInputStream(file)) {
      SurfaceMesh surfaceMesh = Ply2Format.parse(ReadLine.of(inputStream));
      Export.of(HomeDirectory.path("mesh.v.csv"), surfaceMesh.vrt);
      // Export.of(HomeDirectory.file("mesh.i.csv"), surfaceMesh.ind().map(RealScalar.ONE::add));
    }
  }
}
