package meshi.util;

public interface Attributable {
    void addAttribute(MeshiAttribute attribute);
    MeshiAttribute getAttribute(int key);
}
