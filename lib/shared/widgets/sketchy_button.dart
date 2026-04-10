import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'sketchy_button_painter.dart';

/// Bouton pill "dessiné à la main" — style crayonné enfantin.
///
/// Reproduit le style des maquettes Procreate :
/// - Contour noir irrégulier
/// - Fond coloré avec petites marques décoratives
/// - Texte noir bold en majuscules
/// - Animation press (scale down)
///
/// Usage :
/// ```dart
/// SketchyButton(
///   text: 'Démarrer',
///   color: SketchyButton.green,
///   onPressed: () => ...,
///   seed: 1,
/// )
/// ```
class SketchyButton extends StatefulWidget {
  /// Couleurs prédéfinies (inspirées de la maquette Procreate)
  static const Color green  = Color(0xFF6B8E4E); // Vert olive-sauge
  static const Color orange = Color(0xFFD4915D); // Orange terre cuite
  static const Color red    = Color(0xFFCC5A50); // Rouge brique doux

  final String text;
  final Color color;
  final VoidCallback onPressed;
  final IconData? icon;
  final int seed;
  final double? width;
  final double height;

  const SketchyButton({
    super.key,
    required this.text,
    required this.color,
    required this.onPressed,
    this.icon,
    this.seed = 42,
    this.width,
    this.height = 64,
  });

  @override
  State<SketchyButton> createState() => _SketchyButtonState();
}

class _SketchyButtonState extends State<SketchyButton>
    with SingleTickerProviderStateMixin {
  late AnimationController _pressController;
  late Animation<double> _scaleAnimation;
  bool _isPressed = false;

  @override
  void initState() {
    super.initState();
    _pressController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 100),
    );
    _scaleAnimation = Tween<double>(begin: 1.0, end: 0.94).animate(
      CurvedAnimation(parent: _pressController, curve: Curves.easeInOut),
    );
  }

  @override
  void dispose() {
    _pressController.dispose();
    super.dispose();
  }

  void _onTapDown(TapDownDetails _) {
    setState(() => _isPressed = true);
    _pressController.forward();
    HapticFeedback.lightImpact();
  }

  void _onTapUp(TapUpDetails _) {
    setState(() => _isPressed = false);
    _pressController.reverse();
    widget.onPressed();
  }

  void _onTapCancel() {
    setState(() => _isPressed = false);
    _pressController.reverse();
  }

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTapDown: _onTapDown,
      onTapUp: _onTapUp,
      onTapCancel: _onTapCancel,
      child: ScaleTransition(
        scale: _scaleAnimation,
        child: SizedBox(
          width: widget.width ?? double.infinity,
          height: widget.height,
          child: CustomPaint(
            painter: SketchyButtonPainter(
              fillColor: widget.color,
              seed: widget.seed,
              isPressed: _isPressed,
            ),
            child: Center(
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 28),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    if (widget.icon != null) ...[
                      Icon(
                        widget.icon,
                        color: const Color(0xFF2B2B2B),
                        size: 24,
                      ),
                      const SizedBox(width: 8),
                    ],
                    Text(
                      widget.text.toUpperCase(),
                      style: const TextStyle(
                        fontFamily: 'Nunito',
                        fontSize: 20,
                        fontWeight: FontWeight.w900,
                        color: Color(0xFF2B2B2B),
                        letterSpacing: 1.2,
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
