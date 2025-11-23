package com.screenshare.service;

import com.screenshare.entity.ChatInvite;
import com.screenshare.entity.ChatMessage;
import com.screenshare.entity.ChatRoom;
import com.screenshare.entity.User;
import com.screenshare.repository.ChatInviteRepository;
import com.screenshare.repository.ChatMessageRepository;
import com.screenshare.repository.ChatRoomRepository;
import com.screenshare.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {"spring.jpa.hibernate.ddl-auto=create-drop"})
@ActiveProfiles("dev")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
public class ChatServiceIntegrationTest {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatInviteRepository chatInviteRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    private User alice;
    private User bob;

    @BeforeEach
    public void setup() {
        // With create-drop DDL the schema is created for each test context; no need to delete existing rows.
        alice = new User("alice", "alice@example.com", "password");
        bob = new User("bob", "bob@example.com", "password");
        userRepository.save(alice);
        userRepository.save(bob);
    }

    @Test
    public void createPublicGroupRoom_isVisibleAndJoinable() {
        ChatRoom room = chatService.createGroupChat(alice.getId(), "Public Room", "A public room", false);
        assertNotNull(room.getId());
        assertEquals(com.screenshare.entity.RoomType.PUBLIC, room.getRoomType());
        assertTrue(room.getIsActive());
        // creator should be member and admin
        assertTrue(room.isMember(alice));

        // Bob can join
        ChatRoom joined = chatService.joinPublicRoom(room.getId(), bob.getId());
        assertTrue(joined.isMember(bob));
    }

    @Test
    public void createPrivateGroupRoom_descriptionImmutableAfterSave() {
        ChatRoom room = chatService.createGroupChat(alice.getId(), "Private Room", "initial desc", true);
        assertEquals(com.screenshare.entity.RoomType.PRIVATE, room.getRoomType());

        // attempt to change description after persisted should throw
        ChatRoom loaded = chatRoomRepository.findById(room.getId()).orElseThrow();
        assertThrows(IllegalStateException.class, () -> loaded.setDescription("new desc"));
    }

    @Test
    public void dmInvite_flow_acceptActivatesRoomAndAddsMembers() {
        ChatInvite invite = chatService.createChatInvite(alice.getId(), bob.getId(), "Hi Bob");
        assertNotNull(invite.getId());
        assertEquals(com.screenshare.entity.InviteStatus.PENDING, invite.getStatus());

        ChatRoom dmRoom = invite.getChatRoom();
        assertNotNull(dmRoom);
        // DM starts inactive until accepted
        assertFalse(dmRoom.getIsActive());

        // Bob accepts
        ChatRoom activated = chatService.acceptInvite(invite.getId(), bob.getId());
        assertTrue(activated.getIsActive());
        assertTrue(activated.isMember(alice));
        assertTrue(activated.isMember(bob));
    }

    @Test
    public void ownerOnly_inviteToPrivateRoom_enforced() {
        ChatRoom privateRoom = chatService.createGroupChat(alice.getId(), "Private", "p", true);
        // Bob (not owner) tries to invite
        RuntimeException ex = assertThrows(RuntimeException.class, () -> chatService.createChatInviteForRoom(bob.getId(), privateRoom.getId(), alice.getId()));
        assertTrue(ex.getMessage().toLowerCase().contains("only the room owner"));
    }

    @Test
    public void saveMessage_permissionsAndRetrieval() {
        ChatRoom publicRoom = chatService.createGroupChat(alice.getId(), "Public", "desc", false);
        ChatMessage msg = chatService.saveTextMessage(publicRoom.getId(), bob.getId(), "Hello public");
        assertNotNull(msg.getId());

        List<ChatMessage> messages = chatService.getRoomMessages(publicRoom.getId(), bob.getId());
        assertEquals(1, messages.size());

        ChatRoom privateRoom = chatService.createGroupChat(alice.getId(), "Private2", "desc2", true);
        // Bob is not a member, should not be able to send
        assertThrows(RuntimeException.class, () -> chatService.saveTextMessage(privateRoom.getId(), bob.getId(), "can't send"));
    }
}
