import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_svg/flutter_svg.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_text_styles.dart';
import '../../../../data/repositories/animal_repository.dart';
import '../../providers/setup_provider.dart';

/// Section "DERNIERS MINUTEURS" avec cartes colorées (bleu, orange en alternance)
/// conforme à la maquette.
class RecentsSection extends ConsumerWidget {
  const RecentsSection({super.key});

  // Couleurs alternées pour les cartes récentes
  static const List<Color> _cardColors = [
    AppColors.recentBlue,
    AppColors.recentOrange,
    AppColors.recentGreen,
    AppColors.recentPink,
  ];

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final presets = ref.watch(setupProvider).recentPresets;
    if (presets.isEmpty) return const SizedBox.shrink();

    final animalRepo = AnimalRepository();

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text('DERNIERS MINUTEURS', style: AppTextStyles.sectionTitle),
        const SizedBox(height: 12),
        ...presets.take(3).toList().asMap().entries.map((entry) {
          final i = entry.key;
          final preset = entry.value;
          final animal = animalRepo.getById(preset.animalId);
          final cardColor = _cardColors[i % _cardColors.length];

          return Padding(
            padding: const EdgeInsets.only(bottom: 10),
            child: GestureDetector(
              onTap: () => ref.read(setupProvider.notifier).loadPreset(preset),
              child: Container(
                padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
                decoration: BoxDecoration(
                  color: cardColor.withOpacity(0.7),
                  borderRadius: BorderRadius.circular(20),
                  border: Border.all(
                    color: AppColors.pencilDark.withOpacity(0.12),
                    width: 1.5,
                  ),
                ),
                child: Row(
                  children: [
                    // Animal thumbnail in circle
                    Container(
                      width: 46,
                      height: 46,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        color: Colors.white.withOpacity(0.6),
                      ),
                      child: Center(
                        child: SvgPicture.asset(
                          animal.svgAsset, width: 32, height: 32,
                          fit: BoxFit.contain,
                        ),
                      ),
                    ),
                    const SizedBox(width: 14),
                    // Name + duration
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(preset.name, style: AppTextStyles.recentName),
                          const SizedBox(height: 2),
                          Text(preset.formattedDuration, style: AppTextStyles.recentDuration),
                        ],
                      ),
                    ),
                    Icon(Icons.chevron_right,
                      color: AppColors.pencilDark.withOpacity(0.3), size: 22),
                  ],
                ),
              ),
            ),
          );
        }),
      ],
    );
  }
}
