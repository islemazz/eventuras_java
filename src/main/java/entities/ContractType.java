package entities;

public enum ContractType {
    SPONSORSHIP("Sponsorship Agreement"),
    VENUE_RENTAL("Venue Rental Agreement"),
    SUPPLIER("Supplier Agreement"),
    MEDIA_PARTNERSHIP("Media Partnership"),
    TECHNOLOGY_PARTNERSHIP("Technology Partnership"),
    EDUCATION_PARTNERSHIP("Education Partnership"),
    GOVERNMENT_PARTNERSHIP("Government Partnership"),
    NON_PROFIT_PARTNERSHIP("Non-Profit Partnership"),
    OTHER("Other Agreement");

    private final String displayName;

    ContractType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

