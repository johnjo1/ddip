package org.threefour.ddip.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.threefour.ddip.chat.domain.Chat;
import org.threefour.ddip.chat.domain.dto.ChatResponseDTO;
import org.threefour.ddip.chat.domain.dto.ChatroomResponseDTO;
import org.threefour.ddip.chat.domain.dto.ProductResponseDTO;
import org.threefour.ddip.chat.repository.ChatRepository;
import org.threefour.ddip.chat.service.ChatService;
import org.threefour.ddip.member.domain.Member;
import org.threefour.ddip.member.jwt.JWTUtil;
import org.threefour.ddip.product.service.ProductService;
import org.threefour.ddip.product.service.ProductServiceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class APIController {

  private final ChatService chatService;
  private final JWTUtil jwtUtil;

  @GetMapping("/{member}/products")
  public ResponseEntity<List<ProductResponseDTO>> getAllProductByMemberId(@PathVariable("member") String email, @RequestHeader("Authorization") String token) {
    String accessToken = token.substring(7);
    Long id = jwtUtil.getId(accessToken);
    List<ProductResponseDTO> list = chatService.getAllProductBySellerId(id);
    return new ResponseEntity<>(list, HttpStatus.OK);
  }

  @GetMapping("/{member}/chatrooms")
  public ResponseEntity<List<ChatroomResponseDTO>> getAllChatroom(@PathVariable("member") String email, @RequestHeader("Authorization") String token) {
    String accessToken = token.substring(7);
    Long id = jwtUtil.getId(accessToken);
    Map<Long, ChatroomResponseDTO> chatMap = new HashMap<>();

    List<ProductResponseDTO> allProductBySellerId = chatService.getAllProductBySellerId(id);
    for (ProductResponseDTO product : allProductBySellerId) {
      ChatroomResponseDTO chatByProductId = chatService.findChatByProductId(product.getProductId());
      if (chatByProductId != null) {
        updateChatMap(chatMap, chatByProductId);
      }
    }

    List<ChatroomResponseDTO> chatByOwnerId = chatService.findAllChatByOwnerId(id);
    if (chatByOwnerId != null) {
      for (ChatroomResponseDTO chat : chatByOwnerId) {
        updateChatMap(chatMap, chat);
      }
    }

    List<ChatroomResponseDTO> result = new ArrayList<>(chatMap.values());
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @GetMapping("/{member}/chatrooms/{chatroomId}")
  public ResponseEntity<List<ChatResponseDTO>> getChatroomByProductId(@PathVariable("member") String email,
                                                                      @PathVariable("chatroomId") Long chatroomId,
                                                                      @RequestHeader("Authorization") String token) {
    String accessToken = token.substring(7);
    Long id = jwtUtil.getId(accessToken);

    List<ChatResponseDTO> allChatByProductId = chatService.findAllChatByProductId(chatroomId);
    List<ChatResponseDTO> list = new ArrayList<>();
    for (ChatResponseDTO chat : allChatByProductId) {
      if (chat.getSender().getId() != id) chat.setType("left");
      else chat.setType("right");

      list.add(chat);
    }

    return new ResponseEntity<>(list, HttpStatus.OK);
  }

  @GetMapping("/{productId}/unread-count")
  public ResponseEntity<Integer> getUnreadCount(@PathVariable Long productId, @RequestHeader("Authorization") String token) {
    String accessToken = token.substring(7);
    Long id = jwtUtil.getId(accessToken);

    int count = chatService.getUnreadMessageCount(productId, id);
    return ResponseEntity.ok(count);
  }

  @PostMapping("/{productId}/mark-read")
  public ResponseEntity<Void> markAsRead(@PathVariable Long productId, @RequestHeader("Authorization") String token) {
    String accessToken = token.substring(7);
    Long id = jwtUtil.getId(accessToken);
    chatService.markAsRead(productId, id);
    return ResponseEntity.ok().build();
  }

  private void updateChatMap(Map<Long, ChatroomResponseDTO> chatMap, ChatroomResponseDTO newChat) {
    Long productId = newChat.getProductId();
    if (!chatMap.containsKey(productId) || newChat.getSendDate().after(chatMap.get(productId).getSendDate())) {
      chatMap.put(productId, newChat);
    }
  }


}
