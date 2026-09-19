"""Import the user-provided skeleton without repainting or changing its UV/pivots."""
import argparse
import copy
import json
from pathlib import Path
import subprocess
import tempfile
import zipfile

ROOT = Path(__file__).resolve().parents[1]


def import_resources(archive, model_only=False):
    with zipfile.ZipFile(archive) as source:
        model = json.loads(source.read("skeleton.bbmodel"))
        animation = json.loads(source.read("skeleton.animation.json"))
        texture = source.read("skeleton.png")
    assert model["resolution"] == {"width": 48, "height": 48}
    groups = {g["uuid"]: g for g in model["groups"]}
    cubes = {c["uuid"]: c for c in model["elements"]}
    bones, visited = [], []

    def vec(v):
        return [-v[0], v[1], v[2]]

    def rotation(v):
        return [-v[0], -v[1], v[2]]

    def walk(node, parent=None, parent_visible=True):
        group = groups[node["uuid"]]
        visible = parent_visible and group.get("visibility", True)
        bone = {"name": group["name"], "pivot": vec(group["origin"])}
        if parent:
            bone["parent"] = parent
        if any(group.get("rotation", [])):
            bone["rotation"] = rotation(group["rotation"])
        bones.append(bone)
        for child in node["children"]:
            if isinstance(child, dict):
                walk(child, group["name"], visible)
                continue
            visited.append(child)
            cube = cubes[child]
            assert cube["type"] == "cube" and cube.get("export", True)
            size = [b-a for a, b in zip(cube["from"], cube["to"])]
            assert all(n >= 0 for n in size)
            result = {"origin": [-cube["to"][0], cube["from"][1], cube["from"][2]], "size": size, "uv": {}}
            if any(cube.get("rotation", [])):
                result.update(pivot=vec(cube["origin"]), rotation=rotation(cube["rotation"]))
            if cube.get("inflate"):
                result["inflate"] = cube["inflate"]
            for side, face in cube["faces"].items():
                if face.get("texture") is None:
                    continue
                assert face.get("rotation", 0) == 0
                uv = face["uv"]
                if side in ("up", "down"):
                    uv = [uv[2], uv[3], uv[0], uv[1]]
                assert all(0 <= c <= 48 for c in uv)
                result["uv"][side] = {"uv": uv[:2], "uv_size": [uv[2]-uv[0], uv[3]-uv[1]]}
            # The revised head deliberately hides the crystal planes in Blockbench.
            # Retain their bones for animation references, but match the source's visible geometry.
            if visible and cube.get("visibility", True):
                bone.setdefault("cubes", []).append(result)

    for root in model["outliner"]:
        walk(root)
    assert len(visited) == len(set(visited)) == len(cubes) == 12
    # Child anchors put real equipment at the ends of the imported 14-pixel arms.
    bones.extend([
        {"name": "right_item", "parent": "hand_right", "pivot": [5, 12, 0]},
        {"name": "left_item", "parent": "hand_left", "pivot": [-5, 12, 0]},
        {"name": "armor_body", "parent": "body", "pivot": [0, 25, 0]},
    ])
    geo = {"format_version": "1.12.0", "minecraft:geometry": [{"description": {
        "identifier": "geometry.moonreaver_skeleton", "texture_width": 48, "texture_height": 48,
        "visible_bounds_width": 3, "visible_bounds_height": 4, "visible_bounds_offset": [0, 1.25, 0]
    }, "bones": bones}]}
    resources = {
        "assets/theaurorian2/geckolib/models/entity/moonreaver_skeleton.geo.json": geo,
        "assets/theaurorian2/geckolib/animations/entity/moonreaver_skeleton.animation.json": animation,
    }
    if model_only:
        write_resources(resources, texture)
        return
    vanilla = ROOT / "build/moddev/artifacts/minecraft-patched-26.1.2.84-sources.jar"
    with zipfile.ZipFile(vanilla) as source:
        loot = json.loads(source.read("data/minecraft/loot_table/entities/skeleton.json"))
        bow = json.loads(source.read("assets/minecraft/items/bow.json"))
    loot["pools"][1]["entries"][0]["functions"][0]["count"]["min"] = 1
    variants = ("moonreaver_skeleton", "moonreaver_skeleton_swordsman", "moonreaver_skeleton_captain")
    for mob in variants:
        table = copy.deepcopy(loot)
        table["random_sequence"] = "theaurorian2:entities/" + mob
        resources[f"data/theaurorian2/loot_table/entities/{mob}.json"] = table
        resources[f"assets/theaurorian2/items/{mob}_spawn_egg.json"] = {
            "model": {"type": "minecraft:model", "model": "minecraft:item/skeleton_spawn_egg"}}
    for tag in ("undead", "skeletons"):
        path = f"data/minecraft/tags/entity_type/{tag}.json"
        existing = ROOT / "src/main/resources" / path
        data = json.loads(existing.read_text(encoding="utf-8-sig")) if existing.exists() else {"replace": False, "values": []}
        for mob in variants:
            value = "theaurorian2:" + mob
            if value not in data["values"]:
                data["values"].append(value)
        resources[path] = data
    resources["assets/theaurorian2/items/silent_wood_bow.json"] = json.loads(
        json.dumps(bow).replace("minecraft:item/bow", "theaurorian2:item/silent_wood_bow"))
    for suffix in ("", "_pulling_0", "_pulling_1", "_pulling_2"):
        name = "silent_wood_bow" + suffix
        resources[f"assets/theaurorian2/models/item/{name}.json"] = {
            "parent": "minecraft:item/bow", "textures": {"layer0": "theaurorian2:item/" + name}}
    for tag in ("enchantable/bow", "enchantable/durability"):
        path = f"data/minecraft/tags/item/{tag}.json"
        existing = ROOT / "src/main/resources" / path
        data = json.loads(existing.read_text(encoding="utf-8-sig")) if existing.exists() else {"replace": False, "values": []}
        if "theaurorian2:silent_wood_bow" not in data["values"]:
            data["values"].append("theaurorian2:silent_wood_bow")
        resources[path] = data
    for language, names in {
        "zh_cn": ("侵月骸兵·弓手", "侵月骸兵·剑士", "侵月骸兵队长", "刷怪蛋"),
        "en_us": ("Moonreaver Archer", "Moonreaver Swordsman", "Moonreaver Captain", " Spawn Egg"),
    }.items():
        path = f"assets/theaurorian2/lang/{language}.json"
        data = json.loads((ROOT / "src/main/resources" / path).read_text(encoding="utf-8-sig"))
        for mob, name in zip(variants, names[:3]):
            data[f"entity.theaurorian2.{mob}"] = name
            data[f"item.theaurorian2.{mob}_spawn_egg"] = name + names[3]
        resources[path] = data
    write_resources(resources, texture)


