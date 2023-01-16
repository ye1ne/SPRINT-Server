package sprint.server.controller.datatransferobject.response;

import lombok.Data;
import sprint.server.domain.member.Member;

import java.util.Comparator;

@Data
public class FindFriendsResponseVo {
    private Long userId;
    private String nickname;
    private Float height;
    private Float weight;
    private Integer tierId;
    private String picture;

    public FindFriendsResponseVo(Member member){
        this.userId = member.getId();
        this.nickname = member.getNickname();
        this.height = member.getHeight();
        this.weight = member.getWeight();
        this.tierId = member.getTierId();
        this.picture = member.getPicture();
    }

    public static final Comparator<FindFriendsResponseVo> COMPARE_BY_NICKNAME = Comparator.comparing(o -> o.nickname);
}



