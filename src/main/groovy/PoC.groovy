import groovy.util.logging.Log
import marytts.util.data.audio.MaryAudioUtils
import org.apache.commons.codec.EncoderException
import org.kc7bfi.jflac.FLACDecoder
import org.kc7bfi.jflac.PCMProcessor
import org.kc7bfi.jflac.metadata.StreamInfo
import org.kc7bfi.jflac.sound.spi.FlacAudioFileReader
import org.kc7bfi.jflac.util.ByteData
import org.kc7bfi.jflac.util.WavWriter
import org.gagravarr.ogg.OggPacketReader
import org.apache.commons.codec.Encoder
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem


@Log
class PoC implements PCMProcessor {
    def inputStream
    def outputStream
    File inputFile
    WavWriter wav

    PoC(File inFile) {
        log.warning "$this should be loading $inputFile"
        this.inputStream = new FileInputStream(inFile)
        this.inputFile = inFile
    }
    PoC(){}

    def decode(File outputFile) {
        log.info("Setting up decoder")
        this.outputStream = new FileOutputStream(outputFile)
        this.wav = new WavWriter(outputStream)
        def decoder = new FLACDecoder(this.inputStream)
        decoder.addPCMProcessor(this)
        decoder.decode()
    }

    def decode(File outputFile, long startSeconds, long secondsToCopy){
        log.info("Setting up decoder")
        this.outputStream = new FileOutputStream(outputFile)
        this.wav = new WavWriter(outputStream)
        def decoder = new FLACDecoder(getFlacSamples(startSeconds, secondsToCopy))
        decoder.addPCMProcessor(this)
        decoder.decode()
    }

    def getFlacSamples(long startSecond, long secondsToCopy){
        def flacAFR = new FlacAudioFileReader()
        def audioIS = flacAFR.getAudioInputStream(this.inputFile)
        def format = audioIS.getFormat()
        def bitpersecond = format.getSampleRate() * format.getChannels() * 16
        def bytespersecond = bitpersecond / 8
        audioIS.skip(startSecond * (int)bytespersecond)
        long samplesToCopy = secondsToCopy * format.getSampleRate()
        def newStream = new AudioInputStream(audioIS, format, samplesToCopy)
        return newStream
    }

    double[] getSamples(File outputFile) {
        log.warning "$this should be decoding the samples!"
        def actualAIS = AudioSystem.getAudioInputStream(outputFile)
        def actual = MaryAudioUtils.getSamplesAsDoubleArray(actualAIS)
        return actual
    }

    def wrapFlacOgg(File input){

        return true
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