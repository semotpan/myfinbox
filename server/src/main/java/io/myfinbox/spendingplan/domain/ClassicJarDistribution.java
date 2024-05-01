package io.myfinbox.spendingplan.domain;

/**
 * Enumeration representing the distribution of funds into classic jars.
 * <a href="https://www.harveker.com/blog/6-step-money-managing-system/">source 1</a> and
 * <a href="https://note.moneylover.me/get-a-millionaire-mind-set-with-6-jars-of-money-management-system/">source 2</a>
 */
public enum ClassicJarDistribution {

    /**
     * Represents necessities spending: rent, food, bills, etc.
     */
    NECESSITIES("Necessities", 55,
            "Necessities spending: Rent, Food, Bills etc."),

    /**
     * Represents long-term savings spending: big purchases, vacations, rainy day fund, unexpected medical expenses.
     */
    LONG_TERM_SAVING("Long Term Savings", 10,
            "Long Term Savings spending: Big Purchases, Vacations, Rainy Day Fund, Unexpected Medical Expenses."),

    /**
     * Represents education spending: coaching, mentoring, books, courses, etc.
     */
    EDUCATION("Education", 10,
            "Education spending: Coaching, Mentoring, Books, Courses, etc."),

    /**
     * Represents play spending: spoiling yourself & your family, leisure expenses, fun, etc.
     */
    PLAY("Play", 10,
            "Play spending: Spoiling yourself & your family, Leisure expenses, Fun, etc."),

    /**
     * Represents financial spending: stocks, mutual funds, passive income vehicles, real estate investing, any other investments.
     */
    FINANCIAL("Financial", 10,
            "Financial spending: Stocks, Mutual Funds, Passive income Vehicles, Real Estate investing, Any other investments."),

    /**
     * Represents give spending: charitable, donations.
     */
    GIVE("Give", 5, "Give spending: Charitable, Donations.");

    private final String jarName;
    private final int percentage;
    private final String description;

    ClassicJarDistribution(String jarName, int percentage, String description) {
        this.jarName = jarName;
        this.percentage = percentage;
        this.description = description;
    }

    public String jarName() {
        return jarName;
    }

    public int percentage() {
        return percentage;
    }

    public String description() {
        return description;
    }
}
