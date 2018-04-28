package com.thetablock.thetaport.services;

import com.google.common.base.Splitter;
import com.thetablock.thetaport.entities.PortData;
import com.thetablock.thetaport.entities.PortLoc;
import com.thetablock.thetaport.enums.EnumSetTypes;
import com.thetablock.thetaport.repositories.TempStorageRepository;
import com.thetablock.thetaport.repositories.TimerRepository;
import com.thetablock.thetaport.enums.Response;
import com.thetablock.thetaport.repositories.PortDataRepository;
import com.thetablock.thetaport.utils.Tuple2;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

public final class PortServices {
    @Inject
    PortDataRepository portDataRepository;
    @Inject
    TimerRepository timerRepository;
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
        /**
     * @param uuid
     * @param warp
     * @param arrivalMessage
     * @param departureMessage
     * @return {@link Response#SUCCESS}, {@link Response#WARP_EXISTS}, {@link Response#REQUIRE_TWO_POINTS}
     */
    public Response createPort(UUID uuid, String warp, String unparsedOffset, boolean isDisabled, String arrivalMessage, String departureMessage, Optional<String> linked) {
        // PortData warpData = portDataRepository.getWarp(warp);
        List<PortLoc> points = tempStorageRepo.getTempPoints(uuid);
        int count = tempStorageRepo.getCount(uuid);
        if (points.size() == 2) {
            if (null == portDataRepository.getWarp(warp)) {
                PortLoc centerPoint = points.get(0).getCenterPointFrom(points.get(1));
                centerPoint.add(0, 1, 0);
                PortLoc floor = points.get(0).compareWarpLoc(points.get(1));
                PortLoc ceil = points.get(1).equals(floor) ? points.get(0) : points.get(1);

                int offset = 0;
                if (!unparsedOffset.isEmpty()) {
                    List<String> splitter = Splitter.on("(?<=\\D)(?=\\d)").splitToList(unparsedOffset);
                    if (StringUtils.isNumeric(splitter.get(0))) {
                        offset = Integer.valueOf(splitter.get(0));
                        if (splitter.get(0).equalsIgnoreCase("m")) {
                            offset = offset * 60;
                        } else if (splitter.get(0).equalsIgnoreCase("h")) {
                            offset = offset * 120;
                        } else if (splitter.get(0).equalsIgnoreCase("d")) {
                            offset = offset * 86400;
                        } else {
                            return Response.INVALID_OFFSET;
                        }
                    }
                }

                if (offset >= 0) {
                    // public PortData(String name, String linked, String warpMessage, String arrivalMessage, String departureMessage, PortLoc floorPoint, PortLoc ceilPoint, PortLoc warpToPoint, LocalTime lastPortTime, boolean isEnabled, int warpOffset, boolean isTimed, Runnable runnable) {
                    PortData portData = new PortData(warp, null, "", arrivalMessage, departureMessage, floor, ceil, centerPoint, LocalDateTime.now(),
                            true, offset);
                    portDataRepository.createNewWarp(portData, true);

                    if (linked.isPresent()) {
                        Response response = this.link(portData.getName(), linked.get(), "", true);

                        switch (response) {
                            case SUCCESS:
                                return Response.SUCCESS;
                            case INVALID_PORT:
                                return Response.SUCCESS_INVALID_LINK;
                        }
                    }
                    linked.ifPresent(l->{


                    });
                    tempStorageRepo.invalidate(uuid);
                    return Response.SUCCESS;
                }
                return Response.INVALID_OFFSET;
            }
            return Response.WARP_EXISTS;
        }
        return Response.REQUIRE_TWO_POINTS;
    }



    public Response deleteWarp(String warp, boolean clean, boolean confirmed) {
        PortData portData = portDataRepository.getWarp(warp);

        if (null != portData) {
           if (clean) {
               if (confirmed) {
                   deleteWarp(portData.getLinked(), false, true);
               } else {
                   return Response.REQUIRE_CONFIRMED;
               }
           }
           portDataRepository.deleteWarp(warp);
           return Response.SUCCESS;
        }
        return Response.INVALID_PORT;
    }

