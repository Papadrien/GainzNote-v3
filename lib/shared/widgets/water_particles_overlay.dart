import 'dart:math';
import 'package:flutter/material.dart';

/// Overlay de particules "eau" animées (bulles + gouttes).
/// À afficher uniquement quand le crocodile est sélectionné.
class WaterParticlesOverlay extends StatefulWidget {
  const WaterParticlesOverlay({super.key});

  @override
  State<WaterParticlesOverlay> createState() => _WaterParticlesOverlayState();
}

class _WaterParticlesOverlayState extends State<WaterParticlesOverlay>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late List<_WaterParticle> _particles;
  final Random _rng = Random(42);

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 6),
    )..repeat();

    _particles = List.generate(28, (i) => _WaterParticle.random(_rng, i));
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
        builder: (context, _) {
          return CustomPaint(
            painter: _WaterParticlesPainter(
              particles: _particles,
              progress: _controller.value,
            ),
            size: Size.infinite,
          );
        },
      ),
    );
  }
}

enum _ParticleType { bubble, drop }

class _WaterParticle {
  final double x;        // position horizontale normalisée [0,1]
  final double startY;   // position verticale de départ normalisée [0.1, 1.0]
  final double speed;    // vitesse normalisée [0.08, 0.25]
  final double size;     // rayon [3, 10]
  final double opacity;  // opacité de base [0.15, 0.45]
  final double phase;    // décalage de phase [0,1]
  final double drift;    // dérive horizontale sinusoïdale amplitude
  final _ParticleType type;

  const _WaterParticle({
    required this.x,
    required this.startY,
    required this.speed,
    required this.size,
    required this.opacity,
    required this.phase,
    required this.drift,
    required this.type,
  });

  factory _WaterParticle.random(Random rng, int index) {
    return _WaterParticle(
      x: rng.nextDouble(),
      startY: 0.2 + rng.nextDouble() * 0.8,
      speed: 0.06 + rng.nextDouble() * 0.18,
      size: 3.0 + rng.nextDouble() * 8.0,
      opacity: 0.10 + rng.nextDouble() * 0.30,
      phase: rng.nextDouble(),
      drift: 0.01 + rng.nextDouble() * 0.03,
      type: index % 5 == 0 ? _ParticleType.drop : _ParticleType.bubble,
    );
  }
}

class _WaterParticlesPainter extends CustomPainter {
  final List<_WaterParticle> particles;
  final double progress;

  const _WaterParticlesPainter({
    required this.particles,
    required this.progress,
  });

  @override
  void paint(Canvas canvas, Size size) {
    for (final p in particles) {
      // Progression cyclique avec décalage de phase
      final t = ((progress + p.phase) % 1.0);

      // La particule monte de startY vers 0 (haut de l'écran)
      final rawY = p.startY - t * p.speed * 8.0;
      // Si hors écran, on repart du bas
      final normY = rawY < -0.05 ? rawY + 1.1 : rawY;
      final y = normY * size.height;

      // Dérive horizontale sinusoïdale
      final x = (p.x + sin(t * pi * 2 + p.phase * pi * 2) * p.drift) * size.width;

      // Opacité qui pulse légèrement
      final alpha = (p.opacity * (0.7 + 0.3 * sin(t * pi * 4))).clamp(0.0, 1.0);

      if (p.type == _ParticleType.bubble) {
        _drawBubble(canvas, Offset(x, y), p.size, alpha);
      } else {
        _drawDrop(canvas, Offset(x, y), p.size, alpha);
      }
    }
  }

  void _drawBubble(Canvas canvas, Offset center, double radius, double alpha) {
    // Cercle bleu translucide avec reflet
    final paint = Paint()
      ..color = const Color(0xFF4FC3F7).withValues(alpha: alpha * 0.5)
      ..style = PaintingStyle.fill;
    canvas.drawCircle(center, radius, paint);

    // Contour
    final border = Paint()
      ..color = const Color(0xFF81D4FA).withValues(alpha: alpha)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 1.0;
    canvas.drawCircle(center, radius, border);

    // Petit reflet blanc
    final highlight = Paint()
      ..color = Colors.white.withValues(alpha: alpha * 0.7)
      ..style = PaintingStyle.fill;
    canvas.drawCircle(
      Offset(center.dx - radius * 0.3, center.dy - radius * 0.3),
      radius * 0.25,
      highlight,
    );
  }

  void _drawDrop(Canvas canvas, Offset center, double size, double alpha) {
    // Goutte d'eau
    final paint = Paint()
      ..color = const Color(0xFF29B6F6).withValues(alpha: alpha * 0.7)
      ..style = PaintingStyle.fill;

    final path = Path();
    // Corps de la goutte
    path.addOval(Rect.fromCenter(
      center: Offset(center.dx, center.dy + size * 0.2),
      width: size * 1.0,
      height: size * 1.2,
    ));
    // Pointe vers le haut
    path.moveTo(center.dx, center.dy - size * 0.8);
    path.quadraticBezierTo(
      center.dx + size * 0.5, center.dy,
      center.dx, center.dy + size * 0.2,
    );
    path.quadraticBezierTo(
      center.dx - size * 0.5, center.dy,
      center.dx, center.dy - size * 0.8,
    );
    canvas.drawPath(path, paint);
  }

  @override
  bool shouldRepaint(_WaterParticlesPainter old) => old.progress != progress;
}
