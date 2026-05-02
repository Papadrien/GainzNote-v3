import 'dart:math';
import 'package:flutter/material.dart';

/// Overlay de particules "paille" pour la poule.
/// Brins de paille qui flottent doucement, restent proches du bas.
class StrawParticlesOverlay extends StatefulWidget {
  const StrawParticlesOverlay({super.key});

  @override
  State<StrawParticlesOverlay> createState() => _StrawParticlesOverlayState();
}

class _StrawParticlesOverlayState extends State<StrawParticlesOverlay>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late List<_StrawPiece> _pieces;
  final Random _rng = Random(99);

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 7),
    )..repeat();

    _pieces = List.generate(25, (_) => _StrawPiece.random(_rng));
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return IgnorePointer(
      child: AnimatedBuilder(
        animation: _controller,
        builder: (context, _) => CustomPaint(
          painter: _StrawPainter(pieces: _pieces, progress: _controller.value),
          size: Size.infinite,
        ),
      ),
    );
  }
}

const _strawColors = [
  Color(0xFFD4A017), // jaune paille doré
  Color(0xFFE8C84A), // jaune clair
  Color(0xFFBF9B30), // paille foncée
  Color(0xFFF0D060), // jaune doux
  Color(0xFFC8A84B), // brun doré
];

class _StrawPiece {
  final double x;         // X normalisé [0,1]
  final double y;         // Y normalisé (concentré en bas) [0.55, 1.0]
  final double length;    // longueur du brin [12, 35]
  final double thickness; // épaisseur [1.5, 3.0]
  final double angle;     // angle initial (quasi horizontal ±30°)
  final double phase;     // décalage phase
  final double floatAmp;  // amplitude flottement vertical
  final double floatFreq; // fréquence flottement
  final double driftX;    // dérive X légère
  final double speed;     // vitesse de dérive
  final Color color;

  const _StrawPiece({
    required this.x,
    required this.y,
    required this.length,
    required this.thickness,
    required this.angle,
    required this.phase,
    required this.floatAmp,
    required this.floatFreq,
    required this.driftX,
    required this.speed,
    required this.color,
  });

  factory _StrawPiece.random(Random rng) {
    return _StrawPiece(
      x: rng.nextDouble(),
      y: 0.58 + rng.nextDouble() * 0.40,
      length: 12.0 + rng.nextDouble() * 23.0,
      thickness: 1.5 + rng.nextDouble() * 1.5,
      // Angle quasi-horizontal avec légère inclinaison (−35° à +35°)
      angle: (rng.nextDouble() - 0.5) * pi * 0.40,
      phase: rng.nextDouble(),
      floatAmp: 0.005 + rng.nextDouble() * 0.015,
      floatFreq: 0.8 + rng.nextDouble() * 1.5,
      driftX: (rng.nextDouble() - 0.5) * 0.008,
      speed: 0.3 + rng.nextDouble() * 0.4,
      color: _strawColors[rng.nextInt(_strawColors.length)],
    );
  }
}

class _StrawPainter extends CustomPainter {
  final List<_StrawPiece> pieces;
  final double progress;

  const _StrawPainter({required this.pieces, required this.progress});

  @override
  void paint(Canvas canvas, Size size) {
    for (final p in pieces) {
      final t = (progress * p.speed + p.phase) % 1.0;

      // Position avec flottement vertical léger
      final x = (p.x + sin(t * pi * 2) * p.driftX) * size.width;
      final y = (p.y + sin(t * pi * 2 * p.floatFreq + p.phase * pi) * p.floatAmp) * size.height;

      // Légère rotation oscillante autour de l'angle de base
      final currentAngle = p.angle + sin(t * pi * 2 * 0.7) * 0.15;

      // Opacité stable (paille posée)
      final alpha = 0.55 + 0.25 * sin(t * pi * 2);

      canvas.save();
      canvas.translate(x, y);
      canvas.rotate(currentAngle);

      // Brin de paille : rectangle arrondi allongé
      final paint = Paint()
        ..color = p.color.withValues(alpha: alpha.clamp(0.0, 1.0))
        ..style = PaintingStyle.fill
        ..strokeCap = StrokeCap.round;

      final rrect = RRect.fromRectAndRadius(
        Rect.fromCenter(
          center: Offset.zero,
          width: p.length,
          height: p.thickness,
        ),
        Radius.circular(p.thickness / 2),
      );
      canvas.drawRRect(rrect, paint);

      // Ligne centrale légèrement plus sombre (nervure)
      final veinPaint = Paint()
        ..color = p.color.withValues(alpha: alpha * 0.4)
        ..style = PaintingStyle.stroke
        ..strokeWidth = 0.6;
      canvas.drawLine(
        Offset(-p.length / 2 + 2, 0),
        Offset(p.length / 2 - 2, 0),
        veinPaint,
      );

      canvas.restore();
    }
  }

  @override
  bool shouldRepaint(_StrawPainter old) => old.progress != progress;
}
