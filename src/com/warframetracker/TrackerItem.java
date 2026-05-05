package com.warframetracker;

import java.util.ArrayList;
import java.util.List;

public class TrackerItem {
    public enum Category { WARFRAME, PRIMARY, SECONDARY, MELEE, SENTINEL_WEAPON, ARCH_GUN, ARCH_MELEE }
    public enum Status   { WANT, FARMING, OWNED }

    public static class Part {
        private String  name;
        private boolean obtained;

        public Part() {}
        public Part(String name, boolean obtained) {
            this.name     = name;
            this.obtained = obtained;
        }

        public String  getName()              { return name; }
        public void    setName(String name)   { this.name = name; }
        public boolean isObtained()           { return obtained; }
        public void    setObtained(boolean o) { this.obtained = o; }

        @Override public String toString() { return name; }
    }

    private String     name;
    private Category   category;
    private Status     status;
    private String     notes;
    private List<Part> parts = new ArrayList<>();

    public TrackerItem() {}
    public TrackerItem(String name, Category category, Status status, String notes) {
        this.name     = name;
        this.category = category;
        this.status   = status;
        this.notes    = notes == null ? "" : notes;
    }

    public String     getName()                  { return name; }
    public void       setName(String name)       { this.name = name; }
    public Category   getCategory()              { return category; }
    public void       setCategory(Category c)    { this.category = c; }
    public Status     getStatus()                { return status; }
    public void       setStatus(Status s)        { this.status = s; }
    public String     getNotes()                 { return notes; }
    public void       setNotes(String notes)     { this.notes = notes == null ? "" : notes; }
    public List<Part> getParts()                 { return parts; }
    public void       setParts(List<Part> p)     { this.parts = p == null ? new ArrayList<>() : p; }

    public long   obtainedCount()  { return parts.stream().filter(Part::isObtained).count(); }
    public String partsProgress()  { return parts.isEmpty() ? "" : obtainedCount() + " / " + parts.size(); }

    @Override public String toString() { return name; }
}
