import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

/// Bouton avec fond PNG Procreate (forme "pull" dessinée à la main).
/// Le PNG est affiché comme fond, clippé en pill, avec un contour noir
/// en forme de pill par-dessus et le texte superposé au centre.
class ImageButton extends StatefulWidget {
  /// Chemins vers les 3 backgrounds de boutons
  static const String greenBg  = 'assets/images/buttons/btn_green.png';
  static const String orangeBg = 'assets/images/buttons/btn_orange.png';
  static const String redBg    = 'assets/images/buttons/btn_red.png';

  final String text;
  final String backgroundAsset;
  final VoidCallback onPressed;
  final IconData? icon;
  final double height;
  final double? width;
  final double borderWidth;

  const ImageButton({
    super.key,
    required this.text,
    required this.backgroundAsset,
    required this.onPressed,
    this.icon,
    this.height = 64,
    this.width,
    this.borderWidth = 3.0,
  });

  @override
  State<ImageButton> createState() => _ImageButtonState();
}

class _ImageButtonState extends State<ImageButton>
    with SingleTickerProviderStateMixin {
  late AnimationController _ctrl;
  late Animation<double> _scale;

  @override
  void initState() {
    super.initState();
    _ctrl = AnimationController(
      vsync: this, duration: const Duration(milliseconds: 100));
    _scale = Tween<double>(begin: 1.0, end: 0.94).animate(
      CurvedAnimation(parent: _ctrl, curve: Curves.easeInOut));
  }

  @override
  void dispose() {
    _ctrl.dispose();
    super.dispose();
  }

  void _onTapDown(TapDownDetails _) {
    _ctrl.forward();
    HapticFeedback.lightImpact();
  }

  void _onTapUp(TapUpDetails _) {
    _ctrl.reverse();
    widget.onPressed();
  }

  void _onTapCancel() => _ctrl.reverse();

  @override
  Widget build(BuildContext context) {
    final pillRadius = widget.height / 2;

    return GestureDetector(
      onTapDown: _onTapDown,
      onTapUp: _onTapUp,
      onTapCancel: _onTapCancel,
      child: ScaleTransition(
        scale: _scale,
        child: SizedBox(
          width: widget.width ?? double.infinity,
          height: widget.height,
          child: Stack(
            alignment: Alignment.center,
            children: [
              // Layer 1: PNG background, clipped into pill shape, stretched to fill
              Positioned.fill(
                child: ClipRRect(
                  borderRadius: BorderRadius.circular(pillRadius),
                  child: Image.asset(
                    widget.backgroundAsset,
                    fit: BoxFit.fill,
                  ),
                ),
              ),
              // Layer 2: Black pill outline drawn on top
              Positioned.fill(
                child: DecoratedBox(
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(pillRadius),
                    border: Border.all(
                      color: const Color(0xFF2B2B2B),
                      width: widget.borderWidth,
                    ),
                  ),
                ),
              ),
              // Layer 3: Text + optional icon
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 24),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    if (widget.icon != null) ...[
                      Icon(widget.icon,
                        color: const Color(0xFF2B2B2B), size: 24),
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
            ],
          ),
        ),
      ),
    );
  }
}
