import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

/// Service de notifications locales.
/// Utilisé pour notifier l'enfant quand le timer se termine en background.
class NotificationService {
  final FlutterLocalNotificationsPlugin _plugin =
      FlutterLocalNotificationsPlugin();

  bool _initialized = false;

  /// Initialiser le plugin (appeler une seule fois dans main)
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

    _initialized = true;
  }

  /// Demander la permission (iOS + Android 13+)
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
        alert: true,
        badge: true,
        sound: true,
      );
      return granted ?? false;
    }
    return false;
  }

  /// Programmer une notification pour quand le timer se terminera.
  /// [duration] = durée restante du timer.
  Future<void> scheduleTimerEnd({
    required Duration duration,
    String title = 'AnimalTimer',
    String body = "C'est fini ! Le temps est écoulé 🎉",
  }) async {
    // Annuler toute notification précédente
    await cancelAll();

    const androidDetails = AndroidNotificationDetails(
      'timer_channel',
      'Timer Notifications',
      channelDescription: 'Notification quand le minuteur se termine',
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

    // Programmer la notification dans [duration]
    final scheduledDate = DateTime.now().add(duration);

    await _plugin.schedule(
      0, // ID
      title,
      body,
      scheduledDate,
      details,
    );
  }

  /// Afficher une notification immédiate (fallback)
  Future<void> showNow({
    String title = 'AnimalTimer',
    String body = "C'est fini ! Le temps est écoulé 🎉",
  }) async {
    const androidDetails = AndroidNotificationDetails(
      'timer_channel',
      'Timer Notifications',
      channelDescription: 'Notification quand le minuteur se termine',
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

  /// Annuler toutes les notifications programmées
  Future<void> cancelAll() async {
    await _plugin.cancelAll();
  }

  void _onNotificationTap(NotificationResponse response) {
    // L'app est déjà ouverte ou revient au premier plan
    // Pas d'action spéciale nécessaire
  }
}

final notificationServiceProvider = Provider<NotificationService>((ref) {
  return NotificationService();
});
