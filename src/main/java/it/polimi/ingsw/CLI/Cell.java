package it.polimi.ingsw.CLI;

import it.polimi.ingsw.model.enums.BuildingType;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Cell {
    private final Point position;
    private final List<BuildingType> buildings;
    private String worker;

    public Cell(Point pos){
        this.position = pos;
        this.buildings = new ArrayList<>();
    }

    public List<BuildingType> getBuildings() {
        return buildings;
    }

    public Point getPosition() {
        return position;
    }

    public String getWorker() {
        return worker;
    }

    public void setWorker(String worker) {
        this.worker = worker;
    }

    public void removeWorker() {
        this.worker = null;
    }

    public void addBuilding(BuildingType building) {
        buildings.add(building);
    }
}