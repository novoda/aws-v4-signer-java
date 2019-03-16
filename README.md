# aws-v4-signer-java

aws-v4-signer-java is a lightweight implementation of the AWS V4 signing algorithm required by many of the AWS services. 

Compatible with Java 7+.

## Setup

[![CI status](https://ci.novoda.com/buildStatus/icon?job=aws-v4-signer-java)](https://ci.novoda.com/job/aws-v4-signer-java/lastBuild/console) [![Download from Bintray](https://api.bintray.com/packages/novoda/snapshots/aws-v4-signer-java/images/download.svg)](https://bintray.com/novoda/snapshots/aws-v4-signer-java/_latestVersion)

Add the latest aws-v4-signer-java Gradle dependency to your project

```gradle
repository {
    maven {
        url 'https://novoda.bintray.com/snapshots'
    }
} 

dependencies {
    implementation 'com.novoda:aws-v4-signer-java:1.3-java7'
}
```

## Usage

### S3

```java
public class Example {
String contentSha256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
HttpRequest request = new HttpRequest("GET", new URI("https://examplebucket.s3.amazonaws.com?max-keys=2&prefix=J"));
String signature = Signer.builder()
        .awsCredentials(new AwsCredentials(ACCESS_KEY, SECRET_KEY))
        .header("Host", "examplebucket.s3.amazonaws.com")
        .header("x-amz-date", "20130524T000000Z")
        .header("x-amz-content-sha256", contentSha256)
        .buildS3(request, contentSha256)
        .getSignature();
}
```

### Glacier

```java
public class Example {
String contentSha256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
HttpRequest request = new HttpRequest("PUT", new URI("https://glacier.us-east-1.amazonaws.com/-/vaults/examplevault"));
String signature = Signer.builder()
        .awsCredentials(new AwsCredentials(ACCESS_KEY, SECRET_KEY))
        .header("Host", "glacier.us-east-1.amazonaws.com")
        .header("x-amz-date", "20120525T002453Z")
        .header("x-amz-glacier-version", "2012-06-01")
        .buildGlacier(request, contentSha256)
        .getSignature();
}
```
