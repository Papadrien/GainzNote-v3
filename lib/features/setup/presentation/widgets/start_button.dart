import 'package:flutter/material.dart';
import '../../../../core/utils/localization_helper.dart';
import '../../../../shared/widgets/image_button.dart';

/// Gros bouton "DÉMARRER" avec icône play seule, adapté enfants.
class StartButton extends StatelessWidget {
  final VoidCallback onPressed;
  const StartButton({super.key, required this.onPressed});

  @override
  Widget build(BuildContext context) {
    return ImageButton(
      text: context.l10n.start, // pour accessibilité uniquement
      backgroundAsset: ImageButton.greenBg,
      onPressed: onPressed,
      icon: Icons.play_arrow_rounded,
      height: 80,
      bounce: true,
    );
  }
}
