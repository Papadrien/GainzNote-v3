import 'package:flutter/material.dart';

/// Affichage animé du requin.
///
/// Layering (du fond vers le haut) :
///   1. Nageoire droite   → DERRIÈRE le body
///   2. Nageoire arrière  → DERRIÈRE le body  ← derrière le body
///   3. Body              → statique
///   4. Nageoire gauche   → DEVANT le body
///
/// Animations (controller unique 2000ms, weights 40/10/40/10) :
///   - Nageoires gauche & droite : squash VERTICAL, ancrage `center`.
///     Les deux sont strictement synchronisées.
///   - Nageoire arrière : squash HORIZONTAL, ancrage `center`.
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
  // Un seul controller : toutes les nageoires sont synchrones.
  late AnimationController _ctrl;

  // Nageoire arrière : squash horizontal (battement de queue)
  late Animation<double> _tailScaleX;
  late Animation<double> _tailScaleY;

  // Nageoires gauche & droite : squash vertical (battement latéral)
  late Animation<double> _finScaleX;
  late Animation<double> _finScaleY;

  static const _duration = Duration(milliseconds: 2000);

  // ── Amplitudes ──
  // Nageoire arrière : écrasement horizontal droite→gauche.
  static const double _tailScaleXMin = 0.72; // écrasement horizontal
  static const double _tailScaleYMax = 1.10; // légère compensation verticale
  // Nageoires G/D : écrasement vertical bas→haut.
  static const double _finScaleYMin = 0.70; // écrasement vertical (perspective)
  static const double _finScaleXMax = 1.06; // légère compensation horizontale

  /// Construit une TweenSequence "squash" : repos → déformation → repos → retour.
  static Animation<double> _buildSquash(
    AnimationController ctrl, {
    required double rest,
    required double peak,
  }) =>
      TweenSequence<double>([
        TweenSequenceItem(tween: ConstantTween<double>(rest), weight: 40),
        TweenSequenceItem(
          tween: Tween<double>(begin: rest, end: peak)
              .chain(CurveTween(curve: Curves.easeInOut)),
          weight: 10,
        ),
        TweenSequenceItem(tween: ConstantTween<double>(peak), weight: 40),
        TweenSequenceItem(
          tween: Tween<double>(begin: peak, end: rest)
              .chain(CurveTween(curve: Curves.easeInOut)),
          weight: 10,
        ),
      ]).animate(ctrl);

  @override
  void initState() {
    super.initState();
    _ctrl = AnimationController(vsync: this, duration: _duration);

    // Nageoire arrière (horizontal squash)
    _tailScaleX = _buildSquash(_ctrl, rest: 1.0, peak: _tailScaleXMin);
    _tailScaleY = _buildSquash(_ctrl, rest: 1.0, peak: _tailScaleYMax);

    // Nageoires gauche & droite (vertical squash, synchronisées)
    _finScaleX = _buildSquash(_ctrl, rest: 1.0, peak: _finScaleXMax);
    _finScaleY = _buildSquash(_ctrl, rest: 1.0, peak: _finScaleYMin);

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

  @override
  Widget build(BuildContext context) {
    final size = widget.size;

    return SizedBox(
      width: size,
      height: size,
      child: Stack(
        children: [
          // Layer 1 : Nageoire droite   — DERRIÈRE le body.
          // Ancrage bottomRight, écrasement vertical.
          Positioned.fill(
            child: AnimatedBuilder(
              animation: _ctrl,
              builder: (_, child) => Transform(
                alignment: Alignment.bottomRight,
                transform: Matrix4.identity()
                  ..scale(_finScaleX.value, _finScaleY.value, 1.0),
                child: child,
              ),
              child: Image.asset(
                'assets/images/shark/shark_nageoire_droite.png',
                width: size, height: size, fit: BoxFit.contain,
              ),
            ),
          ),

          // Layer 2 : Nageoire arrière  — DERRIÈRE le body.
          Positioned.fill(
            child: AnimatedBuilder(
              animation: _ctrl,
              builder: (_, child) => Transform(
                alignment: Alignment.centerRight,
                transform: Matrix4.identity()
                  ..scale(_tailScaleX.value, _tailScaleY.value, 1.0),
                child: child,
              ),
              child: Image.asset(
                'assets/images/shark/shark_nageoire_arriere.png',
                width: size, height: size, fit: BoxFit.contain,
              ),
            ),
          ),

          // Layer 3 : Corps             — statique.
          Positioned.fill(
            child: Image.asset(
              'assets/images/shark/shark_body.png',
              width: size, height: size, fit: BoxFit.contain,
            ),
          ),

          // Layer 4 : Nageoire gauche — DEVANT le body.
          Positioned.fill(
            child: AnimatedBuilder(
              animation: _ctrl,
              builder: (_, child) => Transform(
                alignment: Alignment.topCenter,
                transform: Matrix4.identity()
                  ..scale(_finScaleX.value, _finScaleY.value, 1.0),
                child: child,
              ),
              child: Image.asset(
                'assets/images/shark/shark_nageoire_gauche.png',
                width: size, height: size, fit: BoxFit.contain,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
