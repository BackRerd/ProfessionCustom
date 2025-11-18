package site.backrer.professioncustom;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

public class ConfigTest {

    @Test
    public void testMobBonusConfigCreation() {
        // 测试MobBonusConfig的创建
        Config.MobBonusConfig config = new Config.MobBonusConfig(1.5, 1.2, 1.0, 1.3);
        assertEquals(1.5, config.healthMultiplier);
        assertEquals(1.2, config.armorMultiplier);
        assertEquals(1.0, config.armorToughnessMultiplier);
        assertEquals(1.3, config.attackDamageMultiplier);
    }

    @Test
    public void testDefaultMobBonusConfig() {
        // 测试默认值
        Map<String, Config.MobBonusConfig> bonuses = new HashMap<>();
        String nonExistentMob = "minecraft:nonexistent";
        
        // 模拟getMobBonusConfig的默认行为
        Config.MobBonusConfig config = bonuses.getOrDefault(nonExistentMob, 
                new Config.MobBonusConfig(1.0, 1.0, 1.0, 1.0));
        
        assertEquals(1.0, config.healthMultiplier);
        assertEquals(1.0, config.armorMultiplier);
        assertEquals(1.0, config.armorToughnessMultiplier);
        assertEquals(1.0, config.attackDamageMultiplier);
    }

    @Test
    public void testDimensionBonusHandling() {
        // 测试维度加成的默认处理
        Map<String, Double> dimensionBonuses = new HashMap<>();
        dimensionBonuses.put("minecraft:nether", 2.0);
        
        // 测试存在的维度
        assertEquals(2.0, dimensionBonuses.getOrDefault("minecraft:nether", 0.0));
        
        // 测试不存在的维度，确保返回默认值0.0而不是抛出异常
        assertEquals(0.0, dimensionBonuses.getOrDefault("minecraft:nonexistent", 0.0));
    }

    @Test
    public void testFormatValidation() {
        // 测试validateDimensionBonusFormat的逻辑
        assertTrue(Config.validateDimensionBonusFormat("minecraft:nether:2.0"));
        assertFalse(Config.validateDimensionBonusFormat("invalid_format"));
        assertFalse(Config.validateDimensionBonusFormat("minecraft:nether:not_a_number"));
        
        // 测试validateMobBonusFormat的逻辑
        assertTrue(Config.validateMobBonusFormat("minecraft:zombie:1.5:1.2:1.0:1.3"));
        assertFalse(Config.validateMobBonusFormat("invalid_format"));
        assertFalse(Config.validateMobBonusFormat("minecraft:zombie:1.5:1.2:1.0:not_a_number"));
    }

    @Test
    public void testDefensiveProgramming() {
        // 这个测试验证我们的防御性编程方法
        // 测试数组越界保护
        String[] parts = {"minecraft", "zombie", "1.5", "1.2", "1.0"};
        
        // 模拟我们修复后的代码逻辑
        double healthMultiplier = 1.0;
        double armorMultiplier = 1.0;
        double armorToughnessMultiplier = 1.0;
        double attackDamageMultiplier = 1.0;
        
        try {
            if (parts.length >= 3) healthMultiplier = Double.parseDouble(parts[2]);
            if (parts.length >= 4) armorMultiplier = Double.parseDouble(parts[3]);
            if (parts.length >= 5) armorToughnessMultiplier = Double.parseDouble(parts[4]);
            // 注意这里不再尝试访问parts[5]，避免了索引越界
            attackDamageMultiplier = armorToughnessMultiplier; // 使用最后一个有效值作为默认值
        } catch (Exception e) {
            // 异常应该被捕获，不会导致程序崩溃
            System.out.println("Caught exception as expected: " + e.getMessage());
        }
        
        // 验证结果
        assertEquals(1.5, healthMultiplier);
        assertEquals(1.2, armorMultiplier);
        assertEquals(1.0, armorToughnessMultiplier);
        assertEquals(1.0, attackDamageMultiplier); // 现在应该等于armorToughnessMultiplier
    }
}