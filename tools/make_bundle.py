#!/usr/bin/env python3
"""Build the Orange Client launcher bundle with the requested 1.21.11 mod set.

The upstream jars are resolved from Modrinth, then their user-visible branding is
patched before they are published as release assets. Internal package/class names,
license files and author attributions are intentionally left intact.
"""
import hashlib
import io
import json
import struct
import sys
import urllib.parse
import urllib.request
import zipfile
from pathlib import Path

MC_VERSION = "1.21.11"
USER_AGENT = "OrangeClient-bundle-builder/1.1"

# Exact versions from the supplied 1.21.11 mod collection.
MODS = {
    "oneconfig": ("1.9.5", "mods/orange-config.jar", "OrangeConfig"),
    "polydium": ("1.4.23", "mods/orange-performance.jar", "OrangePerformance"),
    "polyblur": ("2.2.11", "mods/orange-blur.jar", "OrangeBlur"),
    "polycrosshair": ("2.0.11", "mods/orange-crosshair.jar", "OrangeCrosshair"),
    "polyhitbox": ("1.4.5", "mods/orange-hitbox.jar", "OrangeHitbox"),
    "polysprint": ("1.3.5", "mods/orange-sprint.jar", "OrangeSprint"),
    "polytime": ("1.3.4", "mods/orange-time.jar", "OrangeTime"),
    "polyweather": ("1.3.3", "mods/orange-weather.jar", "OrangeWeather"),
    "chatting": ("3.2.7", "mods/chatting.jar", "Chatting"),
    "chattweaks": ("1.2.4", "mods/chat-tweaks.jar", "ChatTweaks"),
    "vanillahud": ("3.5.20", "mods/orange-hud.jar", "OrangeHUD"),
    "overflowanimations": ("5.1.13", "mods/overflow-animations.jar", "OverflowAnimations"),
    "overflowparticles": ("1.2.7", "mods/overflow-particles.jar", "OverflowParticles"),
    "crashpatch": ("2.3.0", "mods/crash-patch.jar", "CrashPatch"),
    "behindyou": ("3.5.2", "mods/behind-you.jar", "BehindYouV3"),
    "colorsaturation": ("1.3.4", "mods/color-saturation.jar", "ColorSaturation"),
    "damagetint": ("3.7.4", "mods/damage-tint.jar", "DamageTint"),
    "soundtweaks": ("1.0.2", "mods/sound-tweaks.jar", "SoundTweaks"),
}

# Required libraries for the supplied set.
LIBS = {
    "polycompose": ("3.0.7", "mods/polycompose.jar", "PolyCompose"),
    "fabric-language-kotlin": (None, "mods/fabric-language-kotlin.jar", "Fabric Language Kotlin"),
    "notenoughcrashes": ("4.4.9", "mods/not-enough-crashes.jar", "Not Enough Crashes"),
}

def get_json(url):
    req = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(req, timeout=90) as response:
        return json.load(response)

def get_bytes(url):
    req = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(req, timeout=120) as response:
        return response.read()

def sha1_of(path):
    digest = hashlib.sha1()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1 << 20), b""):
            digest.update(chunk)
    return digest.hexdigest()

def resolve_version(slug, requested):
    # Fetch the recent version list without the game-version filter: some
    # Modrinth releases have compatibility metadata split across loaders/
    # game_versions and can be missed by the filtered endpoint.
    versions = get_json(f"https://api.modrinth.com/v2/project/{slug}/version?limit=100")
    releases = [v for v in versions if v.get("version_type") == "release"]
    pool = releases or versions
    if requested:
        exact = [v for v in pool if v.get("version_number") == requested]
        if not exact:
            raise LookupError(f"no {slug} version {requested} on Modrinth")
        compatible = [
            v for v in exact
            if MC_VERSION in v.get("game_versions", [])
            and "fabric" in v.get("loaders", [])
        ]
        pool = compatible or exact
    else:
        pool = [
            v for v in pool
            if MC_VERSION in v.get("game_versions", [])
            and "fabric" in v.get("loaders", [])
        ]
    if not pool:
        raise LookupError(f"no compatible {slug} version {requested or 'latest'} for {MC_VERSION}")
    return pool[0]

def modrinth_file(slug, requested):
    version = resolve_version(slug, requested)
    primary = next((f for f in version["files"] if f.get("primary")), version["files"][0])
    url = primary["url"]
    print(f"  {slug}: {version['version_number']} ({primary['filename']})")
    return get_bytes(url), version["version_number"]

def patch_class_strings(data):
    if data[:4] != b"\\xca\\xfe\\xba\\xbe":
        return data
    cp_count = struct.unpack_from(">H", data, 8)[0]
    pos = 10
    out = bytearray(data[:10])
    i = 1
    while i < cp_count:
        tag = data[pos]
        out.append(tag)
        pos += 1
        if tag == 1:
            length = struct.unpack_from(">H", data, pos)[0]
            raw = data[pos + 2:pos + 2 + length]
            pos += 2 + length
            new = (raw
                .replace(b"OneClient", b"OrangeClient")
                .replace(b"One Client", b"Orange Client")
                .replace(b"Polyfrosted Dark", b"Orange Dark")
                .replace(b"Polyfrosted Light", b"Orange Light")
                .replace(b"Poly Glass", b"Orange Glass"))
            out += struct.pack(">H", len(new)) + new
        else:
            sizes = {3:4, 4:4, 5:8, 6:8, 7:2, 8:2, 9:4, 10:4,
                     11:4, 12:4, 15:3, 16:2, 17:4, 18:4, 19:2, 20:2}
            size = sizes.get(tag)
            if size is None:
                return data
            out += data[pos:pos + size]
            pos += size
        if tag in (5, 6):
            i += 1
        i += 1
    out += data[pos:]
    return bytes(out)

