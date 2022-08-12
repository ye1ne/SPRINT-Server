package sprint.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sprint.server.controller.datatransferobject.request.ModifyMembersRequest;
import sprint.server.controller.exception.ApiException;
import sprint.server.controller.exception.ExceptionEnum;
import sprint.server.domain.member.Member;
import sprint.server.repository.MemberRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional // readOnly = false
    public Long join(Member member){
        if (IsExistsByNickname(member.getNickname())){
            throw new ApiException(ExceptionEnum.MEMBER_DUPLICATE_NICKNAME);
        } else if (IsExistsByEmail(member.getEmail())){
            throw new ApiException(ExceptionEnum.MEMBER_DUPLICATE_EMAIL);
        } else {
            memberRepository.save(member);
            return member.getId();
        }
    }

    @Transactional
    public Boolean ModifyMembers(ModifyMembersRequest request) {
        Optional<Member> member = memberRepository.findById(request.getId());
        if (member.isEmpty()) {
            throw new ApiException(ExceptionEnum.MEMBER_NOT_FOUND);
        }
        member.get().setNickname(request.getNickname());
        member.get().setEmail(request.getEmail());
        member.get().setGender(request.getGender());
        member.get().setBirthDay(request.getBirthDay());
        member.get().setHeight(request.getHeight());
        member.get().setWeight(request.getWeight());
        member.get().setPicture(request.getPicture());
        return true;
    }

    public Member findById(Long id){
        return memberRepository.findById(id).get();
    }
    public Boolean isMemberExistById(Long sourceMemberId) {
        return memberRepository.existsById(sourceMemberId);
    }
    public List<Member> findByNicknameContaining(String nickname) {
        return memberRepository.findByNicknameContaining(nickname);
    }
    public Boolean IsExistsByNickname(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }
    public boolean IsExistsByEmail(String email) {
        return memberRepository.existsByEmail(email);
    }
}
