import groovy.util.logging.Log
import marytts.util.data.audio.MaryAudioUtils
import org.kc7bfi.jflac.FLACDecoder
import org.kc7bfi.jflac.FrameListener
import org.kc7bfi.jflac.PCMProcessor
import org.kc7bfi.jflac.frame.Frame
import org.kc7bfi.jflac.io.RandomFileInputStream
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
        this.inputStream = new RandomFileInputStream(inputFile)
    }

    def decode(File outputFile) {
        log.info("Setting up decoder")
        this.outputStream = new RandomFileInputStream(outputFile)
        this.wav = new WavWriter(outputStream)
        def decoder = new FLACDecoder(this.inputStream)
        decoder.addPCMProcessor(this)
        decoder.decode()
    }

    def decode(File outputfile, int seekpoinFrom, int seekpointTo) {
        log.info("Setting up decoder")
        this.outputStream = new FileOutputStream(outputfile)
        this.wav = new WavWriter(this.outputStream)
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
        if(seekpoinFrom <= seekTable.numberOfPoints() + 1) {
            from = new SeekPoint(seekTable.getSeekPoint(seekpoinFrom).sampleNumber,
                    seekTable.getSeekPoint(seekpoinFrom).streamOffset,
                    seekTable.getSeekPoint(seekpoinFrom).frameSamples)
        }

        if(seekpoinFrom <= seekTable.numberOfPoints() + 1 ) {
            to = new SeekPoint(seekTable.getSeekPoint(seekpointTo).sampleNumber,
                    seekTable.getSeekPoint(seekpointTo).streamOffset,
                    seekTable.getSeekPoint(seekpointTo).frameSamples)
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