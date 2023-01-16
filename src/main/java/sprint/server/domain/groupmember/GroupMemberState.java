package sprint.server.domain.groupmember;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum GroupMemberState {
    REQUEST, ACCEPT, LEAVE, LEADER, REJECT, CANCEL, MEMBER, NOT_MEMBER;
    // MEMBER & NOT_MEMBER is only for api response

    @JsonCreator
    public static GroupMemberState fromGroupMemberState(String input) {
        for (GroupMemberState groupMemberState : GroupMemberState.values()) {
            if(groupMemberState.name().equals(input)) {
                return groupMemberState;
            }
        }
        return null;
    }
}
