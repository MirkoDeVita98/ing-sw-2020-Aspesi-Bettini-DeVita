package it.polimi.ingsw.common.packets;

import java.awt.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class PacketWorkersPositions implements Serializable {

    private static final long serialVersionUID = -6159631003731760627L;
    private final Map<String, Point> workersPositions;

    public PacketWorkersPositions(Map<String, Point> workersPositions) {
        assert (workersPositions != null);
        this.workersPositions = new HashMap<>();
        for(String s : workersPositions.keySet()){
            assert(s != null && workersPositions.get(s) != null);
            this.workersPositions.put(s,new Point(workersPositions.get(s)));
        }
    }

    public Map<String, Point> getWorkersPositions() {
        return workersPositions;
    }
}
