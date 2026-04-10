import 'package:flutter/material.dart';
import '../../../../shared/widgets/sketchy_button.dart';

/// Gros bouton pill vert "DÉMARRER" — style crayonné fait main.
class StartButton extends StatelessWidget {
  final VoidCallback onPressed;
  const StartButton({super.key, required this.onPressed});

  @override
  Widget build(BuildContext context) {
    return SketchyButton(
      text: 'Démarrer',
      color: SketchyButton.green,
      onPressed: onPressed,
      icon: Icons.chevron_right,
      seed: 1,
      height: 64,
    );
  }
}
