package sprint.server.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sprint.server.controller.datatransferobject.request.*;
import sprint.server.controller.datatransferobject.response.*;
import sprint.server.domain.member.Member;
import sprint.server.service.MemberService;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberApiController {
    private final MemberService memberService;

    @ApiOperation(value="회원가입", notes="아직 소셜미디어 로그인 미구현")
    @ApiResponses({
            @ApiResponse(code = 200, message = "정상 작동"),
            @ApiResponse(code = 400, message = "요청 에러"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @PostMapping("/api/members")
    public CreateMemberResponse saveMember(@RequestBody @Valid CreateMemberRequest request){
        Member member = new Member(request.getNickname(), request.getGender(), request.getEmail(), request.getBirthDay(), request.getHeight(), request.getWeight(), request.getPicture());
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @ApiOperation(value="회원 목록 검색", notes="닉네임 기준, LIKE 연산")
    @ApiResponses({
            @ApiResponse(code = 200, message = "정상 작동"),
            @ApiResponse(code = 400, message = "요청 에러"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @GetMapping("/api/members/list")
    public LoadMembersResponseDto<LoadMembersResponseVo> loadMembersByNickname(@RequestBody @Valid LoadMembersByNicknameRequest request){
        List<Member> members = memberService.findByNicknameContaining(request.getNickname());
        List<LoadMembersResponseVo> result = members.stream()
                .map(LoadMembersResponseVo::new)
                .sorted(LoadMembersResponseVo.COMPARE_BY_NICKNAME)
                .collect(Collectors.toList());

        return new LoadMembersResponseDto(result.size(), result);
    }

    @ApiOperation(value="회원 비활성화", notes="삭제는 아직 미구현")
    @ApiResponses({
            @ApiResponse(code = 200, message = "정상 작동"),
            @ApiResponse(code = 400, message = "요청 에러"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @PutMapping("/api/members/disable")
    public BooleanResponse disableMember(@RequestBody @Valid OneMemberRequest request) {
        return new BooleanResponse(memberService.disableMember(request.getUserId()));
    }

    @ApiOperation(value="회원 활성화")
    @ApiResponses({
            @ApiResponse(code = 200, message = "정상 작동"),
            @ApiResponse(code = 400, message = "요청 에러"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @PutMapping("/api/members/enable")
    public BooleanResponse enableMember(@RequestBody @Valid OneMemberRequest request) {
        return new BooleanResponse(memberService.enableMember(request.getUserId()));
    }

    @ApiOperation(value="회원 정보 변경", notes="해당 요청 값으로 정보를 바꾸기 때문에 변경되지 않은 사항이라도 모든 요소가 필요")
    @ApiResponses({
            @ApiResponse(code = 200, message = "정상 작동"),
            @ApiResponse(code = 400, message = "요청 에러"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @PutMapping("/api/members/modify")
    public BooleanResponse modifyMembers(@RequestBody @Valid ModifyMembersRequest request) {
        return new BooleanResponse(memberService.modifyMembers(request));
    }

    @ApiOperation(value="중복 닉네임 확인")
    @ApiResponses({
            @ApiResponse(code = 200, message = "정상 작동"),
            @ApiResponse(code = 400, message = "요청 에러"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @GetMapping ("/api/members/validation_duplicate_name")
    public BooleanResponse validationDuplicateNickname(@RequestBody @Valid ValidationDuplicateNicknameRequest request) {
        return new BooleanResponse(!memberService.existsByNickname(request.getNickname()));
    }

    @ApiOperation(value="중복 이메일 확인")
    @ApiResponses({
            @ApiResponse(code = 200, message = "정상 작동"),
            @ApiResponse(code = 400, message = "요청 에러"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @GetMapping ("/api/members/validation_duplicate_email")
    public BooleanResponse validationDuplicateEmail(@RequestBody @Valid ValidationDuplicateEmailRequest request) {
        return new BooleanResponse(!memberService.existsByEmail(request.getEmail()));
    }
}