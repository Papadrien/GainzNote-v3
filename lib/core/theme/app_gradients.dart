import 'package:flutter/material.dart';

class AppGradients {
  AppGradients._();

  // ── Setup: fond uni couleur animale pâle ──
  static const LinearGradient duckSetup = LinearGradient(
    begin: Alignment.topCenter,
    end: Alignment.bottomCenter,
    colors: [Color(0xFFFFF3D6), Color(0xFFFFEDBF), Color(0xFFFFE8A8)],
    stops: [0.0, 0.5, 1.0],
  );
  static const LinearGradient dogSetup = LinearGradient(
    begin: Alignment.topCenter,
    end: Alignment.bottomCenter,
    colors: [Color(0xFFDCEEFF), Color(0xFFCDE5FF), Color(0xFFBEDCFF)],
    stops: [0.0, 0.5, 1.0],
  );

  // ── Timer: même fond couleur animale (cohérent) ──
  static const LinearGradient duckTimer = LinearGradient(
    begin: Alignment.topCenter,
    end: Alignment.bottomCenter,
    colors: [Color(0xFFFFF3D6), Color(0xFFFFEDBF), Color(0xFFFFE8A8)],
    stops: [0.0, 0.5, 1.0],
  );
  static const LinearGradient dogTimer = LinearGradient(
    begin: Alignment.topCenter,
    end: Alignment.bottomCenter,
    colors: [Color(0xFFDCEEFF), Color(0xFFCDE5FF), Color(0xFFBEDCFF)],
    stops: [0.0, 0.5, 1.0],
  );
}
