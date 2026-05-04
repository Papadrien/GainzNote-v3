import 'package:flutter/material.dart';

/// Affichage animé du requin.
///
/// Animation : déformations de type "écrasement" (squash) pour imiter la nage.
/// Le timing (2000ms, weights 40/10/40/10, déphasage 1/2 cycle) est conservé.
///
///   - Nageoire arrière : squash horizontal (scaleX réduit, scaleY compensé)
///     → imite le battement de queue sans disparaître derrière le corps
///   - Nageoire droite  : skewY + squash, amplitude réduite (derrière le corps)
///   - Corps            : statique
///   - Nageoire gauche  : skewY + squash, amplitude nominale (devant le corps)
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
    with TickerProviderStateMixin {
  late AnimationController _ctrlMain;   // nageoire arrière + nageoire gauche
  late AnimationController _ctrlRight;  // nageoire droite (déphasé 1/2 cycle)

  // Nageoire arrière : squash horizontal (battement de queue)
  late Animation<double> _tailScaleX;
  late Animation<double> _tailScaleY;

  // Nageoire gauche : amplitude nominale
  late Animation<double> _leftSkewY;
  late Animation<double> _leftScaleX;
  late Animation<double> _leftScaleY;

  // Nageoire droite : amplitude réduite (derrière le corps)
  late Animation<double> _rightSkewY;
  late Animation<double> _rightScaleX;
  late Animation<double> _rightScaleY;

  static const _duration = Duration(milliseconds: 2000);

  // ── Amplitudes (timing conservé via weights identiques) ──
  // Nageoire arrière : scaleX 1.0 → 0.75 (au lieu de 0.25, elle disparaissait)
  static const double _tailScaleXMin = 0.75;
  static const double _tailScaleYMax = 1.12; // compensation : squash vertical
  // Nageoire gauche : nominale
  static const double _leftSkewMax    = 0.24;
  static const double _leftScaleXMax  = 1.04; // étirement latéral léger
  static const double _leftScaleYMin  = 0.92; // écrasement vertical léger
  // Nageoire droite : amplitude réduite de moitié
  static const double _rightSkewMax   = 0.12;
  static const double _rightScaleXMax = 1.02;
  static const double _rightScaleYMin = 0.96;

  /// Construit une TweenSequence "squash" : repos → déformation → repos → retour.
  /// Weights 40/10/40/10 identiques à l'original : le timing reste inchangé.
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
    _ctrlMain  = AnimationController(vsync: this, duration: _duration);
    _ctrlRight = AnimationController(vsync: this, duration: _duration);

    // Nageoire arrière (battement de queue : squash horizontal)
    _tailScaleX = _buildSquash(_ctrlMain, rest: 1.0, peak: _tailScaleXMin);
    _tailScaleY = _buildSquash(_ctrlMain, rest: 1.0, peak: _tailScaleYMax);

    // Nageoire gauche
    _leftSkewY   = _buildSquash(_ctrlMain, rest: 0.0, peak: _leftSkewMax);
    _leftScaleX  = _buildSquash(_ctrlMain, rest: 1.0, peak: _leftScaleXMax);
    _leftScaleY  = _buildSquash(_ctrlMain, rest: 1.0, peak: _leftScaleYMin);

    // Nageoire droite (déphasée, amplitude réduite)
    _rightSkewY  = _buildSquash(_ctrlRight, rest: 0.0, peak: _rightSkewMax);
    _rightScaleX = _buildSquash(_ctrlRight, rest: 1.0, peak: _rightScaleXMax);
    _rightScaleY = _buildSquash(_ctrlRight, rest: 1.0, peak: _rightScaleYMin);

    _startAnimation();
  }

  void _startAnimation() {
    if (!widget.animate) return;
    if (widget.playOnce) {
      _ctrlMain.forward(from: 0.0);
      _ctrlRight.forward(from: 0.5);
    } else {
      _ctrlMain.repeat();
      _ctrlRight.forward(from: 0.5).then((_) {
        if (mounted) _ctrlRight.repeat();
      });
    }
  }

  @override
  void didUpdateWidget(SharkAnimatedDisplay old) {
    super.didUpdateWidget(old);
    if (widget.animate && !old.animate) {
      _startAnimation();
    } else if (!widget.animate && old.animate) {
      _ctrlMain.stop();
      _ctrlRight.stop();
    }
  }

  @override
  void dispose() {
    _ctrlMain.dispose();
    _ctrlRight.dispose();
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
          // Layer 1 : Nageoire arrière — squash horizontal (battement de queue)
          Positioned.fill(
            child: AnimatedBuilder(
              animation: _ctrlMain,
              builder: (_, child) => Transform(
                alignment: Alignment.center,
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

          // Layer 2 : Nageoire droite — DERRIÈRE le body, skewY + squash, amplitude réduite
          Positioned.fill(
            child: AnimatedBuilder(
              animation: _ctrlRight,
              builder: (_, child) => Transform(
                alignment: Alignment.topCenter,
                transform: Matrix4.identity()
                  ..scale(_rightScaleX.value, _rightScaleY.value, 1.0)
                  ..setEntry(1, 0, _rightSkewY.value),
                child: child,
              ),
              child: Image.asset(
                'assets/images/shark/shark_nageoire_droite.png',
                width: size, height: size, fit: BoxFit.contain,
              ),
            ),
          ),

          // Layer 3 : Corps — statique, au-dessus de la nageoire droite
          Positioned.fill(
            child: Image.asset(
              'assets/images/shark/shark_body.png',
              width: size, height: size, fit: BoxFit.contain,
            ),
          ),

          // Layer 4 : Nageoire gauche — devant le body, skewY + squash nominal
          Positioned.fill(
            child: AnimatedBuilder(
              animation: _ctrlMain,
              builder: (_, child) => Transform(
                alignment: Alignment.topCenter,
                transform: Matrix4.identity()
                  ..scale(_leftScaleX.value, _leftScaleY.value, 1.0)
                  ..setEntry(1, 0, _leftSkewY.value),
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
