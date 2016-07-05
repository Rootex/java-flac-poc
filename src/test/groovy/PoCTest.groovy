import org.codehaus.groovy.runtime.powerassert.PowerAssertionError
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
    void testDecodingGivenTimeFrame(){
        def proc = 'sox sample2.wav sliced-expected.wav trim 0 00:05'.execute(null, tempDir)
        proc.waitFor()
        def expectedFile = new File("$tempDir/sliced-expected.wav")
        def inputAudio = new File("$tempDir/sample1.flac")
        def actualFile = new File("$tempDir/sliced-actual.wav")
        assert expectedFile.exists()
        def expectedAIS = AudioSystem.getAudioInputStream(expectedFile)
        def expected = MaryAudioUtils.getSamplesAsDoubleArray(expectedAIS)
        def poc = new PoC(inputAudio)
        poc.decode(actualFile, 0, 5)
        assert actualFile.exists()
        def actual = poc.getSamples(actualFile)
        assert actual == expected
    }

}
