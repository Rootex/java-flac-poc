import groovy.util.logging.Log
import marytts.util.data.audio.MaryAudioUtils
import org.kc7bfi.jflac.FLACDecoder
import org.kc7bfi.jflac.PCMProcessor
import org.kc7bfi.jflac.metadata.SeekPoint
import org.kc7bfi.jflac.metadata.StreamInfo
import org.kc7bfi.jflac.util.ByteData
import org.kc7bfi.jflac.util.WavWriter
import javax.sound.sampled.AudioSystem

@Log
class PoC implements PCMProcessor {
    def inputStream
    def outputStream
    WavWriter wav

    PoC(File inputFile) {
        log.warning "$this should be loading $inputFile"
        this.inputStream = new FileInputStream(inputFile)
    }

    PoC(File inputFile, long from, long to){
        log.warning "$this should be loading $inputFile"
        this.from = from
        this.to = tos
        this.inputStream = new FileInputStream(inputFile)
        //TODO: implement seekpoint objects based on samples in frame to decode
        def seekPoint = new SeekPoint()
    }

    def decode(File outputFile) {
        log.info("Setting up decoder")
        this.outputStream = new FileOutputStream(outputFile)
        this.wav = new WavWriter(outputStream)
        def decoder = new FLACDecoder(this.inputStream)
        decoder.addPCMProcessor(this)
        decoder.decode()
    }

    def decodeSlice(File outputFile, SeekPoint from, SeekPoint to){
        log.info("Setting up decoder and decoding slice to target file")
        this.outputStream = new FileOutputStream(outputFile)
        this.wav = new WavWriter(outputStream)
        def decoder = new FLACDecoder(this.inputStream)
        decoder.addPCMProcessor(this)
        decoder.decode(from, to)
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

class Main{

    static void main(String[] args){
        def file = new File("/home/plaix/git/java-flac-poc/src/test/resources/test.flac")
        def outFile = new File("/home/plaix/git/java-flac-poc/expected.wav")
        assert file.exists()
        def pocOb =  new PoC(file)
        pocOb.decode(outFile)
        assert outFile.exists()
        def samples = pocOb.getSamples(outFile)
        println(samples)

    }
}