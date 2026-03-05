// code by jph
package ch.alpine.ascona.gbc.d2;

import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.dv.Biinvariants;
import ch.alpine.tensor.img.ColorDataGradients;

@ReflectionMarker
public class ScatteredSetParam {
  @FieldSelectionArray({ "20", "30", "50" })
  public Integer refine = 20;
  public ColorDataGradients spinnerColorData = ColorDataGradients.CLASSIC;
  public Boolean arrows = false;
  public Biinvariants biinvariants = Biinvariants.USANCE;
}
