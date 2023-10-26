package com.fgama.pillowtalk.service;

import com.fgama.pillowtalk.domain.Member;
import com.fgama.pillowtalk.fcm.FirebaseCloudMessageService;
import com.fgama.pillowtalk.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {"spring.config.location=classpath:application-test.yml"})
public class FCMServiceTest {
    @Autowired
    FirebaseCloudMessageService service;

    @Test
    public void TestFcm() {
        String message = service.getFcmJsonObject("title","type","message");
        String fcmToken = "ckdtz3_rY0MAsqx-Nh3VSA:APA91bHYKubstDncsnelwmL-CWw6sLUD6VqIbA4955S3926mADG1ONahW4aHR0dyHN9zlrZchiHVDhUv485HuWoHX2KUVZGIcRHYvoi0YXslkdoTLFN38S2ltzjj0W1JbRZ04L-x-l71";

        String result = service.sendFcmMessage(message, fcmToken);

        System.out.println("FCM send result: " + result);

    }
}
