package com.thetablock.thetaport.services;

import com.thetablock.thetaport.entities.MessageType;
import com.thetablock.thetaport.entities.PortData;
import com.thetablock.thetaport.entities.PortLoc;
import com.thetablock.thetaport.repositories.TempStorageRepository;
import com.thetablock.thetaport.repositories.TimerRepositories;
import com.thetablock.thetaport.utils.Response;
import com.thetablock.thetaport.repositories.PortDataRepository;
import com.thetablock.thetaport.utils.Tuple2;
import org.bukkit.Location;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

public final class PortServices {
    @Inject
    PortDataRepository portDataRepository;
    @Inject
    TimerRepositories timerRepository;
    @Inject
    TempStorageRepository tempStorageRepo;

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(4);


    public Response createPoint(UUID uuid, PortLoc point) {
        int count = tempStorageRepo.getCount(uuid);

        switch (count) {
            case 0:
                tempStorageRepo.addEntry(uuid, point);
                return Response.FIRST_SLOT;
            case 1:
                tempStorageRepo.addEntry(uuid, point);
                return Response.SECOND_SLOT;
            case 2:
                tempStorageRepo.invalidate(uuid);
                return Response.POINTS_RESET;
        }
        return Response.ERROR;
    }

    private int parseOffset(String offset) {
        int multiplier = 1;

        if (offset.contains("m")) {
            multiplier = 60;
        } else if (offset.contains("h")) {
            multiplier = 120;
        }
        offset = offset.replaceAll("[^\\d.]", "");
        return Integer.valueOf(offset) * multiplier;
    }
    /**
     * @param uuid
     * @param warp
     * @param offsetInSeconds
     * @return {@link Response#SUCCESS}, {@link Response#WARP_EXISTS}, {@link Response#REQUIRE_TWO_POINTS}
     */
    public Response createWarp(UUID uuid, String warp, String unparsedOffset, boolean isDisabled) {
        // PortData warpData = portDataRepository.getWarp(warp);
        List<PortLoc> points = tempStorageRepo.getTempPoints(uuid);
        int count = tempStorageRepo.getCount(uuid);
        if (points.size() == 2) {
            if (null == portDataRepository.getWarp(warp)) {
                PortLoc centerPoint = points.get(0).getCenterPointFrom(points.get(1));
                centerPoint.add(0, 1, 0);
                PortLoc floor = points.get(0).compareWarpLoc(points.get(1));
                PortLoc ceil = points.get(1).equals(floor) ? points.get(0) : points.get(1);

                int offset = unparsedOffset.isEmpty() ? 0 : parseOffset(unparsedOffset);

                PortData portData = new PortData(warp, null, floor, ceil, centerPoint, false,offset);
                portDataRepository.createNewWarp(portData, true);
                tempStorageRepo.invalidate(uuid);
                return Response.SUCCESS;
            }
            return Response.WARP_EXISTS;
        }
        return Response.REQUIRE_TWO_POINTS;
    }

    /**
     * @param warp
     * @param clean
     * @param unlink
     * @return {@link Response#SUCCESS}, {@link Response#WARP_NOT_LINKED}, {@link Response#WARP_DOES_NOT_EXIST}
     */
    public Response deleteWarp(String warp, boolean clean, boolean unlink) {
        PortData portData = portDataRepository.getWarp(warp);

        if (unlink) {
            if (null != portData.getLinked() && !portData.getLinked().isEmpty()) {
                PortData warp2 = portDataRepository.getWarp(portData.getLinked());
                portData.setLinked("");
                return Response.SUCCESS;

            }
            return Response.WARP_NOT_LINKED;
        }
        if (null != portData) {
            deleteWarp(warp);
            if (null != portData.getLinked()) {
                deleteWarp(warp);
            }
            return Response.SUCCESS;
        }
        return Response.WARP_DOES_NOT_EXIST;
    }

    private void deleteWarp(String warp) {
        portDataRepository.deleteWarp(warp);
    }

