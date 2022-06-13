// code by jph
package ch.alpine.ascona.ref.d2;

import org.junit.jupiter.api.Test;

import ch.alpine.sophus.lie.rn.RnBiinvariantMean;
import ch.alpine.sophus.ref.d2.SurfaceMeshRefinement;
import ch.alpine.sophus.srf.SurfaceMesh;

class SurfaceMeshExamplesTest {
  @Test
  void testMixed7() {
    for (SurfaceMeshRefinements smr : SurfaceMeshRefinements.values()) {
      SurfaceMeshRefinement refinement = smr.operator(RnBiinvariantMean.INSTANCE);
      SurfaceMesh refine = refinement.refine(SurfaceMeshExamples.mixed7());
      refinement.refine(refine);
    }
  }

  @Test
  void testMixed11() {
    for (SurfaceMeshRefinements smr : SurfaceMeshRefinements.values()) {
      SurfaceMeshRefinement refinement = smr.operator(RnBiinvariantMean.INSTANCE);
      SurfaceMesh refine = refinement.refine(SurfaceMeshExamples.mixed11());
      refinement.refine(refine);
    }
  }
}
