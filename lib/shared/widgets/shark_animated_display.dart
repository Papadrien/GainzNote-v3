import 'package:flutter/material.dart';

/// Affichage animé du requin.
///
/// Animation : déformations de type "écrasement" (squash) pour imiter la nage.
/// Le timing (2000ms, weights 40/10/40/10) est conservé.
///
///   - Nageoire arrière : squash horizontal (scaleX réduit, scaleY compensé)
///     ancré à gauche (côté corps) pour rester "accrochée" au body.
///   - Nageoires gauche & droite : SYNCHRONISÉES — elles montent et
///     descendent ensemble (même controller, mêmes amplitudes).
///   - Corps            : statique.
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
  // Un seul controller : les nageoires G/D sont parfaitement synchrones.
  late AnimationController _ctrl;

  // Nageoire arrière : squash horizontal (battement de queue)
  late Animation<double> _tailScaleX;
  late Animation<double> _tailScaleY;

  // Nageoires gauche & droite : mêmes animations (synchronisées)
  late Animation<double> _finSkewY;
  late Animation<double> _finScaleX;
  late Animation<double> _finScaleY;

  static const _duration = Duration(milliseconds: 2000);

  // ── Amplitudes ──
  // Nageoire arrière : scaleX 1.0 → 0.75 (évite la disparition derrière le body)
  static const double _tailScaleXMin = 0.75;
  static const double _tailScaleYMax = 1.12; // compensation squash vertical
  // Nageoires (mêmes amplitudes pour synchro G/D)
  static const double _finSkewMax    = 0.24;
  static const double _finScaleXMax  = 1.04;
  static const double _finScaleYMin  = 0.92;

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

    // Nageoire arrière
    _tailScaleX = _buildSquash(_ctrl, rest: 1.0, peak: _tailScaleXMin);
    _tailScaleY = _buildSquash(_ctrl, rest: 1.0, peak: _tailScaleYMax);

    // Nageoires gauche & droite (synchronisées — même controller, mêmes amplitudes)
    _finSkewY   = _buildSquash(_ctrl, rest: 0.0, peak: _finSkewMax);
    _finScaleX  = _buildSquash(_ctrl, rest: 1.0, peak: _finScaleXMax);
    _finScaleY  = _buildSquash(_ctrl, rest: 1.0, peak: _finScaleYMin);

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
          // Layer 1 : Nageoire arrière — squash horizontal (battement de queue).
          // Ancrage à GAUCHE de la nageoire (donc côté droit du body) afin
          // qu'elle reste "accrochée" au corps pendant l'animation.
          Positioned.fill(
            child: AnimatedBuilder(
              animation: _ctrl,
              builder: (_, child) => Transform(
                alignment: Alignment.centerLeft,
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

          // Layer 2 : Nageoire droite — DERRIÈRE le body. Synchronisée avec la gauche.
          Positioned.fill(
            child: AnimatedBuilder(
              animation: _ctrl,
              builder: (_, child) => Transform(
                alignment: Alignment.topCenter,
                transform: Matrix4.identity()
                  ..scale(_finScaleX.value, _finScaleY.value, 1.0)
                  ..setEntry(1, 0, _finSkewY.value),
                child: child,
              ),
              child: Image.asset(
                'assets/images/shark/shark_nageoire_droite.png',
                width: size, height: size, fit: BoxFit.contain,
              ),
            ),
          ),

          // Layer 3 : Corps — statique, au-dessus de la nageoire droite.
          Positioned.fill(
            child: Image.asset(
              'assets/images/shark/shark_body.png',
              width: size, height: size, fit: BoxFit.contain,
            ),
          ),

          // Layer 4 : Nageoire gauche — devant le body. Synchronisée avec la droite.
          Positioned.fill(
            child: AnimatedBuilder(
              animation: _ctrl,
              builder: (_, child) => Transform(
                alignment: Alignment.topCenter,
                transform: Matrix4.identity()
                  ..scale(_finScaleX.value, _finScaleY.value, 1.0)
                  ..setEntry(1, 0, _finSkewY.value),
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
