# BidMachine Android AdManager Adapter

## Useful links
* [AdManager integration documentation](https://developers.google.com/ad-manager/mobile-ads-sdk/android/quick-start)
* [BidMachine integration documentation](https://docs.bidmachine.io/docs/in-house-mediation)
* [BidMachine adapter integration documentation](https://docs.bidmachine.io/docs/google-ad-manager)

## Sample
* [Banner](example_pre_bid/src/main/java/io/bidmachine/example/MainActivity.java#L129)
* [MREC](example_pre_bid/src/main/java/io/bidmachine/example/MainActivity.java#L252)
* [Interstitial](example_pre_bid/src/main/java/io/bidmachine/example/MainActivity.java#L375)
* [Rewarded](example_pre_bid/src/main/java/io/bidmachine/example/MainActivity.java#L489)

## Working with price rounding
BidMachine supports server-side price rounding.<br>
To setup it correctly - please contact your manager to set up your own rounding rules. Manager will provide you with the list of prices and you can use them to create orders/line items in partner's dashboard.<br>

## What's new in last version
Please view the [changelog](CHANGELOG.md) for details.
