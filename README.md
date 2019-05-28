# aws-v4-signer

aws-v4-signer is a lightweight Kotlin-Multiplatform implementation of the AWS V4 signing algorithm required by many of the AWS services. 

Compatible with Java 7+, Kotlin & Swift.

## Setup

[![CI status](https://ci.novoda.com/buildStatus/icon?job=aws-v4-signer)](https://ci.novoda.com/job/aws-v4-signer/lastBuild/console) [![Download from Bintray](https://api.bintray.com/packages/novoda-oss/maven/aws-v4-signer/images/download.svg)](https://bintray.com/novoda-oss/maven/aws-v4-signer/_latestVersion)

### Gradle

Add the latest aws-v4-signer Gradle dependency to your project

```gradle
repository {
    maven {
        url 'https://novoda.bintray.com/snapshots'
    }
} 

dependencies {
    implementation 'com.novoda:aws-v4-signer:0.0.1'
}
```

### Xcode

Generate an Xcode framework and link it to your project following the [official documentation](https://kotlinlang.org/docs/tutorials/native/mpp-ios-android.html).
An example can be found under `/samples/ios`. 

```gradle
./gradlew :aws-v4-signer:packForXCode
```

## Usage

### S3

Swift:
```swift
 let hash = Sha256Encoder().encode(value: "content as string")
        let request = HttpRequest.Companion().create(method: "PUT", pathAndQuery: "?max-keys=2&prefix=J")
        let signature = Signer.Builder()
            .awsCredentials(awsCredentials: AwsCredentials(accessKey: "AKIAIOSFODNN7EXAMPLE", secretKey: "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"))
            .header(header: Header(name: "Host", value: "examplebucket.s3.amazonaws.com"))
            .header(header: Header(name: "x-amz-date", value: "20120525T002453Z"))
            .header(header: Header(name: "x-amz-content-sha256", value: hash))
            .buildS3(request: request, contentSha256: hash)
            .signature
```

Kotlin:
```kotlin
val hash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        val request = HttpRequest.create("GET", "?max-keys=2&prefix=J")

        val signature = Signer.Builder()
                .awsCredentials(AwsCredentials(ACCESS_KEY, SECRET_KEY))
                .header("Host", "examplebucket.s3.amazonaws.com")
                .header("x-amz-date", "20130524T000000Z")
                .header("x-amz-content-sha256", hash)
                .buildS3(request, hash)
                .signature
```

### Glacier

Swift:
```swift
let hash = Sha256Encoder().encode(value: "content as string")
        let request = HttpRequest.Companion().create(method: "PUT", pathAndQuery: "/-/vaults/examplevault")
        let signature = Signer.Builder()
            .awsCredentials(awsCredentials: AwsCredentials(accessKey: "AKIAIOSFODNN7EXAMPLE", secretKey: "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"))
            .header(header: Header(name: "Host", value: "glacier.us-east-1.amazonaws.com"))
            .header(header: Header(name: "x-amz-date", value: "20120525T002453Z"))
            .header(header: Header(name: "x-amz-glacier-version", value: "2012-06-01"))
            .buildGlacier(request: request, contentSha256: hash)
            .signature
```

Kotlin:
```kotlin
val hash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        val request = HttpRequest.create("PUT", "/-/vaults/examplevault")

        val signature = Signer.Builder()
                .awsCredentials(AwsCredentials(ACCESS_KEY, SECRET_KEY))
                .header("Host", "glacier.us-east-1.amazonaws.com")
                .header("x-amz-date", "20120525T002453Z")
                .header("x-amz-glacier-version", "2012-06-01")
                .buildGlacier(request, hash)
                .signature
```
