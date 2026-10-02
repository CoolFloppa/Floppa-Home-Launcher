package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Pure in-memory AudioTrack PCM sound synthesizer for Big Floppa.
 * Eliminates all SoundPool/Stagefright/OMX decoder queries ("Failed to query component interface")
 * and provides instant, zero-latency sound effects on any Android device or virtual emulator.
 */
object FloppaSoundSynthesizer {
    private const val SAMPLE_RATE = 22050
    private val scope = CoroutineScope(Dispatchers.IO)
    private val isInitialized = AtomicBoolean(false)
    private val tracks = ConcurrentHashMap<String, AudioTrack>()

    fun initialize(context: Context? = null) {
        if (isInitialized.getAndSet(true)) return

        scope.launch {
            try {
                prepareTrack("chirp", generateChirpPcm())
                prepareTrack("purr", generatePurrPcm())
                prepareTrack("hiss", generateHissPcm())
                prepareTrack("chomp", generateChompPcm())
                prepareTrack("laser", generateLaserPcm())
            } catch (_: Throwable) {}
        }
    }

    private fun prepareTrack(name: String, pcm: ShortArray) {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(SAMPLE_RATE)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            val track = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(pcm.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(pcm, 0, pcm.size)
            tracks[name] = track
        } catch (_: Throwable) {}
    }

    fun playCaracalChirp() {
        playSound("chirp") { generateChirpPcm() }
    }

    fun playCaracalPurr() {
        playSound("purr") { generatePurrPcm() }
    }

    fun playCaracalHiss() {
        playSound("hiss") { generateHissPcm() }
    }

    fun playPelmeniChomp() {
        playSound("chomp") { generateChompPcm() }
    }

    fun playLaserFlop() {
        playSound("laser") { generateLaserPcm() }
    }

    private fun playSound(name: String, pcmProducer: () -> ShortArray) {
        scope.launch {
            try {
                var track = tracks[name]
                if (track == null) {
                    prepareTrack(name, pcmProducer())
                    track = tracks[name]
                }
                track?.let { t ->
                    if (t.state == AudioTrack.STATE_INITIALIZED) {
                        try {
                            t.stop()
                        } catch (_: Throwable) {}
                        t.reloadStaticData()
                        t.play()
                    }
                }
            } catch (_: Throwable) {}
        }
    }

    // --- High Fidelity Procedural Audio Generators ---

    private fun generateChirpPcm(): ShortArray {
        // Ascending chirp sweep with cheerful caracal timbre
        val durationMs = 140
        val sampleCount = (SAMPLE_RATE * durationMs) / 1000
        val buffer = ShortArray(sampleCount)

        for (i in 0 until sampleCount) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = i.toDouble() / sampleCount
            val freq = 650.0 + (1000.0 * sin(progress * PI))
            val envelope = sin(progress * PI)
            val sample = sin(2.0 * PI * freq * t) * envelope * 24000.0
            buffer[i] = sample.toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generatePurrPcm(): ShortArray {
        // Deep rumbling purr with amplitude modulation
        val durationMs = 260
        val sampleCount = (SAMPLE_RATE * durationMs) / 1000
        val buffer = ShortArray(sampleCount)

        for (i in 0 until sampleCount) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = i.toDouble() / sampleCount
            val am = 0.5 + 0.5 * sin(2.0 * PI * 24.0 * t)
            val envelope = sin(progress * PI)
            val base = sin(2.0 * PI * 125.0 * t) + 0.3 * sin(2.0 * PI * 250.0 * t)
            val sample = base * am * envelope * 22000.0
            buffer[i] = sample.toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateHissPcm(): ShortArray {
        // High frequency white noise burst (caracal warning hiss)
        val durationMs = 180
        val sampleCount = (SAMPLE_RATE * durationMs) / 1000
        val buffer = ShortArray(sampleCount)
        var lastSample = 0.0

        for (i in 0 until sampleCount) {
            val progress = i.toDouble() / sampleCount
            val white = Random.nextDouble(-1.0, 1.0)
            val highPass = white - lastSample
            lastSample = white * 0.7
            val envelope = exp(-progress * 3.5) * sin(progress * PI)
            val sample = highPass * envelope * 18000.0
            buffer[i] = sample.toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateChompPcm(): ShortArray {
        // Crispy dumpling munch (pitch drop with punchy attack)
        val durationMs = 140
        val sampleCount = (SAMPLE_RATE * durationMs) / 1000
        val buffer = ShortArray(sampleCount)

        for (i in 0 until sampleCount) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = i.toDouble() / sampleCount
            val freq = 420.0 * (1.0 - progress * 0.65)
            val envelope = exp(-progress * 5.0)
            val crunch = if (progress < 0.25) Random.nextDouble(-0.3, 0.3) else 0.0
            val sample = (sin(2.0 * PI * freq * t) + crunch) * envelope * 26000.0
            buffer[i] = sample.toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateLaserPcm(): ShortArray {
        // Sci-fi Floppa laser zap
        val durationMs = 120
        val sampleCount = (SAMPLE_RATE * durationMs) / 1000
        val buffer = ShortArray(sampleCount)

        for (i in 0 until sampleCount) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = i.toDouble() / sampleCount
            val freq = 2400.0 * exp(-progress * 4.0) + 200.0
            val envelope = (1.0 - progress)
            val sample = sin(2.0 * PI * freq * t) * envelope * 24000.0
            buffer[i] = sample.toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }
}
