#!/usr/bin/env python3
"""Build the Orange Client launcher bundle (.mrpack) and its catalog (metadata.json).

The Orange Client launcher (OrangeLauncher3) reads a "PolyMrpack" bundle: a zip with a
modrinth.index.json. Files hosted on cdn.modrinth.com become Modrinth-managed mods; any
other download URL is treated as an external file that is verified by SHA-1.

Usage: make_bundle.py <orange-client.jar> <release-download-base-url> <out-dir> [version-id]
"""
import hashlib
import json
import sys
import urllib.parse
import urllib.request
import zipfile
from pathlib import Path

MC_VERSION = "1.21.11"
USER_AGENT = "OrangeClient-bundle-builder/1.0 (github.com/u2803045005abc09734-byte)"

# slug -> (required, path in the instance)
MODRINTH_MODS = {
    "fabric-api": (True, "mods/fabric-api.jar"),
    "sodium": (False, "mods/sodium.jar"),
    "lithium": (False, "mods/lithium.jar"),
    "ferrite-core": (False, "mods/ferrite-core.jar"),
    # Open-source extras also found in other clients' bundles. All optional: a mod without a
    # release for this Minecraft version is skipped instead of failing the build.
    "modmenu": (False, "mods/modmenu.jar"),
    "simple-voice-chat": (False, "mods/simple-voice-chat.jar"),
    "skinlayers3d": (False, "mods/skinlayers3d.jar"),
    "lambdynamiclights": (False, "mods/lambdynamiclights.jar"),
}


def get_json(url: str):
    req = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(req, timeout=60) as response:
        return json.load(response)


def sha1_of(path: Path) -> str:
    digest = hashlib.sha1()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1 << 20), b""):
            digest.update(chunk)
    return digest.hexdigest()


def read_property(name: str) -> str:
    for line in Path("gradle.properties").read_text(encoding="utf-8").splitlines():
        if line.startswith(name + "="):
            return line.split("=", 1)[1].strip()
    raise SystemExit(f"gradle.properties has no {name}")


def modrinth_file(slug: str, path: str) -> dict:
    query = urllib.parse.urlencode(
        {"game_versions": json.dumps([MC_VERSION]), "loaders": json.dumps(["fabric"])}
    )
    versions = get_json(f"https://api.modrinth.com/v2/project/{slug}/version?{query}")
    if not versions:
        raise LookupError(f"no {slug} version for Minecraft {MC_VERSION} on Fabric")
    releases = [v for v in versions if v.get("version_type") == "release"]
    version = (releases or versions)[0]
    files = version["files"]
    primary = next((f for f in files if f.get("primary")), files[0])
    url = primary["url"]
    if not url.startswith("https://cdn.modrinth.com/data/"):
        raise ValueError(f"unexpected Modrinth URL for {slug}: {url}")
    print(f"  {slug}: {version['version_number']} ({primary['filename']})")
    return {
        "path": path,
        "hashes": {"sha1": primary["hashes"]["sha1"]},
        "downloads": [url],
        "fileSize": primary["size"],
        "enabled": True,
        "hidden": slug == "fabric-api",
    }


def main() -> None:
    if len(sys.argv) < 4:
        raise SystemExit(__doc__)
    jar = Path(sys.argv[1])
    base_url = sys.argv[2].rstrip("/")
    out_dir = Path(sys.argv[3])
    version_id = sys.argv[4] if len(sys.argv) > 4 else read_property("mod_version")
    if not jar.is_file():
        raise SystemExit(f"mod jar not found: {jar}")
    out_dir.mkdir(parents=True, exist_ok=True)

    print("Resolving Modrinth dependencies...")
    files = []
    for slug, (required, path) in MODRINTH_MODS.items():
        try:
            files.append(modrinth_file(slug, path))
        except Exception as error:  # noqa: BLE001 - a missing optional mod must not fail the build
            if required:
                raise SystemExit(f"required mod {slug} could not be resolved: {error}")
            print(f"  {slug}: skipped ({error})")

    # The Orange Client mod itself is an external file hosted as a release asset.
    files.append(
        {
            "path": "mods/orange-client.jar",
            "hashes": {"sha1": sha1_of(jar)},
            "downloads": [f"{base_url}/orange-client.jar"],
            "fileSize": jar.stat().st_size,
            "enabled": True,
            "hidden": True,
            "overrides": {
                "name": "Orange Client",
                "description": "The Orange Client in-game mod: menus, HUD editor and free community capes.",
                "authors": ["CleanOrange"],
            },
        }
    )

    index = {
        "formatVersion": 1,
        "game": "minecraft",
        "versionId": version_id,
        "name": "Orange Client",
        "summary": "Orange Client with performance mods",
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
            MC_VERSION: {"fabric": [{"path": "/orange-client.mrpack", "sha1": sha1_of(mrpack)}]}
        }
    }
    (out_dir / "metadata.json").write_text(json.dumps(metadata, indent=2), encoding="utf-8")
    (out_dir / "orange-client.jar").write_bytes(jar.read_bytes())
    print(f"Wrote {mrpack} and metadata.json for Minecraft {MC_VERSION} (version {version_id}).")


if __name__ == "__main__":
    main()
