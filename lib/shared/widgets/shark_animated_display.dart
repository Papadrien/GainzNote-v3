import 'package:flutter/material.dart';

/// Affichage animé du requin — 4 layers.
///
/// Nageoires gauche & droite : scaleY ancré en haut (Alignment.topCenter)  1.0 → 0.5 → 1.0
/// Nageoire arrière           : scaleX ancré à gauche (Alignment.centerLeft) idem, déphasé ½ cycle
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
  // Contrôleur nageoires latérales (gauche + droite)
  late AnimationController _ctrlFins;
  // Contrôleur nageoire arrière — démarre à mi-cycle pour le déphasage
  late AnimationController _ctrlTail;

  late Animation<double> _finScale;
  late Animation<double> _tailScale;

  static Animation<double> _buildScale(AnimationController ctrl) =>
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

  @override
  void initState() {
    super.initState();
    _ctrlFins = AnimationController(vsync: this, duration: const Duration(milliseconds: 2000));
    _ctrlTail = AnimationController(vsync: this, duration: const Duration(milliseconds: 2000));
    _finScale = _buildScale(_ctrlFins);
    _tailScale = _buildScale(_ctrlTail);
    _startAnimation();
  }

  void _startAnimation() {
    if (!widget.animate) return;
    if (widget.playOnce) {
      _ctrlFins.forward(from: 0.0);
      _ctrlTail.forward(from: 0.5);
    } else {
      _ctrlFins.repeat();
      // Démarrer à mi-cycle puis boucler
      _ctrlTail.forward(from: 0.5).then((_) {
        if (mounted) _ctrlTail.repeat();
      });
    }
  }

  @override
  void didUpdateWidget(SharkAnimatedDisplay old) {
    super.didUpdateWidget(old);
    if (widget.animate && !old.animate) {
      _startAnimation();
    } else if (!widget.animate && old.animate) {
      _ctrlFins.stop();
      _ctrlTail.stop();
    }
  }

  @override
  void dispose() {
    _ctrlFins.dispose();
    _ctrlTail.dispose();
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
          // Layer 1 : Nageoire arrière — scaleX ancré à gauche
          Positioned.fill(
            child: AnimatedBuilder(
              animation: _tailScale,
              builder: (_, child) => Transform(
                alignment: Alignment.centerLeft,
                transform: Matrix4.identity()..scale(_tailScale.value, 1.0, 1.0),
                child: child,
              ),
              child: Image.asset(
                'assets/images/shark/shark_nageoire_arriere.png',
                width: size,
                height: size,
                fit: BoxFit.contain,
              ),
            ),
          ),
          // Layer 2 : Corps (statique)
          Positioned.fill(
            child: Image.asset(
              'assets/images/shark/shark_body.png',
              width: size,
              height: size,
              fit: BoxFit.contain,
            ),
          ),
          // Layer 3 : Nageoire gauche — scaleY ancré en haut
          Positioned.fill(
            child: AnimatedBuilder(
              animation: _finScale,
              builder: (_, child) => Transform(
                alignment: Alignment.topCenter,
                transform: Matrix4.identity()..scale(1.0, _finScale.value, 1.0),
                child: child,
              ),
              child: Image.asset(
                'assets/images/shark/shark_nageoire_gauche.png',
                width: size,
                height: size,
                fit: BoxFit.contain,
              ),
            ),
          ),
          // Layer 4 : Nageoire droite — scaleY ancré en haut
          Positioned.fill(
            child: AnimatedBuilder(
              animation: _finScale,
              builder: (_, child) => Transform(
                alignment: Alignment.topCenter,
                transform: Matrix4.identity()..scale(1.0, _finScale.value, 1.0),
                child: child,
              ),
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
  }
}
