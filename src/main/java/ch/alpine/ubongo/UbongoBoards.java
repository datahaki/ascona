// code by jph
package ch.alpine.ubongo;

import java.util.List;

public enum UbongoBoards {
  /***************************************************/
  /************ BATCH 1 ******************************/
  STANDARD(3, "oooo", "oooo", "ooo ", " o  "),
  /** 2,5,6,7,9,10,12,15,16,17,19,20 */
  CARPET_1(5, "o oo ", "ooooo", " oooo", " oooo", "ooooo"),
  /** 0,1,3,5,9,12,14 */
  CARPET_2(5, "oooo ", "ooooo", " oooo", " oooo", "ooooo"),
  /** 0,1,4,5,6,8 */
  CARPET_3(5, "oooo  ", "oooooo", " oooo ", " oooo ", "ooooo "),
  /** only 1 unique solution */
  CARPET_4(5, "oooo  ", "ooooo ", " ooooo", " ooooo", "ooooo "),
  // CARPET5(6, "oooo ", "oooooo", " ooooo", " ooooo", "ooooo"),
  DOTTED_1(5, "oooo ", "ooooo", " oooo", " oo o", "ooooo"),
  /** 2,3,4,5,7,13,14,15,16,20,21,23 */
  BULLET_1(5, "oooo ", "ooooo", "oo  o", "oo  o", "ooooo"),
  /** 0,1,3,5,10,11 */
  HORSES_1(5, "oooooo", "oo  oo", "o     ", "oo  oo", "ooooo "),
  /** 0,2,8,9,14,16 */
  PYRAMID3(5, "   o   ", "  oooo ", " oooo  ", "ooooo  ", " ooo   ", " oo    "),
  /** 0,2,3,5,6,7, 12,15,16,18,19,20 */
  PYRAMID4(5, "   o   ", "  oooo ", " oooo  ", "ooooo  ", " oooo  ", " oo    "),
  /**  */
  PIGGIES1(5, " oooo ", "oooooo", "oooo  ", " o o  ", " o o  "),
  /**  */
  PIGGIES2(5, " oooo ", "oooooo", "ooooo ", " ooo  ", " o o  "),
  MICKEY_1(5, "oo  oo", "oo  oo", " oooo ", " oooo ", " o oo ", "  oo  "),
  MICKEY_2(6, "oo  oo", "oo  oo", " oooo ", " oooo ", " o oo ", "  oo  "),
  /***************************************************/
  /************ BATCH 2 ******************************/
  RAYRAC_1(5, "  oooo ", " oo oo ", "ooo    ", " oooo  ", " ooo   "),
  /**  */
  CRANED_1(5, "  oooo ", " oo oo ", "ooo    ", " oooo  ", " oooo  "),
  /**  */
  BARCODE1(5, "  oooo ", " oo o  ", "ooo o  ", " oo oo ", " ooooo "),
  // interesting
  BOTTLE_1(5, "  oooo ", " oo o  ", "ooooo  ", " oo oo ", " ooooo "),
  // interesting
  PLANET_1(5, "  oooo ", " oo o  ", "o ooo  ", "ooo oo ", " ooooo "),
  // very nice with 6
  CHEESE_1(6, "o oooo ", "ooo oo ", "ooooo  ", "ooo oo ", " oooo  "),
  /***************************************************/
  /************ BATCH 3 ******************************/
  // interesting
  SPIRAL_1(5, "oooooo ", "  oooo ", "  ooo  ", "  oooo ", "   oooo"),
  // interesting
  SPIRAL_2(6, "oooooo  ", " ooooo  ", "  ooo   ", "  oooo  ", "   ooooo"),
  // interesting
  SPIRAL_3(5, "ooooo  ", "  oooo ", "  ooo  ", "  oooo ", "   oooo"),
  // interesting
  SPIRAL_4(5, "ooooo  ", "oooooo ", "  ooo  ", "  oooo ", "   oooo"),
  // 7 solutions
  SHOTGUN1(4, "oooooo", "ooooo ", "ooooo ", "oo    "),
  // interesting
  SHOTGUN2(5, "ooooooo", "oooooo ", "oooooo ", "oo     "),
  // 6 solutions
  SHOTGUN3(6, "oooooooo", " ooooooo", " oooooo", " oo"),
  // one solution
  SHOTGUN4(6, "ooooooo", "ooooooo", "oooooo", "oo"),
  // 7 sol
  CORNER_2(5, "oooooo", "ooooo", "oooo", "oo", "o"),
  // many solutions
  CORNER_3(5, "oooooo", "ooooo", "oooo", "ooo", "o"),
  // many solutions
  CORNER_4(5, "oooooo", "ooooo", "oooo", "ooo", "oo"),
  // sufficient solutions
  CORNER_5(6, "ooooooo", "oooooo", "oooo", "ooo", "oo", "o"),
  // many solutions
  CORNER_6(6, "ooooooo", "ooooo", "oooo", "ooooo", "oo"),
  /***************************************************/
  /************ UNPUBLISHED **************************/
  // ---
  // many solutions
  CORNER_1(4, "oooooo", "ooooo", "ooo", "o"),
  // 12 solutions
  SPIRAL_6(6, " o     ", "ooooo  ", "oooooo", "  ooo ", "  oooo", "   oooo"),
  // only 3 solutions
  SPIRAL_5(6, "ooooo  ", "oooooo", "  ooo ", "  oooo", "   oooo"),
  // maybe not very exciting
  HOOK_A3(5, "  oooo ", " oo o ", "ooo ", " oo oo", " ooooo"),
  // 6 solutions
  GRID_A6(5, "  oooo ", " oo oo", "o ooo", "ooo oo", " oooo "),
  PYRAMID5(6, "   o   ", "  oooo ", " oooo", "ooooo", " oooo", " ooo"),
  HOLE2(5, "oooo ", "ooooo", " oooo", " o  o", "ooooo"),
  HOLE3(5, "oooo ", "ooooo", " o oo", " o  o", "ooooo"),
  HOLE4a(5, "oooo ", "ooooo", "oo  o", " o  o", "ooooo"), // 18
  HOLE4c(5, " oooo", "ooooo", "oo  o", "oo  o", " oooo"),
  HOLE6a(5, " oooo", "oo  o", "oo  o", "oo  o", "ooooo"),
  HOLE6b(5, "oooooo", "oo  o", "oo  o", "oo  o", "ooooo"),
  SHOE1a(5, "oooooo", "oo  o", "oo   ", "oo  o", "ooooo"),
  SHOE1b(5, "oooooo", "oo  oo", "oo   ", "oo  oo", "ooooo"),
  /** only 2 solutions */
  SHOE1c(5, "oooooo", "oo  oo", " o   ", "oo  oo", "ooooo"),
  /** only 1 unique solution */
  PYRAMID1(5, "  ooo ", " oooo", "ooooo", " ooo", " oo"),
  /** 2 solutions */
  PYRAMID2(5, "   o  ", "  ooo ", " oooo", "ooooo", " ooo", " oo"),
  // ---
  MASKDIFF(6, "  o    ", " ooooo ", "o ooo o", "ooooooo", " ooooo "),
  /***************************************************/
  /************ BATCH 4 ******************************/
  MODERN_1(4, "  oo ", "oooo ", " ooo ", " ooo ", "oo oo"),
  AUTOMOB1(4, "  oo  ", " ooooo", "oooooo", " o  o "),
  AUTOMOB2(5, "  oo   ", "ooooooo", "ooooooo", " oo oo "),
  AUTOMOB3(5, " oo    ", "ooooooo", "ooooooo", " oo oo "),
  FACTORY1(4, " o     ", " oo    ", " oo    ", " ooooo ", "ooo ooo"),
  FACTORY2(5, " o     ", " o   o ", "oo   o ", "oooooo ", "ooo ooo"),
  NKAPPE_1(5, "  o    ", "  oo   ", "o ooo o", "ooooooo", " ooooo "),
  TERRIER1(5, "  o    ", " ooo   ", "ooooooo", "  ooooo", "  o  oo", "  o   o"),
  MASKEYES(5, "  o   ", " ooooo", "o oo o", "oooooo", " oooo "),
  MODERN_2(5, "oo oo", "oooo ", " oooo", " ooo ", "ooooo"),
  HOLGRAIL(5, " oo  ", "ooooo", " ooo ", "  o  ", " oooo", "ooooo"),
  // BROTHER1(5, " ooo ", " ooo ", " ooo ", " o ", " oooo", "ooooo"),
  SICHEL_1(5, "  ooo", " oooo", "ooo  ", "oo   ", " oo  ", "  oo ", "  oo "),
  SICHAL_2(5, "  ooo", " ooo ", "ooo  ", "oo   ", " oo  ", " ooo ", " ooo "),
  GIRLHAT1(5, "  o  ", " ooo ", "ooooo", "  ooo", "ooooo", " ooo "),
  GIRLHAT2(5, " oo  ", " ooo ", "oo oo", "  ooo", "ooooo", " ooo "),
  MICKEY_3(6, "oo  oo", "ooo oo", " oooo", " oooo", " o oo", "  oo  "),
  FREIGHT1(6, "    o    ", "   ooo o ", "oo  o  oo", " oooooooo", "  oooooo "),
  CHRISTMT(6, "  o   ", "  oo  ", " oooo ", "oooo  ", " oooo ", "oooooo", "  o   ", "  oo  "),
  KERZENH1(6, "o o o o", "ooooooo", " ooooo ", " oooo  ", "  oo   "),
  // ---
  // dont use:
  LETTERA2(5, " ooo ", "oo oo", "o   o", "ooooo", "o  oo", "o  oo"),
  /***************************************************/
  /************ BATCH 5 ******************************/
  KEYLOCK1(4, "ooooooo", "o o  oo", "ooo  oo"),
  SHOEBLK1(4, " ooo  ", "ooooo ", "o  ooo", "o  ooo"),
  PRINTED1(4, " o   ", " oo  ", " oo o", "ooooo", "ooooo"),
  LETTERA1(5, "ooooo", "oo  o", "oo  o", "ooooo", "o  oo", "o  oo"),
  LETTER_C(5, " oooo ", "ooo oo", "o     ", "o     ", "oo  oo", " oooo "),
  LETTER_G(5, "oooo ", "oo   ", "o    ", "o ooo", "o  oo", "ooooo"),
  LETTER_I(5, "ooooo", "  oo ", "  oo ", "  oo ", " ooo ", "ooooo"),
  LETTER_J(5, " oooo", "   oo", "   oo", "o   o", "oo oo", "ooooo"),
  LETTER_M(5, "o   oo", "oooooo", "o oo o", "o    o", "o   oo", "o   oo"),
  LETTER_P(5, "ooooo", "oo  o", "oo  o", "ooooo", "oo   ", "oo   "),
  RINGRI_1(5, " ooooo", " o  oo", " o   o", " o  oo", "ooooo "),
  RINGRI_2(5, " ooooo", " o  oo", " o   o", " o  oo", "oooooo"),
  RINGRI_3(5, "ooooo ", "oo oo ", "o   o ", "o  oo ", "oooooo"),
  RINGRI_4(5, "ooooo ", "oo oo ", "o   o ", "oo oo ", "oooooo"),
  RINGRI_5(5, " ooo  ", "oo oo ", "o   o ", "oo oo ", "oooooo"),
  CHAPEL_1(5, "oooo ", "oo oo", "o   o", "oo oo", "oo oo"),
  CHAPEL_2(5, "  o  ", "oooo ", "oo oo", "o   o", "oo oo", "oo oo"),
  ARROWHD1(5, "oo ooo  ", "oo   oo ", "oooooooo", "     oo ", "    oo  "),
  FACTORY3(5, " oooo   ", " oooooo ", "oo o  oo", "oooo  oo"),
  FACTORY4(5, "ooooo  ", "ooooo  ", "o  oooo", "o  oooo"),
  AIRPLAN1(5, "   o  o", " oooooo", "ooooooo", "  oo  o", "   o   "),
  CACTUS_1(5, "  o  ", "o o o", "o ooo", "ooooo", " ooo ", "  oo ", "  oo "),
  CHAPEL_3(6, "  o   ", "ooooo ", "oo oo ", "o   oo", "oo ooo", "oo ooo"),
  BATMAN_1(6, " o   o ", " oo oo ", " ooooo ", " o o oo", "oooooo ", " ooooo "),
  LETTER_S(6, " oooo", "oo  o", "ooo  ", " oooo", "   oo", "ooooo"),
  // ---
  ALIENFC1(5, "  o   ", " oooo ", "oo oo ", " ooooo", " oooo ", "   oo "),
  PRALINE1(5, " oooo", " o oo", "ooooo", "o o o", "ooooo"),
  PRALINE2(5, "ooooo", " o oo", "ooooo", "o o o", "ooooo"),
  PRALINE3(5, "ooooo", "oooo ", "ooooo", "o o  ", "ooo  "),
  SOMETRE1(5, "  o  ", " oooo", "  oo ", "ooooo", " ooo ", "  oo ", " ooo "),
  UMBRELL1(5, "  oo  ", " oooo ", "oooooo", "o  o o", "   o  ", "   o  ", "  oo  "),
  SKULLED1(5, "o  o ", "ooooo", "o o o", "ooooo", " ooo ", " ooo "),
  SKULLED2(5, " oo  ", "oooo ", "oo o ", "ooooo", " ooo ", " oo  "),
  ROOFED_1(5, "  o  ", "ooooo", " ooo ", "oooo ", "ooo  ", "ooo  "),
  ROOFED_2(5, "  o  ", "ooooo", " ooo ", " ooo ", " oooo", "ooooo"),
  HYDRANT5(5, "   o ", "  ooo", " oooo", "oooo ", " ooo ", "  ooo", "  oo "),
  FLOWER_1(5, " oo  ", "oooo ", " ooo ", "  oo ", "  oo ", " oooo", "  oo "),
  FLOWER_2(5, " o  ", "ooo ", "oooo", "ooo ", " ooo", "oooo", " oo "),
  FLOWER_3(5, " oo ", "ooo ", "oooo", "ooo ", " ooo", "oooo", " oo "),
  FLOWER_4(5, "  ooo ", " oooo ", "ooooo ", " ooooo", " oooo ", "  o   "),
  SKULLED3(6, " ooo  ", "ooooo ", "oo ooo", "oo  oo", "ooooo ", "  oo  "),
  SKULLED4(6, " ooo  ", "ooooo ", "oo ooo", "oo  oo", "ooooo ", " ooo  "),
  HYDRANT1(6, "  oo ", " oooo", "  ooo", "ooooo", " ooo ", "  oo ", " oooo"),
  HYDRANT2(6, " oooo ", " ooooo", " ooooo", "ooooo ", "  oo  ", "  ooo ", "  oo  "),
  HYDRANT3(6, "  ooo ", " ooooo", " ooooo", "ooooo ", "  oo  ", "  ooo ", "  oo  "),
  HYDRANT4(6, "   oo ", "  oooo", " ooooo", "ooooo ", "  oo  ", "  ooo ", "  oo  "),
  // ---
  MODERN_3(5, "  ooo", "ooooo", "ooooo", " oooo", "oo   "),
  MODERN_4(5, "  ooo", "ooooo", "ooooo", " oooo", " o  o"),
  MODERN_5(6, "  ooo  ", "ooooo o", "ooooooo", " ooooo ", " o  o  "),
  MOUSEGT1(6, "oo    ", "oo oo ", " oooo ", " ooo  ", "ooooo ", "oo ooo"),
  TOWERBR1(6, "  o   o  ", "  oo  o  ", " ooo ooo ", "ooooooooo", " oo   oo "),
  HELMET_1(6, " ooo ", "ooooo", "oo oo", "oo oo", " oooo", "oo  o"),
  /***************************************************/
  /************ BATCH 7 ******************************/
  SLEEPCAT(7, "o  o   ", "oooo   ", "ooo  oo", " ooooo ", "ooooooo", "    ooo"),
  DEVILMSK(7, "o    o", "oooooo", " o oo ", " ooo  ", "  oo o", "   ooo", " ooooo"),
  LARGESAW(7, "     ooo", "    oo o", "   ooooo", "  ooo oo", "  ooooo ", " ooo o  ", "oooooo  "),
  /** 5 solutions */
  NUCLEARP(7, " oo  oo ", " oo  oo ", " oo ooo ", " ooooooo", " ooo ooo", "ooo   oo"),
  ELKFRONT(7, "o o o  ", "ooo o o", "ooo ooo", " ooooo ", "  ooo  ", "  oooo ", "    oo "),
  CASTLEGB(7, "o o oo", "oooooo", "oo o o", "oo ooo", "o oooo", "oooo o"),
  /** many solutions */
  FLATCRAB(7, " oooooooo ", " o o oo oo", "oooooooooo", "oo o   o o"),
  /** exactly one solution */
  CODECARD(7, " ooo  ", "o oooo", "oo ooo", "ooooo ", "oo ooo", " ooo o", "ooooo "),
  /** only 2 solutions */
  SLASHDOT(7, "  ooo ", " o ooo", "oooooo", " ooo o", " oo oo", "oo ooo", " ooooo"),
  /** only 2 solutions */
  FLATFLAT(7, "oooooo ooo", "o ooo o oo", "ooo oooooo"),
  CASTLEDE(7, "oo  o  o o", "oooooooooo", "o ooo o oo", "ooo ooooo "),
  FELIX_F1(7, "ooooooo", " ooooo ", "ooo    ", "ooooo  ", " ooo   ", " o     ", "ooo    ", "ooo    "),
  FELIX_F2(7, "ooooooo", " ooooo ", " ooo   ", " oooo  ", " ooo   ", " oo    ", " oo    ", "ooo    "),
  LETTERH1(7, "ooo    ", " oo    ", " ooo   ", " oooo  ", " ooooo ", " oo oo ", " oo oo ", " oo ooo"),
  // ---
  // --- AT CHILLY's
  SIMSONSB(7, "   o   ", "  oooo ", " oooooo", " ooooo ", "o oooo ", "ooooo  ", " oooo  "),
  SIMSONSH(7, "   o  ", "  oooo", " ooooo", " oo oo", "oo ooo", "ooooo ", " oooo "),
  SIMSONSM(7, "   o  ", "  oooo", " ooooo", " o ooo", "ooo oo", "ooooo ", " oooo "),
  SIMSONSG(7, "  o  ", " oooo", "ooooo", "o o o", "ooooo", "ooooo", "ooooo"),
  SIMSONSC(7, "   o ", "  oo ", " ooo ", "ooooo", "o o o", "ooooo", "ooooo", "ooooo"),
  SIMSONSS(8, "ooooo ", " ooooo", "ooooo ", " oo oo", "oooooo", "o ooo ", "oooo  "),
  SIMSONSY(8, "   ooo ", "  oo oo", "ooooooo", "oooo oo", " ooooo ", "oo o oo"),
  /***************************************************/
  /************ BATCH 8 ******************************/
  LONGHALL(8, " oooo ooo ", "oooo ooooo", "o  oooo oo", "oooo oooo "),
  SOMEBLUB(8, "  oo   ", " ooooo ", " oo ooo", "oo  oo ", "oo  oo ", " ooooo ", " ooooo ", " oo    "),
  QUADLUPI(8, "      oo ", "      ooo", "ooooooooo", "ooo ooo  ", "  ooo oo ", " oooo oo ", " o    oo ", "       o "),
  /** 5 solutions */
  QUADPEDI(8, "      oo ", "     oooo", " ooooooo ", "ooooooo  ", "  ooo oo ", " oooo oo ", " o    oo ", "       o "),
  /** 1 solution */
  CHEESFAC(8, "o o o o", "ooooooo", "o o o o", " oooooo", "oo o  o", "ooooooo"),
  /** 3 solutions */
  PIGGYBNK(8, " ooooooo ", "oooooo oo", "oo  o  oo", "ooo  o oo", "  ooooooo", "   o   o "),
  /** 2 solutions */
  PIGGYUPI(8, "  o      ", " oo oooo ", "oooooo oo", "oo  o  oo", "ooo  o oo", "  ooooooo", "   o   o "),
  /** many solutions */
  PIGGYEAR(8, "  o      ", " oo oooo ", " ooooo oo", "ooo oo oo", " oo  oooo", "  oooooo ", "   o     "),
  CLOVERCL(8, "oo ooo ", "oo  ooo", "oooooo ", "  oo   ", " oooo  ", "oo oo  ", "oo ooo ", "  oooo "),
  // ---
  PACMANAG(9, "o       ", "oo  oooo", "ooooooo ", " oo oo  ", " ooo oo ", " ooooo  ", " oo ooo ", " o   ooo"),
  PACMANDO(9, " o  oooo", " oooooo ", "ooo oo  ", "oooo oo ", " ooooo  ", "ooo ooo ", " o   ooo"),
  NOIDEANO(9, " ooooooo ", "oo o oooo", " ooooooo ", "oo ooo o ", " ooooooo ", "  o o oo "),
  HUNDERTW(9, " o ooooo ", "ooooooo o", " oo ooooo", "oo ooooo ", "oo oo oo "),
  /** 1 solution */
  BABELISH(9, "  o      ", " ooo     ", " oooo  o ", " ooooo oo", "ooo ooooo", "oo  oooo ", "oo   oooo"),
  /** use only 1st solution */
  MODERNHS(9, "oo ooooo ", " o o o oo", "ooooooooo", "oo o oo o", "ooooooooo"),
  /** 6 solutions */
  COOLHUTS(9, "  o      ", "o oo o o ", "oooooo oo", "ooooooooo", "  oo  o o", "ooooooo o"),
  FACTORY9(9, "  o      ", "  o      ", "oooo o o ", "oooooo oo", "ooooooooo", "  oo oo o", "ooooooo o"),
  /** 1 solution */
  STARFIS1(9, "      o   ", "     oo   ", "    oooo  ", "oooooooo  ", "  ooo  o  ", "  oooooo  ", "   ooooooo", "   o  o   ", "   o      "),
  /** 1 solution */
  STARFIS2(9, "      o   ", "      o   ", "    oooo  ", "oooooo o  ", "  ooo oo  ", "  oo ooo  ", "   ooooooo", "   oo o   ", "   o      "),
  /** 6 solutions */
  STARFIS3(9, "      o   ", "      o   ", "    oooo  ", "oooooo o  ", "  oooooo  ", "  oo ooo  ", "   ooooooo", "   oo o   ", "   o      "),
  /** use solutions: 0, 2 */
  FANTASY9(9, "  o      ", "  o  o   ", "  oooo   ", " ooo oo  ", " oo ooo  ", "oo oooooo", " ooooo oo", "  o  oo  ", "  o   o  "),
  /** many solutions */
  TREASURE(9, "ooo  o  ", "ooooooo ", " o o oo ", "ooo oo  ", "oo o ooo", " oooooo ", "  oooo  "),
  SWITZER1(9, " ooo o ", " oooooo", "ooo ooo", " o   oo", "ooo oo ", "ooooooo", "  o  oo"),
  /** 1 solution */
  SWITZER2(9, "oooo ooo", " oooooo ", " oo oo  ", "oo   oo ", " oo ooo ", "ooooooo ", "oo o ooo"),
  /** many solutions */
  SWITZER3(9, "oooo o ", " oooooo", "ooo ooo", " o   oo", "ooo oo ", "ooooooo", " oo  oo"),
  /** 1 solution */
  TENISHT1(10, "  o oooo  ", "  oooo oo ", "  o oo o  ", "oooo oooo ", "  oooooooo", "   oo o o ", "   oooooo "),
  /** 1 solution */
  TENISHT2(10, "   oooo  ", " oooo oo ", " o oo o  ", "ooo oooo ", " oooooooo", "  oo o o ", "  oooooo "),
  /** 1 solution */
  TENISHT3(10, "  o      ", "  o  o   ", "  ooooo  ", " ooo oo  ", "ooo oooo ", "oo oo ooo", " ooooo o ", "  o oooo ", "  o   o  "),
  /** 1 solution */
  TENISHT4(10, "ooo oo  ", "ooooooo ", " o o oo ", "ooo oo  ", "oo o ooo", " ooooooo", "oooo o  "),
  /** 1 solution */
  TENISHT5(10, " oo oo  ", "oooooooo", " o o oo ", "ooo oo  ", "oo o ooo", " oooooo ", "oooo oo "),
  TENISHT9(10, "  o      ", "  o  o   ", "  ooooo  ", " ooo oo  ", " oo oooo ", "oo oo ooo", " ooooo oo", "  o oooo ", "  o   o  "),
  /** 1 solution */
  SWITZERA(10, "  oo o  ", " ooooooo", "ooo oo  ", "oo   oo ", " oo oooo", "oooooooo", "oo o oo "),
  /** 5 solutions */
  SWITZERB(10, " ooo o  ", " ooooooo", "ooo oo  ", "oo   oo ", "ooo oooo", "oooooo  ", " o ooooo"),
  // --- AT CHILLY's
  TENNER01(10, "    o o ", "oo ooooo", " ooooooo", "oo oo oo", "o oo ooo", "ooooooo ", "oo  oo  "),
  ELEVEN01(11, "    o oo ", "oo oooooo", " ooooo o ", "oo oo ooo", "o oo oooo", "oooooooo ", "oo  ooo  "),
  ELEVEN02(11, "  ooo  oo", " oo ooooo", "  oooo  o", " oo oo oo", "oo oo ooo", " oooooooo", "ooo  ooo "),
  ELEVEN03(11, "  o oo   ", "  oooooo ", " oooo oo ", "ooo oo o ", " o ooo oo", " ooo  oo ", "ooooooooo", "  o  o o "),
  ELEVEN04(11, "      o   ", " o    ooo ", " ooo oo oo", "oo ooo oo ", " oo oooooo", "oooooooo o", "   ooo oo ", "       o  ", "      oo  "),
  ELEVEN05(11, "   o o oo ", " oooooo o ", "ooo  oooo ", " o  oo ooo", " ooooooo o", " o ooo ooo", "   o oooo "),
  ELEVEN06(11, "  o    o  ", " ooooo oo ", "oo  oooo  ", "ooooo oooo", "ooooooo o ", " oooo oo  ", "  o ooo   ", "    o o   "),
  ELEVEN07(11, "   o     ", "  oo o o ", " ooo ooo ", "oooooo oo", " o oooooo", "oooooo oo", "  oo ooo ", "   o o oo"),
  TWELVE01(12, "       oo", "  o oo oo", "  oooooo ", " oooo oo ", "ooo oo o ", " o ooo oo", " ooo  oo ", "ooooooooo", "  o  o o "),
  TWELVE02(12, "  o   o   ", "  o ooooo ", " oooooo oo", "oo ooo oo ", " oo ooooo ", "oooo oo o ", " oooooo   ", "oo  o     ", "   oo     "),
  TWELVE03(12, "      o    ", " o o  ooo  ", " oooooo oo ", "oo oo  oo  ", " oo oo ooo ", " ooooooo oo", " o ooo ooo ", "   oo   oo ", "   o       "),
  TWELVE04(12, "   o     ", "  oo o o ", " ooo ooo ", "oooooo oo", " o oooooo", "oooooo oo", "  oo ooo ", "   o o oo", "    oo   ", "   ooo   "),
  TWELVE05(12, " o o o oo ", " oooooo oo", "oo oooooo ", " oo oo ooo", " ooooooo o", " o ooo ooo", "   o oooo "),
  TWELVE06(12, "   o o oo  ", " oooooo oo ", "ooo  oooo  ", "oo  oo oooo", " ooooooo o ", " oo oo ooo ", "  o  oooo  ", "       o   "),
  TWELVE07(12, "   o       ", "  oo o o   ", " ooo ooo   ", "oooooo oo o", " o oooooooo", "oooooo ooo ", "  oooooo   ", "   o o oo  "),
  FINALBOS(12, "  o   o   ", "  o ooooo ", " oooooo   ", "oooooo ooo", "ooo o ooo ", " ooooooooo", "oo oo o o ", " o o  o   "),
  // --- BIRTHDAY
  KIRCH05(5, " oo ", "oooo", "ooo ", "oo  ", " oo ", "oooo", "  o "),
  KIRCH06(6, "   o ", "o ooo", "ooooo", "oooo ", " oo  ", "  oo ", " oooo", "   o "),
  KIRCH07(7, "   o  ", "o ooo ", "ooooo ", "oooo  ", " oo   ", "  oo  ", " ooooo", " o oo "),
  KIRCH08(8, "   o   ", "o ooo  ", "ooooo  ", "oooo   ", " oo    ", "  oo o ", " oooooo", " o oooo"),
  KIRCH09(9, "   o   ", "o ooo  ", "ooooooo", "oooo  o", " oo  oo", "  oo o ", " oooooo", " o oooo"),
  KIRCH10(10, "   o    ", "o ooo o ", "ooooooo ", "oooo  oo", " oo  ooo", " ooo o  ", " oooooo ", "oo o ooo", "     o  "),
  KIRCH11(11, "   o    ", "o ooo o ", "ooooooo ", "oooo  oo", " o   ooo", "oooo o  ", " oooooo ", "oooooooo", " o   o  "),
  KIRCH12(12, "    o    ", " o ooo oo", "oooooooo ", "ooooo  oo", "  o   ooo", " ooo  oo ", "  oo ooo ", " oooooooo", "  o o oo "), //
  ;

  private final int use;
  private final UbongoBoard ubongoBoard;

  private UbongoBoards(int use, String... strings) {
    this.use = use;
    // System.out.println("========================");
    // System.out.println("NAME = "+name());
    ubongoBoard = UbongoBoard.of(strings);
  }

  public int use() {
    return use;
  }

  public UbongoBoard board() {
    return ubongoBoard;
  }

  public List<UbongoSolution> solve() {
    return ubongoBoard.filter0(use);
  }
}
