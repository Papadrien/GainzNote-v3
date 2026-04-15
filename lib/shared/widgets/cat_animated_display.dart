import 'dart:math' as math;
import 'package:flutter/material.dart';

/// Affichage animé du chat avec 3 layers (body, tail, head).
/// La tête tourne de gauche à droite et la queue de haut en bas,
/// synchronisées dans une boucle de 2 secondes.
///
/// Timing (boucle 2s, fractions du controller 0→1) :
///   0.0 → 0.4  : repos position A (tête centrée, queue haute)
///   0.4 → 0.5  : rotation vers position B (tête à gauche, queue en bas)
///   0.5 → 0.9  : repos position B
///   0.9 → 1.0  : rotation vers position A
class CatAnimatedDisplay extends StatefulWidget {
  final double size;
  final bool animate;

  const CatAnimatedDisplay({
    super.key,
    this.size = 180,
    this.animate = true,
  });

  @override
  State<CatAnimatedDisplay> createState() => _CatAnimatedDisplayState();
}

class _CatAnimatedDisplayState extends State<CatAnimatedDisplay>
    with SingleTickerProviderStateMixin {
  late AnimationController _ctrl;

  // Rotation angles (radians)
  static const double _headAngle = 0.12; // ~7 degrees
  static const double _tailAngle = 0.18; // ~10 degrees

  // Pivot points as fractions of the 512x512 image
  // Head pivot: bottom-center of head (222, 270) / 512
  static const double _headPivotX = 0.434;
  static const double _headPivotY = 0.527;

  // Tail pivot: top-left of tail, junction with body (368, 338) / 512
  static const double _tailPivotX = 0.719;
  static const double _tailPivotY = 0.660;

  @override
  void initState() {
    super.initState();
    _ctrl = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 2000),
    );
    if (widget.animate) _ctrl.repeat();
  }

  @override
  void didUpdateWidget(CatAnimatedDisplay old) {
    super.didUpdateWidget(old);
    if (widget.animate && !_ctrl.isAnimating) {
      _ctrl.repeat();
    } else if (!widget.animate && _ctrl.isAnimating) {
      _ctrl.stop();
    }
  }

  @override
  void dispose() {
    _ctrl.dispose();
    super.dispose();
  }

  /// Calcule l'angle de rotation en fonction du temps t (0→1).
  /// Position A = angle positif, Position B = angle négatif.
  /// Repos entre les rotations.
  double _computeAngle(double t, double maxAngle) {
    // 0.0 → 0.4 : repos en position A (+maxAngle)
    // 0.4 → 0.5 : rotation A→B (+maxAngle → -maxAngle)
    // 0.5 → 0.9 : repos en position B (-maxAngle)
    // 0.9 → 1.0 : rotation B→A (-maxAngle → +maxAngle)
    if (t <= 0.4) {
      return maxAngle;
    } else if (t <= 0.5) {
      // Interpolation A→B avec easing
      final progress = (t - 0.4) / 0.1;
      final eased = _easeInOut(progress);
      return maxAngle - 2 * maxAngle * eased;
    } else if (t <= 0.9) {
      return -maxAngle;
    } else {
      // Interpolation B→A avec easing
      final progress = (t - 0.9) / 0.1;
      final eased = _easeInOut(progress);
      return -maxAngle + 2 * maxAngle * eased;
    }
  }

  double _easeInOut(double t) {
    // Smooth ease-in-out
    return t < 0.5
        ? 2 * t * t
        : 1 - (-2 * t + 2) * (-2 * t + 2) / 2;
  }

  @override
  Widget build(BuildContext context) {
    final size = widget.size;

    if (!widget.animate) {
      return SizedBox(
        width: size,
        height: size,
        child: Stack(
          children: [
            _buildLayer('assets/images/cat/cat_tail.png', size),
            _buildLayer('assets/images/cat/cat_body.png', size),
            _buildLayer('assets/images/cat/cat_head.png', size),
          ],
        ),
      );
    }

    return AnimatedBuilder(
      animation: _ctrl,
      builder: (_, __) {
        final t = _ctrl.value;

        final headAngle = _computeAngle(t, _headAngle);
        // Queue : mouvement inverse de la tête
        final tailAngle = _computeAngle(t, -_tailAngle);

        return SizedBox(
          width: size,
          height: size,
          child: Stack(
            children: [
              // Layer 1 : Queue (derrière le corps)
              _buildRotatedLayer(
                'assets/images/cat/cat_tail.png',
                size,
                tailAngle,
                _tailPivotX,
                _tailPivotY,
              ),
              // Layer 2 : Corps (statique)
              _buildLayer('assets/images/cat/cat_body.png', size),
              // Layer 3 : Tête (devant le corps)
              _buildRotatedLayer(
                'assets/images/cat/cat_head.png',
                size,
                headAngle,
                _headPivotX,
                _headPivotY,
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _buildLayer(String asset, double size) {
    return Positioned.fill(
      child: Image.asset(
        asset,
        width: size,
        height: size,
        fit: BoxFit.contain,
      ),
    );
  }

  Widget _buildRotatedLayer(
    String asset,
    double size,
    double angle,
    double pivotX,
    double pivotY,
  ) {
    return Positioned.fill(
      child: Transform(
        alignment: FractionalOffset(pivotX, pivotY),
        transform: Matrix4.rotationZ(angle),
        child: Image.asset(
          asset,
          width: size,
          height: size,
          fit: BoxFit.contain,
        ),
      ),
    );
  }
}
