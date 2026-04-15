import 'package:flutter/material.dart';
import '../../data/models/animal_model.dart';
import 'cat_animated_display.dart';

/// Displays an animal image (PNG) with a gentle breathing + sway animation.
/// For the cat, uses a special multi-layer animation (head + tail).
class AnimalDisplay extends StatefulWidget {
  final AnimalModel animal;
  final double size;
  final bool animate;

  const AnimalDisplay({
    super.key,
    required this.animal,
    this.size = 120,
    this.animate = true,
  });

  @override
  State<AnimalDisplay> createState() => _AnimalDisplayState();
}

class _AnimalDisplayState extends State<AnimalDisplay>
    with SingleTickerProviderStateMixin {
  late AnimationController _ctrl;
  late Animation<double> _breathe;
  late Animation<double> _sway;

  @override
  void initState() {
    super.initState();
    _ctrl = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 2500),
    );
    _breathe = Tween<double>(begin: 1.0, end: 1.05).animate(
      CurvedAnimation(parent: _ctrl, curve: Curves.easeInOut),
    );
    _sway = Tween<double>(begin: -0.02, end: 0.02).animate(
      CurvedAnimation(parent: _ctrl, curve: Curves.easeInOut),
    );
    if (widget.animate && !_isCat) _ctrl.repeat(reverse: true);
  }

  bool get _isCat => widget.animal.id == 'cat';

  @override
  void didUpdateWidget(AnimalDisplay old) {
    super.didUpdateWidget(old);
    if (_isCat) {
      // Cat uses its own animation controller — stop the generic one
      if (_ctrl.isAnimating) _ctrl.stop();
    } else if (widget.animate && !_ctrl.isAnimating) {
      _ctrl.repeat(reverse: true);
    } else if (!widget.animate && _ctrl.isAnimating) {
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
    // Special animated display for the cat
    if (_isCat) {
      return CatAnimatedDisplay(
        size: widget.size,
        animate: widget.animate,
      );
    }

    // Default display for other animals
    final imageWidget = Image.asset(
      widget.animal.imageAsset,
      width: widget.size,
      height: widget.size,
      fit: BoxFit.contain,
      errorBuilder: (_, __, ___) => Center(
        child: Text(widget.animal.emoji,
            style: TextStyle(fontSize: widget.size * 0.4)),
      ),
    );

    if (!widget.animate) return imageWidget;

    return AnimatedBuilder(
      animation: _ctrl,
      builder: (_, __) => Transform.scale(
        scale: _breathe.value,
        child: Transform.rotate(
          angle: _sway.value,
          child: imageWidget,
        ),
      ),
    );
  }
}
