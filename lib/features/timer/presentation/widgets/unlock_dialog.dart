import 'package:flutter/material.dart';
import '../../../../core/utils/localization_helper.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../data/models/animal_model.dart';
import '../../../../shared/widgets/image_button.dart';

/// Pop-up de célébration affichée quand un nouvel animal est débloqué.
/// Affiche l'image de l'animal avec une animation d'apparition,
/// un texte de félicitations, et un bouton OK.
class UnlockDialog extends StatefulWidget {
  final AnimalModel animal;
  final VoidCallback onDismiss;

  const UnlockDialog({
    super.key,
    required this.animal,
    required this.onDismiss,
  });

  @override
  State<UnlockDialog> createState() => _UnlockDialogState();
}

class _UnlockDialogState extends State<UnlockDialog>
    with SingleTickerProviderStateMixin {
  late AnimationController _ctrl;
  late Animation<double> _scale;
  late Animation<double> _opacity;

  @override
  void initState() {
    super.initState();
    _ctrl = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 600),
    );
    _scale = Tween<double>(begin: 0.3, end: 1.0).animate(
      CurvedAnimation(parent: _ctrl, curve: Curves.elasticOut),
    );
    _opacity = Tween<double>(begin: 0.0, end: 1.0).animate(
      CurvedAnimation(
        parent: _ctrl,
        curve: const Interval(0.0, 0.4, curve: Curves.easeIn),
      ),
    );
    _ctrl.forward();
  }

  @override
  void dispose() {
    _ctrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: _ctrl,
      builder: (_, __) => Opacity(
        opacity: _opacity.value.clamp(0.0, 1.0),
        child: Transform.scale(
          scale: _scale.value,
          child: Dialog(
            backgroundColor: Colors.transparent,
            elevation: 0,
            child: Container(
              padding: const EdgeInsets.all(28),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(28),
                border: Border.all(
                  color: AppColors.pencilDark,
                  width: 3,
                ),
                boxShadow: [
                  BoxShadow(
                    color: Colors.black.withValues(alpha: 0.15),
                    blurRadius: 20,
                    offset: const Offset(0, 8),
                  ),
                ],
              ),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  // Star decoration
                  const Text(
                    '\u2B50',
                    style: TextStyle(fontSize: 36),
                  ),
                  const SizedBox(height: 12),
                  // Animal image
                  Container(
                    width: 140,
                    height: 140,
                    decoration: BoxDecoration(
                      shape: BoxShape.circle,
                      color: widget.animal.primaryColor.withValues(alpha: 0.25),
                      border: Border.all(
                        color: widget.animal.primaryColor,
                        width: 3,
                      ),
                    ),
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Image.asset(
                        widget.animal.imageAsset,
                        fit: BoxFit.contain,
                      ),
                    ),
                  ),
                  const SizedBox(height: 20),
                  // Congratulation text
                  const Text(
                    context.l10n.bravo,
                    style: TextStyle(
                      fontFamily: 'Nunito',
                      fontSize: 28,
                      fontWeight: FontWeight.w900,
                      color: AppColors.pencilDark,
                    ),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    context.l10n.unlocked(localizedAnimalName(context, widget.animal.id)),
                    textAlign: TextAlign.center,
                    style: const TextStyle(
                      fontFamily: 'Nunito',
                      fontSize: 18,
                      fontWeight: FontWeight.w700,
                      color: AppColors.pencilDark,
                    ),
                  ),
                  const SizedBox(height: 24),
                  // OK button
                  SizedBox(
                    width: double.infinity,
                    child: ImageButton(
                      text: context.l10n.ok,
                      backgroundAsset: ImageButton.greenBg,
                      height: 50,
                      onPressed: widget.onDismiss,
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}
