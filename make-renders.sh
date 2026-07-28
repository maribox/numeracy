#!/usr/bin/env bash
# Draw every screen of the app and put the pictures where docs/model reads them.
#
#   ./make-renders.sh              every screen
#   ./make-renders.sh practice     one screen, which is the difference between
#                                  waiting for four minutes and waiting for twenty seconds
#
# The renderer paints the real composables off-screen with a written practice history, so a picture
# comes from the same code the app runs and cannot quietly stop matching it. Each screen is drawn
# upright and wide, light and dark.
set -euo pipefail

here="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
gallery="$here/composeApp/build/gallery"
model="$here/docs/model/img"
classpath="$here/composeApp/build/gallery-classpath.txt"

# Skiko loads libGL, X11 and fontconfig by soname, which on NixOS live in the store rather than in
# /usr/lib. The store paths carry a hash and change with every nixpkgs bump, so they are looked up
# by package name at run time; on a distribution with an ordinary /usr/lib nothing matches and the
# loader finds them itself.
if [ -d /nix/store ]; then
  libs=""
  for pattern in libglvnd libx11 libxcb libxau libxdmcp fontconfig freetype gcc; do
    for dir in /nix/store/*-"$pattern"-*/lib; do
      [ -d "$dir" ] || continue
      # A -dev output carries headers and a stub; the library itself is in the plain one.
      case "$dir" in */*-dev/lib) continue ;; esac
      libs="$libs:$dir"
    done
  done
  export LD_LIBRARY_PATH="${libs#:}${LD_LIBRARY_PATH:+:$LD_LIBRARY_PATH}"
fi

only="${1:-}"
echo "Compiling…"
"$here/gradlew" :composeApp:galleryClasspath -q --console=plain

# Compose draws one scene at a time in a process, and a scene is a few seconds of software
# rasterising, so the scenes are dealt out to as many processes as the machine has cores to spare.
workers=$(nproc 2>/dev/null || echo 4)
workers=$(( workers > 8 ? 8 : workers ))
[ -n "$only" ] && workers=1
echo "Rendering ${only:-every screen} in $workers processes…"

pids=""
for shard in $(seq 0 $((workers - 1))); do
  java \
    -Dgallery.out="$gallery" \
    -Dgallery.homes="$here/composeApp/build/gallery-home" \
    -Dgallery.shard="$shard" \
    -Dgallery.shards="$workers" \
    ${only:+-Dgallery.only="$only"} \
    -Djava.awt.headless=true \
    -Dskiko.renderApi=SOFTWARE \
    -cp "$(cat "$classpath")" \
    it.bosler.numeracy.gallery.GalleryKt &
  pids="$pids $!"
done
for pid in $pids; do wait "$pid"; done

mkdir -p "$model" "$model/small" "$model/card"
count=0
for source in "$gallery"/*.png; do
  [ -e "$source" ] || continue
  cp "$source" "$model/$(basename "$source")"
  count=$((count + 1))
done
# The book asks for a small copy where it shows one small and a card on the index; served the full
# picture instead, every thumbnail costs a quarter of a megabyte.
for sized in small card; do
  for source in "$gallery/$sized"/*.png; do
    [ -e "$source" ] || continue
    cp "$source" "$model/$sized/$(basename "$source")"
  done
done

echo "$count renders in docs/model/img, with small and card copies beside them"
