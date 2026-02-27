// code by jph
package ch.alpine.ascona;

import ch.alpine.bridge.pro.RunLaunchPad;

enum AsconaLaunchPad {
  ;
  static void main() {
    RunLaunchPad.create(AsconaLaunchPad.class.getPackageName()).runStandalone();
  }
}
