package dev.rinchan.skillrespecpill.graph;

import dev.rinchan.skillrespecpill.mixin.SkillsModAccessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.config.CategoryConfig;

public final class PuffishGraphSource {
    private PuffishGraphSource() {
    }

    public static CategoryConfig categoryConfig(Identifier categoryId) {
        var listener = ((SkillsModAccessor) SkillsMod.getInstance()).skillRespecPill$getCategories();
        var categories = listener.get();
        if (categories.isEmpty()) throw new GraphException("Puffish category configuration is unavailable");
        CategoryConfig category = categories.get().get(categoryId);
        if (category == null) throw new GraphException("missing Puffish category graph: " + categoryId);
        return category;
    }

    public static SkillGraph extract(CategoryConfig category) {
        var costs = new HashMap<String, Integer>();
        var prerequisites = new HashMap<String, ArrayList<String>>();
        for (var skill : category.skills().getAll()) {
            var definition = category.definitions().getById(skill.definitionId()).orElseThrow(() ->
                    new GraphException("missing definition " + skill.definitionId() + " for node " + skill.id()));
            if (costs.put(skill.id(), definition.cost()) != null) {
                throw new GraphException("duplicate Puffish node id: " + skill.id());
            }
            prerequisites.put(skill.id(), new ArrayList<>());
        }
        for (var connection : category.connections().normal().getAll()) {
            addEdge(prerequisites, connection.skillAId(), connection.skillBId());
            if (connection.bidirectional()) {
                addEdge(prerequisites, connection.skillBId(), connection.skillAId());
            }
        }
        return SkillGraph.of(costs, prerequisites);
    }

    private static void addEdge(
            Map<String, ArrayList<String>> prerequisites,
            String prerequisite,
            String dependent) {
        if (!prerequisites.containsKey(prerequisite) || !prerequisites.containsKey(dependent)) {
            throw new GraphException("connection references missing node: " + prerequisite + " -> " + dependent);
        }
        prerequisites.get(dependent).add(prerequisite);
    }
}
