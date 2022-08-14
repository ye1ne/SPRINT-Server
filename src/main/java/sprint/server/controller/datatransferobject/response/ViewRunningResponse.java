package sprint.server.controller.datatransferobject.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ViewRunningResponse {
    private Long runningId;
    private double distance;
    private double duration;
    private double energy;
    private List<RunningRawDataVO> runningData;
}
