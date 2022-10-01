package sprint.server.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import sprint.server.controller.datatransferobject.request.ModifyMembersRequest;
import sprint.server.controller.exception.ApiException;
import sprint.server.domain.member.Gender;
import sprint.server.domain.member.Member;
import sprint.server.repository.MemberRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberServiceTest {
    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;

    /**
     * 회원가입 테스트
     */
    @Test
    void memberJoinTest(){
        String testName = "TestName";
        String testName2 = "TestName2";
        /* 정상적인 요청 */
        Member member = new Member(testName, Gender.FEMALE, testName + "@sprint.com", LocalDate.of(2011, 2, 28), 180.0f, 70f, null);
        Long saveId = memberService.join(member);
        Member foundMember1 = memberService.findById(saveId);
        assertEquals(testName, foundMember1.getNickname());
        assertEquals(testName+"@sprint.com", foundMember1.getEmail());
        assertEquals(LocalDate.of(2011, 2, 28), foundMember1.getBirthday());
        assertEquals(180.0F, foundMember1.getHeight());
        assertEquals(70F,foundMember1.getWeight());

        /* 동일 닉네임이 이미 존재하는 경우 */
        Member member2 = new Member(testName, Gender.FEMALE, testName + "@sprint.com", LocalDate.of(2011, 2, 28), 180.0f, 70f, null);
        ApiException thrown = assertThrows(ApiException.class, () -> memberService.join(member2));
        assertEquals("M0002", thrown.getErrorCode());

        /* 동일 이메일이 존재하는 경우 */
        Member member3 = new Member(testName2, Gender.FEMALE, testName + "@sprint.com", LocalDate.of(2011, 2, 28), 180.0f, 70f, null);
        ApiException thrown2 = assertThrows(ApiException.class, () -> memberService.join(member3));
        assertEquals("M0003", thrown2.getErrorCode());
    }

    /**
     * 회원 정보 수정 테스트
     */
    @Test
    void modifyMembersTest(){
        Member member = memberService.findById(1L);
        /* 정상적인 요청 */
        ModifyMembersRequest modifyMembersRequest = new ModifyMembersRequest();
        modifyMembersRequest.setNickname("Modify1");
        modifyMembersRequest.setEmail("Modify@test.com");
        modifyMembersRequest.setGender(Gender.MALE);
        modifyMembersRequest.setBirthday(LocalDate.of(2022, 3, 11));
        modifyMembersRequest.setHeight(166.7F);
        modifyMembersRequest.setWeight(70F);
        modifyMembersRequest.setPicture("modify@mtest.com");
        Boolean result = memberService.modifyMembers(member, modifyMembersRequest);
        assertEquals(true, result);
        assertEquals(1L, member.getId());
        assertEquals("Modify1", member.getNickname());
        assertEquals("Modify@test.com", member.getEmail());
        assertEquals(Gender.MALE, member.getGender());
        assertEquals(LocalDate.of(2022, 3, 11), member.getBirthday());
        assertEquals(166.7F, member.getHeight());
        assertEquals(70F, member.getWeight());
        assertEquals("modify@mtest.com", member.getPicture());
    }

    /**
     * 회원 정보 이름으로 검색 (LIKE) 테스트
     */
    @Test
    void findByNicknameContainingTest(){
        /* 정상적인 요청 */
        List<Member> members = memberService.findByNicknameContaining("Test1");
        assertEquals(1, members.size());
        List<Member> members2 = memberService.findByNicknameContaining("Test");
        assertEquals(5, members2.size());
    }

    /**
     * 회원 정보 비활성화 테스트
     */
    @Test
    void disableMemberTest(){
        Member member = memberService.findById(1L);
        /* 정상적인 요청 */
        Boolean result = memberService.disableMember(member);
        assertEquals(true, result);
        ApiException thrown = assertThrows(ApiException.class, () -> memberService.findById(1L));
        assertEquals("M0001",thrown.getErrorCode());
        assertNull(memberService.findById(2L).getDisableDay());
    }
}