"""Bake the approved Blockbench Bezier curves for GeckoLib 5.5.2.

Usage: python scripts/export_blue_tail_wolf.py MODEL.bbmodel LEGACY.geo.json OUTPUT_DIRECTORY
Produces staging JSON; write final resources with scripts/json_utils.ps1 Write-Json.
Geometry is copied semantically. Additive tracks include bind rotations because GeckoLib
5.5.2 subtracts those rotations when combining snapshots. No geometry is flattened.
"""
import bisect
import json
import math
from pathlib import Path
import sys


def number(value):
    text = f"{value:.7f}".rstrip("0").rstrip(".")
    return "0" if text in ("", "-0") else text


def evaluate(keys, time):
    if time <= keys[0]["time"]:
        return [float(keys[0]["data_points"][0][axis]) for axis in "xyz"]
    if time >= keys[-1]["time"]:
        return [float(keys[-1]["data_points"][0][axis]) for axis in "xyz"]
    index = bisect.bisect_right([key["time"] for key in keys], time) - 1
    left, right = keys[index:index + 2]
    u = (time - left["time"]) / (right["time"] - left["time"])
    a = [float(left["data_points"][0][axis]) for axis in "xyz"]
    b = [float(right["data_points"][0][axis]) for axis in "xyz"]
    assert left["interpolation"] == "bezier"
    # The authored curves use linear Bezier time handles; validate before evaluating.
    delta = (right["time"] - left["time"]) / 3
    assert all(abs(x - delta) < 1e-6 for x in left["bezier_right_time"])
    assert all(abs(x + delta) < 1e-6 for x in right["bezier_left_time"])
    return [(1-u)**3*x + 3*(1-u)**2*u*(x+r) + 3*(1-u)*u*u*(y+l) + u**3*y
            for x, y, r, l in zip(a, b, left["bezier_right_value"], right["bezier_left_value"])]


def tracks(clip):
    result = {}
    for animator in clip["animators"].values():
        for key in animator.get("keyframes", []):
            result.setdefault((animator["name"], key["channel"]), []).append(key)
    for keys in result.values():
        keys.sort(key=lambda key: key["time"])
    return result


def sitting_pose(bones):
    """A seated haunch pose with the original four ankle positions held by two-link IK."""
    pose = {(name, channel): [0., 0., 0.] for name in bones for channel in ("rotation", "position")}
    def transform(name, current):
        bone = bones[name]
        angle = -math.radians(bone.get("rotation", [0, 0, 0])[0] + current[name, "rotation"][0])
        y, z = bone["pivot"][1:]
        py, pz = current[name, "position"][1:]
        c, s = math.cos(angle), math.sin(angle)
        ty, tz = y-c*y+s*z+py, z-s*y-c*z+pz
        if "parent" in bone:
            pa, pty, ptz = transform(bone["parent"], current)
            c, s = math.cos(pa), math.sin(pa)
            return angle+pa, c*ty-s*tz+pty, s*ty+c*tz+ptz
        return angle, ty, tz
    def point(matrix, pivot):
        a, ty, tz = matrix
        c, s = math.cos(a), math.sin(a)
        y, z = pivot[1:]
        return c*y-s*z+ty, s*y+c*z+tz
    legs = ["leg_front_right", "leg_front_left", "leg_back_right", "leg_back_left"]
    ankles = {leg: point(transform(leg+"_3", pose), bones[leg+"_3"]["pivot"]) for leg in legs}
    pose["body", "position"] = [0, -4, 0]
    pose["body_front", "position"] = [0, 1.5, 1]
    pose["body_front", "rotation"] = [-30, 0, 0]
    pose["head", "rotation"] = [22, 0, 0]
    pose["tail", "rotation"] = [-18, 0, 0]
    wrap = lambda a: (a+math.pi) % (2*math.pi)-math.pi
    for leg in legs:
        b1, b2, b3 = [bones[leg+suffix] for suffix in ("", "_2", "_3")]
        parent = transform(b1["parent"], pose)
        hip = point(parent, b1["pivot"])
        d = [a-b for a, b in zip(ankles[leg], hip)]
        v1 = [a-b for a, b in zip(b2["pivot"][1:], b1["pivot"][1:])]
        v2 = [a-b for a, b in zip(b3["pivot"][1:], b2["pivot"][1:])]
        l1, l2 = math.hypot(*v1), math.hypot(*v2)
        f1, f2 = math.atan2(v1[1], v1[0]), math.atan2(v2[1], v2[0])
        base = [-math.radians(b["rotation"][0]) for b in (b1, b2, b3)]
        cosine = (sum(x*x for x in d)-l1*l1-l2*l2)/(2*l1*l2)
        assert -1 <= cosine <= 1, (leg, "unreachable sitting paw")
        beta = math.copysign(math.acos(cosine), wrap(base[1]+f2-f1))
        a = math.atan2(d[1], d[0])-math.atan2(l2*math.sin(beta), l1+l2*math.cos(beta))
        angles = [wrap(a-f1-parent[0]), wrap(beta-f2+f1)]
        angles.append(wrap(-parent[0]-sum(angles)))
        for suffix, angle, bind in zip(("", "_2", "_3"), angles, base):
            pose[leg+suffix, "rotation"] = [-math.degrees(wrap(angle-bind)), 0, 0]
        assert max(abs(x-y) for x, y in zip(point(transform(leg+"_3", pose), b3["pivot"]), ankles[leg])) < 1e-8
    return pose


