/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 * <movsim.org@gmail.com>
 * -----------------------------------------------------------------------------------------
 * 
 * This file is part of
 * 
 * MovSim - the multi-model open-source vehicular-traffic simulator.
 * 
 * MovSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MovSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MovSim. If not, see <http://www.gnu.org/licenses/>
 * or <http://www.movsim.org>.
 * 
 * -----------------------------------------------------------------------------------------
 */
package org.movsim.simulator.roadnetwork.controller;

import java.util.HashSet;
import java.util.Set;

import org.movsim.autogen.TrafficLightStatus;
import org.movsim.network.autogen.opendrive.LaneValidity;
import org.movsim.network.autogen.opendrive.OpenDRIVE;
import org.movsim.network.autogen.opendrive.OpenDRIVE.Controller;
import org.movsim.network.autogen.opendrive.OpenDRIVE.Road.Signals.Signal;
import org.movsim.simulator.roadnetwork.Lanes;
import org.movsim.simulator.roadnetwork.RoadSegment;

import com.google.common.base.Preconditions;

/**
 * The Class TrafficLight.
 */
public class TrafficLight extends RoadObjectController {

    /** The status. */
    private TrafficLightStatus status;

    private final String groupId; // unique mapping to infrastructure

    private TriggerCallback triggerCallback;

    private final Set<TrafficLightStatus> possibleStati = new HashSet<>(); // deprecated, just for drawing trafficlights in viewer

    private final Signal signal;
    private final Controller controller;
    private final String signalType;

    public TrafficLight(Signal signal, Controller controller, RoadSegment roadSegment) {
        super(RoadObjectType.TRAFFICLIGHT, signal.getS(), roadSegment);
        this.controller = Preconditions.checkNotNull(controller);
        this.signal = Preconditions.checkNotNull(signal);
        Preconditions.checkArgument(signal.isSetId(), "id not set");
        Preconditions.checkArgument(!signal.getId().isEmpty(), "empty id!");
        Preconditions.checkArgument(signal.isSetS(), "signal.s not set");
        this.signalType = Preconditions.checkNotNull(checkTypesAndExtractSignalType());
        this.groupId = controller.getId();
        setLaneValidity();
    }

    private void setLaneValidity() {
        if (signal.isSetValidity()) {
            for (int lane = Lanes.MOST_INNER_LANE; lane <= roadSegment.laneCount(); lane++) {
                setLaneValidity(lane, false);
            }
            for (LaneValidity laneValidity : signal.getValidity()) {
                for (int lane = Math.abs(laneValidity.getFromLane()); lane <= Math.abs(laneValidity.getToLane()); lane++) {
                    setLaneValidity(lane, true);
                }
            }
        }
    }

    private String checkTypesAndExtractSignalType() {
        String signalType = null;
        for (OpenDRIVE.Controller.Control control : controller.getControl()) {
            if (!control.isSetType()) {
                throw new IllegalArgumentException("controller.control.type must be set in xodr for signal="
                        + signalId());
            }
            if (control.getSignalId().equals(signalId())) {
                signalType = control.getType();
            }
        }
        return signalType;
    }

    /**
     * Returns the id. This id is defined in the infrastructure configuration file.
     * 
     * @return the label
     */
    public String signalId() {
        return signal.getId();
    }

    /**
     * Returns the signal type assigned in the controller.control xodr input. This type links from a 'physical' signal to a 'logical'
     * representation of the trafficlight state.
     * 
     * @return the signal-type id
     */
    public String signalType() {
        return signalType;
    }

    public String controllerId() {
        return controller.getId();
    }

    public Controller getController() {
        return controller;
    }

    /**
     * Returns the controllergroup-id which allows for a unique reference to a set of signals in the infrastructure.
     * 
     * @return groupId
     */
    public String groupId() {
        return groupId;
    }

    public TrafficLightStatus status() {
        return status;
    }

    // must be package-private, access only from controller
    void setState(TrafficLightStatus newStatus) {
        this.status = newStatus;
    }

    // trigger from viewer via mouse-click (direct communication from signal to controller)
    // shouldn't be called from external, use controller instead
    public void triggerNextPhase() {
        triggerCallback.nextPhase();
    }

    public void setTriggerCallback(TriggerCallback triggerCallback) {
        this.triggerCallback = Preconditions.checkNotNull(triggerCallback);
    }

    public boolean hasTriggerCallback() {
        return triggerCallback != null;
    }

    // not needed in future
    @Deprecated
    void addPossibleState(TrafficLightStatus status) {
        possibleStati.add(status);
    }

    /**
     * Return the number of lights this traffic light has, can be 1, 2 or 3.
     * 
     * @return
     */
    @Deprecated
    public int lightCount() {
        // return Math.min(3, possibleStati.size());
        return 3;
    }

    @Override
    public String toString() {
        return "TrafficLight [controllerId = " + controllerId() + ", signalId = " + signalId() + ", status=" + status
                + ", position=" + position + ", signalType=" + signalType + ", groupId = " + groupId
                + ", roadSegment.id=" + ((roadSegment == null) ? "null" : roadSegment.userId()) + "]";
    }

    @Override
    public void createSignalPositions() {
        // TODO Auto-generated method stub
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        // nothing to do, single TrafficLight will be controlled by ControllerGroup
    }

}