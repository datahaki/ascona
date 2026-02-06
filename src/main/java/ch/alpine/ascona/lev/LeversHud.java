// code by jph
package ch.alpine.ascona.lev;

import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.sophis.dv.Biinvariants;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.img.ColorDataGradient;
import ch.alpine.tensor.img.ColorDataGradients;

public enum LeversHud {
  ;
  public static final ColorDataGradient COLOR_DATA_GRADIENT = //
      ColorDataGradients.TEMPERATURE.deriveWithOpacity(RealScalar.of(0.5));

  public static void render(Biinvariants bitype, LeversRender leversRender) {
    render(bitype, leversRender, COLOR_DATA_GRADIENT);
  }

  public static void render( //
      Biinvariants bitype, LeversRender leversRender, ColorDataGradient colorDataGradient) {
    leversRender.renderSequence();
    leversRender.renderOrigin();
    leversRender.renderLevers();
    // ---
    switch (bitype) {
    case METRIC:
      leversRender.renderTangentsXtoP(false); // boolean: no tangent plane
      leversRender.renderEllipseIdentity();
      leversRender.renderWeightsLength();
      break;
    // case METRIC2:
    // leversRender.renderTangentsPtoX(false); // boolean: no tangent plane
    // leversRender.renderEllipseIdentityP();
    // leversRender.renderWeightsLength();
    // break;
    case LEVERAGES:
      leversRender.renderTangentsXtoP(false); // boolean: no tangent plane
      if (leversRender.getSequence().length() <= 2)
        leversRender.renderMahalanobisFormXEV(colorDataGradient);
      else
        leversRender.renderEllipseMahalanobis();
      leversRender.renderWeightsLeveragesSqrt();
      break;
    // case LEVERAGES2:
    // leversRender.renderInfluenceX(colorDataGradient);
    // leversRender.renderWeightsLeveragesSqrt();
    // break;
    case GARDEN:
      leversRender.renderTangentsPtoX(false); // boolean: no tangent planes
      leversRender.renderEllipseMahalanobisP(); // no evs
      leversRender.renderWeightsGarden();
      break;
    case HARBOR:
      leversRender.renderInfluenceX(colorDataGradient);
      leversRender.renderInfluenceP(colorDataGradient);
      break;
    case CUPOLA:
      // this is for the presentation
      // leversRender.renderLevers();
      leversRender.renderTangentsXtoP(false);
      leversRender.renderInfluenceX(colorDataGradient);
      // leversRender.renderInfluenceP(colorDataGradient);
      break;
    default:
      break;
    }
    leversRender.renderIndexX();
    leversRender.renderIndexP();
  }
}
