import 'package:flutter/material.dart';
import '../../../../core/utils/localization_helper.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../shared/widgets/gradient_background.dart';
import '../../../../shared/widgets/water_particles_overlay.dart';
import '../../../../shared/widgets/yarn_particles_overlay.dart';
import '../../../../shared/widgets/grass_particles_overlay.dart';
import '../../../../shared/widgets/dust_particles_overlay.dart';
import '../../../../shared/widgets/straw_particles_overlay.dart';
import '../../../timer/presentation/screens/timer_screen.dart';
import '../../../settings/presentation/screens/settings_sheet.dart';
import '../../providers/setup_provider.dart';
import '../widgets/time_picker_card.dart';
import '../widgets/animal_selector.dart';
import '../widgets/recents_list.dart';
import '../widgets/start_button.dart';

class SetupScreen extends ConsumerWidget {
  const SetupScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final setup = ref.watch(setupProvider);
    final bottomPad = MediaQuery.of(context).padding.bottom;
    final animalId = setup.selectedAnimal.id;

    return Scaffold(
      backgroundColor: Colors.transparent,
      extendBody: true,
      extendBodyBehindAppBar: true,
      body: GradientBackground(
        gradient: setup.selectedAnimal.setupGradient,
        child: Stack(
          children: [
            // Particules selon l'animal sélectionné
            if (animalId == 'crocodile') const WaterParticlesOverlay(),
            if (animalId == 'cat') const YarnParticlesOverlay(),
            if (animalId == 'dog') const GrassParticlesOverlay(),
            if (animalId == 'pony') const GrassParticlesOverlay(),
            if (animalId == 'chicken') const StrawParticlesOverlay(),
            if (animalId == 'shark') const WaterParticlesOverlay(),
            SingleChildScrollView(
              padding: EdgeInsets.only(
                  left: 24, right: 24, bottom: bottomPad + 24),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  const SizedBox(height: 8),
                  // Top row: title + settings gear
                  Row(
                    children: [
                      Expanded(
                        child: Text(
                          context.l10n.appName,
                          style: const TextStyle(
                            fontFamily: 'Nunito',
                            fontSize: 28,
                            fontWeight: FontWeight.w900,
                            color: AppColors.pencilDark,
                            letterSpacing: 0.5,
                          ),
                        ),
                      ),
                      GestureDetector(
                        onTap: () {
                          HapticFeedback.selectionClick();
                          _showSettings(context);
                        },
                        child: Container(
                          width: 44, height: 44,
                          decoration: BoxDecoration(
                            shape: BoxShape.circle,
                            color: AppColors.paperLight.withValues(alpha: 0.6),
                            border: Border.all(
                                color: AppColors.pencilDark, width: 2.5),
                          ),
                          child: const Icon(Icons.settings,
                              color: AppColors.pencilDark, size: 22),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  const Center(child: AnimalSelector()),
                  const SizedBox(height: 28),
                  const TimePickerCard(),
                  const SizedBox(height: 24),
                  StartButton(onPressed: () {
                    if (!setup.isValid) { HapticFeedback.heavyImpact(); return; }
                    HapticFeedback.mediumImpact();
                    ref.read(setupProvider.notifier).saveCurrentAsRecent();
                    Navigator.of(context).push(PageRouteBuilder(
                      pageBuilder: (_, __, ___) => const TimerScreen(),
                      transitionsBuilder: (_, anim, __, child) => FadeTransition(
                        opacity: anim,
                        child: ScaleTransition(
                          scale: Tween<double>(begin: 0.95, end: 1.0).animate(
                              CurvedAnimation(parent: anim, curve: Curves.easeOutCubic)),
                          child: child)),
                      transitionDuration: const Duration(milliseconds: 400),
                    ));
                  }),
                  const SizedBox(height: 32),
                  const RecentsSection(),
                  const SizedBox(height: 20),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  void _showSettings(BuildContext context) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      enableDrag: false,
      builder: (_) => const SettingsSheet(),
    );
  }
}
