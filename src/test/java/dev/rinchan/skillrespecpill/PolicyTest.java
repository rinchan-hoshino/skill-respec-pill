package dev.rinchan.skillrespecpill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.rinchan.skillrespecpill.policy.SkillPolicy;
import java.io.StringReader;
import java.util.Set;
import org.junit.jupiter.api.Test;

final class PolicyTest {
    @Test
    void acceptsOnlyCategoryDefaultsAndForcedIds() {
        var policy = SkillPolicy.parse(new StringReader("""
                {
                  "category": "example:tree",
                  "default_enabled": ["new_default", "old_default"],
                  "forced_enabled": ["root"]
                }
                """));

        assertEquals("example:tree", policy.category());
        assertEquals(Set.of("new_default", "old_default"), policy.defaultEnabled());
        assertEquals(Set.of("root"), policy.forcedEnabled());
    }

    @Test
    void rejectsMissingCategoryAndEveryForbiddenPolicyField() {
        assertThrows(IllegalArgumentException.class, () -> SkillPolicy.parse(new StringReader("{}")));
        for (String field : Set.of(
                "wmf_ids", "costs", "subsidy", "revision", "migration", "attributes",
                "bed", "spawn", "chat_policy", "dependencies")) {
            String json = "{\"category\":\"example:tree\",\"" + field + "\":{}}";
            assertThrows(IllegalArgumentException.class, () -> SkillPolicy.parse(new StringReader(json)), field);
        }
    }
}
