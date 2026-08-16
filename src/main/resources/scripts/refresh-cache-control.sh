#!/bin/bash

# Ré-applique l'en-tête cache-control sur les fichiers déjà présents dans le bucket Supabase.
# Les objets uploadés avant cette évolution portent le défaut Supabase (max-age=3600).
# Le contenu et les URLs sont inchangés : seules les métadonnées sont réécrites.
#
# Usage:
#   export SUPABASE_URL="https://xxxx.supabase.co"
#   export SUPABASE_SERVICE_KEY="eyJ..."
#   ./refresh-cache-control.sh              # simulation (aucune écriture)
#   ./refresh-cache-control.sh --apply      # exécution
#   ./refresh-cache-control.sh --apply --immutable
#   ./refresh-cache-control.sh --apply --include-large   # inclut les médias volumineux

set -euo pipefail

BUCKET="${SUPABASE_BUCKET:-oeuvres}"
MAX_AGE="${CACHE_CONTROL_MAX_AGE:-2592000}"
PAGE_SIZE=100
MAX_BYTES="${MAX_FILE_BYTES:-26214400}"

APPLY=false
IMMUTABLE=false
INCLUDE_LARGE=false

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

for arg in "$@"; do
    case "$arg" in
        --apply) APPLY=true ;;
        --immutable) IMMUTABLE=true ;;
        --include-large) INCLUDE_LARGE=true ;;
        *) echo -e "${RED}Argument inconnu: $arg${NC}"; exit 1 ;;
    esac
done

if [ -z "${SUPABASE_URL:-}" ] || [ -z "${SUPABASE_SERVICE_KEY:-}" ]; then
    echo -e "${RED}SUPABASE_URL et SUPABASE_SERVICE_KEY doivent être définis.${NC}"
    exit 1
fi

if [ "$IMMUTABLE" = true ]; then
    LONG_CACHE="max-age=31536000, immutable"
else
    LONG_CACHE="max-age=${MAX_AGE}"
fi
SHORT_CACHE="max-age=3600"

WORK_DIR=$(mktemp -d)
INVENTORY="$WORK_DIR/inventory.tsv"
PAYLOAD="$WORK_DIR/payload.bin"
trap 'rm -rf "$WORK_DIR"' EXIT

encode_path() {
    jq -rn --arg value "$1" '$value | split("/") | map(@uri) | join("/")'
}

# Les chemins à nom fixe sont réécrits par l'admin (x-upsert) : ils gardent un cache court.
target_cache_control() {
    local path="$1"
    if [[ "$path" =~ /videos/ ]] || [[ "$path" =~ ^expositions/[^/]+/images/image-[0-9]+\. ]]; then
        echo "$SHORT_CACHE"
    else
        echo "$LONG_CACHE"
    fi
}

list_page() {
    local prefix="$1" offset="$2"
    curl -sS -X POST "$SUPABASE_URL/storage/v1/object/list/$BUCKET" \
        -H "Authorization: Bearer $SUPABASE_SERVICE_KEY" \
        -H "Content-Type: application/json" \
        -d "$(jq -nc --arg prefix "$prefix" --argjson limit "$PAGE_SIZE" --argjson offset "$offset" \
              '{prefix: $prefix, limit: $limit, offset: $offset, sortBy: {column: "name", order: "asc"}}')"
}

