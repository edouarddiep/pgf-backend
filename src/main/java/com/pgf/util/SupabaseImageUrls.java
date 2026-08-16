package com.pgf.util;

import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class SupabaseImageUrls {

    private static final String OBJECT_PATH = "/storage/v1/object/public/";
    private static final String RENDER_PATH = "/storage/v1/render/image/public/";
    private static final List<Integer> WIDTHS = List.of(400, 800, 1200, 1600);
    private static final List<String> IMAGE_EXTENSIONS = List.of(".jpg", ".jpeg", ".png", ".webp", ".avif");
    private static final int QUALITY = 75;

    private SupabaseImageUrls() {
    }

    public static String srcSet(String imageUrl) {
        if (!isTransformable(imageUrl)) {
            return null;
        }
        String renderUrl = imageUrl.replace(OBJECT_PATH, RENDER_PATH);
        return WIDTHS.stream()
                .map(width -> "%s?width=%d&quality=%d %dw".formatted(renderUrl, width, QUALITY, width))
                .collect(Collectors.joining(", "));
    }

    public static List<String> srcSets(List<String> imageUrls) {
        if (imageUrls == null) {
            return null;
        }
        return imageUrls.stream().map(SupabaseImageUrls::srcSet).toList();
    }

    public static String resized(String imageUrl, int width) {
        if (!isTransformable(imageUrl)) {
            return imageUrl;
        }
        return "%s?width=%d&quality=%d".formatted(imageUrl.replace(OBJECT_PATH, RENDER_PATH), width, QUALITY);
    }

    private static boolean isTransformable(String imageUrl) {
        if (!StringUtils.hasText(imageUrl) || !imageUrl.contains(OBJECT_PATH)) {
            return false;
        }
        String lowerCaseUrl = imageUrl.toLowerCase(Locale.ROOT);
        return IMAGE_EXTENSIONS.stream().anyMatch(lowerCaseUrl::endsWith);
    }
}
