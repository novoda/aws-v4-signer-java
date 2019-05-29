import com.novoda.aws.v4.signer.Header
import com.novoda.aws.v4.signer.HttpRequest
import com.novoda.aws.v4.signer.Signer
import com.novoda.aws.v4.signer.credentials.AwsCredentials
import com.novoda.aws.v4.signer.hash.Sha256Encoder
import org.junit.Assert
import org.junit.Assume
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import java.io.IOException
import java.net.URISyntaxException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

@RunWith(Parameterized::class)
class AWSTestSuite(private val testData: TestData) {
    private val signer: Signer

    init {

        Assume.assumeFalse(
                "This test is probably buggy: it expects us to translate '/?p aram1=val ue1' to '/?p=' without any reason.",
                "post-vanilla-query-space" == testData.name)

        val request = testData.request

        val builder = Signer.Builder().awsCredentials(AwsCredentials(ACCESS_KEY, SECRET_KEY))
                .region(REGION)
        for (header in testData.request.headers) {
            builder.header(header)
        }

        val httpRequest = HttpRequest.create(request.method, request.pathAndQuery)
        this.signer = builder.build(httpRequest, SERVICE, request.contentHash)
    }

    @Test
    fun canonicalRequest() {
        Assert.assertEquals("Invalid canonical request", testData.expectedCanonicalRequest, signer.canonicalRequest)
    }

    @Test
    fun stringToSign() {
        Assert.assertEquals("Invalid string to sign", testData.expectedStringToSign, signer.stringToSign)
    }

    @Test
    fun signature() {
        Assert.assertEquals("Invalid signature", testData.expectedSignature, signer.signature)
    }

    class TestData(val name: String,
                   val request: TestAWSRequestToSign,
                   val expectedCanonicalRequest: String,
                   val expectedStringToSign: String,
                   val expectedSignature: String) {

        override fun toString(): String {
            return name
        }
    }

    class TestAWSRequestToSign(
            val method: String,
            val pathAndQuery: String,
            val headers: List<Header>,
            val contentHash: String
    )

    companion object {

        @Parameters(name = "{0}")
        @Throws(IOException::class, URISyntaxException::class)
        @JvmStatic
        fun data(): Array<Any> {
            // Obtains the folder of /src/test/resources
            val url = ClassLoader.getSystemResource("aws-sig-v4-test-suite/tests/")
            val testPath = Paths.get(url.toURI())
            Files.walk(testPath).use { stream -> return stream.filter({ isTestDirectory(it) }).map<TestData> { parseTestData(it) }.toArray() }
        }

        private val ACCESS_KEY = "AKIDEXAMPLE"
        private val SECRET_KEY = "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY"
        private val REGION = "us-east-1"
        private val SERVICE = "service"

        private fun isTestDirectory(path: Path): Boolean {
            val name = path.fileName.toString()
            val requestFile = path.resolve("$name.req")
            return Files.exists(requestFile)
        }

        private fun parseTestData(directory: Path): TestData {
            val name = directory.fileName.toString()
            try {
                val request = parseRequest(directory.resolve(Paths.get("$name.req")))
                val expectedCanonicalRequest = readString(directory.resolve(Paths.get("$name.creq")))
                val expectedStringToSign = readString(directory.resolve(Paths.get("$name.sts")))
                val expectedSignature = readString(directory.resolve(Paths.get("$name.authz")))
                return TestData(name, request, expectedCanonicalRequest, expectedStringToSign, expectedSignature)
            } catch (e: RuntimeException) {
                throw IllegalStateException("Could not read test data at '$directory'", e)
            } catch (e: IOException) {
                throw IllegalStateException("Could not read test data at '$directory'", e)
            } catch (e: URISyntaxException) {
                throw IllegalStateException("Could not read test data at '$directory'", e)
            }

        }

        @Throws(IOException::class, URISyntaxException::class)
        private fun parseRequest(requestFile: Path): TestAWSRequestToSign {
            val lines = Files.readAllLines(requestFile)

            val it = lines.iterator()

            var requestLine = it.next()
            requestLine = requestLine.replace(" HTTP/1.1$".toRegex(), "")

            val requestLineParts = splitOnFirst(requestLine, ' ')
            var method = requestLineParts[0]
            // Remove the zero-width non-breaking spaces (codepoint 65279) in some
            // files...
            method = method.replace("\\p{C}".toRegex(), "")
            val pathAndQuery = requestLineParts[1]

            val headers = parseHeaders(it)

            val contentHash = Sha256Encoder.encode(parseContent(it))

            return TestAWSRequestToSign(method, pathAndQuery, headers, contentHash)
        }

        private fun parseHeaders(it: Iterator<String>): List<Header> {
            val headers = ArrayList<Header>()
            while (it.hasNext()) {
                val line = it.next()
                if (line.isEmpty()) {
                    break
                }
                if (line.startsWith(" ")) {
                    // Multi-line value
                    val lastIndex = headers.size - 1
                    val (name, value) = headers[lastIndex]
                    val newHeader = Header(name, value + "\n" + line)
                    headers[lastIndex] = newHeader
                } else {
                    val headerLineParts = splitOnFirst(line, ':')
                    val headerName = headerLineParts[0].toLowerCase(Locale.ROOT)
                    val headerValue = headerLineParts[1]
                    headers.add(Header(headerName, headerValue))
                }
            }
            return headers
        }

        private fun parseContent(it: Iterator<String>): String {
            val content = StringBuilder()
            var firstLine = true
            while (it.hasNext()) {
                val line = it.next()
                if (firstLine) {
                    firstLine = false
                } else {
                    content.append("\n")
                }
                content.append(line)
            }
            return content.toString()
        }

        private fun splitOnFirst(line: String, separator: Char): Array<String> {
            val firstSeparator = line.indexOf(separator)
            return arrayOf(line.substring(0, firstSeparator), line.substring(firstSeparator + 1, line.length))
        }

        @Throws(IOException::class)
        private fun readString(path: Path): String {
            return String(Files.readAllBytes(path), StandardCharsets.UTF_8)
        }
    }

}
