package sprint.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sprint.server.domain.Member;
import sprint.server.domain.friends.FriendState;
import sprint.server.domain.friends.Friends;
import sprint.server.repository.FriendsRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendsService {
    private final FriendsRepository friendsRepository;
    private final MemberService memberService;

    /**
     * 친구 요청
     * @param sourceMemberId
     * @param targetMemberId
     * @return friends
     */
    @Transactional
    public Friends FriendsRequest(Long sourceMemberId, Long targetMemberId) {
        validationFriendsRequest(sourceMemberId, targetMemberId);
        Friends friends = Friends.createFriendsRelationship(sourceMemberId, targetMemberId);
        friends.setRegisteredDate(Timestamp.valueOf(LocalDateTime.now()));
        friendsRepository.save(friends);
        return friends;
    }


    /**
     * 친구 요청 거절
     * @param sourceMemberId, targetMemberId
     * @return 결과(true/false)
     */
    @Transactional
    public Boolean RejectFriendsRequest(Long sourceMemberId, Long targetMemberId){
        boolean isExists = isFriendsRequestExist(sourceMemberId, targetMemberId, FriendState.REQUEST);
        if (!isExists) {
            throw new IllegalStateException("해당 친구 요청이 존재하지 않습니다.");
        }
        Friends friends = findFriendsRequest(sourceMemberId, targetMemberId, FriendState.REQUEST).get();
        setFriendsStateAndTime(friends, FriendState.REJECT);
        if (friendsRepository.existsBySourceMemberIdAndTargetMemberIdAndEstablishState(sourceMemberId, targetMemberId, FriendState.REJECT)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 친구 요청 수락
     * @param sourceMemberId, targetMemberId
     * @return 결과(true/false)
     */
    @Transactional
    public Boolean AcceptFriendsRequest(Long sourceMemberId, Long targetMemberId){
        boolean isExists = isFriendsRequestExist(sourceMemberId, targetMemberId, FriendState.REQUEST);
        if (!isExists) {
            throw new IllegalStateException("해당 친구 요청이 존재하지 않습니다.");
        }
        boolean isExists2 = isFriendsRequestExist(sourceMemberId, targetMemberId, FriendState.ACCEPT);
        if (isExists2) {
            throw new IllegalStateException("이미 친구입니다.");
        }
        Friends friends = findFriendsRequest(sourceMemberId, targetMemberId, FriendState.REQUEST).get();
        setFriendsStateAndTime(friends, FriendState.ACCEPT);
        Friends newFriends = Friends.createFriendsRelationship(targetMemberId, sourceMemberId);
        setFriendsStateAndTime(newFriends, FriendState.ACCEPT);

        friendsRepository.save(newFriends);
        if (isFriendsRequestExist(sourceMemberId, targetMemberId, FriendState.ACCEPT) &&
                isFriendsRequestExist(targetMemberId, sourceMemberId, FriendState.ACCEPT)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 친구 제거
     * @param sourceMemberId
     * @param targetMemberId
     * @return
     */
    @Transactional
    public Boolean DeleteFriends(Long sourceMemberId, Long targetMemberId) {
        Optional<Friends> sourceFriends = findFriendsRequest(sourceMemberId, targetMemberId, FriendState.ACCEPT);
        Optional<Friends> targetFriends = findFriendsRequest(targetMemberId, sourceMemberId, FriendState.ACCEPT);
        if(!sourceFriends.isPresent() || !targetFriends.isPresent()) {
            throw new IllegalStateException("잘못된 요청입니다. : 친구가 아닙니다.");
        }
        setFriendsStateAndTime(sourceFriends.get(), FriendState.REJECT);
        setFriendsStateAndTime(targetFriends.get(), FriendState.REJECT);

        if (isFriendsRequestExist(sourceMemberId, targetMemberId, FriendState.REJECT) &&
                isFriendsRequestExist(targetMemberId, sourceMemberId, FriendState.REJECT)){
            return true;
        } else {
            return false;
        }
    }


    /**
     * 친구 요청 Validation
     * @param sourceMemberId
     * @param targetMemberId
     */
    private void validationFriendsRequest(Long sourceMemberId, Long targetMemberId){
        memberService.isMemberExistById(sourceMemberId, "sourceMember가 database에 존재하지 않습니다.");
        memberService.isMemberExistById(targetMemberId, "targetMember가 database에 존재하지 않습니다.");
        if (isFriendsRequestExist(sourceMemberId, targetMemberId, FriendState.REQUEST)){
            throw new IllegalStateException("이미 전송된 요청입니다.");
        } else if (isFriendsRequestExist(sourceMemberId, targetMemberId, FriendState.ACCEPT)) {
            throw new IllegalStateException("이미 친구입니다.");
        }
    }

    @Transactional
    public Boolean CancelFriends(Long sourceMemberId, Long targetMemberId) {
        Optional<Friends> sourceFriends = findFriendsRequest(sourceMemberId, targetMemberId, FriendState.REQUEST);
        if (!sourceFriends.isPresent()) {
            throw new IllegalStateException("친구요청을 보낸적이 없습니다.");
        }
        setFriendsStateAndTime(sourceFriends.get(), FriendState.CANCELED);

        if (isFriendsRequestExist(sourceMemberId, targetMemberId, FriendState.CANCELED)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 친구 요청 응답 전 Validation
     * @param sourceMemberId
     * @param targetMemberId
     */
    private boolean isFriendsRequestExist(Long sourceMemberId, Long targetMemberId, FriendState friendState) {
        Boolean isExists = friendsRepository.existsBySourceMemberIdAndTargetMemberIdAndEstablishState(sourceMemberId, targetMemberId, friendState);
        return isExists;
    }

    private Optional<Friends> findFriendsRequest(Long sourceMemberId, Long targetMemberId, FriendState friendState){
        return friendsRepository.findBySourceMemberIdAndTargetMemberIdAndEstablishState(sourceMemberId, targetMemberId, friendState);
    }

    private void setFriendsStateAndTime(Friends friends, FriendState friendState) {
        friends.setRegisteredDate(Timestamp.valueOf(LocalDateTime.now()));
        friends.setEstablishState(friendState);
    }

    public List<Member> LoadFriendsBySourceMember(Long memberId, FriendState friendState) {
        List<Friends> friendsList = friendsRepository.findBySourceMemberIdAndEstablishState(memberId, friendState);
        List<Member> result = friendsList.stream().map(friends -> memberService.findById(friends.getTargetMemberId())).collect(Collectors.toList());
        return result;
    }

    public List<Member> LoadFriendsByTargetMember(Long memberId, FriendState friendState) {
        List<Friends> friendsList = friendsRepository.findByTargetMemberIdAndEstablishState(memberId, friendState);
        List<Member> result = friendsList.stream().map(friends -> memberService.findById(friends.getSourceMemberId())).collect(Collectors.toList());
        return result;
    }
}
