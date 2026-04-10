import 'dart:math';
import 'package:flutter/material.dart';

/// CustomPainter qui dessine un bouton pill "fait main" avec :
/// - Contour noir irrégulier (effet crayon)
/// - Fond coloré avec la même forme wobbly
/// - Petits traits décoratifs à l'intérieur (V, arcs, lignes)
/// - Ombre portée décalée
class SketchyButtonPainter extends CustomPainter {
  final Color fillColor;
  final int seed;
  final bool isPressed;

  SketchyButtonPainter({
    required this.fillColor,
    required this.seed,
    this.isPressed = false,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final w = size.width;
    final h = size.height;
    final radius = h / 2; // Full pill shape

    // === Shadow (offset bottom-right) ===
    final shadowPath = _buildWobblyPillPath(w, h, radius, seed + 100);
    final shadowPaint = Paint()
      ..color = const Color(0x33000000) // ~0.2 alpha
      ..maskFilter = const MaskFilter.blur(BlurStyle.normal, 4);
    canvas.save();
    canvas.translate(3, 4);
    canvas.drawPath(shadowPath, shadowPaint);
    canvas.restore();

    // === Fill ===
    final mainPath = _buildWobblyPillPath(w, h, radius, seed);
    final fillPaint = Paint()
      ..color = isPressed
          ? Color.lerp(fillColor, Colors.black, 0.1)!
          : fillColor
      ..style = PaintingStyle.fill;
    canvas.drawPath(mainPath, fillPaint);

    // === Interior decorative strokes ===
    _drawInteriorMarks(canvas, w, h, radius);

    // === Black outline (drawn twice for sketchy overlap) ===
    for (int i = 0; i < 2; i++) {
      final outlinePath = _buildWobblyPillPath(
        w, h, radius, seed + 200 + i,
        wobbleAmount: 1.8 + i * 0.4,
      );
      final outlinePaint = Paint()
        ..color = const Color(0xFF2B2B2B)
        ..style = PaintingStyle.stroke
        ..strokeWidth = 3.5 - i * 0.8
        ..strokeCap = StrokeCap.round
        ..strokeJoin = StrokeJoin.round;
      canvas.drawPath(outlinePath, outlinePaint);
    }
  }

  /// Builds a pill/capsule path with wobbly edges
  Path _buildWobblyPillPath(
    double w,
    double h,
    double radius,
    int pathSeed, {
    double wobbleAmount = 2.0,
  }) {
    final rng = Random(pathSeed);
    final path = Path();
    const int segments = 64;
    final points = <Offset>[];

    for (int i = 0; i < segments; i++) {
      final t = i / segments;
      Offset point;

      if (t < 0.25) {
        // Top edge (left to right)
        final frac = t / 0.25;
        point = Offset(radius + frac * (w - 2 * radius), 0);
      } else if (t < 0.5) {
        // Right cap (semicircle)
        final angle = -pi / 2 + (t - 0.25) / 0.25 * pi;
        point = Offset(
          w - radius + cos(angle) * radius,
          h / 2 + sin(angle) * (h / 2),
        );
      } else if (t < 0.75) {
        // Bottom edge (right to left)
        final frac = (t - 0.5) / 0.25;
        point = Offset(w - radius - frac * (w - 2 * radius), h);
      } else {
        // Left cap (semicircle)
        final angle = pi / 2 + (t - 0.75) / 0.25 * pi;
        point = Offset(
          radius + cos(angle) * radius,
          h / 2 + sin(angle) * (h / 2),
        );
      }

      // Add wobble
      final wobbleX = (rng.nextDouble() - 0.5) * 2 * wobbleAmount;
      final wobbleY = (rng.nextDouble() - 0.5) * 2 * wobbleAmount;
      points.add(Offset(point.dx + wobbleX, point.dy + wobbleY));
    }

    // Build smooth path with quadratic bezier through points
    path.moveTo(points[0].dx, points[0].dy);
    for (int i = 0; i < points.length; i++) {
      final p0 = points[i];
      final p1 = points[(i + 1) % points.length];
      final midX = (p0.dx + p1.dx) / 2;
      final midY = (p0.dy + p1.dy) / 2;
      path.quadraticBezierTo(p0.dx, p0.dy, midX, midY);
    }
    path.close();
    return path;
  }

  /// Draw small decorative marks inside the button (V shapes, lines, arcs)
  void _drawInteriorMarks(Canvas canvas, double w, double h, double radius) {
    final darkerColor = Color.lerp(fillColor, Colors.black, 0.18)!;
    final markPaint = Paint()
      ..color = darkerColor.withValues(alpha: 0.35)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 1.5
      ..strokeCap = StrokeCap.round;

    final markRng = Random(seed + 500);
    final markCount = 8 + markRng.nextInt(5);

    for (int i = 0; i < markCount; i++) {
      final cx = radius * 0.6 + markRng.nextDouble() * (w - 1.2 * radius);
      final cy = h * 0.22 + markRng.nextDouble() * h * 0.56;
      final markSize = 4.0 + markRng.nextDouble() * 6;
      final angle = markRng.nextDouble() * pi * 0.3 - 0.15;
      final markType = markRng.nextInt(3);

      canvas.save();
      canvas.translate(cx, cy);
      canvas.rotate(angle);

      if (markType == 0) {
        // V shape (like on the green button in the reference)
        final p = Path()
          ..moveTo(-markSize / 2, -markSize / 3)
          ..lineTo(0, markSize / 3)
          ..lineTo(markSize / 2, -markSize / 3);
        canvas.drawPath(p, markPaint);
      } else if (markType == 1) {
        // Small line
        canvas.drawLine(
          Offset(-markSize / 2, 0),
          Offset(markSize / 2, 0),
          markPaint,
        );
      } else {
        // Small arc
        final p = Path()
          ..moveTo(-markSize / 2, markSize / 4)
          ..quadraticBezierTo(0, -markSize / 3, markSize / 2, markSize / 4);
        canvas.drawPath(p, markPaint);
      }

      canvas.restore();
    }
  }

  @override
  bool shouldRepaint(SketchyButtonPainter oldDelegate) =>
      fillColor != oldDelegate.fillColor ||
      seed != oldDelegate.seed ||
      isPressed != oldDelegate.isPressed;
}
