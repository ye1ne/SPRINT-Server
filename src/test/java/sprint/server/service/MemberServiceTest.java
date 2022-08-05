package sprint.server.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import sprint.server.SaveTempMember;
import sprint.server.domain.Member;

import javax.annotation.PostConstruct;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class MemberServiceTest {
    @Autowired MemberService memberService;

    @Test
    public void MemberSaveTest() throws Exception {
        Member member = new Member();
        String testName = "TestName";
        member.setName(testName);
        Long saveId = memberService.join(member);
        assertEquals(testName, memberService.findById(saveId).getName());
    }

    // SaveTempMember 클래스: 시작 시 Test1, Test2 member 추가
    @Test
    public void findByIdTest() throws Exception {
        Member findByIdMember1 = memberService.findById(1L);
        Member findByIdMember2 = memberService.findById(2L);
        assertEquals(findByIdMember1.getName(), "Test1");
        assertEquals(findByIdMember2.getName(), "Test2");
    }

    @Test
    public void duplicateSaveTest() throws Exception {
        Member member = new Member();
        member.setName("Test1"); // 이미 존재함
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> memberService.join(member));
        assertEquals("이미 존재하는 회원 이름입니다.", thrown.getMessage());
    }
}