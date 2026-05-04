import 'package:flutter/material.dart';

/// Affichage animé du requin.
///
/// Animation : déformation uniquement (pas de déplacement des nageoires).
///   - Nageoire arrière  : scaleX depuis Alignment.centerLeft
///   - Nageoire droite   : skewY depuis Alignment.topCenter, DERRIÈRE le body, déphasé 1/2 cycle
///   - Corps             : statique
///   - Nageoire gauche   : skewY depuis Alignment.topCenter, devant le body
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

  late Animation<double> _tailScaleX;  // nageoire arrière : déformation horizontale
  late Animation<double> _leftSkewY;   // nageoire gauche  : déformation verticale
  late Animation<double> _rightSkewY;  // nageoire droite  : déformation verticale déphasée

  static const _duration = Duration(milliseconds: 2000);

  static Animation<double> _buildScaleX(AnimationController ctrl) =>
      TweenSequence<double>([
        TweenSequenceItem(tween: ConstantTween<double>(1.0), weight: 40),
        TweenSequenceItem(
          tween: Tween<double>(begin: 1.0, end: 0.5)
              .chain(CurveTween(curve: Curves.easeInOut)),
          weight: 10,
        ),
        TweenSequenceItem(tween: ConstantTween<double>(0.5), weight: 40),
        TweenSequenceItem(
          tween: Tween<double>(begin: 0.5, end: 1.0)
              .chain(CurveTween(curve: Curves.easeInOut)),
          weight: 10,
        ),
      ]).animate(ctrl);

  static Animation<double> _buildSkewY(AnimationController ctrl) =>
      TweenSequence<double>([
        TweenSequenceItem(tween: ConstantTween<double>(0.0), weight: 40),
        TweenSequenceItem(
          tween: Tween<double>(begin: 0.0, end: 0.12)
              .chain(CurveTween(curve: Curves.easeInOut)),
          weight: 10,
        ),
        TweenSequenceItem(tween: ConstantTween<double>(0.12), weight: 40),
        TweenSequenceItem(
          tween: Tween<double>(begin: 0.12, end: 0.0)
              .chain(CurveTween(curve: Curves.easeInOut)),
          weight: 10,
        ),
      ]).animate(ctrl);

  @override
  void initState() {
    super.initState();
    _ctrlMain  = AnimationController(vsync: this, duration: _duration);
    _ctrlRight = AnimationController(vsync: this, duration: _duration);

    _tailScaleX = _buildScaleX(_ctrlMain);
    _leftSkewY  = _buildSkewY(_ctrlMain);
    _rightSkewY = _buildSkewY(_ctrlRight);

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
          // Layer 1 : Nageoire arrière — scaleX (derrière tout)
          Positioned.fill(
            child: AnimatedBuilder(
              animation: _tailScaleX,
              builder: (_, child) => Transform(
                alignment: Alignment.centerLeft,
                transform: Matrix4.identity()..scale(_tailScaleX.value, 1.0, 1.0),
                child: child,
              ),
              child: Image.asset(
                'assets/images/shark/shark_nageoire_arriere.png',
                width: size, height: size, fit: BoxFit.contain,
              ),
            ),
          ),

          // Layer 2 : Nageoire droite — DERRIÈRE le body, skewY déphasé
          Positioned.fill(
            child: AnimatedBuilder(
              animation: _rightSkewY,
              builder: (_, child) => Transform(
                alignment: Alignment.topCenter,
                transform: Matrix4.identity()..setEntry(1, 0, _rightSkewY.value),
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

          // Layer 4 : Nageoire gauche — devant le body, skewY principal
          Positioned.fill(
            child: AnimatedBuilder(
              animation: _leftSkewY,
              builder: (_, child) => Transform(
                alignment: Alignment.topCenter,
                transform: Matrix4.identity()..setEntry(1, 0, _leftSkewY.value),
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