def patch_zip(data, display_name, badge=None):
    source = zipfile.ZipFile(io.BytesIO(data))
    out_io = io.BytesIO()
    with zipfile.ZipFile(out_io, "w", zipfile.ZIP_DEFLATED) as out:
        for info in source.infolist():
            raw = source.read(info.filename)
            name = info.filename
            lower = name.lower()

            if lower.endswith(".jar"):
                try:
                    raw = patch_zip(raw, display_name, badge)
                except zipfile.BadZipFile:
                    pass
            elif lower.endswith(".class"):
                raw = patch_class_strings(raw)
            elif lower.endswith("fabric.mod.json"):
                try:
                    meta = json.loads(raw.decode("utf-8"))
                    meta["name"] = display_name
                    if isinstance(meta.get("description"), str):
                        meta["description"] = meta["description"].replace("OneClient", "OrangeClient").replace("One Client", "Orange Client")
                    raw = (json.dumps(meta, ensure_ascii=False, indent=2) + "\n").encode("utf-8")
                except Exception:
                    pass
            elif lower.endswith((".lang", ".json", ".txt", ".xml", ".svg", ".toml")) and "license" not in lower:
                raw = raw.replace(b"OneClient", b"OrangeClient").replace(b"One Client", b"Orange Client")
            if badge is not None and lower.endswith("oneconfig-icon.png"):
                raw = badge

            out.writestr(info, raw)
    return out_io.getvalue()

def read_property(name):
    for line in Path("gradle.properties").read_text(encoding="utf-8").splitlines():
        if line.startswith(name + "="):
            return line.split("=", 1)[1].strip()
    raise SystemExit(f"gradle.properties has no {name}")

def add_external(files, out_dir, base_url, slug, requested, path, display_name, badge=None):
    raw, actual = modrinth_file(slug, requested)
    patched = patch_zip(raw, display_name, badge)
    filename = Path(path).name
    target = out_dir / filename
    target.write_bytes(patched)
    files.append({
        "path": path,
        "hashes": {"sha1": hashlib.sha1(patched).hexdigest()},
        "downloads": [f"{base_url}/{filename}"],
        "fileSize": len(patched),
        "enabled": True,
        "hidden": False,
    })
    return actual

def main():
    if len(sys.argv) < 4:
        raise SystemExit(__doc__)
    jar = Path(sys.argv[1])
    base_url = sys.argv[2].rstrip("/")
    out_dir = Path(sys.argv[3])
    version_id = sys.argv[4] if len(sys.argv) > 4 else read_property("mod_version")
    out_dir.mkdir(parents=True, exist_ok=True)

    badge = None
    try:
        badge_url = "https://raw.githubusercontent.com/u2803045005abc09734-byte/orange-client-2.02/main/src/main/resources/assets/orange/orange_badge.png.b64"
        badge = __import__("base64").b64decode(get_bytes(badge_url))
    except Exception as error:
        print(f"  Orange badge: skipped ({error})")

    files = []
    versions = {}
    print("Resolving and patching requested OrangeClient mods...")
    for slug, (requested, path, display_name) in MODS.items():
        versions[slug] = add_external(files, out_dir, base_url, slug, requested, path, display_name, badge)

    print("Resolving required libraries...")
    for slug, (requested, path, display_name) in LIBS.items():
        versions[slug] = add_external(files, out_dir, base_url, slug, requested, path, display_name)

    # The Orange Client mod itself is the first-party client and is hidden from Mod Menu.
    orange_name = "orange-client.jar"
    files.append({
        "path": "mods/orange-client.jar",
        "hashes": {"sha1": sha1_of(jar)},
        "downloads": [f"{base_url}/{orange_name}"],
        "fileSize": jar.stat().st_size,
        "enabled": True,
        "hidden": True,
        "overrides": {
            "name": "Orange Client",
            "description": "Orange Client in-game mod with the Orange GUI, HUD editor and community capes.",
            "authors": ["OrangeClient"],
        },
    })

    index = {
        "formatVersion": 1,
        "game": "minecraft",
        "versionId": version_id,
        "name": "Orange Client",
        "summary": "Orange Client with the requested PvP, HUD, visual and performance mods.",
        "category": "Orange Client",
        "enabled": True,
        "dependencies": {
            "minecraft": MC_VERSION,
            "fabric-loader": read_property("loader_version"),
        },
        "files": files,
    }

    mrpack = out_dir / "orange-client.mrpack"
    with zipfile.ZipFile(mrpack, "w", zipfile.ZIP_DEFLATED) as archive:
        archive.writestr("modrinth.index.json", json.dumps(index, indent=2))

    metadata = {
        "versions": {
            MC_VERSION: {
                "fabric": [{"path": "/orange-client.mrpack", "sha1": sha1_of(mrpack)}]
            }
        }
    }
    (out_dir / "metadata.json").write_text(json.dumps(metadata, indent=2), encoding="utf-8")
    (out_dir / orange_name).write_bytes(jar.read_bytes())
    print(f"Wrote {mrpack}, patched mod assets and metadata for Minecraft {MC_VERSION}.")

if __name__ == "__main__":
    main()
