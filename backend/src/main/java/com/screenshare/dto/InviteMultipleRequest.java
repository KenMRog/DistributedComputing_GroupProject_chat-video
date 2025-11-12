package com.screenshare.dto;

import java.util.List;

public class InviteMultipleRequest {
    private List<Long> invitedUserIds;

    public InviteMultipleRequest() {}

    public InviteMultipleRequest(List<Long> invitedUserIds) {
        this.invitedUserIds = invitedUserIds;
    }

    public List<Long> getInvitedUserIds() {
        return invitedUserIds;
    }

    public void setInvitedUserIds(List<Long> invitedUserIds) {
        this.invitedUserIds = invitedUserIds;
    }
}
