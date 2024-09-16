package io.bidmachine.example;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback;
import com.google.android.gms.ads.admanager.AppEventListener;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import io.bidmachine.BidMachine;
import io.bidmachine.banner.BannerRequest;
import io.bidmachine.banner.BannerSize;
import io.bidmachine.banner.BannerView;
import io.bidmachine.example.databinding.ActivityMainBinding;
import io.bidmachine.example.databinding.ItemAdmanagerNativeAdBinding;
import io.bidmachine.example.databinding.ItemBidmachineNativeAdBinding;
import io.bidmachine.interstitial.InterstitialAd;
import io.bidmachine.interstitial.InterstitialRequest;
import io.bidmachine.mediation.admanager.AMBidMachineUtils;
import io.bidmachine.models.AuctionResult;
import io.bidmachine.nativead.NativeRequest;
import io.bidmachine.nativead.view.NativeAdContentLayout;
import io.bidmachine.rewarded.RewardedRequest;
import io.bidmachine.utils.BMError;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String BID_MACHINE_SELLER_ID = "122";
    private static final String BANNER_ID = "YOUR_BANNER_ID";
    private static final String MREC_ID = "YOUR_MREC_ID";
    private static final String INTERSTITIAL_ID = "YOUR_INTERSTITIAL_ID";
    private static final String REWARDED_ID = "YOUR_REWARDED_ID";
    private static final String NATIVE_ID = "YOUR_NATIVE_ID";
    private static final Handler HANDLER = new Handler(Looper.getMainLooper());

    private ActivityMainBinding binding;

    private BannerRequest bidMachineBannerRequest;
    private AdManagerAdView adManagerBannerAdView;
    private BannerView bidMachineBannerView;

    private BannerRequest bidMachineMrecRequest;
    private AdManagerAdView adManagerMrecAdView;
    private BannerView bidMachineMrecView;

    private InterstitialRequest bidMachineInterstitialRequest;
    private AdManagerInterstitialAd adManagerInterstitialAd;
    private io.bidmachine.interstitial.InterstitialAd bidMachineInterstitialAd;

    private RewardedRequest bidMachineRewardedRequest;
    private RewardedAd adManagerRewardedAd;
    private io.bidmachine.rewarded.RewardedAd bidMachineRewardedAd;

    private NativeRequest bidMachineNativeRequest;
    private AdLoader adManagerNativeAdLoader;
    private NativeAd adManagerNativeAd;
    private io.bidmachine.nativead.NativeAd bidMachineNativeAd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.bInitialize.setOnClickListener(v -> initialize());
        binding.bLoadBanner.setOnClickListener(v -> loadBanner());
        binding.bShowBanner.setOnClickListener(v -> showBanner());
        binding.bLoadMrec.setOnClickListener(v -> loadMrec());
        binding.bShowMrec.setOnClickListener(v -> showMrec());
        binding.bLoadInterstitial.setOnClickListener(v -> loadInterstitial());
        binding.bShowInterstitial.setOnClickListener(v -> showInterstitial());
        binding.bLoadRewarded.setOnClickListener(v -> loadRewarded());
        binding.bShowRewarded.setOnClickListener(v -> showRewarded());
        binding.bLoadNative.setOnClickListener(v -> loadNative());
        binding.bShowNative.setOnClickListener(v -> showNative());

        if (BidMachine.isInitialized()) {
            enableButton();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        destroyBanner();
        destroyMrec();
        destroyInterstitial();
        destroyRewarded();
        destroyNative();
    }

    private void initialize() {
        // Initialize BidMachine SDK first
        BidMachine.setTestMode(true);
        BidMachine.setLoggingEnabled(true);
        BidMachine.initialize(this, BID_MACHINE_SELLER_ID);

        MobileAds.initialize(this);

        enableButton();
    }

    private void enableButton() {
        binding.bInitialize.setEnabled(false);

        binding.bLoadBanner.setEnabled(true);
        binding.bLoadMrec.setEnabled(true);
        binding.bLoadInterstitial.setEnabled(true);
        binding.bLoadRewarded.setEnabled(true);
        binding.bLoadNative.setEnabled(true);
    }

    private void addAdView(View view) {
        binding.adContainer.removeAllViews();
        binding.adContainer.addView(view);
    }

    /**
     * Method for load BannerRequest
     */
    private void loadBanner() {
        binding.bShowBanner.setEnabled(false);

        // Destroy previous ad
        destroyBanner();

        Log.d(TAG, "loadBanner");

        // Create new BidMachine request
        bidMachineBannerRequest = new BannerRequest.Builder()
                .setSize(BannerSize.Size_320x50)
                .setListener(new BannerRequestListener())
                .build();

        // Request an ad from BidMachine without loading it
        bidMachineBannerRequest.request(this);
    }

    /**
     * Method for load AdManagerAdView
     */
    private void loadAdManagerBanner(@Nullable BannerRequest bannerRequest) {
        Log.d(TAG, "loadAdManagerBanner");

        // Create AdManagerAdRequest builder
        AdManagerAdRequest.Builder adRequestBuilder = new AdManagerAdRequest.Builder();

        // Append BidMachine BannerRequest to AdManagerAdRequest
        if (bannerRequest != null) {
            AMBidMachineUtils.appendRequest(adRequestBuilder, bannerRequest);
        }

        BannerListener bannerListener = new BannerListener();

        // Create new AdView instance and load it
        adManagerBannerAdView = new AdManagerAdView(this);
        adManagerBannerAdView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                         ViewGroup.LayoutParams.MATCH_PARENT));
        adManagerBannerAdView.setAdUnitId(BANNER_ID);
        adManagerBannerAdView.setAdListener(bannerListener);
        adManagerBannerAdView.setAppEventListener(bannerListener);
        adManagerBannerAdView.setAdSize(AdSize.BANNER);
        adManagerBannerAdView.loadAd(adRequestBuilder.build());
    }

    private void bidMachineBannerWin() {
        Log.d(TAG, "bidMachineBannerWin");

        // Notify BidMachine about win
        bidMachineBannerRequest.notifyMediationWin();

        // Load BidMachine ad object, before show BidMachine ad
        loadBidMachineBanner();
    }

    private void bidMachineBannerLoss() {
        Log.d(TAG, "bidMachineBannerLoss");

        // Notify BidMachine about loss
        bidMachineBannerRequest.notifyMediationLoss();

        // No need to load BidMachine ad object
        // Show AdManager ad object
        binding.bShowBanner.setEnabled(true);
    }

    /**
     * Method for load BannerView
     */
    private void loadBidMachineBanner() {
        Log.d(TAG, "loadBidMachineBanner");

        // Create BannerView to load an ad from loaded BidMachine BannerRequest
        bidMachineBannerView = new BannerView(this);
        bidMachineBannerView.setListener(new BidMachineBannerListener());
        bidMachineBannerView.load(bidMachineBannerRequest);
    }

    /**
     * Method for show BidMachine BannerView
     */
    private void showBanner() {
        Log.d(TAG, "showBanner");

        binding.bShowBanner.setEnabled(false);

        // Check if an ad can be shown before actual impression
        if (bidMachineBannerView != null) {
            if (bidMachineBannerView.canShow() && bidMachineBannerView.getParent() == null) {
                addAdView(bidMachineBannerView);
            } else {
                Log.e(TAG, "Show error - BidMachine banner object can't be shown");
            }
            return;
        } else if (adManagerBannerAdView != null) {
            addAdView(adManagerBannerAdView);
            return;
        }

        Log.e(TAG, "Can't show anything - banner objects are null");
    }

    /**
     * Method for destroy banner
     */
    private void destroyBanner() {
        Log.d(TAG, "destroyBanner");

        binding.adContainer.removeAllViews();
        if (bidMachineBannerView != null) {
            bidMachineBannerView.setListener(null);
            bidMachineBannerView.destroy();
            bidMachineBannerView = null;
        }
        if (bidMachineBannerRequest != null) {
            bidMachineBannerRequest.destroy();
            bidMachineBannerRequest = null;
        }
        if (adManagerBannerAdView != null) {
            adManagerBannerAdView.destroy();
            adManagerBannerAdView = null;
        }
    }

    /**
     * Method for load BannerRequest
     */
    private void loadMrec() {
        binding.bShowMrec.setEnabled(false);

        // Destroy previous ad
        destroyMrec();

        Log.d(TAG, "loadMrec");

        // Create new BidMachine request
        bidMachineMrecRequest = new BannerRequest.Builder()
                .setSize(BannerSize.Size_300x250)
                .setListener(new MrecRequestListener())
                .build();

        // Request an ad from BidMachine without loading it
        bidMachineMrecRequest.request(this);
    }

    /**
     * Method for load AdManagerAdView
     */
    private void loadAdManagerMrec(@Nullable BannerRequest mrecRequest) {
        Log.d(TAG, "loadAdManagerMrec");

        // Create AdManagerAdRequest builder
        AdManagerAdRequest.Builder adRequestBuilder = new AdManagerAdRequest.Builder();

        // Append BidMachine BannerRequest to AdManagerAdRequest
        if (mrecRequest != null) {
            AMBidMachineUtils.appendRequest(adRequestBuilder, mrecRequest);
        }

        MrecListener mrecListener = new MrecListener();

        // Create new AdView instance and load it
        adManagerMrecAdView = new AdManagerAdView(this);
        adManagerMrecAdView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                       ViewGroup.LayoutParams.MATCH_PARENT));
        adManagerMrecAdView.setAdUnitId(MREC_ID);
        adManagerMrecAdView.setAdSize(AdSize.MEDIUM_RECTANGLE);
        adManagerMrecAdView.setAdListener(mrecListener);
        adManagerMrecAdView.setAppEventListener(mrecListener);
        adManagerMrecAdView.loadAd(adRequestBuilder.build());
    }

    private void bidMachineMrecWin() {
        Log.d(TAG, "bidMachineMrecWin");

        // Notify BidMachine about win
        bidMachineMrecRequest.notifyMediationWin();

        // Load BidMachine ad object, before show BidMachine ad
        loadBidMachineMrec();
    }

    private void bidMachineMrecLoss() {
        Log.d(TAG, "bidMachineMrecLoss");

        // Notify BidMachine about loss
        bidMachineMrecRequest.notifyMediationLoss();

        // No need to load BidMachine ad object
        // Show AdManager ad object
        binding.bShowMrec.setEnabled(true);
    }

    /**
     * Method for load BannerView
     */
    private void loadBidMachineMrec() {
        Log.d(TAG, "loadBidMachineMrec");

        // Create BannerView to load an ad from loaded BidMachine BannerRequest
        bidMachineMrecView = new BannerView(this);
        bidMachineMrecView.setListener(new BidMachineMrecListener());
        bidMachineMrecView.load(bidMachineMrecRequest);
    }

    /**
     * Method for show BidMachine BannerView
     */
    private void showMrec() {
        Log.d(TAG, "showMrec");

        binding.bShowMrec.setEnabled(false);

        // Check if an ad can be shown before actual impression
        if (bidMachineMrecView != null) {
            if (bidMachineMrecView.canShow() && bidMachineMrecView.getParent() == null) {
                addAdView(bidMachineMrecView);
            } else {
                Log.e(TAG, "Show error - BidMachine mrec object can't be shown");
            }
            return;
        } else if (adManagerMrecAdView != null) {
            addAdView(adManagerMrecAdView);
            return;
        }

        Log.e(TAG, "Can't show anything - mrec objects are null");
    }

    /**
     * Method for destroy mrec
     */
    private void destroyMrec() {
        Log.d(TAG, "destroyMrec");

        binding.adContainer.removeAllViews();
        if (bidMachineMrecView != null) {
            bidMachineMrecView.setListener(null);
            bidMachineMrecView.destroy();
            bidMachineMrecView = null;
        }
        if (bidMachineMrecRequest != null) {
            bidMachineMrecRequest.destroy();
            bidMachineMrecRequest = null;
        }
        if (adManagerMrecAdView != null) {
            adManagerMrecAdView.destroy();
            adManagerMrecAdView = null;
        }
    }

    /**
     * Method for load InterstitialRequest
     */
    private void loadInterstitial() {
        binding.bShowInterstitial.setEnabled(false);

        // Destroy previous ad
        destroyInterstitial();

        Log.d(TAG, "loadInterstitial");

        // Create new BidMachine request
        bidMachineInterstitialRequest = new InterstitialRequest.Builder()
                .setListener(new InterstitialRequestListener())
                .build();

        // Request an ad from BidMachine without loading it
        bidMachineInterstitialRequest.request(this);
    }

    /**
     * Method for load AdManagerInterstitialAd
     */
    private void loadAdManagerInterstitial(@Nullable InterstitialRequest interstitialRequest) {
        Log.d(TAG, "loadAdManagerInterstitial");

        // Create AdManagerAdRequest builder
        AdManagerAdRequest.Builder adRequestBuilder = new AdManagerAdRequest.Builder();

        // Append BidMachine InterstitialRequest to AdManagerAdRequest
        if (interstitialRequest != null) {
            AMBidMachineUtils.appendRequest(adRequestBuilder, interstitialRequest);
        }

        // Load InterstitialAd
        AdManagerInterstitialAd.load(this,
                                     INTERSTITIAL_ID,
                                     adRequestBuilder.build(),
                                     new InterstitialLoadListener());
    }

    private void bidMachineInterstitialWin() {
        Log.d(TAG, "bidMachineInterstitialWin");

        // Notify BidMachine about win
        bidMachineInterstitialRequest.notifyMediationWin();

        // Load BidMachine ad object, before show BidMachine ad
        loadBidMachineInterstitial();
    }

    private void bidMachineInterstitialLoss() {
        Log.d(TAG, "bidMachineInterstitialLoss");

        // Notify BidMachine about loss
        bidMachineInterstitialRequest.notifyMediationLoss();

        // No need to load BidMachine ad object
        // Show AdManager ad object
        binding.bShowInterstitial.setEnabled(true);
    }

    /**
     * Method for load BidMachine InterstitialAd
     */
    private void loadBidMachineInterstitial() {
        Log.d(TAG, "loadBidMachineInterstitial");

        // Create InterstitialAd for load with previously loaded InterstitialRequest
        bidMachineInterstitialAd = new InterstitialAd(this);
        bidMachineInterstitialAd.setListener(new BidMachineInterstitialListener());
        bidMachineInterstitialAd.load(bidMachineInterstitialRequest);
    }

    /**
     * Method for show BidMachine InterstitialAd
     */
    private void showInterstitial() {
        Log.d(TAG, "showInterstitial");

        binding.bShowInterstitial.setEnabled(false);

        // Check if an ad can be shown before actual impression
        if (bidMachineInterstitialAd != null) {
            if (bidMachineInterstitialAd.canShow()) {
                bidMachineInterstitialAd.show();
            } else {
                Log.e(TAG, "Show error - BidMachine interstitial object can't be shown");
            }
            return;
        } else if (adManagerInterstitialAd != null) {
            adManagerInterstitialAd.show(this);
            return;
        }

        Log.e(TAG, "Can't show anything - interstitial objects are null");
    }

    /**
     * Method for destroy interstitial
     */
    private void destroyInterstitial() {
        Log.d(TAG, "destroyInterstitial");

        if (bidMachineInterstitialAd != null) {
            bidMachineInterstitialAd.setListener(null);
            bidMachineInterstitialAd.destroy();
            bidMachineInterstitialAd = null;
        }
        if (bidMachineInterstitialRequest != null) {
            bidMachineInterstitialRequest.destroy();
            bidMachineInterstitialRequest = null;
        }
        adManagerInterstitialAd = null;
    }

    /**
     * Method for load RewardedRequest
     */
    private void loadRewarded() {
        binding.bShowRewarded.setEnabled(false);

        // Destroy previous ad
        destroyRewarded();

        Log.d(TAG, "loadRewarded");

        // Create new BidMachine request
        bidMachineRewardedRequest = new RewardedRequest.Builder()
                .setListener(new RewardedRequestListener())
                .build();

        // Request an ad from BidMachine without loading it
        bidMachineRewardedRequest.request(this);
    }

    /**
     * Method for load AdManager RewardedAd
     */
    private void loadAdManagerRewarded(@Nullable RewardedRequest rewardedRequest) {
        Log.d(TAG, "loadAdManagerRewarded");

        // Create AdManagerAdRequest builder
        AdManagerAdRequest.Builder adRequestBuilder = new AdManagerAdRequest.Builder();

        // Append BidMachine RewardedRequest to AdManagerAdRequest
        if (rewardedRequest != null) {
            AMBidMachineUtils.appendRequest(adRequestBuilder, rewardedRequest);
        }

        // Load RewardedAd
        RewardedAd.load(this,
                        REWARDED_ID,
                        adRequestBuilder.build(),
                        new RewardedAdLoadListener());
    }

    private void bidMachineRewardedWin() {
        Log.d(TAG, "bidMachineRewardedWin");

        // Notify BidMachine about win
        bidMachineRewardedRequest.notifyMediationWin();

        // Load BidMachine ad object, before show BidMachine ad
        loadBidMachineRewarded();
    }

    private void bidMachineRewardedLoss() {
        Log.d(TAG, "bidMachineRewardedLoss");

        // Notify BidMachine about loss
        bidMachineRewardedRequest.notifyMediationLoss();

        // No need to load BidMachine ad object
        // Show AdManager ad object
        binding.bShowRewarded.setEnabled(true);
    }

    /**
     * Method for load BidMachine RewardedAd
     */
    private void loadBidMachineRewarded() {
        Log.d(TAG, "loadBidMachineRewarded");

        // Create RewardedAd for load with previously loaded RewardedRequest
        bidMachineRewardedAd = new io.bidmachine.rewarded.RewardedAd(this);
        bidMachineRewardedAd.setListener(new BidMachineRewardedListener());
        bidMachineRewardedAd.load(bidMachineRewardedRequest);
    }

    /**
     * Method for show BidMachine RewardedAd
     */
    private void showRewarded() {
        Log.d(TAG, "showRewarded");

        binding.bShowRewarded.setEnabled(false);

        // Check if an ad can be shown before actual impression
        if (bidMachineRewardedAd != null) {
            if (bidMachineRewardedAd.canShow()) {
                bidMachineRewardedAd.show();
            } else {
                Log.e(TAG, "Show error - BidMachine rewarded object can't be shown");
            }
            return;
        } else if (adManagerRewardedAd != null) {
            adManagerRewardedAd.show(this, new OnRewardListener());
            return;
        }

        Log.e(TAG, "Can't show anything - rewarded objects are null");
    }

    /**
     * Method for destroy rewarded ad
     */
    private void destroyRewarded() {
        Log.d(TAG, "destroyRewarded");

        if (bidMachineRewardedAd != null) {
            bidMachineRewardedAd.setListener(null);
            bidMachineRewardedAd.destroy();
            bidMachineRewardedAd = null;
        }
        if (bidMachineRewardedRequest != null) {
            bidMachineRewardedRequest.destroy();
            bidMachineRewardedRequest = null;
        }
        adManagerRewardedAd = null;
    }

    /**
     * Method for load NativeRequest
     */
    private void loadNative() {
        binding.bShowNative.setEnabled(false);

        // Destroy previous ad
        destroyNative();

        Log.d(TAG, "loadNative");

        // Create new BidMachine request
        bidMachineNativeRequest = new NativeRequest.Builder()
                .setListener(new NativeRequestListener())
                .build();

        // Request an ad from BidMachine without loading it
        bidMachineNativeRequest.request(this);
    }

    /**
     * Method for load AdManager NativeAd
     */
    private void loadAdManagerNative(@Nullable NativeRequest nativeRequest) {
        Log.d(TAG, "loadAdManagerNative");

        // Create AdManagerAdRequest builder
        AdManagerAdRequest.Builder adRequestBuilder = new AdManagerAdRequest.Builder();

        // Append BidMachine NativeRequest to AdManagerAdRequest
        if (nativeRequest != null) {
            AMBidMachineUtils.appendRequest(adRequestBuilder, nativeRequest);
        }

        // Create new AdLoader instance and load it
        NativeListener nativeListener = new NativeListener();
        adManagerNativeAdLoader = new AdLoader.Builder(this, NATIVE_ID)
                .forNativeAd(nativeListener)
                .withAdListener(nativeListener)
                .build();
        adManagerNativeAdLoader.loadAd(adRequestBuilder.build());
    }

    private void bidMachineNativeWin() {
        Log.d(TAG, "bidMachineNativeWin");

        // Notify BidMachine about win
        bidMachineNativeRequest.notifyMediationWin();

        // Load BidMachine ad object, before show BidMachine ad
        loadBidMachineNative();
    }

    private void bidMachineNativeLoss() {
        Log.d(TAG, "bidMachineNativeLoss");

        // Notify BidMachine about loss
        bidMachineNativeRequest.notifyMediationLoss();

        // No need to load BidMachine ad object
        // Show AdManager ad object
        binding.bShowNative.setEnabled(true);
    }

    /**
     * Method for load BidMachine NativeAd
     */
    private void loadBidMachineNative() {
        Log.d(TAG, "loadBidMachineNative");

        // Create NativeAd for load with previously loaded NativeRequest
        bidMachineNativeAd = new io.bidmachine.nativead.NativeAd(this);
        bidMachineNativeAd.setListener(new BidMachineNativeListener());
        bidMachineNativeAd.load(bidMachineNativeRequest);
    }

    /**
     * Method for show BidMachine NativeAd
     */
    private void showNative() {
        Log.d(TAG, "showNative");

        binding.bShowNative.setEnabled(false);

        if (bidMachineNativeAd != null) {
            // Check if an ad can be shown before actual impression
            if (bidMachineNativeAd.canShow()) {
                ItemBidmachineNativeAdBinding itemBidmachineNativeAdBinding = ItemBidmachineNativeAdBinding
                        .inflate(getLayoutInflater(), binding.adContainer, false);
                NativeAdContentLayout nativeAdContentLayout = itemBidmachineNativeAdBinding.nativeLayout;
                nativeAdContentLayout.bind(bidMachineNativeAd);
                nativeAdContentLayout.registerViewForInteraction(bidMachineNativeAd);

                addAdView(itemBidmachineNativeAdBinding.getRoot());
            } else {
                Log.e(TAG, "Show error - BidMachine native object can't be shown");
            }
            return;
        } else if (adManagerNativeAd != null) {
            ItemAdmanagerNativeAdBinding itemAdmanagerNativeAdBinding = ItemAdmanagerNativeAdBinding
                    .inflate(getLayoutInflater(), binding.adContainer, false);
            NativeAdView nativeAdView = itemAdmanagerNativeAdBinding.nativeLayout;
            nativeAdView.setHeadlineView(itemAdmanagerNativeAdBinding.tvTitle);
            nativeAdView.setBodyView(itemAdmanagerNativeAdBinding.tvDescription);
            nativeAdView.setCallToActionView(itemAdmanagerNativeAdBinding.bCta);
            nativeAdView.setIconView(itemAdmanagerNativeAdBinding.ivIcon);
            nativeAdView.setMediaView(itemAdmanagerNativeAdBinding.mediaView);
            nativeAdView.setStarRatingView(itemAdmanagerNativeAdBinding.rbRating);
            nativeAdView.setAdvertiserView(itemAdmanagerNativeAdBinding.providerView);
            nativeAdView.setNativeAd(adManagerNativeAd);

            addAdView(itemAdmanagerNativeAdBinding.getRoot());
            return;
        }

        Log.e(TAG, "Can't show anything - native objects are null");
    }

    /**
     * Method for destroy native ad
     */
    private void destroyNative() {
        Log.d(TAG, "destroyNative");

        if (bidMachineNativeAd != null) {
            bidMachineNativeAd.unregisterView();
            bidMachineNativeAd.setListener(null);
            bidMachineNativeAd.destroy();
            bidMachineNativeAd = null;
        }
        if (bidMachineNativeRequest != null) {
            bidMachineNativeRequest.destroy();
            bidMachineNativeRequest = null;
        }
        if (adManagerNativeAd != null) {
            adManagerNativeAd.destroy();
            adManagerNativeAd = null;
        }
        adManagerNativeAdLoader = null;
    }


    /**
     * Class for definition behavior BidMachine BannerRequest
     */
    private class BannerRequestListener implements BannerRequest.AdRequestListener {

        @Override
        public void onRequestSuccess(@NonNull BannerRequest bannerRequest, @NonNull AuctionResult auctionResult) {
            Log.d(TAG, "BannerRequestListener - onRequestSuccess");

            runOnUiThread(() -> loadAdManagerBanner(bannerRequest));
        }

        @Override
        public void onRequestFailed(@NonNull BannerRequest bannerRequest, @NonNull BMError bmError) {
            Log.d(TAG,
                  String.format("BannerRequestListener - onRequestFailed with message: %s",
                                bmError.getMessage()));

            runOnUiThread(() -> loadAdManagerBanner(null));
        }

        @Override
        public void onRequestExpired(@NonNull BannerRequest bannerRequest) {
            // ignore
        }

    }

    /**
     * Class for definition behavior AdManager AdManagerAdView
     */
    private class BannerListener extends AdListener implements AppEventListener {

        private final Runnable timeOutRunnable = MainActivity.this::bidMachineBannerLoss;

        @Override
        public void onAdLoaded() {
            Log.d(TAG, "BannerListener - onAdLoaded");

            // Wait for AppEventListener#onAppEvent to fire to determine if BidMachine won.
            // It is recommended to add a timer to prevent mediation from stopping if BidMachine loses the mediation.
            // In this case, AppEventListener#onAppEvent will not fire.
            HANDLER.postDelayed(timeOutRunnable, 200);
        }

        @Override
        public void onAppEvent(@NonNull String key, @NonNull String value) {
            // Checking whether it is BidMachine or not
            if (AMBidMachineUtils.isBidMachineBanner(key)) {
                // Don't forget to stop a timer.
                HANDLER.removeCallbacks(timeOutRunnable);

                bidMachineBannerWin();
            }
        }

        @Override
        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
            Log.d(TAG,
                  String.format("BannerListener - onAdFailedToLoad with message: %s",
                                loadAdError.getMessage()));
            Toast.makeText(MainActivity.this,
                           "BannerFailedToLoad",
                           Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAdOpened() {
            Log.d(TAG, "BannerListener - onAdOpened");
        }

        @Override
        public void onAdImpression() {
            Log.d(TAG, "BannerListener - onAdImpression");
        }

        @Override
        public void onAdClicked() {
            Log.d(TAG, "BannerListener - onAdClicked");
        }

        @Override
        public void onAdClosed() {
            Log.d(TAG, "BannerListener - onAdClosed");
        }

    }

    /**
     * Class for definition behavior BidMachine BannerView
     */
    private class BidMachineBannerListener implements io.bidmachine.banner.BannerListener {

        @Override
        public void onAdLoaded(@NonNull BannerView bannerView) {
            binding.bShowBanner.setEnabled(true);

            Log.d(TAG, "BidMachineBannerListener - onAdLoaded");
            Toast.makeText(MainActivity.this,
                           "BannerLoaded",
                           Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAdLoadFailed(@NonNull BannerView bannerView, @NonNull BMError bmError) {
            Log.d(TAG,
                  String.format("BidMachineBannerListener - onAdLoadFailed with message: %s (%s)",
                                bmError.getCode(),
                                bmError.getMessage()));
            Toast.makeText(MainActivity.this,
                           "BannerFailedToLoad",
                           Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAdImpression(@NonNull BannerView bannerView) {
            Log.d(TAG, "BidMachineBannerListener - onAdImpression");
        }

        @Override
        public void onAdShowFailed(@NonNull BannerView bannerView, @NonNull BMError bmError) {
            Log.d(TAG,
                  String.format("BidMachineBannerListener - onAdShowFailed with message: %s (%s)",
                                bmError.getCode(),
                                bmError.getMessage()));
        }

        @Override
        public void onAdClicked(@NonNull BannerView bannerView) {
            Log.d(TAG, "BidMachineBannerListener - onAdClicked");
        }

        @Override
        public void onAdExpired(@NonNull BannerView bannerView) {
            Log.d(TAG, "BidMachineBannerListener - onAdExpired");
        }

    }


    /**
     * Class for definition behavior BidMachine BannerRequest
     */
    private class MrecRequestListener implements BannerRequest.AdRequestListener {

        @Override
        public void onRequestSuccess(@NonNull BannerRequest bannerRequest, @NonNull AuctionResult auctionResult) {
            Log.d(TAG, "MrecRequestListener - onRequestSuccess");

            runOnUiThread(() -> loadAdManagerMrec(bannerRequest));
        }

        @Override
        public void onRequestFailed(@NonNull BannerRequest bannerRequest, @NonNull BMError bmError) {
            Log.d(TAG,
                  String.format("MrecRequestListener - onRequestFailed with message: %s",
                                bmError.getMessage()));

            runOnUiThread(() -> loadAdManagerMrec(null));
        }

        @Override
        public void onRequestExpired(@NonNull BannerRequest bannerRequest) {
            // ignore
        }

    }

    /**
     * Class for definition behavior AdManager AdManagerAdView
     */
    private class MrecListener extends AdListener implements AppEventListener {

        private final Runnable timeOutRunnable = MainActivity.this::bidMachineMrecLoss;

        @Override
        public void onAdLoaded() {
            Log.d(TAG, "MrecListener - onAdLoaded");

            // Wait for AppEventListener#onAppEvent to fire to determine if BidMachine won.
            // It is recommended to add a timer to prevent mediation from stopping if BidMachine loses the mediation.
            // In this case, AppEventListener#onAppEvent will not fire.
            HANDLER.postDelayed(timeOutRunnable, 200);
        }

        @Override
        public void onAppEvent(@NonNull String key, @NonNull String value) {
            // Checking whether it is BidMachine or not
            if (AMBidMachineUtils.isBidMachineBanner(key)) {
                // Don't forget to stop a timer.
                HANDLER.removeCallbacks(timeOutRunnable);

                bidMachineMrecWin();
            }
        }

        @Override
        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
            Log.d(TAG,
                  String.format("MrecListener - onAdFailedToLoad with message: %s",
                                loadAdError.getMessage()));
            Toast.makeText(MainActivity.this,
                           "MrecFailedToLoad",
                           Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAdOpened() {
            Log.d(TAG, "MrecListener - onAdOpened");
        }

        @Override
        public void onAdImpression() {
            Log.d(TAG, "MrecListener - onAdImpression");
        }

        @Override
        public void onAdClicked() {
            Log.d(TAG, "MrecListener - onAdClicked");
        }

        @Override
        public void onAdClosed() {
            Log.d(TAG, "MrecListener - onAdClosed");
        }

    }

    /**
     * Class for definition behavior BidMachine BannerView
     */
    private class BidMachineMrecListener implements io.bidmachine.banner.BannerListener {

        @Override
        public void onAdLoaded(@NonNull BannerView bannerView) {
            binding.bShowMrec.setEnabled(true);

            Log.d(TAG, "BidMachineMrecListener - onAdLoaded");
            Toast.makeText(MainActivity.this,
                           "MrecLoaded",
                           Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAdLoadFailed(@NonNull BannerView bannerView, @NonNull BMError bmError) {
            Log.d(TAG,
                  String.format("BidMachineMrecListener - onAdLoadFailed with message: %s (%s)",
                                bmError.getCode(),
                                bmError.getMessage()));
            Toast.makeText(MainActivity.this,
                           "MrecFailedToLoad",
                           Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAdImpression(@NonNull BannerView bannerView) {
            Log.d(TAG, "BidMachineMrecListener - onAdImpression");
        }

        @Override
        public void onAdShowFailed(@NonNull BannerView bannerView, @NonNull BMError bmError) {
            Log.d(TAG,
                  String.format("BidMachineMrecListener - onAdShowFailed with message: %s (%s)",
                                bmError.getCode(),
                                bmError.getMessage()));
        }

        @Override
        public void onAdClicked(@NonNull BannerView bannerView) {
            Log.d(TAG, "BidMachineMrecListener - onAdClicked");
        }

        @Override
        public void onAdExpired(@NonNull BannerView bannerView) {
            Log.d(TAG, "BidMachineMrecListener - onAdExpired");
        }

    }


    /**
     * Class for definition behavior BidMachine InterstitialRequest
     */
    private class InterstitialRequestListener implements InterstitialRequest.AdRequestListener {

        @Override
        public void onRequestSuccess(@NonNull InterstitialRequest interstitialRequest,
                                     @NonNull AuctionResult auctionResult) {
            Log.d(TAG, "InterstitialRequestListener - onRequestSuccess");

            runOnUiThread(() -> loadAdManagerInterstitial(interstitialRequest));
        }

        @Override
        public void onRequestFailed(@NonNull InterstitialRequest interstitialRequest, @NonNull BMError bmError) {
            Log.d(TAG,
                  String.format("InterstitialRequestListener - onRequestFailed with message: %s",
                                bmError.getMessage()));

            runOnUiThread(() -> loadAdManagerInterstitial(null));
        }

        @Override
        public void onRequestExpired(@NonNull InterstitialRequest interstitialRequest) {
            // ignore
        }

    }

    /**
     * Class for definition behavior AdManager InterstitialAd
     */
    private class InterstitialLoadListener extends AdManagerInterstitialAdLoadCallback implements AppEventListener {

        private final Runnable timeOutRunnable = MainActivity.this::bidMachineInterstitialLoss;

        @Override
        public void onAdLoaded(@NonNull AdManagerInterstitialAd adManagerInterstitialAd) {
            Log.d(TAG, "InterstitialLoadListener - onAdLoaded");

            MainActivity.this.adManagerInterstitialAd = adManagerInterstitialAd;
            adManagerInterstitialAd.setAppEventListener(this);

            // Wait for AppEventListener#onAppEvent to fire to determine if BidMachine won.
            // It is recommended to add a timer to prevent mediation from stopping if BidMachine loses the mediation.
            // In this case, AppEventListener#onAppEvent will not fire.
            HANDLER.postDelayed(timeOutRunnable, 200);
        }

        @Override
        public void onAppEvent(@NonNull String key, @NonNull String value) {
            // Checking whether it is BidMachine or not
            if (AMBidMachineUtils.isBidMachineInterstitial(key)) {
                // Don't forget to stop a timer.
                HANDLER.removeCallbacks(timeOutRunnable);

                bidMachineInterstitialWin();
            }
        }

        @Override
        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
            Log.d(TAG,
                  String.format("InterstitialLoadListener - onAdFailedToLoad with message: %s",
                                loadAdError.getMessage()));
            Toast.makeText(MainActivity.this,
                           "InterstitialFailedToLoad",
                           Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Class for definition behavior BidMachine InterstitialAd
     */
    private class BidMachineInterstitialListener implements io.bidmachine.interstitial.InterstitialListener {

        @Override
        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
            binding.bShowInterstitial.setEnabled(true);

            Log.d(TAG, "BidMachineInterstitialListener - onAdLoaded");
            Toast.makeText(MainActivity.this,
                           "BidMachineInterstitialLoaded",
                           Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAdLoadFailed(@NonNull InterstitialAd interstitialAd,
                                   @NonNull BMError bmError) {
            Log.d(TAG,
                  String.format("BidMachineInterstitialListener - onAdLoadFailed with message: %s (%s)",
                                bmError.getCode(),
                                bmError.getMessage()));
            Toast.makeText(MainActivity.this,
                           "BidMachineInterstitialFailedToLoad",
                           Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAdShowFailed(@NonNull InterstitialAd interstitialAd,
                                   @NonNull BMError bmError) {
            Log.d(TAG, "BidMachineInterstitialListener - onAdShowFailed");
        }

        @Override
        public void onAdImpression(@NonNull InterstitialAd interstitialAd) {
            Log.d(TAG, "BidMachineInterstitialListener - onAdImpression");
        }

        @Override
        public void onAdClicked(@NonNull InterstitialAd interstitialAd) {
            Log.d(TAG, "BidMachineInterstitialListener - onAdClicked");
        }

        @Override
        public void onAdClosed(@NonNull InterstitialAd interstitialAd, boolean b) {
            Log.d(TAG, "BidMachineInterstitialListener - onAdClosed");
        }

        @Override
        public void onAdExpired(@NonNull InterstitialAd interstitialAd) {
            Log.d(TAG, "BidMachineInterstitialListener - onAdExpired");
        }

    }


    /**
     * Class for definition behavior BidMachine RewardedRequest
     */
    private class RewardedRequestListener implements RewardedRequest.AdRequestListener {

        @Override
        public void onRequestSuccess(@NonNull RewardedRequest rewardedRequest, @NonNull AuctionResult auctionResult) {
            Log.d(TAG, "RewardedRequestListener - onRequestSuccess");

            runOnUiThread(() -> loadAdManagerRewarded(rewardedRequest));
        }

        @Override
        public void onRequestFailed(@NonNull RewardedRequest rewardedRequest, @NonNull BMError bmError) {
            Log.d(TAG,
                  String.format("RewardedRequestListener - onRequestFailed with message: %s",
                                bmError.getMessage()));

            runOnUiThread(() -> loadAdManagerRewarded(null));
        }

        @Override
        public void onRequestExpired(@NonNull RewardedRequest rewardedRequest) {
            // ignore
        }

    }

    /**
     * Class for definition behavior AdManager RewardedAdLoadCallback
     */
    private class RewardedAdLoadListener extends RewardedAdLoadCallback {

        @Override
        public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
            // Checking whether it is BidMachine or not
            if (AMBidMachineUtils.isBidMachineRewarded(rewardedAd)) {
                bidMachineRewardedWin();
            } else {
                adManagerRewardedAd = rewardedAd;

                bidMachineRewardedLoss();
            }
        }

        @Override
        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
            Log.d(TAG,
                  String.format("RewardedAdLoadListener - onAdFailedToLoad with message: %s",
                                loadAdError.getMessage()));
            Toast.makeText(MainActivity.this,
                           "RewardedAdFailedToLoad",
                           Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Class for definition behavior AdManager OnUserEarnedRewardListener
     */
    private static class OnRewardListener implements OnUserEarnedRewardListener {

        @Override
        public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
            Log.d(TAG, "OnRewardListener - onUserEarnedReward");
        }

    }

    /**
     * Class for definition behavior BidMachine RewardedAd
     */
    private class BidMachineRewardedListener implements io.bidmachine.rewarded.RewardedListener {

        @Override
        public void onAdLoaded(@NonNull io.bidmachine.rewarded.RewardedAd rewardedAd) {
            binding.bShowRewarded.setEnabled(true);

            Log.d(TAG, "BidMachineRewardedListener - onAdLoaded");
            Toast.makeText(MainActivity.this,
                           "BidMachineRewardedLoaded",
                           Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAdLoadFailed(@NonNull io.bidmachine.rewarded.RewardedAd rewardedAd,
                                   @NonNull BMError bmError) {
            Log.d(TAG,
                  String.format("BidMachineRewardedListener - onAdLoadFailed with message: %s (%s)",
                                bmError.getCode(),
                                bmError.getMessage()));
            Toast.makeText(MainActivity.this,
                           "BidMachineRewardedFailedToLoad",
                           Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAdShowFailed(@NonNull io.bidmachine.rewarded.RewardedAd rewardedAd,
                                   @NonNull BMError bmError) {
            Log.d(TAG,
                  String.format("BidMachineRewardedListener - onAdShowFailed with message: %s (%s)",
                                bmError.getCode(),
                                bmError.getMessage()));
            Toast.makeText(MainActivity.this,
                           "BidMachineRewardedFailedToShow",
                           Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAdImpression(@NonNull io.bidmachine.rewarded.RewardedAd rewardedAd) {
            Log.d(TAG, "BidMachineRewardedListener - onAdImpression");
        }

        @Override
        public void onAdClicked(@NonNull io.bidmachine.rewarded.RewardedAd rewardedAd) {
            Log.d(TAG, "BidMachineRewardedListener - onAdClicked");
        }

        @Override
        public void onAdClosed(@NonNull io.bidmachine.rewarded.RewardedAd rewardedAd, boolean b) {
            Log.d(TAG, "BidMachineRewardedListener - onAdClosed");
        }

        @Override
        public void onAdRewarded(@NonNull io.bidmachine.rewarded.RewardedAd rewardedAd) {
            Log.d(TAG, "BidMachineRewardedListener - onAdRewarded");
        }

        @Override
        public void onAdExpired(@NonNull io.bidmachine.rewarded.RewardedAd rewardedAd) {
            Log.d(TAG, "BidMachineRewardedListener - onAdExpired");
        }

    }


    /**
     * Class for definition behavior BidMachine NativeRequest
     */
    private class NativeRequestListener implements NativeRequest.AdRequestListener {

        @Override
        public void onRequestSuccess(@NonNull NativeRequest nativeRequest, @NonNull AuctionResult auctionResult) {
            Log.d(TAG, "NativeRequestListener - onRequestSuccess");

            runOnUiThread(() -> loadAdManagerNative(nativeRequest));
        }

        @Override
        public void onRequestFailed(@NonNull NativeRequest nativeRequest, @NonNull BMError bmError) {
            Log.d(TAG,
                  String.format("NativeRequestListener - onRequestFailed with message: %s",
                                bmError.getMessage()));

            runOnUiThread(() -> loadAdManagerNative(null));
        }

        @Override
        public void onRequestExpired(@NonNull NativeRequest nativeRequest) {
            // ignore
        }

    }

    /**
     * Class for definition behavior AdManager NativeAd
     */
    private class NativeListener extends AdListener implements NativeAd.OnNativeAdLoadedListener {

        @Override
        public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
            // Checking whether it is BidMachine or not
            if (AMBidMachineUtils.isBidMachineNative(nativeAd)) {
                bidMachineNativeWin();
            } else {
                adManagerNativeAd = nativeAd;

                bidMachineNativeLoss();
            }
        }

        @Override
        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
            Log.d(TAG,
                  String.format("NativeListener - onAdFailedToLoad with message: %s",
                                loadAdError.getMessage()));
            Toast.makeText(MainActivity.this,
                           "NativeFailedToLoad",
                           Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAdOpened() {
            Log.d(TAG, "NativeListener - onAdOpened");
        }

        @Override
        public void onAdImpression() {
            Log.d(TAG, "NativeListener - onAdImpression");
        }

        @Override
        public void onAdClicked() {
            Log.d(TAG, "NativeListener - onAdClicked");
        }

        @Override
        public void onAdClosed() {
            Log.d(TAG, "NativeListener - onAdClosed");
        }

    }

    /**
     * Class for definition behavior BidMachine NativeAd
     */
    private class BidMachineNativeListener implements io.bidmachine.nativead.NativeListener {

        @Override
        public void onAdLoaded(@NonNull io.bidmachine.nativead.NativeAd nativeAd) {
            binding.bShowNative.setEnabled(true);

            Log.d(TAG, "BidMachineNativeListener - onAdLoaded");
            Toast.makeText(MainActivity.this,
                           "BidMachineNativeLoaded",
                           Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAdLoadFailed(@NonNull io.bidmachine.nativead.NativeAd nativeAd, @NonNull BMError bmError) {
            Log.d(TAG,
                  String.format("BidMachineNativeListener - onAdLoadFailed with message: %s (%s)",
                                bmError.getCode(),
                                bmError.getMessage()));
            Toast.makeText(MainActivity.this,
                           "BidMachineNativeFailedToLoad",
                           Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAdImpression(@NonNull io.bidmachine.nativead.NativeAd nativeAd) {
            Log.d(TAG, "BidMachineNativeListener - onAdImpression");
        }

        @Override
        public void onAdShowFailed(@NonNull io.bidmachine.nativead.NativeAd nativeAd, @NonNull BMError bmError) {
            Log.d(TAG,
                  String.format("BidMachineNativeListener - onAdShowFailed with message: %s (%s)",
                                bmError.getCode(),
                                bmError.getMessage()));
        }

        @Override
        public void onAdClicked(@NonNull io.bidmachine.nativead.NativeAd nativeAd) {
            Log.d(TAG, "BidMachineNativeListener - onAdClicked");
        }

        @Override
        public void onAdExpired(@NonNull io.bidmachine.nativead.NativeAd nativeAd) {
            Log.d(TAG, "BidMachineNativeListener - onAdExpired");
        }

    }

}