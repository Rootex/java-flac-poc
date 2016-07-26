import org.testng.annotations.*
import marytts.util.data.audio.MaryAudioUtils
import javax.sound.sampled.AudioSystem

class PoCTest {

    def tempDir

    @BeforeClass
    void setup() {
        tempDir = new File(System.getProperty('tempDir'))
    }

    @Test
    void testDecoding() {
        def expectedFile = new File("$tempDir/expected.wav")
        def expectedAIS = AudioSystem.getAudioInputStream(expectedFile)
        def expected = MaryAudioUtils.getSamplesAsDoubleArray(expectedAIS)
        def actualFile = new File("$tempDir/actual.wav")
        def inputFile = new File("$tempDir/test.flac")
        def poc = new PoC(inputFile)
        poc.decode(actualFile)
        assert actualFile.exists()
        def actual = poc.getSamples(actualFile)
        assert expected == actual

    }

    @Test
    void testDecodingExternal() {
        def expectedFile = new File("$tempDir/expected.wav")
        def expectedAIS = AudioSystem.getAudioInputStream(expectedFile)
        def expected = MaryAudioUtils.getSamplesAsDoubleArray(expectedAIS)
        def proc = 'sox test.flac actual-external.wav'.execute(null, tempDir)
        proc.waitFor()
        def actualFile = new File("$tempDir/actual-external.wav")
        assert actualFile.exists()
        def actualAIS = AudioSystem.getAudioInputStream(actualFile)
        def actual = MaryAudioUtils.getSamplesAsDoubleArray(actualAIS)
        assert expected == actual
    }

    @Test
    void testDecodingGivenTimeFrame1(){
        def proc = 'sox sample1.flac sliced-expected.wav trim 0 3'.execute(null, tempDir)
        proc.waitFor()
        def expectedFile = new File("$tempDir/sliced-expected.wav")
        def inputAudio = new File("$tempDir/sample1.flac")
        def actualFile = new File("$tempDir/sliced-actual.wav")
        assert expectedFile.exists()
        def poc = new PoC(inputAudio)
        def expected = poc.getSamples(expectedFile)
        poc.decode(actualFile, 0, 3)
        assert actualFile.exists()
        def actual = poc.getSamples(actualFile)
        assert actual == expected

    }

    @Test
    void testDecodingGivenTimeFrame2(){
        def proc = 'sox sample1.flac sliced-expected2.wav trim 3 7'.execute(null, tempDir)
        proc.waitFor()
        def expectedFile = new File("$tempDir/sliced-expected2.wav")
        def inputAudio = new File("$tempDir/sample1.flac")
        def decodedFile = new File("$tempDir/decoded.wav")
        def actualFile = new File("$tempDir/sliced-actual2.wav")
        assert expectedFile.exists()

        def poc = new PoC(inputAudio)
        poc.decode(decodedFile)
        assert decodedFile.exists()
        poc.getWavSamples(decodedFile, actualFile, 3, 7)
        def expected = poc.getSamples(expectedFile)
        def actual = poc.getSamples(actualFile)
        assert actual == expected
    }

    @Test
    void testDecodingGivenTimeFrame3(){
        def proc = 'sox sample1.flac sliced-expected3.wav trim 7 14'.execute(null, tempDir)
        proc.waitFor()
        def expectedFile = new File("$tempDir/sliced-expected3.wav")
        def inputAudio = new File("$tempDir/sample1.flac")
        def decodedFile = new File("$tempDir/decoded.wav")
        def actualFile = new File("$tempDir/sliced-actual3.wav")
        assert expectedFile.exists()

        def poc = new PoC(inputAudio)
        poc.decode(decodedFile)
        assert decodedFile.exists()
        poc.getWavSamples(decodedFile, actualFile, 7, 14)
        def expected = poc.getSamples(expectedFile)
        def actual = poc.getSamples(actualFile)
        assert actual == expected
    }

    @Test
    void testOggWrappingFlac(){
        def flacFile = new File("$tempDir/test.flac")
        def poc = new PoC()
        def result = poc.wrapFlacOgg(flacFile)
        assert result == true
    }

}
