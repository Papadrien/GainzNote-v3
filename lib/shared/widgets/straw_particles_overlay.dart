import 'dart:math';
import 'package:flutter/material.dart';

/// Overlay de particules "paille" pour la poule.
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
    _pieces = List.generate(25, (i) => _StrawPiece.random(_rng, i));
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
  Color(0xFFD4A017),
  Color(0xFFE8C84A),
  Color(0xFFBF9B30),
  Color(0xFFF0D060),
  Color(0xFFC8A84B),
];

class _StrawPiece {
  final double x;
  final double y;
  final double length;
  final double thickness;
  final double angle;
  final double phase;
  final double floatAmp;
  final double floatFreq;
  final double driftX;
  final double speed;
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

  factory _StrawPiece.random(Random rng, int index) {
    return _StrawPiece(
      x: rng.nextDouble(),
      y: 0.58 + rng.nextDouble() * 0.40,
      length: 12.0 + rng.nextDouble() * 23.0,
      thickness: 1.5 + rng.nextDouble() * 1.5,
      angle: (rng.nextDouble() - 0.5) * pi * 0.40,
      phase: index / 25.0,
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

      final x = (p.x + sin(t * pi * 2) * p.driftX) * size.width;
      final y = (p.y + sin(t * pi * 2 * p.floatFreq + p.phase * pi) * p.floatAmp) * size.height;
      final currentAngle = p.angle + sin(t * pi * 2 * 0.7) * 0.15;

      // Fondu doux : 20% fade in / 20% fade out
      final fade = _fadeAlpha(t, fadeIn: 0.20, fadeOut: 0.20);
      final alpha = (fade * (0.55 + 0.25 * sin(t * pi * 2))).clamp(0.0, 1.0);
      if (alpha <= 0) continue;

      canvas.save();
      canvas.translate(x, y);
      canvas.rotate(currentAngle);

      final paint = Paint()
        ..color = p.color.withValues(alpha: alpha)
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

  double _fadeAlpha(double t, {required double fadeIn, required double fadeOut}) {
    if (t < fadeIn) return t / fadeIn;
    if (t > 1.0 - fadeOut) return (1.0 - t) / fadeOut;
    return 1.0;
  }

  @override
  bool shouldRepaint(_StrawPainter old) => old.progress != progress;
}