def write_resources(resources, texture):
    with tempfile.TemporaryDirectory(prefix="skeleton-import-") as temp:
        data_path = Path(temp) / "resources.json"
        data_path.write_text(json.dumps(resources, ensure_ascii=False), encoding="utf-8")
        script = Path(temp) / "write.ps1"
        script.write_text("param($Root,$Data)\n. (Join-Path $Root 'scripts/json_utils.ps1')\n"
                          "$entries=Get-Content -LiteralPath $Data -Raw | ConvertFrom-Json\n"
                          "foreach($entry in $entries.PSObject.Properties){ Write-Json (Join-Path $Root ('src/main/resources/'+$entry.Name)) $entry.Value }\n", encoding="utf-8-sig")
        subprocess.run(["pwsh", "-NoProfile", "-File", str(script), str(ROOT), str(data_path)], check=True)
    dest = ROOT / "src/main/resources/assets/theaurorian2/textures/entity/moonreaver_skeleton.png"
    dest.write_bytes(texture)
    print(f"Validated source geometry and UVs; copied original texture and wrote {len(resources)} JSON resources.")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("archive", type=Path)
    parser.add_argument("--model-only", action="store_true", help="Update geometry, animations and texture without regenerating gameplay data.")
    args = parser.parse_args()
    import_resources(args.archive, args.model_only)
