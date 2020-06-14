package dummydomain.yetanothercallblocker.sia.model;

public enum NumberCategory {

    NONE(0),
    TELEMARKETER(1),
    DEPT_COLLECTOR(2),
    SILENT_CALL(3),
    NUISANCE_CALL(4),
    UNSOLICITED_CALL(5),
    CALL_CENTER(6),
    FAX_MACHINE(7),
    NON_PROFIT(8),
    POLITICAL(9),
    SCAM(10),
    PRANK(11),
    SMS(12),
    SURVEY(13),
    OTHER(14),
    FINANCE_SERVICE(15),
    COMPANY(16),
    SERVICE(17),
    ROBOCALL(18),
    // TODO: check: these are probably not present in the db
    SAFE_PERSONAL(100),
    SAFE_COMPANY(101),
    SAFE_NONPROFIT(102);

    private int id;

    NumberCategory(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static NumberCategory getById(int id) {
        for (NumberCategory category : values()) {
            if (category.getId() == id) return category;
        }
        return null;
    }

}
