import 'dart:math';
import 'package:flutter/material.dart';

/// Overlay de particules "pelotes de laine" pour le chat.
class YarnParticlesOverlay extends StatefulWidget {
  const YarnParticlesOverlay({super.key});

  @override
  State<YarnParticlesOverlay> createState() => _YarnParticlesOverlayState();
}

class _YarnParticlesOverlayState extends State<YarnParticlesOverlay>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late List<_YarnParticle> _particles;
  final Random _rng = Random(7);

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 8),
    )..repeat();
    _particles = List.generate(14, (i) => _YarnParticle.random(_rng));
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
          painter: _YarnPainter(particles: _particles, progress: _controller.value),
          size: Size.infinite,
        ),
      ),
    );
  }
}

/// Couleurs douces de laine
const _yarnColors = [
  Color(0xFFE91E8C), // rose vif
  Color(0xFFFF6FB7), // rose clair
  Color(0xFFA855F7), // violet
  Color(0xFFFF9800), // orange
  Color(0xFF42A5F5), // bleu ciel
  Color(0xFFEC407A), // framboise
];

class _YarnParticle {
  final double x;        // position X normalisée [0,1]
  final double y;        // position Y normalisée [0,1]
  final double radius;   // rayon de la pelote [10, 22]
  final double phase;    // décalage de phase
  final double driftX;   // amplitude dérive X
  final double driftY;   // amplitude dérive Y
  final double speed;    // vitesse de déplacement
  final double rotation; // rotation initiale
  final Color color;     // couleur de la laine
  final int loops;       // nombre de spirales [3,6]

  const _YarnParticle({
    required this.x,
    required this.y,
    required this.radius,
    required this.phase,
    required this.driftX,
    required this.driftY,
    required this.speed,
    required this.rotation,
    required this.color,
    required this.loops,
  });

  factory _YarnParticle.random(Random rng) {
    return _YarnParticle(
      x: 0.05 + rng.nextDouble() * 0.90,
      y: 0.05 + rng.nextDouble() * 0.90,
      radius: 10.0 + rng.nextDouble() * 12.0,
      phase: rng.nextDouble(),
      driftX: 0.03 + rng.nextDouble() * 0.06,
      driftY: 0.02 + rng.nextDouble() * 0.04,
      speed: 0.5 + rng.nextDouble() * 0.5,
      rotation: rng.nextDouble() * pi * 2,
      color: _yarnColors[rng.nextInt(_yarnColors.length)],
      loops: 3 + rng.nextInt(4),
    );
  }
}

class _YarnPainter extends CustomPainter {
  final List<_YarnParticle> particles;
  final double progress;

  const _YarnPainter({required this.particles, required this.progress});

  @override
  void paint(Canvas canvas, Size size) {
    for (final p in particles) {
      final t = (progress * p.speed + p.phase) % 1.0;

      // Position flottante avec oscillation douce
      final cx = (p.x + sin(t * pi * 2) * p.driftX) * size.width;
      final cy = (p.y + cos(t * pi * 2 + p.phase) * p.driftY) * size.height;

      // Légère pulsation du rayon
      final r = p.radius * (0.9 + 0.1 * sin(t * pi * 4));

      // Rotation progressive
      final rot = p.rotation + t * pi * 2;

      canvas.save();
      canvas.translate(cx, cy);
      canvas.rotate(rot);

      _drawYarnBall(canvas, r, p.color, p.loops);

      canvas.restore();
    }
  }

  void _drawYarnBall(Canvas canvas, double r, Color color, int loops) {
    // Corps principal de la pelote (cercle de base)
    final bodyPaint = Paint()
      ..color = color.withValues(alpha: 0.85)
      ..style = PaintingStyle.fill;
    canvas.drawCircle(Offset.zero, r, bodyPaint);

    // Contour
    final borderPaint = Paint()
      ..color = color.withValues(alpha: 1.0)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 1.2;
    canvas.drawCircle(Offset.zero, r, borderPaint);

    // Fils en spirale autour de la pelote
    final threadPaint = Paint()
      ..color = Colors.white.withValues(alpha: 0.45)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 1.0
      ..strokeCap = StrokeCap.round;

    for (int l = 0; l < loops; l++) {
      final angleOffset = (l / loops) * pi;
      final path = Path();
      bool first = true;
      for (int i = 0; i <= 40; i++) {
        final a = (i / 40) * pi * 2;
        // Ellipse inclinée simulant le fil enroulé
        final ex = cos(a + angleOffset) * r;
        final ey = sin(a + angleOffset) * r * 0.35;
        if (first) {
          path.moveTo(ex, ey);
          first = false;
        } else {
          path.lineTo(ex, ey);
        }
      }
      canvas.drawPath(path, threadPaint);
    }

    // Petit fil libre qui dépasse (queue de laine)
    final tailPaint = Paint()
      ..color = color.withValues(alpha: 0.9)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 1.5
      ..strokeCap = StrokeCap.round;

    final tail = Path();
    tail.moveTo(r * 0.6, -r * 0.3);
    tail.quadraticBezierTo(r * 1.3, -r * 0.6, r * 1.1, -r * 1.1);
    canvas.drawPath(tail, tailPaint);

    // Reflet brillant
    final highlightPaint = Paint()
      ..color = Colors.white.withValues(alpha: 0.35)
      ..style = PaintingStyle.fill;
    canvas.drawCircle(Offset(-r * 0.3, -r * 0.3), r * 0.22, highlightPaint);
  }

  @override
  bool shouldRepaint(_YarnPainter old) => old.progress != progress;
}
