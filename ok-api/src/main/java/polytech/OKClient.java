package polytech;

import polis.ok.api.domain.Attachment;
import polis.ok.api.exceptions.OkApiException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public interface OKClient {

    long postMediaTopic(String accessToken, long groupId, Attachment attachment)
            throws URISyntaxException, IOException, OkApiException;

    long uploadVideo(String accessToken, long groupId, File video)
            throws URISyntaxException, IOException, OkApiException;

    List<String> uploadPhotos(String accessToken, long groupId, List<File> photos)
            throws URISyntaxException, IOException, OkApiException;

    String getShortLink(String accessToken, String link) throws IOException, URISyntaxException, OkApiException;
}
