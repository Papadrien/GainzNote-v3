import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../../core/theme/app_colors.dart';
import '../../providers/setup_provider.dart';

class TimePickerCard extends ConsumerWidget {
  const TimePickerCard({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final setup = ref.watch(setupProvider);
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 20),
      decoration: BoxDecoration(
        color: AppColors.paperLight.withOpacity(0.8),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: AppColors.pencilDark.withOpacity(0.15), width: 1.5),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          _Col(value: setup.hours, label: 'Heures', color: AppColors.crayonBlue,
            max: 23, onChanged: ref.read(setupProvider.notifier).setHours),
          const SizedBox(width: 10),
          _Col(value: setup.minutes, label: 'Minutes', color: AppColors.crayonOrange,
            max: 59, onChanged: ref.read(setupProvider.notifier).setMinutes),
          const SizedBox(width: 10),
          _Col(value: setup.seconds, label: 'Secondes', color: AppColors.crayonRed,
            max: 59, onChanged: ref.read(setupProvider.notifier).setSeconds),
        ],
      ),
    );
  }
}

class _Col extends StatefulWidget {
  final int value;
  final String label;
  final Color color;
  final int max;
  final ValueChanged<int> onChanged;
  const _Col({required this.value, required this.label, required this.color,
    required this.max, required this.onChanged});
  @override
  State<_Col> createState() => _ColState();
}

class _ColState extends State<_Col> {
  late FixedExtentScrollController _ctrl;

  @override
  void initState() {
    super.initState();
    _ctrl = FixedExtentScrollController(initialItem: widget.value);
  }

  @override
  void didUpdateWidget(_Col old) {
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
        Container(
          width: 90, height: 140,
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(16),
            border: Border.all(color: widget.color.withOpacity(0.4), width: 2),
            boxShadow: [BoxShadow(color: widget.color.withOpacity(0.1),
              blurRadius: 8, offset: const Offset(0, 2))],
          ),
          child: ShaderMask(
            shaderCallback: (bounds) => LinearGradient(
              begin: Alignment.topCenter, end: Alignment.bottomCenter,
              colors: [Colors.white.withOpacity(0.0), Colors.white,
                Colors.white, Colors.white.withOpacity(0.0)],
              stops: const [0.0, 0.25, 0.75, 1.0],
            ).createShader(bounds),
            blendMode: BlendMode.dstIn,
            child: ListWheelScrollView.useDelegate(
              controller: _ctrl,
              itemExtent: 48,
              perspective: 0.003,
              diameterRatio: 1.4,
              physics: const FixedExtentScrollPhysics(),
              squeeze: 1.0,
              onSelectedItemChanged: widget.onChanged,
              childDelegate: ListWheelChildBuilderDelegate(
                childCount: widget.max + 1,
                builder: (context, index) {
                  final sel = index == widget.value;
                  return Center(child: AnimatedDefaultTextStyle(
                    duration: const Duration(milliseconds: 200),
                    style: TextStyle(fontFamily: 'Nunito',
                      fontSize: sel ? 38 : 24, fontWeight: FontWeight.w900,
                      color: sel ? widget.color : AppColors.pencilFaint.withOpacity(0.5)),
                    child: Text('$index'),
                  ));
                },
              ),
            ),
          ),
        ),
        const SizedBox(height: 8),
        Text(widget.label, style: TextStyle(fontFamily: 'Nunito', fontSize: 13,
          fontWeight: FontWeight.w700, color: widget.color, letterSpacing: 0.5)),
      ],
    );
  }
}