    /**
     * @param first
     * @param second
     * @param sync
     * @param roundTrip
     * @return {@link Response#SUCCESS}, {@link Response#WARP_NOT_LINKED}, {@link Response#INVALID_PORT}
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
                    portDataRepository.deleteWarp(first);
                    portDataRepository.deleteWarp(second);
                    portDataRepository.update(firstPoint, secondPoint);
                    return Response.SUCCESS;
                }
            }
            return Response.POINT_ALREADY_LINKED;
        }
        return Response.INVALID_PORT;
    }

    public Response setArrivalMessage(String portName, String message) {
        PortData portData = portDataRepository.getWarp(portName);

        if (null != portData) {
            message = message.replaceAll("&", "§");
            portData.setArrivalMessage(message);
            portDataRepository.deleteWarp(portName);
            portDataRepository.update(portData);
            return Response.SUCCESS;
        }
        return Response.INVALID_PORT;
    }

    public Response setDepatureMessage(String portName, String message) {
        PortData portData = portDataRepository.getWarp(portName);

        if (null != portData) {
            message = message.replaceAll("&", "§");
            portData.setDepartureMessage(message);
            portDataRepository.deleteWarp(portName);
            portDataRepository.update(portData);
            return Response.SUCCESS;
        }
        return Response.INVALID_PORT;
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
        return Response.INVALID_PORT;
    }

    public Tuple2<Response, List<String>> getLinkedList(int page) {
        int itemsPerPage = 5;
        int offset = (page - 1) * itemsPerPage;
        List<String> ignoreList = new ArrayList<>();
        List<String> results = new ArrayList<>();
        Response response;


        if (page > 0 && portDataRepository.getWarpData().values().size() > offset) {

            final int max = portDataRepository.getWarpData().values().stream()
                    .map(m->m.getName().length())
                    .max(Integer::compare).orElse(0);

            for (PortData portData : portDataRepository.getWarpData().values()) {
                if (null != portData.getLinked() && !portData.getLinked().isEmpty()) {
                    if (!ignoreList.contains(portData.getName())) {
                        int space = ((max - portData.getName().length()) + portData.getName().length()) + 3;
                        space = portData.getName().length() == max ? space -2 : space;
                        PortData linkedPort = portDataRepository.getWarp(portData.getLinked());
                        String status = " -------> ";

                        if (!linkedPort.getLinked().isEmpty() && linkedPort.getLinked().equalsIgnoreCase(portData.getName())) {
                            status = " <------> ";
                            ignoreList.add(portData.getLinked());
                        }
                        results.add(String.format("%-" + space + "s %-1s %-2s","§3" + portData.getName(),"§9" + status, "§A" + portData.getLinked()));
                    }
                }
            }
            response = Response.SUCCESS;
        } else {
            response = Response.PAGE_OUT_OF_RANGE;
        }
        return new Tuple2<>(response, results);
    }

    private String formatter(String first, String second, int max, boolean isBiDirectional) {
        final int space = max - first.length();
        final String marker = (isBiDirectional) ? " <------> " : " -------> ";

        return String.format("%-" + space + "s %-2s", first  + marker + second);
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
        return new Tuple2<>(Response.DISABLED, new ArrayList<>());
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
            response = Response.INVALID_PORT;
        }
        return new Tuple2<>(response, portData);
    }

    public void set(Location location, String enumSetTypes, String value) {
        final PortLoc portLoc = new PortLoc(location);

        Optional<PortData> optionalPortData = portDataRepository.getWarpData().values().stream()
                .filter(pd->portLoc.isWithinBounds(pd.getFloorPoint(), pd.getCeilPoint()))
                .findFirst();

        if (optionalPortData.isPresent()) {
            PortData portData = optionalPortData.get();
            EnumSetTypes setType = EnumSetTypes.valueOf(enumSetTypes);

            switch (setType) {
                case OFFSET:

                    break;
                case NAME:
                    break;
                case LINK:
                    break;
                case FLOOR:
                    break;
                case CEIL:

            }

        }
    }
}
