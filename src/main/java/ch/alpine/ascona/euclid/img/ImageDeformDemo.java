// code by jph
package ch.alpine.ascona.euclid.img;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import ch.alpine.ascona.ref.OpenTileParam;
import ch.alpine.ascony.api.LogWeightings;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.msh.AveragedMovingDomain2D;
import ch.alpine.ascony.ren.AxesRender;
import ch.alpine.ascony.ren.ColorPairs;
import ch.alpine.ascony.ren.GridRender;
import ch.alpine.ascony.ren.ImageRender;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.EuclideanPlaneDemo;
import ch.alpine.bridge.fig.Meshgrid;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.plt.ImagePlot;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.PvmBuilder;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.dv.Biinvariant;
import ch.alpine.sophis.dv.Biinvariants;
import ch.alpine.sophis.dv.Sedarim;
import ch.alpine.sophis.var.VariogramFunctions;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Array;
import ch.alpine.tensor.alg.Flatten;
import ch.alpine.tensor.alg.Outer;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.img.ImageResize;
import ch.alpine.tensor.io.ImageFormat;
import ch.alpine.tensor.itp.Interpolation;
import ch.alpine.tensor.itp.LinearInterpolation;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.sca.Clips;

class ImageDeformDemo extends EuclideanPlaneDemo {
  @ReflectionMarker
  static class Param {
    public final OpenTileParam otp = new OpenTileParam();
    public ImageResize imgRes = ImageResize.DEGREE_0;
    public Integer resw = 40;
    public Integer resh = 30;
    @FieldFuse
    public Boolean fuse = false;
  }

  // private final BufferedImage bufferedImage = ResourceData.bufferedImage("ch/alpine/ascona/image/13_4017_3003.png");
  private final Param param;
  private BufferedImage bufferedImage = null;
  private Interpolation interpolation;
  private Tensor src;
  private final Tensor reference;

  public ImageDeformDemo() {
    super(param = new Param());
    fieldsEditor(param).addUniversalListener(this::reload);
    reload();
    Tensor dx = Subdivide.of(0, 255, 3);
    reference = Flatten.of(Outer.of(Tensors::of, dx, dx), 1);
    setGeodesicControlPoints(reference);
    geometricComponent().setModel2Pixel(PvmBuilder.rhs().setOffset(100, 600).setPerPixel(2).digest());
    geometricComponent().addRenderInterfaceBackground(new GridRender(geometricComponent()::getSize));
    geometricComponent().addRenderInterfaceBackground(AxesRender.INSTANCE);
  }

  private void reload() {
    bufferedImage = param.otp.getImage();
    src = ImageFormat.from(bufferedImage);
    interpolation = LinearInterpolation.of(src);
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.HEAD_TAIL;
  }

  public static TensorUnaryOperator toImagePixel(int h) {
    return p -> Tensors.of(RealScalar.of(h).subtract(p.Get(1)), p.Get(0));
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    if (param.fuse) {
      param.fuse = false;
      IO.println(getGeodesicControlPoints());
    }
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    CoordinateBoundingBox cbb = CoordinateBoundingBox.of( //
        Clips.positive(bufferedImage.getWidth()), //
        Clips.positive(bufferedImage.getHeight()));
    ImageRender imageRender = new ImageRender(bufferedImage, cbb);
    imageRender.render(geometricLayer, graphics);
    Tensor sequence = getGeodesicControlPoints();
    Tensor points = toImagePixel(bufferedImage.getHeight()).slash(sequence);
    manifoldDisplay.showPoints(ColorPairs.CONTROL_POINTS, RealScalar.of(100.), sequence) //
        .render(geometricLayer, graphics);
    {
      LeversRender leversRender = //
          LeversRender.of(manifoldDisplay, sequence, null, geometricLayer, graphics);
      leversRender.renderIndexP();
    }
    {
      LeversRender leversRender = //
          LeversRender.of(manifoldDisplay, reference, null, geometricLayer, graphics);
      leversRender.renderIndexP("q");
    }
    Dimension dimension = geometricComponent().getSize();
    dimension.width /= 2;
    dimension.height /= 2;
    Meshgrid meshgrid = new Meshgrid( //
        CoordinateBoundingBox.of( //
            Clips.positive(bufferedImage.getWidth()), //
            Clips.positive(bufferedImage.getHeight())), //
        param.resw, param.resh);
    {
      Show show = new Show();
      show.setShowLabel("Inv. Dist Coordinate");
      HomogeneousSpace homogeneousSpace = manifoldDisplay().homogeneousSpace();
      Biinvariant biinvariant = Biinvariants.METRIC.ofSafe(homogeneousSpace);
      ScalarUnaryOperator vf = VariogramFunctions.INVERSE_POWER.of(RealScalar.TWO);
      // ---
      Sedarim sedarim = LogWeightings.COORDINATE.sedarim(biinvariant, vf, reference);
      Tensor weights = meshgrid.image(sedarim::sunder);
      BiinvariantMean biinvariantMean = homogeneousSpace.biinvariantMean();
      AveragedMovingDomain2D averagedMovingDomain2D = new AveragedMovingDomain2D(weights, biinvariantMean, ZEROS);
      Tensor lift = averagedMovingDomain2D.forward(points, safeWrap(interpolation::get));
      show.add(ImagePlot.of(ImageFormat.of(lift), param.imgRes));
      show.render_autoIndent(graphics, new Rectangle(dimension.width, 0, dimension.width, dimension.height));
    }
  }

  private static final Tensor ZEROS = Array.zeros(4);

  private static TensorUnaryOperator safeWrap(TensorUnaryOperator tuo) {
    return x -> {
      try {
        return tuo.apply(x);
      } catch (Exception exception) {
        return ZEROS;
      }
    };
  }

  static void main() {
    new ImageDeformDemo().runStandalone();
  }
}
