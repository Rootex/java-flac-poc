import groovy.util.logging.Log
import marytts.util.data.audio.MaryAudioUtils
import org.kc7bfi.jflac.FLACDecoder
import org.kc7bfi.jflac.FrameListener
import org.kc7bfi.jflac.PCMProcessor
import org.kc7bfi.jflac.frame.Frame
import org.kc7bfi.jflac.metadata.Metadata
import org.kc7bfi.jflac.metadata.SeekPoint
import org.kc7bfi.jflac.metadata.SeekTable
import org.kc7bfi.jflac.metadata.StreamInfo
import org.kc7bfi.jflac.sound.spi.FlacAudioFileReader
import org.kc7bfi.jflac.util.ByteData
import org.kc7bfi.jflac.util.WavWriter

import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.LineUnavailableException
import javax.sound.sampled.SourceDataLine
import javax.sound.sampled.spi.AudioFileReader

@Log
class PoC implements PCMProcessor {
    def inputStream
    def outputStream
    WavWriter wav

    PoC(File inputFile) {
        log.warning "$this should be loading $inputFile"
        this.inputStream = new FileInputStream(inputFile)
    }

    def decode(File outputFile) {
        log.info("Setting up decoder")
        this.outputStream = new FileOutputStream(outputFile)
        this.wav = new WavWriter(outputStream)
        def decoder = new FLACDecoder(this.inputStream)
        decoder.addPCMProcessor(this)
        decoder.decode()
    }

    double[] getSamples(File outputFile) {
        log.warning "$this should be decoding the samples!"
        def actualAIS = AudioSystem.getAudioInputStream(outputFile)
        def actual = MaryAudioUtils.getSamplesAsDoubleArray(actualAIS)
        return actual
    }

    @Override
    void processStreamInfo(StreamInfo streamInfo) {
        log.info("Writing Stream Information")
        try {
            this.wav.writeHeader(streamInfo)
        } catch (IOException io) {
            io.printStackTrace()
        }
    }

    @Override
    void processPCM(ByteData pcm) {
        log.info("Adding PCM")
        try {
            this.wav.writePCM(pcm)
        } catch (IOException io) {
            io.printStackTrace()
        }
    }
}