walk() {
    local prefix="$1" offset=0 page count
    while true; do
        page=$(list_page "$prefix" "$offset")
        count=$(echo "$page" | jq 'length')
        [ "$count" -eq 0 ] && break

        while IFS=$'\t' read -r name kind size mimetype cache; do
            [ -z "$name" ] && continue
            [[ "$name" == .* ]] && continue

            local child
            if [ -z "$prefix" ]; then child="$name"; else child="$prefix/$name"; fi

            if [ "$kind" = "folder" ]; then
                walk "$child"
            else
                printf '%s\t%s\t%s\t%s\n' "$child" "$size" "$mimetype" "$cache" >> "$INVENTORY"
            fi
        done < <(echo "$page" | jq -r '.[] | [
                    .name,
                    (if .id == null then "folder" else "file" end),
                    ((.metadata.size // 0) | tostring),
                    (.metadata.mimetype // ""),
                    (.metadata.cacheControl // "")
                 ] | @tsv')

        [ "$count" -lt "$PAGE_SIZE" ] && break
        offset=$((offset + PAGE_SIZE))
    done
}

file_size() {
    if stat -f%z "$1" >/dev/null 2>&1; then stat -f%z "$1"; else stat -c%s "$1"; fi
}

echo -e "${YELLOW}=== Rafraîchissement du cache-control — bucket '$BUCKET' ===${NC}"
echo -e "Cible fichiers stables    : ${BLUE}public, $LONG_CACHE${NC}"
echo -e "Cible fichiers réécrits   : ${BLUE}public, $SHORT_CACHE${NC}"
if [ "$APPLY" = true ]; then
    echo -e "Mode                      : ${RED}ÉCRITURE${NC}"
else
    echo -e "Mode                      : ${GREEN}simulation${NC} (ajouter --apply pour exécuter)"
fi
echo ""

: > "$INVENTORY"
echo -e "${YELLOW}Inventaire du bucket...${NC}"
walk ""
total=$(wc -l < "$INVENTORY" | tr -d ' ')
echo -e "${GREEN}$total fichier(s) trouvé(s)${NC}"
echo ""

updated=0
skipped=0
skipped_large=0
failed=0
bytes_total=0

while IFS=$'\t' read -r path size mimetype cache; do
    target=$(target_cache_control "$path")

    if [ "$cache" = "$target" ]; then
        skipped=$((skipped + 1))
        continue
    fi

    if [ "$INCLUDE_LARGE" = false ] && [ "$size" -gt "$MAX_BYTES" ]; then
        skipped_large=$((skipped_large + 1))
        continue
    fi

    if [ "$APPLY" = false ]; then
        echo -e "  ${BLUE}à mettre à jour${NC} $path  (${cache:-aucun} -> $target)"
        updated=$((updated + 1))
        bytes_total=$((bytes_total + size))
        continue
    fi

    encoded=$(encode_path "$path")
    object_url="$SUPABASE_URL/storage/v1/object/$BUCKET/$encoded"

    if ! curl -sS -f -H "Authorization: Bearer $SUPABASE_SERVICE_KEY" -o "$PAYLOAD" "$object_url"; then
        echo -e "  ${RED}échec téléchargement${NC} $path"
        failed=$((failed + 1))
        continue
    fi

    downloaded=$(file_size "$PAYLOAD")
    if [ "$downloaded" != "$size" ] || [ "$downloaded" -eq 0 ]; then
        echo -e "  ${RED}taille incohérente${NC} $path ($downloaded o attendus $size o) — ignoré"
        failed=$((failed + 1))
        continue
    fi

    status=$(curl -sS -o /dev/null -w "%{http_code}" -X POST "$object_url" \
        -H "Authorization: Bearer $SUPABASE_SERVICE_KEY" \
        -H "x-upsert: true" \
        -H "cache-control: $target" \
        -H "Content-Type: ${mimetype:-application/octet-stream}" \
        --data-binary "@$PAYLOAD")

    if [ "$status" = "200" ]; then
        echo -e "  ${GREEN}✓${NC} $path -> $target"
        updated=$((updated + 1))
        bytes_total=$((bytes_total + size))
    else
        echo -e "  ${RED}✗ HTTP $status${NC} $path"
        failed=$((failed + 1))
    fi
done < "$INVENTORY"

echo ""
echo -e "${YELLOW}=== Résumé ===${NC}"
echo -e "Mis à jour     : ${GREEN}$updated${NC}"
echo -e "Inchangés      : $skipped"
echo -e "Volume traité  : $((bytes_total / 1048576)) Mo"
if [ "$skipped_large" -gt 0 ]; then
    echo -e "Trop volumineux: ${YELLOW}$skipped_large${NC} (> $((MAX_BYTES / 1048576)) Mo, ajouter --include-large)"
fi
echo -e "Échecs         : ${RED}$failed${NC}"

if [ "$APPLY" = false ] && [ "$updated" -gt 0 ]; then
    echo ""
    echo -e "${YELLOW}Relancer avec --apply pour appliquer.${NC}"
fi

[ "$failed" -eq 0 ]
