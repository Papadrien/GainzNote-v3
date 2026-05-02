import 'dart:math';
import 'package:flutter/material.dart';

/// Overlay de particules "pelotes de laine" pour le chat.
/// Couleur rouge translucide, animation en boucle avec fondu pour éviter
/// toute disparition brusque.
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
    _particles = List.generate(14, (i) => _YarnParticle.random(_rng, i));
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

/// Palette rouge translucide
const _yarnColors = [
  Color(0xFFE53935), // rouge vif
  Color(0xFFEF5350), // rouge moyen
  Color(0xFFC62828), // rouge foncé
  Color(0xFFFF5252), // rouge clair
  Color(0xFFB71C1C), // rouge très foncé
  Color(0xFFFF1744), // rouge-rose
];

class _YarnParticle {
  final double x;
  final double y;
  final double radius;
  final double phase;      // phase de départ dans le cycle [0,1]
  final double driftX;
  final double driftY;
  final double speed;
  final double rotation;
  final Color color;
  final int loops;

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

  factory _YarnParticle.random(Random rng, int index) {
    return _YarnParticle(
      x: 0.05 + rng.nextDouble() * 0.90,
      y: 0.05 + rng.nextDouble() * 0.90,
      radius: 10.0 + rng.nextDouble() * 12.0,
      // Phases décalées régulièrement pour que les particules n'apparaissent
      // et disparaissent à des moments différents (pas toutes en même temps).
      phase: index / 14.0,
      driftX: 0.03 + rng.nextDouble() * 0.06,
      driftY: 0.02 + rng.nextDouble() * 0.04,
      speed: 0.4 + rng.nextDouble() * 0.4,
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

      // Fondu entrée/sortie sur 20% du cycle pour éviter la disparition brusque
      final fade = _fadeAlpha(t, fadeIn: 0.20, fadeOut: 0.20);
      if (fade <= 0) continue;

      // Opacité globale faible pour rester discret (translucide)
      final globalAlpha = fade * 0.42;

      final cx = (p.x + sin(t * pi * 2) * p.driftX) * size.width;
      final cy = (p.y + cos(t * pi * 2 + p.phase * pi) * p.driftY) * size.height;

      // Légère pulsation du rayon
      final r = p.radius * (0.9 + 0.1 * sin(t * pi * 4));

      // Rotation progressive
      final rot = p.rotation + t * pi * 2;

      canvas.save();
      canvas.translate(cx, cy);
      canvas.rotate(rot);
      _drawYarnBall(canvas, r, p.color, p.loops, globalAlpha);
      canvas.restore();
    }
  }

  double _fadeAlpha(double t, {required double fadeIn, required double fadeOut}) {
    if (t < fadeIn) return t / fadeIn;
    if (t > 1.0 - fadeOut) return (1.0 - t) / fadeOut;
    return 1.0;
  }

  void _drawYarnBall(Canvas canvas, double r, Color color, int loops, double alpha) {
    // Corps principal : rouge translucide
    final bodyPaint = Paint()
      ..color = color.withValues(alpha: alpha * 0.75)
      ..style = PaintingStyle.fill;
    canvas.drawCircle(Offset.zero, r, bodyPaint);

    // Contour légèrement plus opaque
    final borderPaint = Paint()
      ..color = color.withValues(alpha: alpha)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 1.2;
    canvas.drawCircle(Offset.zero, r, borderPaint);

    // Fils en spirale
    final threadPaint = Paint()
      ..color = Colors.white.withValues(alpha: alpha * 0.35)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 1.0
      ..strokeCap = StrokeCap.round;

    for (int l = 0; l < loops; l++) {
      final angleOffset = (l / loops) * pi;
      final path = Path();
      bool first = true;
      for (int i = 0; i <= 40; i++) {
        final a = (i / 40) * pi * 2;
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

    // Petit fil libre
    final tailPaint = Paint()
      ..color = color.withValues(alpha: alpha * 0.85)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 1.5
      ..strokeCap = StrokeCap.round;

    final tail = Path();
    tail.moveTo(r * 0.6, -r * 0.3);
    tail.quadraticBezierTo(r * 1.3, -r * 0.6, r * 1.1, -r * 1.1);
    canvas.drawPath(tail, tailPaint);

    // Reflet discret
    final highlightPaint = Paint()
      ..color = Colors.white.withValues(alpha: alpha * 0.25)
      ..style = PaintingStyle.fill;
    canvas.drawCircle(Offset(-r * 0.3, -r * 0.3), r * 0.22, highlightPaint);
  }

  @override
  bool shouldRepaint(_YarnPainter old) => old.progress != progress;
}
