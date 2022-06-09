// code by jph
package ch.alpine.ascona.util.api;

import java.awt.Dimension;
import java.util.List;

import ch.alpine.ascona.util.dat.GokartPoseData;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.swing.SpinnerLabel;

// TODO ASCONA the contents of this package need to be restructured
@ReflectionMarker
public abstract class AbstractGeodesicDatasetDemo extends AbstractManifoldDisplayDemo {
  protected final GokartPoseData gokartPoseData;
  protected final SpinnerLabel<String> spinnerLabelString;
  protected final SpinnerLabel<Integer> spinnerLabelLimit;

  public AbstractGeodesicDatasetDemo(List<ManifoldDisplay> list, GokartPoseData gokartPoseData) {
    super(list);
    this.gokartPoseData = gokartPoseData;
    {
      spinnerLabelString = SpinnerLabel.of(gokartPoseData.list());
      spinnerLabelString.addSpinnerListener(type -> updateState());
      spinnerLabelString.setValue(gokartPoseData.list().get(0));
      spinnerLabelString.addToComponentReduced(timerFrame.jToolBar, new Dimension(180, 28), "data");
    }
    {
      spinnerLabelLimit = SpinnerLabel.of(500, 750, 800, 900, 1000, 1500, 2000, 3000, 5000);
      spinnerLabelLimit.setValue(750);
      spinnerLabelLimit.addToComponentReduced(timerFrame.jToolBar, new Dimension(60, 28), "limit");
      spinnerLabelLimit.addSpinnerListener(type -> updateState());
    }
    timerFrame.jToolBar.addSeparator();
  }

  protected abstract void updateState();
}
