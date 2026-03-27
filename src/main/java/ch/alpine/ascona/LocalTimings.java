// code by jph
package ch.alpine.ascona;

import ch.alpine.bridge.pro.RunProviderTimings;

enum LocalTimings {
  ;
  static void main() throws Exception {
    RunProviderTimings.of(LocalTimings.class.getPackageName());
  }
}
