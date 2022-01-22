package com.loohp.holomobhealth.utils;

import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class RayTrace {

    public static Entity getLookingEntity(LivingEntity entity, double range) {
        RayTrace ray = new RayTrace(entity.getEyeLocation().toVector(), entity.getEyeLocation().getDirection());
        List<Entity> entities = entity.getNearbyEntities(range, range, range);
        Entity closest = null;
        double distanceSquared = range * range + 1;
        for (Entity each : entities) {
            if (each instanceof Player && ((Player) each).getGameMode().equals(GameMode.SPECTATOR)) {
                continue;
            }
            Vector intersect = ray.positionOfIntersection(BoundingBoxUtils.getBoundingBox(each), range, 0.1);
            if (intersect != null) {
                double dis = entity.getEyeLocation().distanceSquared(intersect.toLocation(each.getWorld()));
                if (dis < distanceSquared) {
                    closest = each;
                    distanceSquared = dis;
                }
            }
        }
        return closest;
    }

    //general intersection detection
    public static boolean intersects(Vector position, Vector min, Vector max) {
        if (position.getX() < min.getX() || position.getX() > max.getX()) {
            return false;
        } else if (position.getY() < min.getY() || position.getY() > max.getY()) {
            return false;
        } else {
            return !(position.getZ() < min.getZ()) && !(position.getZ() > max.getZ());
        }
    }
    //origin = start position
    //direction = direction in which the raytrace will go
    private final Vector origin;
    private final Vector direction;

    public RayTrace(Vector origin, Vector direction) {
        this.origin = origin;
        this.direction = direction;
    }

    //get a point on the raytrace at X blocks away
    public Vector getPostion(double blocksAway) {
        return origin.clone().add(direction.clone().multiply(blocksAway));
    }

    //checks if a position is on contained within the position
    public boolean isOnLine(Vector position) {
        double t = (position.getX() - origin.getX()) / direction.getX();
        return position.getBlockY() == origin.getY() + (t * direction.getY()) && position.getBlockZ() == origin.getZ() + (t * direction.getZ());
    }

    //get all postions on a raytrace
    public List<Vector> traverse(double blocksAway, double accuracy) {
        List<Vector> positions = new ArrayList<>();
        for (double d = 0; d <= blocksAway; d += accuracy) {
            positions.add(getPostion(d));
        }
        return positions;
    }

    //intersection detection for current raytrace with return
    public Vector positionOfIntersection(Vector min, Vector max, double blocksAway, double accuracy) {
        List<Vector> positions = traverse(blocksAway, accuracy);
        for (Vector position : positions) {
            if (intersects(position, min, max)) {
                return position;
            }
        }
        return null;
    }

    //intersection detection for current raytrace
    public boolean intersects(Vector min, Vector max, double blocksAway, double accuracy) {
        List<Vector> positions = traverse(blocksAway, accuracy);
        for (Vector position : positions) {
            if (intersects(position, min, max)) {
                return true;
            }
        }
        return false;
    }

    //bounding box instead of vector
    public Vector positionOfIntersection(BoundingBox boundingBox, double blocksAway, double accuracy) {
        List<Vector> positions = traverse(blocksAway, accuracy);
        for (Vector position : positions) {
            if (intersects(position, boundingBox.getMin(), boundingBox.getMax())) {
                return position;
            }
        }
        return null;
    }

    //bounding box instead of vector
    public boolean intersects(BoundingBox boundingBox, double blocksAway, double accuracy) {
        List<Vector> positions = traverse(blocksAway, accuracy);
        for (Vector position : positions) {
            if (intersects(position, boundingBox.getMin(), boundingBox.getMax())) {
                return true;
            }
        }
        return false;
    }

    //debug / effects
    public void highlight(World world, double blocksAway, double accuracy) {
        for (Vector position : traverse(blocksAway, accuracy)) {
            world.spawnParticle(Particle.REDSTONE, position.toLocation(world), 1, new DustOptions(Color.fromRGB(0, 255, 0), 1));
        }
    }

}
