package com.novoda.aws.signer.v4.sample

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.novoda.aws.v4.signer.HttpRequest
import com.novoda.aws.v4.signer.Signer
import com.novoda.aws.v4.signer.credentials.AwsCredentials
import com.novoda.aws.v4.signer.hash.Sha256Encoder
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val content = "some content"
        val contentSha256 = Sha256Encoder.encode(content)
        val url = "https://examplebucket.s3.amazonaws.com?max-keys=2&prefix=J"
        val request = HttpRequest.create("GET", url)

        val signature = Signer.Builder()
            .awsCredentials(AwsCredentials("key", "secret"))
            .header("Content-Type", "application/json")
            .header("Host", "examplebucket.s3.amazonaws.com")
            .header("x-amz-date", "20130524T000000Z")
            .header("x-amz-content-sha256", contentSha256)
            .buildS3(request, contentSha256)
            .signature

        Log.d("AWS V4 Signature", "Signature $signature")
    }
}
