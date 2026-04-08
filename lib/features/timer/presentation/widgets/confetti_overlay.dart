import 'dart:math';
import 'package:flutter/material.dart';

class ConfettiOverlay extends StatefulWidget {
  const ConfettiOverlay({super.key});
  @override
  State<ConfettiOverlay> createState() => _ConfettiOverlayState();
}

class _ConfettiOverlayState extends State<ConfettiOverlay>
    with SingleTickerProviderStateMixin {
  late AnimationController _ctrl;
  late List<_Confetti> _pieces;
  final _rng = Random();

  @override
  void initState() {
    super.initState();
    _pieces = List.generate(80, (_) => _Confetti(_rng));
    _ctrl = AnimationController(
      vsync: this, duration: const Duration(seconds: 8))
      ..forward();
  }

  @override
  void dispose() { _ctrl.dispose(); super.dispose(); }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: _ctrl,
      builder: (_, __) {
        return CustomPaint(
          size: MediaQuery.of(context).size,
          painter: _ConfettiPainter(_pieces, _ctrl.value),
        );
      },
    );
  }
}

class _Confetti {
  final double x;
  final double startDelay;
  final double fallSpeed;
  final double size;
  final double drift;
  final double rotSpeed;
  final Color color;
  final int shape;

  _Confetti(Random rng)
      : x = rng.nextDouble(),
        startDelay = rng.nextDouble() * 0.3,
        fallSpeed = 0.5 + rng.nextDouble() * 0.5,
        size = 6 + rng.nextDouble() * 10,
        drift = (rng.nextDouble() - 0.5) * 0.15,
        rotSpeed = rng.nextDouble() * 6,
        shape = rng.nextInt(3),
        color = [
          const Color(0xFFFF6B6B),
          const Color(0xFFFFD43B),
          const Color(0xFF74C0FC),
          const Color(0xFF69DB7C),
          const Color(0xFFB197FC),
          const Color(0xFFF783AC),
          const Color(0xFFFF922B),
        ][rng.nextInt(7)];
}

class _ConfettiPainter extends CustomPainter {
  final List<_Confetti> pieces;
  final double t;
  _ConfettiPainter(this.pieces, this.t);

  @override
  void paint(Canvas canvas, Size size) {
    for (final c in pieces) {
      final adjusted = ((t - c.startDelay) / (1.0 - c.startDelay)).clamp(0.0, 1.0);
      final progress = adjusted * c.fallSpeed + adjusted * (1.0 - c.fallSpeed);
      final y = -c.size + progress * (size.height + c.size * 2);
      if (y > size.height + c.size) continue;
      final x = c.x * size.width + sin(t * c.rotSpeed * pi) * c.drift * size.width;
      final paint = Paint()..color = c.color.withOpacity(0.9);
      final rot = t * c.rotSpeed * pi;

      canvas.save();
      canvas.translate(x, y);
      canvas.rotate(rot);

      if (c.shape == 0) {
        canvas.drawRect(Rect.fromCenter(center: Offset.zero,
          width: c.size, height: c.size * 0.6), paint);
      } else if (c.shape == 1) {
        canvas.drawCircle(Offset.zero, c.size * 0.4, paint);
      } else {
        final path = Path()
          ..moveTo(0, -c.size * 0.5)
          ..lineTo(c.size * 0.4, c.size * 0.3)
          ..lineTo(-c.size * 0.4, c.size * 0.3)
          ..close();
        canvas.drawPath(path, paint);
      }
      canvas.restore();
    }
  }

  @override
  bool shouldRepaint(_ConfettiPainter old) => true;
}
