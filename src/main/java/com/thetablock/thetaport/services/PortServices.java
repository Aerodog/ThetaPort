package com.thetablock.thetaport.services;

import com.google.common.base.Splitter;
import com.thetablock.thetaport.entities.PortData;
import com.thetablock.thetaport.entities.PortLoc;
import com.thetablock.thetaport.entities.Core;
import com.thetablock.thetaport.enums.EnumSetTypes;
import com.thetablock.thetaport.repositories.TempRepository;
import com.thetablock.thetaport.enums.Response;
import com.thetablock.thetaport.repositories.PortDataRepository;
import com.thetablock.thetaport.utils.Tuple2;
import javafx.geometry.Point3D;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

public final class PortServices {
    @Inject
    PortDataRepository portDataRepository;
    @Inject
    TempRepository tempRepository;

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(4);

    @Deprecated
    public Response createPoint(UUID uuid, PortLoc point) {
        return Response.DISABLED;
    }

    public Response createPort(UUID uuid, String name, boolean override, boolean isDisabled, boolean hasOffset, boolean hasRequiredItem, boolean hasArrivalMessage, boolean hasDepartureMessage,  Optional<String> unparsedOffset, Optional<ItemStack> requiredItemOptional, boolean reset) {
        Core core = tempRepository.getTempPort(uuid);
        int offset = 0;

        if (core != null && reset) {
            core = null;
        }

        if (null == core || override) {
            //checks to see if the warp already exists.
            if (!portDataRepository.getWarpData().containsKey(name)) {
                if (hasOffset) {
                    if (unparsedOffset.isPresent()) {
                        offset = parseOffset(unparsedOffset.get());
                    } else {
                        return Response.INVALID_OFFSET;
                    }
                }
                ItemStack item = null;
                if (hasRequiredItem) {
                    if (requiredItemOptional
                            .filter(v -> !v.getType().equals(Material.AIR))
                            .isPresent()) {
                        item = requiredItemOptional.get();
                    } else {
                        return Response.INVALID_REQUIRED_ITEM;
                    }
                }
                core = new Core(name, offset, null, item, null, null, null, null, null);

                tempRepository.addTempPort(uuid, core);
                return Response.SUCCESS;
            }
            return Response.INVALID_OFFSET;
        }
        return Response.TEMP_PORT_EXISTS;
    }

    /**
     *
     * @param uniqueId
     * @param location
     * @return {@link Response#SUCCESS}
     */
    public Response setPoint(UUID uniqueId, ItemStack itemStack, Location location, int size) {
        PortLoc portLoc = new PortLoc(location);
        Response response = null;
        Core core = tempRepository.getTempPort(uniqueId);

        switch (size) {
            case 0:
                core.setFloorPoint(portLoc);
                response =  Response.FIRST_POINT_SET;
                tempRepository.addTempPort(uniqueId, core);
                break;
            case 1:
                if (portLoc.getY() < core.getFloorPoint().getY()) {
                    core.setCeilPoint(core.getFloorPoint());
                    core.setFloorPoint(portLoc);
                } else {
                    core.setCeilPoint(portLoc);
                }
                response = Response.SECOND_POINT_SET;
                break;
            case 2:
                portLoc.add(0,1,0); //moves the block up one from where it was clicked.
                core.setArrivalPoint(portLoc);
                portDataRepository.createNewWarp(new PortData(core), true);
                tempRepository.invalidateTempPort(uniqueId);
                response = Response.WARP_TO_POINT_SET;
                break;
        }
        return response;
    }

//    /**
//     * @param uuid
//     * @param warp
//     * @param arrivalMessage
//     * @param departureMessage
//     * @param itemInMainHand
//     * @return {@link Response#SUCCESS}, {@link Response#WARP_EXISTS}, {@link Response#REQUIRE_TWO_POINTS}
//     */
//    public Response createPort(UUID uuid, String warp, boolean hasOffset, String unparsedOffset, boolean isDisabled, String arrivalMessage, String departureMessage, Optional<String> linked, boolean requiresItem, ItemStack itemInMainHand) {
//        // PortData warpData = portDataRepository.getWarp(warp);
//        List<PortLoc> points = tempStorageRepo.getTempPoints(uuid);
//        int count = tempStorageRepo.getCount(uuid);
//        if (points.size() == 2) {
//            if (null == portDataRepository.getWarp(warp)) {
//                PortLoc centerPoint = points.get(0).getCenterPointFrom(points.get(1));
//                centerPoint.add(0, 1, 0);
//                PortLoc floor = points.get(0).compareWarpLoc(points.get(1));
//                PortLoc ceil = points.get(1).equals(floor) ? points.get(0) : points.get(1);
//
//                System.out.println("requires item " + requiresItem + " " + hasOffset + " " + unparsedOffset);
//
//                int offset = parseOffset(unparsedOffset);
//                if (hasOffset && unparsedOffset.isEmpty()) {
//                    return Response.INVALID_OFFSET;
//                }
//                Item item = null;
//                if (requiresItem ) {
//                    if (null != itemInMainHand) {
//                        ItemMeta itemMeta = itemInMainHand.getItemMeta();
//                        item = new Item(itemMeta.getDisplayName(), itemMeta.getLore(), itemInMainHand.getType(), 0);
//                    } else {
//                        return Response.INVALID_REQUIRED_ITEM;
//                    }
//                }
//                // public PortData(String name, String linked, String warpMessage, String arrivalMessage, String departureMessage,
//                //                    PortLoc floorPoint, PortLoc ceilPoint, PortLoc warpToPoint, PortLoc arrivalRedstone, PortLoc departureRedstone,
//                //                    ItemStack requiredItem, LocalDateTime lastPortTime, boolean isEnabled, int warpOffset) {
//
//                PortData  portData = new PortData(warp, null, "", "", "", floor, ceil, centerPoint, null, null, item, LocalDateTime.now(), true, offset);
////                PortData portData = new PortData(warp, null, "", arrivalMessage, departureMessage, floor, ceil, centerPoint, null, null, LocalDateTime.now(), true, offset, itemInMainHand);
//                portDataRepository.createNewWarp(portData, true);
//
//                if (linked.isPresent()) {
//                    Response response = this.link(portData.getName(), linked.get(), "", true);
//
//                    switch (response) {
//                        case SUCCESS:
//                            return Response.SUCCESS;
//                        case INVALID_PORT:
//                            return Response.SUCCESS_INVALID_LINK;
//                    }
//                }
//                tempStorageRepo.invalidate(uuid);
//                return Response.SUCCESS;
//            }
//            return Response.WARP_EXISTS;
//        }
//        return Response.REQUIRE_TWO_POINTS;
//    }

