package site.backrer.professioncustom.profession;

public class Profession {
    private String name; //职业注册名称
    private String displayName; //职业名称
    private boolean isNormal; //是否为基础职业(一级职业)
    /**
     * 以下两个变量为上级职业变量，有的职业需要通过一级职业转职才能成为二级职业
     * **/
    private Profession upperProfession; //上级职业
    private int professionLevel; //职业等级(一级职业为基础职业、二级职业为初级职业、三级为高级职业)

    private int maxLevel; //当前职业满级数值
    private int maxExp; //当前职业每等级需要多少exp
    /**
     * 整体倍率，如当前经验需要10,然后等级变成二时需要的经验就是10*multiplier
     * 同理应用于血量护甲攻击力等
     * */
    private double multiplier;

    private double health; //生命值
    private double armor; //护甲值
    private double damage; //攻击伤害
    private double damageSpeed; //攻击速度

    /**
     * 基础数值面板
     * */
    public static double HEALTH = 10.0;
    public static double ARMOR = 0.0;
    public static double DAMAGE = 1.0;
    public static double DAMAGE_SPEED = 1.0;

    public Profession(String name, String displayName, boolean isNormal, Profession upperProfession, int professionLevel, int maxLevel, int maxExp, double multiplier, double health, double armor, double damage, double damageSpeed) {
        this.name = name;
        this.displayName = displayName;
        this.isNormal = isNormal;
        this.upperProfession = upperProfession;
        this.professionLevel = professionLevel;
        this.maxLevel = maxLevel;
        this.maxExp = maxExp;
        this.multiplier = multiplier;
        this.health = roundToTwoDecimals(health);
        this.armor = roundToTwoDecimals(armor);
        this.damage = roundToTwoDecimals(damage);
        this.damageSpeed = roundToTwoDecimals(damageSpeed);
    }
    
    /**
     * 将数字四舍五入到两位小数
     */
    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isNormal() {
        return isNormal;
    }

    public void setNormal(boolean normal) {
        isNormal = normal;
    }

    public Profession getUpperProfession() {
        return upperProfession;
    }

    public void setUpperProfession(Profession upperProfession) {
        this.upperProfession = upperProfession;
    }

    public int getProfessionLevel() {
        return professionLevel;
    }

    public void setProfessionLevel(int professionLevel) {
        this.professionLevel = professionLevel;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public int getMaxExp() {
        return maxExp;
    }

    public void setMaxExp(int maxExp) {
        this.maxExp = maxExp;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(int multiplier) {
        this.multiplier = multiplier;
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = roundToTwoDecimals(health);
    }

    public double getArmor() {
        return armor;
    }

    public void setArmor(double armor) {
        this.armor = roundToTwoDecimals(armor);
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = roundToTwoDecimals(damage);
    }

    public double getDamageSpeed() {
        return damageSpeed;
    }

    public void setDamageSpeed(double damageSpeed) {
        this.damageSpeed = roundToTwoDecimals(damageSpeed);
    }
}
