import 'package:audioplayers/audioplayers.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

class AudioService {
  final AudioPlayer _ambientPlayer = AudioPlayer();
  final AudioPlayer _endPlayer = AudioPlayer();
  bool _isAmbientPlaying = false;

  Future<void> playAmbient(String assetPath, {double volume = 0.5}) async {
    try {
      _isAmbientPlaying = true;
      await _ambientPlayer.setReleaseMode(ReleaseMode.loop);
      await _ambientPlayer.setVolume(volume);
      await _ambientPlayer.play(AssetSource(assetPath));
    } catch (_) {
      _isAmbientPlaying = false;
    }
  }

  Future<void> pauseAmbient() async {
    if (_isAmbientPlaying) {
      await _ambientPlayer.pause();
    }
  }

  Future<void> resumeAmbient() async {
    if (_isAmbientPlaying) {
      await _ambientPlayer.resume();
    }
  }

  Future<void> stopAmbient() async {
    _isAmbientPlaying = false;
    await _ambientPlayer.stop();
  }

  Future<void> playEndSound(String assetPath, {double volume = 0.7}) async {
    try {
      await _endPlayer.setVolume(volume);
      await _endPlayer.play(AssetSource(assetPath));
    } catch (_) {
      // Graceful fallback
    }
  }

  Future<void> stopAll() async {
    await _ambientPlayer.stop();
    await _endPlayer.stop();
    _isAmbientPlaying = false;
  }

  Future<void> setAmbientVolume(double volume) async {
    await _ambientPlayer.setVolume(volume);
  }

  void dispose() {
    _ambientPlayer.dispose();
    _endPlayer.dispose();
  }
}

final audioServiceProvider = Provider<AudioService>((ref) {
  final service = AudioService();
  ref.onDispose(service.dispose);
  return service;
});
