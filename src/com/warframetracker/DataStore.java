package com.warframetracker;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Lightweight JSON persistence — no external libraries required.
 * Supports TrackerItem with nested Part list.
 */
public class DataStore {

    private static final Path DATA_DIR  = Paths.get(System.getProperty("user.home"), ".warframe-tracker");
    private static final Path DATA_FILE = DATA_DIR.resolve("data.json");

    // ─────────────────────────────────────────────────────────────────────
    //  Save
    // ─────────────────────────────────────────────────────────────────────
    public static void save(List<TrackerItem> items) throws IOException {
        Files.createDirectories(DATA_DIR);
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < items.size(); i++) {
            TrackerItem it = items.get(i);
            sb.append("  {\n");
            sb.append("    \"name\": ")     .append(jsonStr(it.getName()))            .append(",\n");
            sb.append("    \"category\": ").append(jsonStr(it.getCategory().name()))  .append(",\n");
            sb.append("    \"status\": ")  .append(jsonStr(it.getStatus().name()))    .append(",\n");
            sb.append("    \"notes\": ")   .append(jsonStr(it.getNotes()))            .append(",\n");
            sb.append("    \"parts\": ");
            writeParts(sb, it.getParts());
            sb.append("\n  }");
            if (i < items.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]");
        Files.writeString(DATA_FILE, sb.toString());
    }

    private static void writeParts(StringBuilder sb, List<TrackerItem.Part> parts) {
        sb.append("[\n");
        for (int i = 0; i < parts.size(); i++) {
            TrackerItem.Part p = parts.get(i);
            sb.append("      { \"partName\": ").append(jsonStr(p.getName()))
              .append(", \"obtained\": ").append(p.isObtained()).append(" }");
            if (i < parts.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("    ]");
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Load
    // ─────────────────────────────────────────────────────────────────────
    public static List<TrackerItem> load() throws IOException {
        List<TrackerItem> items = new ArrayList<>();
        if (!Files.exists(DATA_FILE)) return items;

        String json = Files.readString(DATA_FILE).trim();
        if (json.isEmpty() || json.equals("[]")) return items;

        // Strip outer array brackets
        json = json.substring(1, json.lastIndexOf(']')).trim();

        for (String block : splitObjects(json)) {
            try {
                TrackerItem item = parseItem(block.trim());
                if (item != null) items.add(item);
            } catch (Exception ignored) {}
        }
        return items;
    }

    private static TrackerItem parseItem(String block) {
        String name     = extractStringField(block, "name");
        String category = extractStringField(block, "category");
        String status   = extractStringField(block, "status");
        String notes    = extractStringField(block, "notes");

        if (name == null || category == null || status == null) return null;
        try {
            TrackerItem.Category cat  = TrackerItem.Category.valueOf(category);
            TrackerItem.Status   stat = TrackerItem.Status.valueOf(status);
            TrackerItem item = new TrackerItem(name, cat, stat, notes == null ? "" : notes);
            item.setParts(parseParts(block));
            return item;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static List<TrackerItem.Part> parseParts(String block) {
        List<TrackerItem.Part> parts = new ArrayList<>();
        // Find the "parts" array inside the outer object
        String key = "\"parts\"";
        int idx = block.indexOf(key);
        if (idx < 0) return parts;
        int arrStart = block.indexOf('[', idx + key.length());
        if (arrStart < 0) return parts;
        int arrEnd = block.indexOf(']', arrStart);
        if (arrEnd < 0) return parts;
        String arrContent = block.substring(arrStart + 1, arrEnd).trim();
        if (arrContent.isEmpty()) return parts;

        for (String obj : splitObjects(arrContent)) {
            String partName = extractStringField(obj, "partName");
            String obtained = extractBoolField(obj, "obtained");
            if (partName != null) {
                parts.add(new TrackerItem.Part(partName, "true".equals(obtained)));
            }
        }
        return parts;
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Parsing helpers
    // ─────────────────────────────────────────────────────────────────────
    private static List<String> splitObjects(String json) {
        List<String> blocks = new ArrayList<>();
        int depth = 0, start = -1;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if      (c == '{') { if (depth++ == 0) start = i; }
            else if (c == '}') { if (--depth == 0 && start >= 0) { blocks.add(json.substring(start, i + 1)); start = -1; } }
        }
        return blocks;
    }

    private static String extractStringField(String json, String field) {
        String key = "\"" + field + "\"";
        int idx = json.indexOf(key);
        if (idx < 0) return null;
        int colon = json.indexOf(':', idx + key.length());
        if (colon < 0) return null;
        int start = json.indexOf('"', colon + 1);
        if (start < 0) return null;
        int end = start + 1;
        while (end < json.length()) {
            char c = json.charAt(end);
            if (c == '\\') { end += 2; continue; }
            if (c == '"') break;
            end++;
        }
        return json.substring(start + 1, end)
                   .replace("\\\"", "\"").replace("\\\\", "\\")
                   .replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t");
    }

    /** Extracts a bare boolean value (not quoted). */
    private static String extractBoolField(String json, String field) {
        String key = "\"" + field + "\"";
        int idx = json.indexOf(key);
        if (idx < 0) return null;
        int colon = json.indexOf(':', idx + key.length());
        if (colon < 0) return null;
        // skip whitespace after colon
        int start = colon + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
        int end = start;
        while (end < json.length() && (Character.isLetter(json.charAt(end)))) end++;
        return json.substring(start, end).trim();
    }

    private static String jsonStr(String s) {
        if (s == null) return "\"\"";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"")
                       .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t") + "\"";
    }

    public static Path getDataFilePath() { return DATA_FILE; }
}
