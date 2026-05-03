import 'package:flutter/material.dart';

/// Affichage animé du requin — 4 layers (nageoire_arrière, body, nageoire_gauche, nageoire_droite).
///
/// Timing (boucle 2s, t 0→1) :
///   0.0→0.4  pause (scaleY/X = 1.0)
///   0.4→0.5  écrasement (scaleY/X 1.0→0.5)
///   0.5→0.9  pause (scaleY/X = 0.5)
///   0.9→1.0  relâchement (scaleY/X 0.5→1.0)
class SharkAnimatedDisplay extends StatefulWidget {
  final double size;
  final bool animate;
  final bool playOnce;

  const SharkAnimatedDisplay({
    super.key,
    this.size = 180,
    this.animate = true,
    this.playOnce = false,
  });

  @override
  State<SharkAnimatedDisplay> createState() => _SharkAnimatedDisplayState();
}

class _SharkAnimatedDisplayState extends State<SharkAnimatedDisplay>
    with SingleTickerProviderStateMixin {
  late AnimationController _ctrl;

  @override
  void initState() {
    super.initState();
    _ctrl = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 2000),
    );
    _startAnimation();
  }

  void _startAnimation() {
    if (!widget.animate) return;
    if (widget.playOnce) {
      _ctrl.forward(from: 0.0);
    } else {
      _ctrl.repeat();
    }
  }

  @override
  void didUpdateWidget(SharkAnimatedDisplay old) {
    super.didUpdateWidget(old);
    if (widget.animate && !old.animate) {
      _startAnimation();
    } else if (!widget.animate && old.animate) {
      _ctrl.stop();
    }
  }

  @override
  void dispose() {
    _ctrl.dispose();
    super.dispose();
  }

  double _easeInOut(double t) {
    return t < 0.5 ? 2 * t * t : 1 - (-2 * t + 2) * (-2 * t + 2) / 2;
  }

  /// Retourne la valeur de scale selon le timing.
  /// start=1.0, end=0.5 (écrasement à 50%).
  double _computeScale(double t) {
    if (t <= 0.4) {
      return 1.0;
    } else if (t <= 0.5) {
      final p = (t - 0.4) / 0.1;
      return 1.0 - 0.5 * _easeInOut(p); // 1.0 → 0.5
    } else if (t <= 0.9) {
      return 0.5;
    } else {
      final p = (t - 0.9) / 0.1;
      return 0.5 + 0.5 * _easeInOut(p); // 0.5 → 1.0
    }
  }

  @override
  Widget build(BuildContext context) {
    final size = widget.size;

    return AnimatedBuilder(
      animation: _ctrl,
      builder: (_, __) {
        final t = _ctrl.value;
        final scale = _computeScale(t);

        return SizedBox(
          width: size,
          height: size,
          child: Stack(
            children: [
              // Layer 1 : Nageoire arrière (derrière le corps)
              // Ancrage gauche → scaleX, la droite se rapproche de la gauche
              Positioned.fill(
                child: Transform(
                  alignment: Alignment.centerLeft,
                  transform: Matrix4.identity()..scale(scale, 1.0, 1.0),
                  child: Image.asset(
                    'assets/images/shark/shark_nageoire_arriere.png',
                    width: size,
                    height: size,
                    fit: BoxFit.contain,
                  ),
                ),
              ),
              // Layer 2 : Corps (statique, centre)
              Positioned.fill(
                child: Image.asset(
                  'assets/images/shark/shark_body.png',
                  width: size,
                  height: size,
                  fit: BoxFit.contain,
                ),
              ),
              // Layer 3 : Nageoire gauche
              // Ancrage haut → scaleY, le bas se rapproche du haut
              Positioned.fill(
                child: Transform(
                  alignment: Alignment.topCenter,
                  transform: Matrix4.identity()..scale(1.0, scale, 1.0),
                  child: Image.asset(
                    'assets/images/shark/shark_nageoire_gauche.png',
                    width: size,
                    height: size,
                    fit: BoxFit.contain,
                  ),
                ),
              ),
              // Layer 4 : Nageoire droite (même animation que gauche)
              Positioned.fill(
                child: Transform(
                  alignment: Alignment.topCenter,
                  transform: Matrix4.identity()..scale(1.0, scale, 1.0),
                  child: Image.asset(
                    'assets/images/shark/shark_nageoire_droite.png',
                    width: size,
                    height: size,
                    fit: BoxFit.contain,
                  ),
                ),
              ),
            ],
          ),
        );
      },
    );
  }
}
