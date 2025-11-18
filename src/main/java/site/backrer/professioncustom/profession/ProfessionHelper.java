package site.backrer.professioncustom.profession;

import site.backrer.professioncustom.profession.network.ModVariables;
import net.minecraft.world.entity.player.Player;

/**
 * 职业帮助类，提供获取玩家职业相关的静态方法
 */
public class ProfessionHelper {
    
    /**
     * 获取玩家的职业对象
     * @param player 玩家实体
     * @return 如果玩家有职业且职业存在，则返回对应的Profession对象；否则返回null
     */
    public static Profession getPlayerProfession(Player player) {
        if (player == null) {
            return null;
        }
        
        // 1. 获取玩家的变量数据，这里包含职业名称等基础信息
        ModVariables.PlayerVariables playerVariables = player.getCapability(ModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                .orElse(new ModVariables.PlayerVariables());
        
        // 2. 检查玩家是否有职业
        if (!playerVariables.hasProfession()) {
            return null;
        }
        
        // 3. 获取玩家的职业名称
        String professionName = playerVariables.professionName;
        if (professionName == null || professionName.isEmpty()) {
            return null;
        }
        
        // 4. 从职业管理器中获取完整的职业对象
        // 注意：这里获取的是配置中的职业模板，不包含玩家的等级、经验等个人数据
        return ProfessionManager.getProfessionByName(professionName);
    }
    
    /**
     * 获取玩家的职业信息，包含个人数据（如等级、经验）
     * @param player 玩家实体
     * @return 包含玩家职业信息的对象，包含模板数据和个人数据
     */
    public static PlayerProfessionInfo getPlayerProfessionInfo(Player player) {
        if (player == null) {
            return null;
        }
        
        // 获取玩家变量数据
        ModVariables.PlayerVariables playerVariables = player.getCapability(ModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                .orElse(new ModVariables.PlayerVariables());
        
        // 如果玩家没有职业，返回空的职业信息
        if (!playerVariables.hasProfession()) {
            return new PlayerProfessionInfo(null, 0, 0, 0);
        }
        
        // 获取职业模板
        Profession professionTemplate = ProfessionManager.getProfessionByName(playerVariables.professionName);
        
        // 创建包含玩家个人数据的职业信息对象
        return new PlayerProfessionInfo(
                professionTemplate,
                playerVariables.professionLevel,
                playerVariables.currentExperience,
                playerVariables.maxExperience
        );
    }
    
    /**
     * 设置玩家的职业
     * @param player 玩家实体
     * @param professionName 要设置的职业名称
     * @return 如果设置成功则返回true，否则返回false
     */
    public static boolean setPlayerProfession(Player player, String professionName) {
        if (player == null || professionName == null || professionName.isEmpty()) {
            return false;
        }
        
        // 验证职业是否存在
        Profession profession = ProfessionManager.getProfessionByName(professionName);
        if (profession == null) {
            return false;
        }
        
        // 获取玩家变量并设置职业
        ModVariables.PlayerVariables playerVariables = player.getCapability(ModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                .orElse(new ModVariables.PlayerVariables());
        
        playerVariables.setProfession(professionName, player);
        return true;
    }
    
    /**
     * 表示玩家的职业信息，包含职业模板和个人数据
     */
    public static class PlayerProfessionInfo {
        private final Profession profession; // 职业模板
        private final int level;           // 玩家在此职业的等级
        private final int currentExp;      // 当前经验
        private final int maxExp;          // 升级所需的最大经验
        
        public PlayerProfessionInfo(Profession profession, int level, int currentExp, int maxExp) {
            this.profession = profession;
            this.level = level;
            this.currentExp = currentExp;
            this.maxExp = maxExp;
        }
        
        /**
         * 获取职业模板
         */
        public Profession getProfession() {
            return profession;
        }
        
        /**
         * 获取玩家在此职业的等级
         */
        public int getLevel() {
            return level;
        }
        
        /**
         * 获取当前经验
         */
        public int getCurrentExp() {
            return currentExp;
        }
        
        /**
         * 获取升级所需的最大经验
         */
        public int getMaxExp() {
            return maxExp;
        }
        
        /**
         * 获取经验进度比例（0.0 - 1.0）
         */
        public float getExpProgress() {
            if (maxExp <= 0) return 0.0f;
            return (float) currentExp / maxExp;
        }
        
        /**
         * 检查玩家是否有职业
         */
        public boolean hasProfession() {
            return profession != null;
        }
        
        /**
         * 获取职业名称
         */
        public String getProfessionName() {
            return profession != null ? profession.getName() : "";
        }
        
        /**
         * 获取职业显示名称
         */
        public String getProfessionDisplayName() {
            return profession != null ? profession.getDisplayName() : "";
        }
    }
}
