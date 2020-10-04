package au.net.immortius.wardrobe.site.entities;

public class TradingPostEntry {
  private final PriceEntry bestSellPrice;
  private final PriceEntry bestBuyPrice;

  public TradingPostEntry(PriceEntry bestSellPrice, PriceEntry bestBuyPrice) {
    this.bestSellPrice = bestSellPrice;
    this.bestBuyPrice = bestBuyPrice;
  }

  public PriceEntry getBestSellPrice() {
    return bestSellPrice;
  }

  public PriceEntry getBestBuyPrice() {
    return bestBuyPrice;
  }
}
