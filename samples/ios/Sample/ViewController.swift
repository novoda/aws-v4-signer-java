import UIKit
import main

class ViewController: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()
        
        let hash = Sha256Encoder().encode(value: "content as string")
        let request = HttpRequest.Companion().create(method: "PUT", pathAndQuery: "/-/vaults/examplevault")
        let signature = Signer.Builder()
            .awsCredentials(awsCredentials: AwsCredentials(accessKey: "AKIAIOSFODNN7EXAMPLE", secretKey: "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"))
            .header(header: Header(name: "Host", value: "glacier.us-east-1.amazonaws.com"))
            .header(header: Header(name: "x-amz-date", value: "20120525T002453Z"))
            .header(header: Header(name: "x-amz-glacier-version", value: "2012-06-01"))
            .buildGlacier(request: request, contentSha256: hash)
            .signature
        
        print(signature)
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

}
