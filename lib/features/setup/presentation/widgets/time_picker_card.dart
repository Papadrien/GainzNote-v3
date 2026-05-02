import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import '../../../../core/utils/localization_helper.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../../core/theme/app_colors.dart';
import '../../providers/setup_provider.dart';

class TimePickerCard extends ConsumerWidget {
  const TimePickerCard({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final setup = ref.watch(setupProvider);
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 16),
      decoration: BoxDecoration(
        color: AppColors.paperLight.withValues(alpha: 0.8),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: AppColors.pencilDark, width: 2.5),
      ),
      child: Row(
        children: [
          _Col(value: setup.hours, label: context.l10n.hours, color: AppColors.crayonBlue,
            max: 23, step: 1, onChanged: ref.read(setupProvider.notifier).setHours),
          const SizedBox(width: 8),
          _Col(value: setup.minutes, label: context.l10n.minutes, color: AppColors.crayonOrange,
            max: 59, step: 1, onChanged: ref.read(setupProvider.notifier).setMinutes),
          const SizedBox(width: 8),
          _Col(value: setup.seconds, label: context.l10n.seconds, color: AppColors.crayonRed,
            max: 59, step: 5, onChanged: ref.read(setupProvider.notifier).setSeconds),
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
  final int step;
  final ValueChanged<int> onChanged;
  const _Col({required this.value, required this.label,
    required this.color, required this.max, required this.step, required this.onChanged});
  @override
  State<_Col> createState() => _ColState();
}

class _ColState extends State<_Col> {
  late FixedExtentScrollController _ctrl;
  bool _userScrolling = false;

  /// Convertit une valeur réelle en index dans la liste
  int _valueToIndex(int value) {
    if (widget.step <= 1) return value;
    return (value ~/ widget.step).clamp(0, _itemCount - 1);
  }

  /// Nombre d'items disponibles
  int get _itemCount => widget.step <= 1 ? widget.max + 1 : (widget.max ~/ widget.step) + 1;

  @override
  void initState() {
    super.initState();
    _ctrl = FixedExtentScrollController(initialItem: _valueToIndex(widget.value));
  }

  @override
  void didUpdateWidget(_Col old) {
    super.didUpdateWidget(old);
    if (!_userScrolling && old.value != widget.value && _ctrl.hasClients) {
      _ctrl.jumpToItem(_valueToIndex(widget.value));
    }
  }

  @override
  void dispose() { _ctrl.dispose(); super.dispose(); }

  @override
  Widget build(BuildContext context) {
    return Expanded(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            height: 160,
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(16),
              border: Border.all(
                color: AppColors.pencilDark, width: 2),
            ),
            child: NotificationListener<ScrollNotification>(
              onNotification: (notif) {
                if (notif is ScrollStartNotification) {
                  _userScrolling = true;
                } else if (notif is ScrollEndNotification) {
                  _userScrolling = false;
                }
                return true;
              },
              child: CupertinoPicker.builder(
                scrollController: _ctrl,
                itemExtent: 52,
                diameterRatio: 1.2,
                squeeze: 1.0,
                useMagnifier: true,
                magnification: 1.15,
                backgroundColor: Colors.transparent,
                selectionOverlay: Container(
                  decoration: BoxDecoration(
                    border: Border.symmetric(
                      horizontal: BorderSide(
                        color: widget.color.withValues(alpha: 0.25),
                        width: 1.5)),
                  ),
                ),
                onSelectedItemChanged: (index) {
                  final realValue = index * widget.step;
                  widget.onChanged(realValue);
                },
                childCount: _itemCount,
                itemBuilder: (context, index) {
                  final displayValue = index * widget.step;
                  final isSelected = displayValue == widget.value;
                  return Center(child: Text(
                    '$displayValue',
                    style: TextStyle(
                      fontFamily: 'Nunito',
                      fontSize: 32,
                      fontWeight: FontWeight.w900,
                      color: isSelected
                          ? widget.color
                          : AppColors.pencilFaint,
                    ),
                  ));
                },
              ),
            ),
          ),
          const SizedBox(height: 8),
          Text(widget.label, style: TextStyle(
            fontFamily: 'Nunito', fontSize: 13,
            fontWeight: FontWeight.w700,
            color: widget.color, letterSpacing: 0.5)),
        ],
      ),
    );
  }
}
