// code by jph
package ch.alpine.ascona.geo;

enum Pois {
  FARO_DE_PUNTO_NARIGA("43•19`13.4N", "008•54`35.0W"),
  PLAYA_DEL_SILENCIO__("43•33`53.3N", "006•17`46.0W"),
  LAGO_DEL_VALLE______("43•02`40.8N", "006•08`18.4W"),
  LAS_MEDULAS_________("42•27`26.9N", "006•45`03.4W"),
  HAYEDO_DE_OTZARRETA_("43•03`01.2N", "002•42`45.8W"),
  SAN_JUAN_DE_GAZTELUG("43•26`27.3N", "002•47`04.5W"),
  LA_MONTANA_PALENTINA("43•03`18.0N", "004•45`33.0W"),
  VISTAS_DE_LA_REINA__("37•08`34.0N", "003•20`38.0W"),
  CABO_DE_GATA________("36•51`57.3N", "002•04`30.3W"),
  SACA_DE_LAS_YEGUAS__("37•13`31.5N", "006•30`15.8W"),
  MONTSERRAT_BARCELONA("41•35`23.2N", "001•39`08.3E"),
  PAISAJE_LUNAR_______("42•12`37.7N", "001•30`56.5W"),
  CASTILLO_DE_BELMONTE("39•33`44.6N", "002•45`25.6W"),
  MOLINOS_VALENCIANOS_("39•47`23.4N", "000•44`12.2W"),
  //
  ;

  public final POI poi;

  Pois(String lat, String lon) {
    poi = POI.of(lat, lon);
  }
}
