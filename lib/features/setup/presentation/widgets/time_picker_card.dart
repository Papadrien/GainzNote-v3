import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_text_styles.dart';
import '../../providers/setup_provider.dart';

/// Time picker maquette style: 3 cases côte à côte [0h] [2m] [10s]
/// avec une barre colorée en dessous et labels H / m / S.
class TimePickerCard extends ConsumerWidget {
  const TimePickerCard({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final setup = ref.watch(setupProvider);
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 20),
      decoration: BoxDecoration(
        color: AppColors.paperLight.withOpacity(0.8),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(
          color: AppColors.pencilDark.withOpacity(0.15),
          width: 1.5,
        ),
      ),
      child: Column(
        children: [
          // Color bar indicator
          Container(
            height: 6,
            margin: const EdgeInsets.only(bottom: 16),
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(3),
              gradient: const LinearGradient(
                colors: [
                  AppColors.crayonBlue,
                  AppColors.crayonGreen,
                  AppColors.crayonYellow,
                  AppColors.crayonOrange,
                  AppColors.crayonRed,
                ],
              ),
            ),
          ),
          // Time columns
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              _TimeBox(
                value: setup.hours,
                unit: 'h',
                label: 'H',
                color: AppColors.crayonBlue,
                maxValue: 23,
                onChanged: ref.read(setupProvider.notifier).setHours,
              ),
              const SizedBox(width: 12),
              _TimeBox(
                value: setup.minutes,
                unit: 'm',
                label: 'm',
                color: AppColors.crayonOrange,
                maxValue: 59,
                onChanged: ref.read(setupProvider.notifier).setMinutes,
              ),
              const SizedBox(width: 12),
              _TimeBox(
                value: setup.seconds,
                unit: 's',
                label: 'S',
                color: AppColors.crayonRed,
                maxValue: 59,
                onChanged: ref.read(setupProvider.notifier).setSeconds,
              ),
            ],
          ),
        ],
      ),
    );
  }
}

/// Boîte individuelle avec scroll vertical pour une valeur de temps
class _TimeBox extends StatefulWidget {
  final int value;
  final String unit;
  final String label;
  final Color color;
  final int maxValue;
  final ValueChanged<int> onChanged;

  const _TimeBox({
    required this.value,
    required this.unit,
    required this.label,
    required this.color,
    required this.maxValue,
    required this.onChanged,
  });

  @override
  State<_TimeBox> createState() => _TimeBoxState();
}

class _TimeBoxState extends State<_TimeBox> {
  late FixedExtentScrollController _ctrl;

  @override
  void initState() {
    super.initState();
    _ctrl = FixedExtentScrollController(initialItem: widget.value);
  }

  @override
  void didUpdateWidget(_TimeBox old) {
    super.didUpdateWidget(old);
    if (old.value != widget.value && _ctrl.hasClients) {
      _ctrl.animateToItem(widget.value,
        duration: const Duration(milliseconds: 300), curve: Curves.easeOutCubic);
    }
  }

  @override
  void dispose() { _ctrl.dispose(); super.dispose(); }

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        // Scrollable value box
        Container(
          width: 80,
          height: 80,
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(16),
            border: Border.all(
              color: widget.color.withOpacity(0.4),
              width: 2,
            ),
            boxShadow: [
              BoxShadow(
                color: widget.color.withOpacity(0.1),
                blurRadius: 8,
                offset: const Offset(0, 2),
              ),
            ],
          ),
          child: ListWheelScrollView.useDelegate(
            controller: _ctrl,
            itemExtent: 60,
            perspective: 0.003,
            diameterRatio: 1.2,
            physics: const FixedExtentScrollPhysics(),
            onSelectedItemChanged: widget.onChanged,
            childDelegate: ListWheelChildBuilderDelegate(
              childCount: widget.maxValue + 1,
              builder: (context, index) {
                final selected = index == widget.value;
                return Center(
                  child: Text(
                    '$index',
                    style: TextStyle(
                      fontFamily: 'Nunito',
                      fontSize: selected ? 40 : 30,
                      fontWeight: FontWeight.w900,
                      color: selected ? widget.color : AppColors.pencilFaint,
                    ),
                  ),
                );
              },
            ),
          ),
        ),
        const SizedBox(height: 6),
        // Unit label with color
        Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(
              widget.unit,
              style: TextStyle(
                fontFamily: 'Nunito',
                fontSize: 18,
                fontWeight: FontWeight.w800,
                color: widget.color,
              ),
            ),
          ],
        ),
        Text(
          widget.label,
          style: AppTextStyles.timePickerLabel,
        ),
      ],
    );
  }
}
