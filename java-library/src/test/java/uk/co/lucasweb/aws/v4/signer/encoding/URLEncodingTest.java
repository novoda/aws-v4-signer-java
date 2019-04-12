package uk.co.lucasweb.aws.v4.signer.encoding;

import com.novoda.aws.v4.signer.encoding.URLEncodingKt;

import java.net.URI;

import org.junit.Test;

import uk.co.lucasweb.aws.v4.signer.HttpRequest;

import static org.assertj.core.api.Assertions.assertThat;

public class URLEncodingTest {

    @Test
    public void shouldNormalizePathWithStartingSlash() throws Exception {
        HttpRequest request = new HttpRequest("PUT", new URI("https://s3.us-east-1.amazonaws.com/my-object//example//photo.user"));
        String encodePath = URLEncoding.encodePath(request.getPath());

        assertThat(encodePath).isEqualTo("/my-object//example//photo.user");
    }

    @Test
    public void shouldNormalizePathWithStartingSlashUsingKotlin() throws Exception {
        HttpRequest request = new HttpRequest("PUT", new URI("https://s3.us-east-1.amazonaws.com/my-object//example//photo.user"));
        String encodePath = URLEncodingKt.encodePath(request.getPath());

        assertThat(encodePath).isEqualTo("/my-object//example//photo.user");
    }
}
