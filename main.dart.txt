// lib/main.dart
// Point d'entrée de l'application.
// ProviderScope est le wrapper Riverpod qui rend tous les providers accessibles.

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'providers/providers.dart';
import 'theme/theme.dart';
import 'screens/home_screen.dart';

void main() {
  runApp(
    // ProviderScope DOIT entourer toute l'app pour que Riverpod fonctionne
    const ProviderScope(
      child: GainzNoteApp(),
    ),
  );
}

class GainzNoteApp extends ConsumerWidget {
  const GainzNoteApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final dark = ref.watch(darkThemeProvider);

    return MaterialApp(
      title: 'GainzNote',
      debugShowCheckedModeBanner: false,
      theme: buildTheme(dark: false),
      darkTheme: buildTheme(dark: true),
      themeMode: dark ? ThemeMode.dark : ThemeMode.light,
      home: const HomeScreen(),
    );
  }
}
