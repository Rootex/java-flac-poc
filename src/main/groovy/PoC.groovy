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
import javax.sound.sampled.AudioSystem

@Log
class PoC implements PCMProcessor, FrameListener{
    def inputStream
    def outputStream
    WavWriter wav
    SeekTable seekTable

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

    def decode(File outputfile, long fromSamples, long toSamples, long fromOffset, long toOffset,
               int fromFramesamples, int toFramesamples) {
        log.info("Setting up decoder")
        this.outputStream = new FileOutputStream(outputfile)
        this.wav = new WavWriter(outputStream)
        def decoder = new FLACDecoder(this.inputStream)
        decoder.addPCMProcessor(this)
        decoder.addFrameListener(this)
        decoder.readMetadata()

        if(seekTable == null){
            return
        }

        //TODO Either create seekpoints depending on the samples, offset or by number of seekpoints.
        SeekPoint from
        SeekPoint to
        if(fromSamples >=0 && fromSamples <= decoder.getStreamInfo().getTotalSamples()) {
            from = new SeekPoint(fromSamples, fromOffset, fromFramesamples)
        }

        if(toSamples >=0 && toSamples <= decoder.getStreamInfo().getTotalSamples()) {
            to = new SeekPoint(toSamples, toOffset, toFramesamples)
        }


        decoder.decode(from, to)
    }

    double[] getSamples(File outputFile) {
        log.warning "$this should be decoding the samples!"
        def actualAIS = AudioSystem.getAudioInputStream(outputFile)
        def actual = MaryAudioUtils.getSamplesAsDoubleArray(actualAIS)
        return actual
    }

    void processStreamInfo(StreamInfo streamInfo) {
        log.info("Writing Stream Information")
        try {
            this.wav.writeHeader(streamInfo)
        } catch (IOException io) {
            io.printStackTrace()
        }
    }

    void processPCM(ByteData pcm) {
        log.info("Adding PCM")
        try {
            this.wav.writePCM(pcm)
        } catch (IOException io) {
            io.printStackTrace()
        }
    }

    void processMetadata(Metadata metadata){
        if(metadata instanceof SeekTable) {
            seekTable = (SeekTable) metadata
        }
    }

    void processFrame(Frame frame){}

    void processError(String msg){
        log.warning "$this Error" + msg
    }
}


class Main{
    def static tempDir = new File(System.getProperty('user.dir'))
    static void main(String[] args){
        def test = new File("$tempDir/src/test/resources/Test.flac")
        def output = new File("$tempDir/src/test/resources/TestOut.wav")
        def poc = new PoC(test)
        poc.decode(output, 0, 5000, 124, 8327, 65608, 65608)
    }
}