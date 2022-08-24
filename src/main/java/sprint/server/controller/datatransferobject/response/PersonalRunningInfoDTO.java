package sprint.server.controller.datatransferobject.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class PersonalRunningInfoDTO {

    private Long runningId;
    private double duration;
    private double distance;
    private String startTime;
    private double energy;
    @Builder

    public PersonalRunningInfoDTO(Long runningId, double duration, double distance, String startTime, double energy) {
        this.runningId = runningId;
        this.duration = duration;
        this.distance = distance;
        this.startTime = startTime;
        this.energy = energy;
    }
}
