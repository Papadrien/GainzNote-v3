import 'package:flutter/material.dart';

/// Affichage animé du requin.
///
/// Layering (du fond vers le haut) :
///   1. Nageoire droite   → DERRIÈRE le body
///   2. Nageoire arrière  → DERRIÈRE le body
///   3. Body              → statique
///   4. Nageoire gauche   → DEVANT le body
///
/// Animations (controller unique 2000ms, weights 40/10/40/10) :
///   - Nageoire gauche  : ancrage topCenter  → descend (scaleY < 1, le haut reste fixe).
///   - Nageoire droite  : ancrage topCenter  → monte  (scaleY > 1, le haut reste fixe).
///   - Nageoire arrière : ancrage centerRight → glisse vers la gauche (scaleX < 1).
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

  // Nageoire arrière : squash horizontal (glisse vers la gauche)
  late Animation<double> _tailScaleX;
  late Animation<double> _tailScaleY;

  // Nageoire gauche : descend depuis le haut (scaleY réduit)
  late Animation<double> _leftFinScaleX;
  late Animation<double> _leftFinScaleY;

  // Nageoire droite : monte depuis le haut (scaleY augmenté)
  late Animation<double> _rightFinScaleX;
  late Animation<double> _rightFinScaleY;

  static const _duration = Duration(milliseconds: 2000);

  // ── Nageoire arrière : glissement gauche ──
  static const double _tailScaleXMin = 0.72;
  static const double _tailScaleYMax = 1.10;

  // ── Nageoire gauche : descend (haut fixe, bas descend) ──
  static const double _leftFinScaleYMin  = 0.70;   // écrasement = descente vers le bas
  static const double _leftFinScaleXMax  = 1.06;

  // ── Nageoire droite : monte (haut fixe, bas remonte) ──
  static const double _rightFinScaleYMax = 1.30;   // étirement = montée vers le bas (bas remonte)
  static const double _rightFinScaleXMin = 0.94;

  /// Squash : repos → pic → repos → retour (weights 40/10/40/10).
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

    // Nageoire arrière
    _tailScaleX = _buildSquash(_ctrl, rest: 1.0, peak: _tailScaleXMin);
    _tailScaleY = _buildSquash(_ctrl, rest: 1.0, peak: _tailScaleYMax);

    // Nageoire gauche (descend)
    _leftFinScaleX = _buildSquash(_ctrl, rest: 1.0, peak: _leftFinScaleXMax);
    _leftFinScaleY = _buildSquash(_ctrl, rest: 1.0, peak: _leftFinScaleYMin);

    // Nageoire droite (monte)
    _rightFinScaleX = _buildSquash(_ctrl, rest: 1.0, peak: _rightFinScaleXMin);
    _rightFinScaleY = _buildSquash(_ctrl, rest: 1.0, peak: _rightFinScaleYMax);

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
          // Layer 1 : Nageoire droite — DERRIÈRE le body.
          // Ancrage bottomLeft : coin bas-gauche fixe (point d'ancrage rouge).
          Positioned.fill(
            child: AnimatedBuilder(
              animation: _ctrl,
              builder: (_, child) => Transform(
                alignment: Alignment.bottomLeft,
                transform: Matrix4.identity()
                  ..scale(_rightFinScaleX.value, _rightFinScaleY.value, 1.0),
                child: child,
              ),
              child: Image.asset(
                'assets/images/shark/shark_nageoire_droite.png',
                width: size, height: size, fit: BoxFit.contain,
              ),
            ),
          ),

          // Layer 2 : Nageoire arrière — DERRIÈRE le body.
          // Ancrage centerRight : la coche droite reste fixe, la nageoire glisse à gauche.
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

          // Layer 3 : Corps — statique.
          Positioned.fill(
            child: Image.asset(
              'assets/images/shark/shark_body.png',
              width: size, height: size, fit: BoxFit.contain,
            ),
          ),

          // Layer 4 : Nageoire gauche — DEVANT le body.
          // Ancrage topLeft : coin haut-gauche fixe (point d'ancrage rouge).
          Positioned.fill(
            child: AnimatedBuilder(
              animation: _ctrl,
              builder: (_, child) => Transform(
                alignment: Alignment.topLeft,
                transform: Matrix4.identity()
                  ..scale(_leftFinScaleX.value, _leftFinScaleY.value, 1.0),
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