def export(model_path, geometry_path, output):
    model = json.loads(Path(model_path).read_text(encoding="utf-8-sig"))
    geometry = json.loads(Path(geometry_path).read_text(encoding="utf-8-sig"))
    bones = {bone["name"]: bone for bone in geometry["minecraft:geometry"][0]["bones"]}
    clips = {clip["name"]: clip for clip in model["animations"]}
    clip_tracks = {name: tracks(clip) for name, clip in clips.items() if not name.startswith(("preview.", "reference."))}
    result, errors = {}, {"rotation": 0, "position": 0}
    weights = {"misc.idle": "query.theaurorian2_wolf_pose_3", "move.walk": "query.theaurorian2_wolf_pose_0",
               "move.run": "query.theaurorian2_wolf_pose_1", "misc.sit": "query.theaurorian2_wolf_pose_2",
               "misc.idle_look": "query.theaurorian2_wolf_gesture", "misc.idle_sniff": "query.theaurorian2_wolf_gesture"}
    selected = ["misc.idle", "move.walk", "move.run", "misc.idle_look", "misc.idle_sniff", "attack.bite", "misc.howl"]
    for name in selected:
        clip, source = clips[name], clip_tracks[name]
        length = clip["length"]
        # Adaptively flatten Bezier curves. Do not apply GeckoLib's different spline
        # interpolation to already-authored Blockbench curves.
        animation = {"loop": name in ("misc.idle", "move.walk", "move.run"), "animation_length": length, "bones": {}}
        for (bone, channel), keys in source.items():
            def sample(time):
                raw = evaluate(keys, time)
                if name in ("misc.idle_look", "misc.idle_sniff"):
                    idle = evaluate(clip_tracks["misc.idle"][bone, channel], time)
                    raw = [a-b for a, b in zip(raw, idle)]
                # Blockbench -> Bedrock axis conversion (inverse of the original import).
                return [-raw[0], -raw[1] if channel == "rotation" else raw[1], raw[2]]
            knots = {0, length, *(key["time"] for key in keys)}
            if name in ("misc.idle_look", "misc.idle_sniff"):
                knots.update(key["time"] for key in clip_tracks["misc.idle"][bone, channel])
            times = []
            tolerance = .035 if channel == "rotation" else .001
            def flatten(a, b, depth=0):
                va, vb = sample(a), sample(b)
                error = max(abs((1-u)*x+u*y-z) for u in (.25, .5, .75)
                            for x, y, z in zip(va, vb, sample(a+(b-a)*u)))
                if error > tolerance and depth < 12:
                    flatten(a, (a+b)/2, depth+1)
                    flatten((a+b)/2, b, depth+1)
                else:
                    times.append(a)
            knots = sorted(knots)
            for a, b in zip(knots, knots[1:]):
                flatten(a, b)
            times.append(knots[-1])
            values = [sample(time) for time in times]
            for i in range(len(times)-1):
                midpoint = (times[i]+times[i+1])/2
                expected = evaluate(keys, midpoint)
                if name in ("misc.idle_look", "misc.idle_sniff"):
                    idle = evaluate(clip_tracks["misc.idle"][bone, channel], midpoint)
                    expected = [a-b for a, b in zip(expected, idle)]
                expected = [-expected[0], -expected[1] if channel == "rotation" else expected[1], expected[2]]
                errors[channel] = max(errors[channel], max(abs((a+b)/2-c) for a, b, c in zip(values[i], values[i+1], expected)))
            bind = bones[bone].get("rotation", [0, 0, 0]) if channel == "rotation" and name != "misc.idle" else [0, 0, 0]
            def encode(value):
                return [f"{number(base)}+({number(offset)})*{weights[name]}" if name in weights and abs(offset) > 1e-8
                        else float(number(base+offset if name not in weights else base)) for base, offset in zip(bind, value)]
            track = {number(t): encode(v) for t, v in zip(times, values)}
            encoded = list(track.values())
            animation["bones"].setdefault(bone, {})[channel] = encoded[0] if all(v == encoded[0] for v in encoded) else track
        result[name] = animation
    seated = sitting_pose(bones)
    result["misc.sit"] = {"loop": True, "animation_length": 6, "bones": {}}
    for (bone, channel), value in seated.items():
        bind = bones[bone].get("rotation", [0, 0, 0]) if channel == "rotation" else [0, 0, 0]
        result["misc.sit"]["bones"].setdefault(bone, {})[channel] = [
            f"{number(base)}+({number(offset)})*{weights['misc.sit']}" if abs(offset) > 1e-8 else base
            for base, offset in zip(bind, value)]
    assert errors["rotation"] < .15 and errors["position"] < .01, errors
    output = Path(output)
    output.mkdir(parents=True, exist_ok=True)
    for filename, data in [("blue_tail_wolf.animation.json", {"format_version": "1.8.0", "animations": result}),
                           ("blue_tail_wolf.geo.json", geometry), ("export-validation.json", errors)]:
        (output/filename).write_text(json.dumps(data, ensure_ascii=False, separators=(",", ":")), encoding="utf-8")
    print("Validated Bezier bake maximum midpoint errors:", errors)
    print("Sitting pose: all four ankle positions preserved within 1e-8 model pixels.")


if __name__ == "__main__":
    export(*sys.argv[1:])
