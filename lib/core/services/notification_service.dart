import 'dart:io';
import 'package:flutter/widgets.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

class NotificationService with WidgetsBindingObserver {
  final FlutterLocalNotificationsPlugin _plugin =
      FlutterLocalNotificationsPlugin();
  bool _initialized = false;

  /// Track whether the app is in the foreground.
  AppLifecycleState _appState = AppLifecycleState.resumed;

  Future<void> init() async {
    if (_initialized) return;
    const androidSettings = AndroidInitializationSettings('@mipmap/ic_launcher');
    const iosSettings = DarwinInitializationSettings(
      requestAlertPermission: true,
      requestBadgePermission: true,
      requestSoundPermission: true,
    );
    const initSettings = InitializationSettings(
      android: androidSettings,
      iOS: iosSettings,
    );
    await _plugin.initialize(
      initSettings,
      onDidReceiveNotificationResponse: _onNotificationTap,
    );

    // Start observing app lifecycle
    WidgetsBinding.instance.addObserver(this);

    _initialized = true;
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    _appState = state;
  }

  Future<bool> requestPermission() async {
    if (Platform.isAndroid) {
      final android = _plugin.resolvePlatformSpecificImplementation<
          AndroidFlutterLocalNotificationsPlugin>();
      final granted = await android?.requestNotificationsPermission();
      return granted ?? false;
    } else if (Platform.isIOS) {
      final ios = _plugin.resolvePlatformSpecificImplementation<
          IOSFlutterLocalNotificationsPlugin>();
      final granted = await ios?.requestPermissions(
        alert: true, badge: true, sound: true,
      );
      return granted ?? false;
    }
    return false;
  }

  /// Schedule a notification after [duration].
  /// Only shows the notification if the app is NOT in the foreground,
  /// since the app already handles the timer-end screen itself.
  Future<void> scheduleTimerEnd({
    required Duration duration,
    String title = 'AnimalTimer',
    String body = 'Timer ended',
  }) async {
    await cancelAll();

    Future.delayed(duration, () async {
      // Only show notification if app is in background or killed.
      // When the app is in foreground, the timer_screen already
      // navigates to the finish_screen — no need to notify.
      if (_appState != AppLifecycleState.resumed) {
        await showNow(title: title, body: body);
      }
    });
  }

  Future<void> showNow({
    String title = 'AnimalTimer',
    String body = 'Timer ended',
  }) async {
    const androidDetails = AndroidNotificationDetails(
      'timer_channel',
      'Timer Notifications',
      channelDescription: 'Notification when the timer ends',
      importance: Importance.high,
      priority: Priority.high,
      playSound: true,
      enableVibration: true,
      icon: '@mipmap/ic_launcher',
    );
    const iosDetails = DarwinNotificationDetails(
      presentAlert: true,
      presentBadge: true,
      presentSound: true,
    );
    const details = NotificationDetails(
      android: androidDetails,
      iOS: iosDetails,
    );
    await _plugin.show(0, title, body, details);
  }

  Future<void> cancelAll() async {
    await _plugin.cancelAll();
  }

  void _onNotificationTap(NotificationResponse response) {}
}

final notificationServiceProvider = Provider<NotificationService>((ref) {
  return NotificationService();
});