    /**
     * @param first
     * @param second
     * @param sync
     * @param roundTrip
     * @return {@link Response#SUCCESS}, {@link Response#WARP_NOT_LINKED}, {@link Response#INVALID_WARP}
     */
    public Response link(final String first, final String second, String sync, boolean roundTrip) {
        PortData firstPoint = portDataRepository.getWarp(first);
        PortData secondPoint = portDataRepository.getWarp(second);

        if (null != firstPoint && null != secondPoint) {
            if (null == firstPoint.getLinked()) {
                if (null == secondPoint.getLinked()) {

                    firstPoint.setLinked(second);
                    secondPoint.setLinked(first);
                    firstPoint.setWarpToPoint(secondPoint.getFloorPoint().getCenterPointFrom(secondPoint.getCeilPoint()));
                    secondPoint.setWarpToPoint(firstPoint.getFloorPoint().getCenterPointFrom(firstPoint.getCeilPoint()));

                    //checks to see if a value was set for sync.
                    if (null != sync && !sync.isEmpty()) {
                        if (sync.equals(first)) {
                            secondPoint.setWarpOffset(firstPoint.getWarpOffset());
                        } else if (sync.equals(second)) {
                            firstPoint.setWarpOffset(secondPoint.getWarpOffset());
                        }
                    }
                    //Updates all the data and saves it to the system.
                    portDataRepository.update(firstPoint, secondPoint);
                    return Response.SUCCESS;
                }
            }
            return Response.POINT_ALREADY_LINKED;
        }
        return Response.INVALID_WARP;
    }
    /**
     * @param node
     * @param type
     * @param message
     * @return {@link Response#SUCCESS}, {@link Response#INVALID_MESSAGE_TYPE}, {@link Response#WARP_DOES_NOT_EXIST}
     */
    public Response setMessage(String node, MessageType type, String message) {
        PortData firstPoint = portDataRepository.getWarp(node);
        if (null != firstPoint) {
            if (type.equals(MessageType.ARRIVAL)) {
                firstPoint.setArrivalMessage(message);
                return Response.SUCCESS;

            } else if (type.equals(MessageType.DEPARTURE)) {
                firstPoint.setDepartureMessage(message);
                return Response.SUCCESS;

            }
            return Response.INVALID_MESSAGE_TYPE;
        }
        return Response.WARP_DOES_NOT_EXIST;
    }

    public List<PortData> getWarpData(int page) {

        return null;
    }

    public Response unlink(String arg) {
        PortData point1 = portDataRepository.getWarp(arg);

        if (null != point1) {
                if (null != point1.getLinked() && !point1.getLinked().isEmpty()) {
                    PortData point2 = portDataRepository.getWarp(point1.getLinked());
                    portDataRepository.deleteWarp(arg);
                    portDataRepository.deleteWarp(arg);
                    point1.setLinked(null);
                    point2.setLinked(null);
                    portDataRepository.update(point1, point2);
                    return Response.SUCCESS;
                }
                return Response.WARP_NOT_LINKED;
            }
        return Response.INVALID_WARP;
    }

    public Tuple2<Response, List<String>> getLinkedList(int page) {
        int limit = 5;
        int offset = page * limit + 1;
        Collection<PortData> portDataList = portDataRepository.getWarpData().values();
        List<String> results = new ArrayList<>();
        Response response;

        if (page > 0 && portDataList.size() > offset) {
            portDataList.stream()
                    .filter(wd-> null != wd.getLinked() && !wd.getLinked().isEmpty())
                    .forEach(wd->{
                        PortData linked = portDataRepository.getWarp(wd.getLinked());

                        if (linked.getLinked().equals(wd.getName())) {
                            results.add(wd.getName() + " <------> " + wd.getLinked());
                            portDataList.remove(wd);
                        } else {
                            results.add(wd.getName() + " -------> " + wd.getLinked());

                        }
                        portDataList.remove(wd);

                    });
            response = Response.SUCCESS;
        } else {
            response = Response.PAGE_OUT_OF_RANGE;
        }
        return new Tuple2<Response, List<String>>(response, results);
    }

    public Tuple2<Response, List<String>> getUnlinked(int page) {
        final int limit = 5;
        List<String> list = portDataRepository.getWarpData().values()
                .stream()
                .filter(wd->null == wd.getLinked() || wd.getLinked().isEmpty())
                .map(PortData::getName)
                .collect(Collectors.toList());

        return new Tuple2<Response, List<String>>(Response.SUCCESS, list);

    }

    public Tuple2<Response,List<String>> getPortsByPage(int page) {
        return null;
    }

    public Tuple2<Response,List<String>> getNearbyWarps(Location location) {
        PortLoc portLoc = new PortLoc(location);
         List<String> warpNames = portDataRepository.getWarpData().values().stream()
                .filter(wd->wd.getWarpToPoint().distance(new PortLoc(location)) < 20)
                 .map(wd->wd.getWarpToPoint().distance(portLoc) + "m  : " + wd.getName())
                .collect(Collectors.toList());
         return new Tuple2<>(Response.SUCCESS, warpNames);
    }

    public Tuple2<Response,PortData> getWarpData(String arg) {
        PortData portData = portDataRepository.getWarp(arg);
        Response response;
        if (null != portData) {
            response = Response.SUCCESS;
        } else {
            response = Response.INVALID_WARP;
        }
        return new Tuple2<>(response, portData);
    }
}
