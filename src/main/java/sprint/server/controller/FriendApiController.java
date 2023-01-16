package sprint.server.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import sprint.server.controller.datatransferobject.request.*;
import sprint.server.controller.datatransferobject.response.*;
import sprint.server.controller.exception.ApiException;
import sprint.server.controller.exception.ExceptionEnum;
import sprint.server.domain.member.Member;
import sprint.server.domain.friend.FriendState;
import sprint.server.service.FriendService;
import sprint.server.service.MemberService;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/user-management/friend")
public class FriendApiController {
    private final FriendService friendService;
    private final MemberService memberService;

    @ApiOperation(value="친구추가 요청", notes =
            "sourceUserId -> 친구추가 요청을 보내는 유저\ntargetUserId -> 친구추가 요청을 받는 유저")
    @ApiResponses({
            @ApiResponse(code = 200, message = "정상 작동"),
            @ApiResponse(code = 400, message = "요청 에러"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @PostMapping("")
    public BooleanResponse createFriends(@RequestBody @Valid TwoMemberRequest request) {
        log.info("친구 등록 요청");
        Member sourceMember = memberService.findById(request.getSourceUserId());
        Member targetMember = memberService.findById(request.getTargetUserId());
        return new BooleanResponse(friendService.requestFriends(sourceMember, targetMember));
    }

    @ApiOperation(value="친구 수락/거절/취소", notes =
    "수락: ACCEPT\n거절: REJECT\n취소: CANCEL")
    @ApiResponses({
            @ApiResponse(code = 200, message = "정상 작동"),
            @ApiResponse(code = 400, message = "요청 에러"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @PutMapping("")
    public BooleanResponse modifyFriendsState(@RequestBody @Valid ModifyFriendsRequest request) {
        log.info("친구 등록 요청 응답");
        Member sourceMember = memberService.findById(request.getSourceUserId());
        Member targetMember = memberService.findById(request.getTargetUserId());
        switch (request.getFriendState()) {
            case ACCEPT:
                return new BooleanResponse(friendService.acceptFriendsRequest(targetMember, sourceMember));
            case REJECT:
                return new BooleanResponse(friendService.rejectFriendsRequest(targetMember, sourceMember));
            case CANCEL:
                return new BooleanResponse(friendService.cancelFriends(sourceMember, targetMember));
            default:
                log.error("{} : 존재하지 않는 관계.", request.getFriendState());
                throw new ApiException(ExceptionEnum.FRIEND_METHOD_NOT_FOUND);
        }
    }

    @ApiOperation(value = "친구 제거")
    @DeleteMapping("")
    public BooleanResponse deleteFriends(@RequestBody @Valid TwoMemberRequest request) {
        log.info("친구 제거 요청");
        Member sourceMember = memberService.findById(request.getSourceUserId());
        Member targetMember = memberService.findById(request.getTargetUserId());
        return new BooleanResponse(friendService.deleteFriends(sourceMember, targetMember));
    }

    @ApiOperation(value="친구 목록 요청",
            notes = "Example: http://localhost:8080/api/user-management/friends/3")
    @ApiResponses({
            @ApiResponse(code = 200, message = "정상 작동"),
            @ApiResponse(code = 400, message = "요청 에러"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @GetMapping("/{userId}")
    public FindMembersResponseDto<FindFriendsResponseVo> findFriends(@PathVariable Long userId) {
        log.info("친구 목록 요청");
        Member member = memberService.findById(userId);
        List<Member> memberList = friendService.findFriendsByMemberId(member, FriendState.ACCEPT);
        List<FindFriendsResponseVo> result = memberList.stream()
                .map(FindFriendsResponseVo::new)
                .sorted(FindFriendsResponseVo.COMPARE_BY_NICKNAME)
                .collect(Collectors.toList());
        log.info("{} 회원 : 친구 목록 완료, 결과 : {}개", userId, result.size() );
        return new FindMembersResponseDto(result.size(), result);
    }

    @ApiOperation(value="사용자가 받은 친구 추가 요청 목록",
            notes = "Example: http://localhost:8080/api/user-management/friends/3/received")
    @ApiResponses({
            @ApiResponse(code = 200, message = "정상 작동"),
            @ApiResponse(code = 400, message = "요청 에러"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @GetMapping("/{userId}/received")
    public FindMembersResponseDto<FindFriendsResponseVo> findFriendsReceive(@PathVariable Long userId) {
        log.info("받은 친구 추가 요청 목록 요청");
        Member member = memberService.findById(userId);
        log.info("{} 회원 : 받은 친구 추가 요청 ({}) 목록 검색",member.getId(), FriendState.REQUEST);
        List<Member> members = friendService.findByTargetMemberIdAndFriendState(member, FriendState.REQUEST);
        List<FindFriendsResponseVo> result = members.stream()
                .map(FindFriendsResponseVo::new)
                .collect(Collectors.toList());
        log.info("{} 회원 : 받은 친구 추가 요청 목록 완료, 결과 : {}개", userId, result.size() );
        return new FindMembersResponseDto(result.size(), result);
    }

    @ApiOperation(value="사용자가 보낸 친구 추가 요청 목록",
            notes = "Example: http://localhost:8080/api/user-management/friends/3/requested")
    @ApiResponses({
            @ApiResponse(code = 200, message = "정상 작동"),
            @ApiResponse(code = 400, message = "요청 에러"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @GetMapping("/{userId}/requested")
    public FindMembersResponseDto<FindFriendsResponseVo> findFriendsRequest(@PathVariable Long userId) {
        log.info("보낸 친구 추가 요청 목록 요청");
        Member member = memberService.findById(userId);
        log.info("ID : {}, 보낸 친구 추가 요청 ({}) 목록 검색",member.getId(), FriendState.REQUEST);
        List<Member> members = friendService.findBySourceMemberIdAndFriendState(member, FriendState.REQUEST);
        List<FindFriendsResponseVo> result = members.stream()
                .map(FindFriendsResponseVo::new)
                .collect(Collectors.toList());
        log.info("ID : {}, 보낸 친구 추가 요청 목록 완료, 결과 : {}개", userId, result.size() );
        return new FindMembersResponseDto(result.size(), result);
    }
}
