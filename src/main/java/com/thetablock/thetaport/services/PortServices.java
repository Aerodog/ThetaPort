package com.thetablock.thetaport.services;

import com.google.common.base.Splitter;
import com.thetablock.thetaport.entities.PortData;
import com.thetablock.thetaport.entities.PortLoc;
import com.thetablock.thetaport.entities.Core;
import com.thetablock.thetaport.enums.Direction;
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
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Rails;

import javax.inject.Inject;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.awt.GridBagConstraints.SOUTHWEST;

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

    public Response createPort(UUID uuid, String name, CommandLine cmdLine, String unparsedOffset, ItemStack requiredItem) {
        Core core = tempRepository.getTempPort(uuid);
        int offset = 0;

        if (null == core || cmdLine.hasOption("r")) {
            if (cmdLine.hasOption("r")) {
                core = null;
            }
            if (!portDataRepository.getWarpData().containsKey(name)) {
                if (cmdLine.hasOption("os")) {
                    String offsetUnparsed = cmdLine.getOptionValue("os");
                    System.out.println("offset" + offsetUnparsed);
                    if (null != offsetUnparsed && !offsetUnparsed.isEmpty()) {
                        offset = parseOffset(offsetUnparsed);
                    } else {
                        return Response.INVALID_OFFSET;
                    }
                }
                System.out.println("has " + cmdLine.hasOption("t"));
                if (cmdLine.hasOption("t")) {
                    if (null == requiredItem || requiredItem.getType().equals(Material.AIR)) {
                        return Response.INVALID_REQUIRED_ITEM;
                    }
                } else {
                    requiredItem = null;
                }
                core = new Core(name, offset, null, requiredItem, null, null, null, null, null);

                tempRepository.addTempPort(uuid, core);
                return Response.SUCCESS;
            }
            return Response.WARP_EXISTS;
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
                System.out.println("Before " + portLoc.toString());
                portLoc.add(0,2,0); //moves the block up one from where it was clicked.
                System.out.println("After " + portLoc.toString());

                core.setArrivalPoint(portLoc);
                portDataRepository.createNewWarp(new PortData(core), true);
                tempRepository.invalidateTempPort(uniqueId);
                response = Response.WARP_TO_POINT_SET;
                break;
        }
        return response;
    }

    private boolean isWithinBounds(PortLoc lower, PortLoc upper, Location loc) {
        double x1 = Math.min(lower.getX(), upper.getX());
        double y1 = Math.min(lower.getY(), upper.getY());
        double z1 = Math.min(lower.getZ(), upper.getZ());
        double x2 = Math.max(lower.getX(), upper.getX());
        double y2 = Math.max(lower.getY(), upper.getY());
        double z2 = Math.max(lower.getZ(), upper.getZ());

        return loc.getBlockX() >= x1 && loc.getBlockX() <= x2
                && loc.getBlockY() >= y1 && loc.getBlockY() <= y2
                && loc.getBlockZ() >= z1 && loc.getBlockZ() <= z2;
    }

    private Optional<Block> findExternalFacingRail(final BlockFace blockFace, PortLoc first, PortLoc second) {
        return IntStream.range((int) first.getX(), 1000)
               .mapToObj(i->{
                   switch (blockFace) {
                       case NORTH:
                           first.add(i, 0, 0);    break;
                       case EAST:
                           first.add(0, 0, 1); break;
                       case SOUTH:
                           first.add(i * -1, 0, 0);  break;
                       case WEST:
                           first.add(0, 0, i * -1);  break;
                   }
                   return first.getLocation().getBlock();
               })
               .filter(l->l.getType().equals(Material.RAILS))
               .findFirst();
    }

    public static List<Block> blocksFromTwoPoints(Location loc1, Location loc2)
    {
        List<Block> blocks = new ArrayList<Block>();

        int topBlockX = (loc1.getBlockX() < loc2.getBlockX() ? loc2.getBlockX() : loc1.getBlockX());
        int bottomBlockX = (loc1.getBlockX() > loc2.getBlockX() ? loc2.getBlockX() : loc1.getBlockX());

        int topBlockY = (loc1.getBlockY() < loc2.getBlockY() ? loc2.getBlockY() : loc1.getBlockY());
        int bottomBlockY = (loc1.getBlockY() > loc2.getBlockY() ? loc2.getBlockY() : loc1.getBlockY());

        int topBlockZ = (loc1.getBlockZ() < loc2.getBlockZ() ? loc2.getBlockZ() : loc1.getBlockZ());
        int bottomBlockZ = (loc1.getBlockZ() > loc2.getBlockZ() ? loc2.getBlockZ() : loc1.getBlockZ());

        for(int x = bottomBlockX; x <= topBlockX; x++)
        {
            for(int z = bottomBlockZ; z <= topBlockZ; z++)
            {
                for(int y = bottomBlockY; y <= topBlockY; y++)
                {
                    Block block = loc1.getWorld().getBlockAt(x, y, z);

                    blocks.add(block);
                }
            }
        }

        return blocks;
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
        List<String> splitter = Splitter.fixedLength(unparsedOffset.length() -1).splitToList(unparsedOffset);
        System.out.println(splitter);
        if (StringUtils.isNumeric(splitter.get(0))) {
            System.out.println("IT WORKS");
            offset = Integer.valueOf(splitter.get(0));
            if (splitter.get(0).equalsIgnoreCase("m")) {
                offset = offset * 60;
            } else if (splitter.get(0).equalsIgnoreCase("h")) {
                offset = offset * 120;
            } else if (splitter.get(0).equalsIgnoreCase("d")) {
                offset = offset * 86400;
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

    public Tuple2<Response, PortData> selectNearest(UUID uuid, Location location, boolean select) {
        Response response = null;
        PortData portData = null;

        PortLoc portLoc = new PortLoc(location);

        Optional<PortData> portDataOpt = portDataRepository.getWarpData().values().stream()
                .min(Comparator.comparingDouble(s -> s.getArrivalPoint().distance(portLoc)));

        if (portDataOpt.isPresent()) {
            portData = portDataOpt.get();

            if (select) {
                tempRepository.addSelected(uuid, portData.getName());
            }
            response = Response.SUCCESS;
        } else {
            response = Response.NO_PORT_IN_RANGE;
        }
        return new Tuple2<>(response, portData);
    }

    public <T extends Point3D> PortLoc getCorner(String world, T p1, T p2, boolean isTop) {
        double cx = (p1.getX() + p2.getX()) /2;
        double cy = (p1.getZ() + p2.getZ()) /2;
        double angle = Math.toDegrees(Math.atan2(p1.getZ() - p2.getZ(), p2.getX() - p1.getX()));
        double xr = (p1.getX() - cx) * Math.cos(angle) - (p1.getZ() -cy) * Math.sin(angle);
        double yr = (p2.getX() - cx) * Math.sin(angle) - (p2.getZ() -cy) * Math.cos(angle);
        double y = (isTop) ? p2.getY() : p1.getY();
        return new PortLoc(world, xr, y, yr);
    }

    private PortLoc calculateOffsett(double x, double y, double cx, double cy, double angle) {
        double xr = (x - cx) * Math.cos(angle) - (y -cy) * Math.sin(angle);
        double yr = (x - cx) * Math.sin(angle) - (y -cy) * Math.cos(angle);
        return new PortLoc("", xr, 0D, yr);
    }

    /**
     * Gets a list of ports by page.
     * @param request
     * @return {@link Response#SUCCESS}, {@link Response#INVALID_PAGE}
     */
    public Tuple2<Response, List<String>> getPortsByPage(String request) {
        List<String> list = new ArrayList<>();
        Response response = null;
        if (StringUtils.isNumeric(request)) {
            int page = Integer.valueOf(request);
            list = portDataRepository.getWarpData().values().stream()
                    .map(Core::getName)
                    .skip(page - 1)
                    .limit(5)
                    .collect(Collectors.toList());

            response = Response.SUCCESS;
        } else {
            response = Response.INVALID_PAGE;
        }
        return new Tuple2<>(response, list);
    }

    /**
     *
     * @param location
     * @return {@link Tuple2}<{@link Response#SUCCESS} {@link List}< {@link String}>
     */
    public Tuple2<Response, List<String>> getNearbyWarps(Location location) {
        PortLoc portLoc = new PortLoc(location);
        List<String> warpNames = portDataRepository.getWarpData().values().stream()
                .filter(wd -> wd.getArrivalPoint().distance(new PortLoc(location)) < 20)
                .map(wd -> wd.getArrivalPoint().distance(portLoc) + "m  : " + wd.getName())
                .collect(Collectors.toList());
        return new Tuple2<>(Response.SUCCESS, warpNames);
    }




    public Response expand(UUID uuid, Location location, int amount) {
        if (tempRepository.getSelectedMap().containsKey(uuid)) {
            String port = tempRepository.getSelectedMap().get(uuid);
            PortData portData = portDataRepository.getWarp(port);
            Direction direction = Direction.getDirection(location.getY());
            PortLoc lower = portData.getFloorPoint();
            PortLoc upper = portData.getCeilPoint();

            switch (direction) {
                case NORTH: // X + 1
                    if (portData.getFloorPoint().getX() > portData.getCeilPoint().getX()) {
                        portData.getFloorPoint().add(amount, 0, 0);
                    } else {
                        portData.getCeilPoint().add(amount, 0, 0);
                    }
                    break;
                case EAST: //Z + 1
                    if (portData.getFloorPoint().getZ() > portData.getCeilPoint().getZ()) {
                        portData.getFloorPoint().add(0, 0, amount);
                    } else {
                        portData.getCeilPoint().add(0, 0, amount);
                    }
                    break;
                case SOUTH: //X -1
                    if (portData.getFloorPoint().getX() < portData.getCeilPoint().getX()) {
                        portData.getFloorPoint().subtract(amount, 0, 0);
                    } else {
                        portData.getCeilPoint().subtract(amount, 0, 0);
                    }
                    break;
                case WEST:
                    if (portData.getFloorPoint().getZ() > portData.getCeilPoint().getZ()) {
                        portData.getFloorPoint().add(0, 0, amount);
                    } else {
                        portData.getCeilPoint().add(0, 0, amount);
                    }
                    break;
                case NORTHEAST:
                    if (lower.getX() > upper.getX() && lower.getY() > upper.getZ()) {
                        portData.getFloorPoint().add(amount, 0, amount);
                    } else {
                        portData.getFloorPoint().add(amount, 0, amount);
                    }
                    case SOUTHEAST: case NORTHWEST: case SOUTHWEST:
                    return Response.INVALID_DIRECTION;
            }

        }
        return null;
    }

    private double getDistance(PortLoc portLoc, PortLoc portLoc2) {
        return Math.sqrt(Math.pow(portLoc2.getX() - portLoc.getX(), 2) + Math.pow(portLoc2.getZ() - portLoc.getZ(), 2));
    }

    private double getAngle(PortLoc portLoc, PortLoc portLoc2) {
        double xDiff = portLoc.getX() - portLoc2.getX();
        double zDiff = portLoc.getZ() - portLoc2.getZ();
        return Math.toDegrees(Math.atan2(xDiff, zDiff));
    }

}
