// code by jph
package ch.alpine.ascona;

import ch.alpine.bridge.io.FileBlock;
import ch.alpine.bridge.io.ResourceLocator;
import ch.alpine.bridge.pro.RunLaunchPad;

enum AsconaLaunchPad {
  ;
  static void main() {
    if (!FileBlock.of(ResourceLocator.of(AsconaLaunchPad.class).resolve("")))
      RunLaunchPad.create(AsconaLaunchPad.class.getPackageName()).runStandalone();
  }
}
