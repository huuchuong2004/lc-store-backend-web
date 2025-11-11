package vn.huuchuong.lcstorebackendweb.service;

import vn.huuchuong.lcstorebackendweb.base.BaseResponse;

public interface IMailSenderService {
    BaseResponse<String> sendMessageWithAttachment(String to, String subject, String text);

    BaseResponse<String> sendActivationEmail(String to, String activationLink);
}
