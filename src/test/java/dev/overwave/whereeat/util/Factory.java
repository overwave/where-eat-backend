package dev.overwave.whereeat.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static it.tdlight.jni.TdApi.*;

@UtilityClass
public class Factory {
    private final static ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

    public Messages tdMessages(long... ids) {
        List<Message> messagesList = messages(ids);
        return new Messages(messagesList.size(), messagesList.toArray(Message[]::new));
    }

    public List<Message> messages(long... ids) {
        return Arrays.stream(ids)
                .mapToObj(Factory::message)
                .toList();
    }

    public Message message(long id) {
        Message message = new Message();
        message.id = id;
        MessageText messageText = new MessageText();
        messageText.text = new FormattedText();
        messageText.text.text = DigestUtils.md5Hex("text" + id);
        message.content = messageText;
        return message;
    }

    public List<Message> videoMessages(long... ids) {
        return Arrays.stream(ids)
                .mapToObj(Factory::videoMessage)
                .toList();
    }

    public Message videoMessage(long id) {
        Message message = new Message();
        message.id = id;
        MessageVideo messageVideo = new MessageVideo();
        messageVideo.caption = new FormattedText();
        messageVideo.caption.text = DigestUtils.md5Hex("video" + id);

        messageVideo.video = new Video();
        messageVideo.video.video = new File();
        messageVideo.video.video.size = RANDOM.nextInt(100, 5000);
        messageVideo.video.video.id = 1;
        messageVideo.video.video.local = new LocalFile();
        messageVideo.video.video.local.isDownloadingCompleted = true;
        messageVideo.video.video.local.path = DigestUtils.md5Hex("video-path" + id) + "/" +
                                              DigestUtils.md5Hex("video-path" + -id) + ".MOV";
        message.content = messageVideo;
        return message;
    }
}
