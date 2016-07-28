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
    void testDecodingSPOneTwo(){
        def testFile = new File("$tempDir/Test.flac")
        def sliceOne = new File("$tempDir/sliceone.wav")
        def poc = new PoC(testFile)
        poc.decode(sliceOne, 5000, 13000)
        assert sliceOne.exists()
    }

    @Test
    void testDecodingSPStartOne(){
        def testFile = new File("$tempDir/Test.flac")
        def sliceOne = new File("$tempDir/sliceone.wav")
        def poc = new PoC(testFile)
        poc.decode(sliceOne, 0, 5000)
        assert sliceOne.exists()
    }

    @Test
    void testDecodingSPTwoEnd(){
        def testFile = new File("$tempDir/Test.flac")
        def sliceOne = new File("$tempDir/sliceone.wav")
        def poc = new PoC(testFile)
        poc.decode(sliceOne, 13000, 16000)
        assert sliceOne.exists()
    }
}
