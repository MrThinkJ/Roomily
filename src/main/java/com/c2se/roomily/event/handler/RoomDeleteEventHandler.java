package com.c2se.roomily.event.handler;

import com.c2se.roomily.entity.FindPartnerPost;
import com.c2se.roomily.entity.Room;
import com.c2se.roomily.enums.FindPartnerPostStatus;
import com.c2se.roomily.event.pojo.RoomDeleteEvent;
import com.c2se.roomily.payload.request.CreateNotificationRequest;
import com.c2se.roomily.repository.FindPartnerPostRepository;
import com.c2se.roomily.service.ChatRoomService;
import com.c2se.roomily.service.FindPartnerService;
import com.c2se.roomily.service.NotificationService;
import com.c2se.roomily.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoomDeleteEventHandler {
    private final FindPartnerPostRepository findPartnerPostRepository;
    private final ChatRoomService chatRoomService;
    private final NotificationService notificationService;
    private final RoomService roomService;
    @EventListener
    @Async
    @Transactional
    public void handleRoomDeleteEvent(RoomDeleteEvent event) {
        String roomId = event.getRoomId();
        Room room = roomService.getRoomEntityById(roomId);
        CreateNotificationRequest landlordNotificationRequest = CreateNotificationRequest.builder()
                .header("Phòng"+ room.getTitle() +" đã bị xóa")
                .body("Phòng "+ room.getTitle()+ " đã bị xóa")
                .userId(room.getLandlord().getId())
                .build();
        notificationService.sendNotification(landlordNotificationRequest);
        List<FindPartnerPost> findPartnerPosts = findPartnerPostRepository.findByRoomIdAndStatus(
                roomId, FindPartnerPostStatus.ACTIVE);
        findPartnerPosts.forEach(findPartnerPost -> {
            findPartnerPost.getParticipants().forEach(participant -> {
                CreateNotificationRequest createNotificationRequest = CreateNotificationRequest.builder()
                        .header("Bài đăng tìm bạn đã bị xóa do phòng đã bị xóa")
                        .body("Bài đăng tìm bạn đã bị xóa do phòng đã bị xóa")
                        .userId(participant.getId())
                        .build();
                notificationService.sendNotification(createNotificationRequest);
            });
            chatRoomService.archiveAllChatRoomsByFindPartnerPostId(findPartnerPost.getId());
            findPartnerPost.setStatus(FindPartnerPostStatus.DELETED);
            findPartnerPostRepository.save(findPartnerPost);
        });
        log.info("Room deleted: {}", event.getRoomId());
    }
}
