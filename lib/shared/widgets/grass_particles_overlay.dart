import 'dart:math';
import 'package:flutter/material.dart';

/// Overlay de particules "gazon et herbe" pour le chien (et le cheval).
class GrassParticlesOverlay extends StatefulWidget {
  const GrassParticlesOverlay({super.key});

  @override
  State<GrassParticlesOverlay> createState() => _GrassParticlesOverlayState();
}

class _GrassParticlesOverlayState extends State<GrassParticlesOverlay>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late List<_GrassBlade> _blades;
  late List<_LeafParticle> _leaves;
  final Random _rng = Random(13);

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 5),
    )..repeat();

    // Plus de brins d'herbe ancrés en bas
    _blades = List.generate(65, (_) => _GrassBlade.random(_rng));
    // Plus de feuilles flottantes
    _leaves = List.generate(18, (_) => _LeafParticle.random(_rng));
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
          painter: _GrassPainter(
            blades: _blades,
            leaves: _leaves,
            progress: _controller.value,
          ),
          size: Size.infinite,
        ),
      ),
    );
  }
}

// Couleurs herbe
const _grassColors = [
  Color(0xFF4CAF50), // vert moyen
  Color(0xFF66BB6A), // vert clair
  Color(0xFF2E7D32), // vert foncé
  Color(0xFF81C784), // vert pastel
  Color(0xFF388E3C), // vert prairie
  Color(0xFF1B5E20), // vert très foncé
  Color(0xFFA5D6A7), // vert très clair
];

class _GrassBlade {
  final double x;
  final double height;
  final double width;
  final double phase;
  final double sway;
  final Color color;
  final double curve;

  const _GrassBlade({
    required this.x,
    required this.height,
    required this.width,
    required this.phase,
    required this.sway,
    required this.color,
    required this.curve,
  });

  factory _GrassBlade.random(Random rng) {
    return _GrassBlade(
      x: rng.nextDouble(),
      height: 0.05 + rng.nextDouble() * 0.15,
      width: 1.2 + rng.nextDouble() * 2.5,
      phase: rng.nextDouble(),
      sway: 0.006 + rng.nextDouble() * 0.014,
      color: _grassColors[rng.nextInt(_grassColors.length)],
      curve: 0.3 + rng.nextDouble() * 0.5,
    );
  }
}

class _LeafParticle {
  final double x;
  final double startY;
  final double size;
  final double phase;
  final double speed;
  final double driftX;
  final Color color;

  const _LeafParticle({
    required this.x,
    required this.startY,
    required this.size,
    required this.phase,
    required this.speed,
    required this.driftX,
    required this.color,
  });

  factory _LeafParticle.random(Random rng) {
    return _LeafParticle(
      x: rng.nextDouble(),
      startY: 0.65 + rng.nextDouble() * 0.30,
      size: 4.0 + rng.nextDouble() * 6.0,
      phase: rng.nextDouble(),
      speed: 0.04 + rng.nextDouble() * 0.06,
      driftX: (rng.nextDouble() - 0.5) * 0.04,
      color: _grassColors[rng.nextInt(_grassColors.length)],
    );
  }
}

class _GrassPainter extends CustomPainter {
  final List<_GrassBlade> blades;
  final List<_LeafParticle> leaves;
  final double progress;

  const _GrassPainter({
    required this.blades,
    required this.leaves,
    required this.progress,
  });

  @override
  void paint(Canvas canvas, Size size) {
    for (final b in blades) {
      _drawBlade(canvas, size, b);
    }
    for (final l in leaves) {
      _drawLeaf(canvas, size, l);
    }
  }

  void _drawBlade(Canvas canvas, Size size, _GrassBlade b) {
    final t = (progress + b.phase) % 1.0;
    final swayOffset = sin(t * pi * 2) * b.sway * size.width;

    final baseX = b.x * size.width;
    final baseY = size.height;
    final bladeH = b.height * size.height;

    final tipX = baseX + swayOffset;
    final tipY = baseY - bladeH;
    final ctrlX = baseX + swayOffset * b.curve + (swayOffset * 0.5);
    final ctrlY = baseY - bladeH * 0.6;

    final paint = Paint()
      ..color = b.color.withValues(alpha: 0.85)
      ..style = PaintingStyle.stroke
      ..strokeWidth = b.width
      ..strokeCap = StrokeCap.round;

    final path = Path();
    path.moveTo(baseX, baseY);
    path.quadraticBezierTo(ctrlX, ctrlY, tipX, tipY);
    canvas.drawPath(path, paint);
  }

  void _drawLeaf(Canvas canvas, Size size, _LeafParticle l) {
    // t va de 0 à 1 sur le cycle complet
    final t = ((progress * l.speed * 15 + l.phase) % 1.0);

    // Fondu : apparaît sur 15%, stable, disparaît sur 15% avant la fin
    final alpha = _fadeAlpha(t, fadeIn: 0.15, fadeOut: 0.15) * 0.75;
    if (alpha <= 0) return;

    // Monte depuis le bas
    final rawY = l.startY - t * 0.35;
    final normY = rawY < 0.55 ? rawY + 0.5 : rawY;
    final y = normY * size.height;
    final x = (l.x + sin(t * pi * 3) * l.driftX) * size.width;
    final rotation = t * pi * 1.5;

    final paint = Paint()
      ..color = l.color.withValues(alpha: alpha)
      ..style = PaintingStyle.fill;

    canvas.save();
    canvas.translate(x, y);
    canvas.rotate(rotation);

    final rect = Rect.fromCenter(
      center: Offset.zero,
      width: l.size,
      height: l.size * 2.0,
    );
    canvas.drawOval(rect, paint);

    final veinPaint = Paint()
      ..color = Colors.white.withValues(alpha: alpha * 0.4)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 0.8;
    final vein = Path();
    vein.moveTo(0, -l.size);
    vein.lineTo(0, l.size);
    canvas.drawPath(vein, veinPaint);

    canvas.restore();
  }

  /// Courbe de fondu : fade in puis fade out en douceur.
  double _fadeAlpha(double t, {required double fadeIn, required double fadeOut}) {
    if (t < fadeIn) return t / fadeIn;
    if (t > 1.0 - fadeOut) return (1.0 - t) / fadeOut;
    return 1.0;
  }

  @override
  bool shouldRepaint(_GrassPainter old) => old.progress != progress;
}