    private int parseOffset(String unparsedOffset) {
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
                }
            }
        }
        return offset;
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
//                    firstPoint.setArrivalPoint(secondPoint.getFloorPoint().getCenterPointFrom(secondPoint.getCeilPoint()));
//                    secondPoint.setArrivalPoint(firstPoint.getFloorPoint().getCenterPointFrom(firstPoint.getCeilPoint()));

                    //checks to see if a value was set for sync.
                    if (null != sync && !sync.isEmpty()) {
                        if (sync.equals(first)) {
                            secondPoint.setOffset(firstPoint.getOffset());
                        } else if (sync.equals(second)) {
                            firstPoint.setOffset(secondPoint.getOffset());
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
                    .map(m -> m.getName().length())
                    .max(Integer::compare).orElse(0);

            for (PortData portData : portDataRepository.getWarpData().values()) {
                if (null != portData.getLinked() && !portData.getLinked().isEmpty()) {
                    if (!ignoreList.contains(portData.getName())) {
                        int space = ((max - portData.getName().length()) + portData.getName().length()) + 3;
                        space = portData.getName().length() == max ? space - 2 : space;
                        PortData linkedPort = portDataRepository.getWarp(portData.getLinked());
                        String status = " -------> ";

                        if (!linkedPort.getLinked().isEmpty() && linkedPort.getLinked().equalsIgnoreCase(portData.getName())) {
                            status = " <------> ";
                            ignoreList.add(portData.getLinked());
                        }
                        results.add(String.format("%-" + space + "s %-1s %-2s", "§3" + portData.getName(), "§9" + status, "§A" + portData.getLinked()));
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

        return String.format("%-" + space + "s %-2s", first + marker + second);
    }

    public Tuple2<Response, List<String>> getUnlinked(int page) {
        final int limit = 5;
        List<String> list = portDataRepository.getWarpData().values()
                .stream()
                .filter(wd -> null == wd.getLinked() || wd.getLinked().isEmpty())
                .map(PortData::getName)
                .collect(Collectors.toList());

        return new Tuple2<Response, List<String>>(Response.SUCCESS, list);

    }

    public Tuple2<Response, List<String>> getPortsByPage(int page) {
        return new Tuple2<>(Response.DISABLED, new ArrayList<>());
    }

    public Tuple2<Response, List<String>> getNearbyWarps(Location location) {
        PortLoc portLoc = new PortLoc(location);
        List<String> warpNames = portDataRepository.getWarpData().values().stream()
                .filter(wd -> wd.getArrivalPoint().distance(new PortLoc(location)) < 20)
                .map(wd -> wd.getArrivalPoint().distance(portLoc) + "m  : " + wd.getName())
                .collect(Collectors.toList());
        return new Tuple2<>(Response.SUCCESS, warpNames);
    }

    public Tuple2<Response, PortData> getWarpData(String arg) {
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
                .filter(pd -> portLoc.isWithinBounds(pd.getFloorPoint(), pd.getCeilPoint()))
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
