import XCTest
@testable import sample
import main


class sampleTests: XCTestCase {
    
    override func setUp() {
        super.setUp()
        // Put setup code here. This method is called before the invocation of each test method in the class.
    }
    
    override func tearDown() {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
        super.tearDown()
    }
    
    func testSha256Request() {
        let request = "PUT\n" +
            "/-/vaults/examplevault\n" +
            "\n" +
            "host:glacier.us-east-1.amazonaws.com\n" +
            "x-amz-date:20120525T002453Z\n" +
            "x-amz-glacier-version:2012-06-01\n" +
            "\n" +
            "host;x-amz-date;x-amz-glacier-version\n" +
        "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        
        let contentHash = Sha256Encoder().encode(value: request)
        
        assert(contentHash == "5f1da1a2d0feb614dd03d71e87928b8e449ac87614479332aced3a701f916743")
    }
    
    func testHmac256Signature() {
        let stringToSign = "AWS4-HMAC-SHA256\n" +
            "20150830T123600Z\n" +
            "20150830/us-east-1/iam/aws4_request\n" +
        "f536975d06c0309214f805bb90ccff089219ecd68b2577efef23edd43b7e1a59"
        let signingKey = "c4afb1cc5771d871763a393e44b703571b55cc28424d1a5e86da6ed3c154a4b9"
        
        let signature = HmacTestUtils().createSignature(stringToSign: stringToSign, signingKey: signingKey)
        
        assert(signature == "fe52b221b5173b501c9863cec59554224072ca34c1c827ec5fb8a257f97637b1")
    }
    
    func  testSignRequest() {
        let hash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        let request = HttpRequest.Companion().create(method: "PUT", pathAndQuery: "/-/vaults/examplevault")
        let signature = Signer.Builder()
        .awsCredentials(awsCredentials: AwsCredentials(accessKey: "AKIAIOSFODNN7EXAMPLE", secretKey: "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"))
        .header(header: Header(name: "Host", value: "glacier.us-east-1.amazonaws.com"))
        .header(header: Header(name: "x-amz-date", value: "20120525T002453Z"))
        .header(header: Header(name: "x-amz-glacier-version", value: "2012-06-01"))
        .buildGlacier(request: request, contentSha256: hash)
        .signature
        
        let expectedSignature = "AWS4-HMAC-SHA256 Credential=AKIAIOSFODNN7EXAMPLE/20120525/us-east-1/glacier/aws4_request, " + "SignedHeaders=host;x-amz-date;x-amz-glacier-version, Signature=3ce5b2f2fffac9262b4da9256f8d086b4aaf42eba5f111c21681a65a127b7c2a"
        
        assert(signature == expectedSignature)
    }
    
}
