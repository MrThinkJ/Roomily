package com.c2se.roomily.event.handler;

import com.c2se.roomily.enums.ErrorCode;
import com.c2se.roomily.exception.APIException;
import com.c2se.roomily.payload.response.CheckoutResponse;
import com.c2se.roomily.repository.CheckoutInfoRepository;
import com.c2se.roomily.service.PaymentProcessingService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import vn.payos.PayOS;
import vn.payos.type.PaymentLinkData;

@Component
@RequiredArgsConstructor
public class CheckoutExpirationListener implements MessageListener {
    private final PayOS payOS;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisMessageListenerContainer container;

    @PostConstruct
    public void init() {
        container.addMessageListener(this, new ChannelTopic("__keyevent@0__:expired"));
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = new String(message.getBody());
        if (expiredKey.startsWith("checkout:")) {
            String checkoutId = expiredKey.substring("checkout:".length());
            handleCheckoutExpiration(checkoutId);
        }
    }

    private void handleCheckoutExpiration(String checkoutId) {
        Object value = redisTemplate.opsForValue().get("persistent:checkout:" + checkoutId);
        if (value != null) {
            CheckoutResponse checkoutResponse = (CheckoutResponse) value;
            try{
                PaymentLinkData paymentLinkData = payOS.getPaymentLinkInformation(checkoutResponse.getOrderCode());
                if (paymentLinkData.getStatus().equals("PENDING")){
                    payOS.cancelPaymentLink(checkoutResponse.getOrderCode(),
                                            checkoutResponse.getOrderCode() + " expired" );
                }
            } catch (Exception e) {
                throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.PAYMENT_LINK_GET_FAILED,
                                       checkoutResponse.getOrderCode() + ". Error: " + e.getMessage());
            }
            redisTemplate.delete("persistent:checkout:" + checkoutId);
        }
    }
}
