package com.c2se.roomily.service.impl;

import com.c2se.roomily.payload.request.RentalRequest;
import com.c2se.roomily.service.RequestCacheService;
import com.c2se.roomily.util.UtilFunction;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RequestCacheServiceImpl implements RequestCacheService {
    private static final int REQUEST_TTL = 30;
    private static final String REQUEST_KEY_PREFIX = "request:";
    private static final String REQUESTER_INDEX_PREFIX = "requester:";
    private static final String RECIPIENT_INDEX_PREFIX = "recipient:";
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public RentalRequest saveRequest(RentalRequest request) {
        String requestId = UtilFunction.hash(UUID.randomUUID().toString());
        request.setId(requestId);
        request.setExpiresAt(LocalDateTime.now().plusMinutes(REQUEST_TTL).toString());
        
        // Store the request
        redisTemplate.opsForValue().set(
                REQUEST_KEY_PREFIX+requestId, request, REQUEST_TTL, TimeUnit.MINUTES);
        
        // Add to requester index
        if (request.getRequesterId() != null) {
            redisTemplate.opsForSet().add(REQUESTER_INDEX_PREFIX + request.getRequesterId(), requestId);
            redisTemplate.expire(REQUESTER_INDEX_PREFIX + request.getRequesterId(), REQUEST_TTL, TimeUnit.MINUTES);
        }
        
        // Add to recipient index
        if (request.getRecipientId() != null) {
            redisTemplate.opsForSet().add(RECIPIENT_INDEX_PREFIX + request.getRecipientId(), requestId);
            redisTemplate.expire(RECIPIENT_INDEX_PREFIX + request.getRecipientId(), REQUEST_TTL, TimeUnit.MINUTES);
        }
        
        return request;
    }

    @Override
    public Optional<RentalRequest> getRequest(String requestId) {
        Object value = redisTemplate.opsForValue()
                .get(REQUEST_KEY_PREFIX + requestId);
        if (value == null) {
            return Optional.empty();
        }
        RentalRequest request = (RentalRequest) value;
        return Optional.of(request);
    }

    @Override
    public void removeRequest(String requestId) {
        // Get the request to get requester and recipient IDs
        Optional<RentalRequest> requestOpt = getRequest(requestId);
        if (requestOpt.isPresent()) {
            RentalRequest request = requestOpt.get();
            
            // Remove from indices
            if (request.getRequesterId() != null) {
                redisTemplate.opsForSet().remove(REQUESTER_INDEX_PREFIX + request.getRequesterId(), requestId);
            }
            
            if (request.getRecipientId() != null) {
                redisTemplate.opsForSet().remove(RECIPIENT_INDEX_PREFIX + request.getRecipientId(), requestId);
            }
        }
        
        // Remove the request
        redisTemplate.delete(REQUEST_KEY_PREFIX + requestId);
    }
    
    @Override
    public List<RentalRequest> getRequestsBySender(String senderId) {
        Set<Object> requestIds = redisTemplate.opsForSet().members(REQUESTER_INDEX_PREFIX + senderId);
        return getRequestsByIds(requestIds);
    }
    
    @Override
    public List<RentalRequest> getRequestsByReceiver(String receiverId) {
        Set<Object> requestIds = redisTemplate.opsForSet().members(RECIPIENT_INDEX_PREFIX + receiverId);
        return getRequestsByIds(requestIds);
    }
    
    private List<RentalRequest> getRequestsByIds(Set<Object> requestIds) {
        List<RentalRequest> requests = new ArrayList<>();
        
        if (requestIds != null && !requestIds.isEmpty()) {
            for (Object id : requestIds) {
                Optional<RentalRequest> request = getRequest((String) id);
                request.ifPresent(requests::add);
            }
        }
        
        return requests;
    }
}
    