// code by jph
package ch.alpine.ascona.usr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import ch.alpine.sophus.srf.SurfaceMesh;
import ch.alpine.sophus.srf.io.Ply2Format;
import ch.alpine.tensor.ext.HomeDirectory;
import ch.alpine.tensor.ext.ReadLine;
import ch.alpine.tensor.io.Export;

public enum Ply2FormatDemo {
  ;
  public static void main(String[] args) throws IOException {
    File file = HomeDirectory.file("doraemon.ply2");
    try (InputStream inputStream = new FileInputStream(file)) {
      SurfaceMesh surfaceMesh = Ply2Format.parse(ReadLine.of(inputStream));
      Export.of(HomeDirectory.file("mesh.v.csv"), surfaceMesh.vrt);
      // Export.of(HomeDirectory.file("mesh.i.csv"), surfaceMesh.ind().map(RealScalar.ONE::add));
    }
  }
}
