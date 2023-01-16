package sprint.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sprint.server.controller.datatransferobject.request.FinishRunningRequest;
import sprint.server.domain.friend.FriendState;
import sprint.server.domain.member.Member;
import sprint.server.domain.Running;
import sprint.server.domain.RunningRawData;
import sprint.server.repository.FriendRepository;
import sprint.server.repository.MemberRepository;
import sprint.server.repository.RunningRepository;

import javax.xml.crypto.Data;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RunningService {

    private final MemberRepository memberRepository;
    private final RunningRepository runningRepository;


    public Optional<Running> findOne(Long runningId){
        return runningRepository.findById(runningId);
    }
    @Transactional
    public Long addRun(Member member, String startTime) {
        SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        Date data = null;
        try {
           data = simpleFormat.parse(new StringTokenizer(startTime,"Z").nextToken());

        } catch (ParseException e){
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(data);
        calendar.add(Calendar.HOUR_OF_DAY,9);

        Running running = createRunning(member);

        running.setStartTime(new Timestamp(calendar.getTimeInMillis()));
        runningRepository.save(running);

        return running.getId();

    }


    @Transactional
    public Running finishRunning(FinishRunningRequest request)  {

        Running running = runningRepository.findById(request.getRunningId()).get();
        Member member = memberRepository.findById(request.getUserId()).get();

        double distance = calculateTotalDistance(request.getRunningData());
        float weight = member.getWeight();
        double energy = calculateEnergy(weight, request.getDuration(), request.getDistance());

        running.setEnergy(energy);
        running.setWeight(weight);
        running.setDuration(request.getDuration());
        running.setDistance(request.getDistance());
        running.setRunningRawDataList(request.getRunningData());

        for(RunningRawData data : request.getRunningData()){
            data.setRunning(running);
        }
        return running;
    }

    public Running createRunning(Member member){
        Running running = new Running();
        running.setMember(member);
        return running;
    }


    /**
     * 개인 러닝 기록을 가져오는 메소드
     * @param pageNumber
     * @param loginMember
     * @return
     */
    public Page<Running> fetchPersonalRunningPages(Integer pageNumber, Member loginMember){
        List<Member> allMembers = new ArrayList<>(Arrays.asList(loginMember));
        return fetchRunningPages(pageNumber,allMembers,3);
    }


    /**
     * 친구 및 회원의 러닝 기록을 가져오는 메소드
     * @param pageNumber
     * @param allMembers
     * @return
     */
    public Page<Running> fetchPublicRunningPages(Integer pageNumber, List<Member> allMembers){
        return fetchRunningPages(pageNumber,allMembers,3);
    }

    public Page<Running> fetchRunningPages(Integer pageNumber, List<Member> allMembers,int size) {
        PageRequest pageRequest = PageRequest.of(pageNumber,size);
        return runningRepository.findByMemberInOrderByIdDesc(allMembers, pageRequest);
    }

    /**
     *
     * @param rowData 경도, 위도, 고도, 시간 등의 데이터가 저장되어있음
     * @return
     */
    private double calculateTotalDistance(List<RunningRawData> rowData) {
        double distance = 0;
        for(int i = 0; i< rowData.size()-1; i++){
            /**
             * 시간 차이가 1500ms 이상이면 멈췄던 상태이므로 거리에 카운트 하지 않음
             */
            Timestamp t1 = Timestamp.valueOf(new StringTokenizer(rowData.get(i).getTimestamp(),"Z").nextToken());
            Timestamp t2 = Timestamp.valueOf(new StringTokenizer(rowData.get(i+1).getTimestamp(),"Z").nextToken());
            if(t2.getTime()-t1.getTime() > 1500){ continue; }

            distance += calculateDistance(rowData.get(i).getLongitude(),
                    rowData.get(i + 1).getLongitude(),
                    rowData.get(i).getLatitude(),
                    rowData.get(i + 1).getLatitude());

        }
        return distance;
    }

    /**
     *
     * @param weight 몸무게
     * @param duration 총 달린시간(초단위)
     * @param distance 총 달린거리(미터 단위)
     * @return 소모 칼로리 반환
     */
    public static double calculateEnergy(double weight, double duration, double distance){
        double coefficient;
        double speed =  distance /( 1000 * secondToHour(duration));
        /**칼로리 계산 매커니즘**/
        if(speed < 5){
            coefficient = 0.9;
        }else if(speed < 6.4){
            coefficient = 1.2;
        }else{
            coefficient = 2;
        }

        return coefficient * weight * secondToHour(duration)*6; //10분단위로 곱해진다 예를들어 0.2 시간 = 12분 = 1.2가 곱해져야함
    }

    /**
     *
     * @param duration 달린 시간 (초단위)
     * @return 초단위의 달린시간을 시간단위로 반환
     */
    private static double secondToHour(double duration){
        return (duration/3600.0);
    }

    /**
     *
     * @param startLongitude 출발지 경도
     * @param endLongitude 도착지 경도
     * @param startLatitude 출발지 위도
     * @param endLatitude 도착지 위도
     * @return 전체 뛴 거리 (m단위)
     */
    public static double calculateDistance(double startLongitude, double endLongitude, double startLatitude, double endLatitude){
        double earthRadius = 6378137.0;
        double dLat = degreeToRadians(endLatitude-startLatitude);
        double dLon = degreeToRadians(endLongitude - startLongitude);
        double a = Math.pow(Math.sin(dLat/2),2)+
                Math.pow(Math.sin(dLon/2),2) *
                        Math.cos(degreeToRadians(startLatitude))*
                        Math.cos(degreeToRadians(endLatitude));
        double c = 2*Math.asin(Math.sqrt(a));

        return earthRadius*c; //단위 meter
    }

    /**
     *
     * @param degree 일반 각도 입력
     * @return 각도를 라디안으로 변환
     */
    private static double degreeToRadians(double degree){
        return (degree * Math.PI/180.0);
    }

}
