import 'package:flutter/material.dart';

/// Affichage animé du poney avec 3 layers (body, tail, head).
/// La tête oscille doucement de gauche à droite.
/// La queue tourne autour de son point d'attache (haut de la queue)
/// avec un rythme plus rapide que la tête pour un effet naturel.
///
/// [playOnce] : si true, joue exactement 1 cycle puis s'arrête.
///              si false, boucle indéfiniment.
///
/// Timing tête (boucle 2s, fractions du controller 0→1) :
///   0.0 → 0.4  : repos position A
///   0.4 → 0.5  : rotation vers position B
///   0.5 → 0.9  : repos position B
///   0.9 → 1.0  : rotation vers position A
///
/// Timing queue : cycle 1.5x plus rapide, déphasé,
///   utilise une sinusoïdale pour un balancement fluide.
class PonyAnimatedDisplay extends StatefulWidget {
  final double size;
  final bool animate;
  final bool playOnce;

  const PonyAnimatedDisplay({
    super.key,
    this.size = 180,
    this.animate = true,
    this.playOnce = false,
  });

  @override
  State<PonyAnimatedDisplay> createState() => _PonyAnimatedDisplayState();
}

class _PonyAnimatedDisplayState extends State<PonyAnimatedDisplay>
    with SingleTickerProviderStateMixin {
  late AnimationController _ctrl;

  // Head rotation angle (radians) — ~6 degrees
  static const double _headAngle = 0.10;

  // Tail rotation angle (radians) — ~12 degrees, more visible swing
  static const double _tailAngle = 0.20;

  // Tail speed multiplier — swings faster than the head
  static const double _tailSpeedMultiplier = 1.5;

  // Head pivot: bottom-center of head, where neck meets body
  // (fractions of image, works for any image size)
  static const double _headPivotX = 0.38;
  static const double _headPivotY = 0.55;

  // Tail pivot: top of tail, junction with body (attachment point)
  static const double _tailPivotX = 0.70;
  static const double _tailPivotY = 0.42;

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
  void didUpdateWidget(PonyAnimatedDisplay old) {
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

  // ── Head timing (same pattern as cat: pause-rotate-pause-rotate) ──

  double _computeHeadAngle(double t, double maxAngle) {
    if (t <= 0.4) {
      return maxAngle;
    } else if (t <= 0.5) {
      final progress = (t - 0.4) / 0.1;
      final eased = _easeInOut(progress);
      return maxAngle - 2 * maxAngle * eased;
    } else if (t <= 0.9) {
      return -maxAngle;
    } else {
      final progress = (t - 0.9) / 0.1;
      final eased = _easeInOut(progress);
      return -maxAngle + 2 * maxAngle * eased;
    }
  }

  double _computeHeadAngleOnce(double t, double maxAngle) {
    if (t <= 0.15) {
      final progress = t / 0.15;
      return maxAngle * _easeInOut(progress);
    } else if (t <= 0.35) {
      return maxAngle;
    } else if (t <= 0.65) {
      final progress = (t - 0.35) / 0.30;
      return maxAngle - 2 * maxAngle * _easeInOut(progress);
    } else if (t <= 0.85) {
      return -maxAngle;
    } else {
      final progress = (t - 0.85) / 0.15;
      return -maxAngle * (1 - _easeInOut(progress));
    }
  }

  // ── Tail timing (sinusoidal, faster, independent of head) ──

  double _computeTailAngle(double t, double maxAngle) {
    // Sinusoidal swing at tailSpeedMultiplier × base frequency
    // sin() gives smooth natural pendulum-like motion
    final phase = t * _tailSpeedMultiplier * 2 * 3.14159265;
    return maxAngle * _sin(phase);
  }

  double _computeTailAngleOnce(double t, double maxAngle) {
    // Envelope: ramp up, swing, ramp down
    double envelope;
    if (t <= 0.10) {
      envelope = t / 0.10; // fade in
    } else if (t >= 0.90) {
      envelope = (1.0 - t) / 0.10; // fade out
    } else {
      envelope = 1.0;
    }
    final phase = t * _tailSpeedMultiplier * 2 * 3.14159265;
    return maxAngle * _sin(phase) * envelope;
  }

  /// Fast sin approximation (good enough for animation)
  double _sin(double x) {
    // Normalize to [-pi, pi]
    x = x % (2 * 3.14159265);
    if (x > 3.14159265) x -= 2 * 3.14159265;
    // Bhaskara I approximation
    final abs = x < 0 ? -x : x;
    final sign = x < 0 ? -1.0 : 1.0;
    final q = abs * (3.14159265 - abs);
    return sign * 4 * q / (40.5 - q);
  }

  double _easeInOut(double t) {
    return t < 0.5
        ? 2 * t * t
        : 1 - (-2 * t + 2) * (-2 * t + 2) / 2;
  }

  @override
  Widget build(BuildContext context) {
    final size = widget.size;

    return AnimatedBuilder(
      animation: _ctrl,
      builder: (_, __) {
        final t = _ctrl.value;

        final double headAngle;
        final double tailAngle;
        if (!_ctrl.isAnimating && !(_ctrl.status == AnimationStatus.forward)) {
          // Not animating → neutral position
          headAngle = 0.0;
          tailAngle = 0.0;
        } else if (widget.playOnce) {
          headAngle = _computeHeadAngleOnce(t, _headAngle);
          tailAngle = _computeTailAngleOnce(t, _tailAngle);
        } else {
          headAngle = _computeHeadAngle(t, _headAngle);
          tailAngle = _computeTailAngle(t, _tailAngle);
        }

        return SizedBox(
          width: size,
          height: size,
          child: Stack(
            children: [
              // Layer 1 : Queue (derrière le corps) — rotation autour de l'attache
              _buildRotatedLayer(
                'assets/images/pony/pony_tail.png',
                size,
                tailAngle,
                _tailPivotX,
                _tailPivotY,
              ),
              // Layer 2 : Corps (statique)
              _buildLayer('assets/images/pony/pony_body.png', size),
              // Layer 3 : Tête (devant le corps) — oscillation gauche/droite
              _buildRotatedLayer(
                'assets/images/pony/pony_head.png',
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
